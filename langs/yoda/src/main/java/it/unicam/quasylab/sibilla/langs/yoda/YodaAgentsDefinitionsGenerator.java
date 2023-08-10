/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 *             Copyright (C) 2020.
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package it.unicam.quasylab.sibilla.langs.yoda;

import it.unicam.quasylab.sibilla.core.models.yoda.*;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.Collectors;

public class YodaAgentsDefinitionsGenerator extends YodaModelBaseVisitor<Boolean> {

    private final YodaElementNameRegistry registry;

    private final YodaAgentsDefinitions definitions;

    private final YodaVariableRegistry variableRegistry;


    private final Function<String, Optional<SibillaValue>> constantsAndParameters;

    public YodaAgentsDefinitionsGenerator(YodaElementNameRegistry registry, YodaVariableRegistry variableRegistry, Function<String, Optional<SibillaValue>> constantsAndParameters) {
        this.registry = registry;
        this.variableRegistry = variableRegistry;
        this.constantsAndParameters = constantsAndParameters;
        this.definitions = new YodaAgentsDefinitions();
    }

    @Override
    public Boolean visitAgentDeclaration(YodaModelParser.AgentDeclarationContext ctx) {
        YodaElementName name = registry.get(ctx.agentName.getText());
        if (name == null) { return false; }
        definitions.add(name, getVariableMapping(ctx.agentStateAttributes), getVariableMapping(ctx.agentFeaturesAttributes), getVariableMapping(ctx.agentObservationsAttributes));
        definitions.setBehaviour(name, getBehaviour(ctx));
        return true;
    }

    private YodaBehaviour getBehaviour(YodaModelParser.AgentDeclarationContext agentDeclarationContext) {
        Map<String, YodaAction> agentActions = getAgentActions(agentDeclarationContext.actionBody());
        return getAgentBehaviour(agentActions, agentDeclarationContext.behaviourDeclaration());
    }

    private YodaBehaviour getAgentBehaviour(Map<String, YodaAction> agentActions, YodaModelParser.BehaviourDeclarationContext behaviourDeclarationContext) {
        return YodaBehaviour.behaviourOf(generateBehaviourElements(agentActions, behaviourDeclarationContext.cases), generateDefaultBehaviourCase(agentActions, behaviourDeclarationContext.defaultcase));
    }

    private YodaBehaviourElement generateDefaultBehaviourCase(Map<String, YodaAction> agentActions, List<YodaModelParser.WeightedActionContext> defaultCase) {
        YodaFunctionalExpressionEvaluator<YodaBehaviourExpressionEvaluationParameters> expressionEvaluator = new YodaFunctionalExpressionEvaluator<>(constantsAndParameters, YodaBehaviourExpressionEvaluationParameters.EVALUATION_CONTEXT, variableRegistry);
        Map<YodaAction, ToDoubleBiFunction<YodaVariableMapping, YodaVariableMapping>> actionWeightMapping =
                defaultCase.stream().collect(Collectors.toMap(wa -> agentActions.get(wa.actionName.getText()), wa -> toStandardWeightBehaviourFunction(wa.weight.accept(expressionEvaluator))));
        return new YodaBehaviourElement((agentState, agentObservations) -> true, actionWeightMapping);
    }

    private List<YodaBehaviourElement> generateBehaviourElements(Map<String, YodaAction> agentActions, List<YodaModelParser.RuleCaseContext> cases) {
        return cases.stream().map(bc -> generateBehaviourElement(agentActions, bc)).collect(Collectors.toList());
    }

    private YodaBehaviourElement generateBehaviourElement(Map<String, YodaAction> agentActions, YodaModelParser.RuleCaseContext bc) {
        YodaFunctionalExpressionEvaluator<YodaBehaviourExpressionEvaluationParameters> expressionEvaluator = new YodaFunctionalExpressionEvaluator<>(constantsAndParameters, YodaBehaviourExpressionEvaluationParameters.EVALUATION_CONTEXT, variableRegistry);
        Function<YodaBehaviourExpressionEvaluationParameters, SibillaValue> guard = bc.guard.accept(expressionEvaluator);
        Map<YodaAction, ToDoubleBiFunction<YodaVariableMapping, YodaVariableMapping>> actionWeightMapping =
                bc.weightedAction().stream().collect(Collectors.toMap(wa -> agentActions.get(wa.actionName.getText()), wa -> toStandardWeightBehaviourFunction(wa.weight.accept(expressionEvaluator))));
        return new YodaBehaviourElement((agentState, agentObservations) -> guard.apply(new YodaBehaviourExpressionEvaluationParameters(agentState, agentObservations)).booleanOf(), actionWeightMapping);
    }

    private ToDoubleBiFunction<YodaVariableMapping, YodaVariableMapping> toStandardWeightBehaviourFunction(Function<YodaBehaviourExpressionEvaluationParameters, SibillaValue> f) {
        return (agentState, agentObservations) -> f.apply(new YodaBehaviourExpressionEvaluationParameters(agentState, agentObservations)).doubleOf();
    }

    private Map<String, YodaAction> getAgentActions(List<YodaModelParser.ActionBodyContext> actionBodyContexts) {
        Map<String, YodaAction> actions = new HashMap<>();
        for (YodaModelParser.ActionBodyContext actionBodyContext: actionBodyContexts) {
            String actionName = actionBodyContext.actionName.getText();
            actions.put(actionName, YodaAction.actionOf(actionName, getUpdateFunctions(actionBodyContext.updates)));
        }
        return actions;
    }

    private Map<YodaVariable, BiFunction<RandomGenerator, YodaVariableMapping, SibillaValue>> getUpdateFunctions(List<YodaModelParser.NameUpdateContext> updates) {
        return updates.stream().collect(Collectors.toMap(nu -> variableRegistry.get(nu.fieldName.getText()), nu -> this.getUpdateFunction(nu.value)));
    }

    private BiFunction<RandomGenerator, YodaVariableMapping, SibillaValue> getUpdateFunction(YodaModelParser.ExprContext expr) {
        Function<YodaAgentExpressionEvaluationParameters, SibillaValue> updateFunction = expr.accept(new YodaFunctionalExpressionEvaluator<>(constantsAndParameters, YodaAgentExpressionEvaluationParameters.EVALUATION_CONTEXT, variableRegistry, s -> Set.of()));
        return YodaAgentExpressionEvaluationParameters.unpack(updateFunction);
    }


    private YodaVariableMapping getVariableMapping(List<YodaModelParser.NameDeclarationContext> nameDeclarationList) {
        return new YodaVariableMapping(
            nameDeclarationList.stream()
                    .collect(Collectors.toMap(
                            nd -> variableRegistry.get(nd.name.getText()),
                            nd -> nd.value.accept(new YodaScalarExpressionEvaluator(constantsAndParameters)))
        ));
    }

    @Override
    public Boolean visitSceneElementDeclaration(YodaModelParser.SceneElementDeclarationContext ctx) {
        this.definitions.addElement(registry.get(ctx.agentName.getText()),getVariableMapping(ctx.elementFeaturesAttributes));
        return true;
    }

    @Override
    public Boolean visitSystemDeclaration(YodaModelParser.SystemDeclarationContext ctx) {
        ctx.agentSensing.forEach(this::generateSensingFunctions);
        ctx.agentDynamics.forEach(this::generateDynamicFunctions);
        return super.visitSystemDeclaration(ctx);
    }

    private void generateDynamicFunctions(YodaModelParser.AgentAttributesUpdateContext agentAttributesUpdateContext) {
        YodaElementName agentName = registry.get(agentAttributesUpdateContext.agentName.getText());
        definitions.setDynamics(agentName, generateDynamicFunction(agentAttributesUpdateContext.updates));
    }

    private YodaAgentEnvironmentalAttributeUpdateFunction generateDynamicFunction(List<YodaModelParser.NameUpdateContext> nameUpdates) {
        YodaFunctionalExpressionEvaluator<YodaEnvironmentalAttributeEvaluationParameters> expressionEvaluator = new YodaFunctionalExpressionEvaluator<>(constantsAndParameters, YodaEnvironmentalAttributeEvaluationParameters.EVALUATION_CONTEXT, variableRegistry, registry::getGroup);
        return YodaAgentEnvironmentalAttributeUpdateFunction.of(
                nameUpdates.stream()
                        .collect(Collectors.toMap(
                                        nu -> variableRegistry.get(nu.fieldName.getText()),
                                        nu -> YodaEnvironmentalAttributeEvaluationParameters.unpack(nu.expr().accept(expressionEvaluator))
                                )
                        ));
    }

    private void generateSensingFunctions(YodaModelParser.AgentAttributesUpdateContext agentAttributesUpdateContext) {
        YodaElementName agentName = registry.get(agentAttributesUpdateContext.agentName.getText());
        definitions.setSensing(agentName, generateSensingFunction(agentAttributesUpdateContext.nameUpdate()));
    }

    private YodaAgentSensingFunction generateSensingFunction(List<YodaModelParser.NameUpdateContext> nameUpdates) {
        YodaFunctionalExpressionEvaluator<YodaSensingFunctionEvaluationParameters> expressionEvaluator = new YodaFunctionalExpressionEvaluator<>(constantsAndParameters, YodaSensingFunctionEvaluationParameters.EVALUATION_CONTEXT, variableRegistry, registry::getGroup);
        return YodaAgentSensingFunction.of(
                nameUpdates.stream()
                        .collect(Collectors.toMap(
                                nu -> variableRegistry.get(nu.fieldName.getText()),
                                nu -> YodaSensingFunctionEvaluationParameters.unpack(nu.expr().accept(expressionEvaluator))
                        )
        ));
    }


    public YodaAgentsDefinitions getAgentsDefinitions() {
        return definitions;
    }
}
