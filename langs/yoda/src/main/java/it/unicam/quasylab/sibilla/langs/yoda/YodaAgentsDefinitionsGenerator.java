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
import it.unicam.quasylab.sibilla.core.util.datastructures.Pair;
import it.unicam.quasylab.sibilla.core.util.values.SibillaRandomBiFunction;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import org.antlr.v4.runtime.Token;

import java.util.*;
import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class YodaAgentsDefinitionsGenerator extends YodaModelBaseVisitor<Boolean> {

    private final YodaElementNameRegistry registry;

    private final YodaAgentsDefinitions definitions;

    private final YodaVariableRegistry variableRegistry;


    private final Function<String, Optional<SibillaValue>> constantsAndParameters;

    private final Function<String, Optional<YodaFunction>> functions;


    public YodaAgentsDefinitionsGenerator(Function<String, Optional<YodaFunction>> functions, YodaElementNameRegistry registry, YodaVariableRegistry variableRegistry, Function<String, Optional<SibillaValue>> constantsAndParameters) {
        this.registry = registry;
        this.variableRegistry = variableRegistry;
        this.constantsAndParameters = constantsAndParameters;
        this.definitions = new YodaAgentsDefinitions();
        this.functions = functions;
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
        YodaExpressionEvaluator expressionEvaluator = new YodaExpressionEvaluator(functions, constantsAndParameters, variableRegistry);
        Map<YodaAction, ToDoubleBiFunction<YodaVariableMapping, YodaVariableMapping>> actionWeightMapping =
                defaultCase.stream().collect(Collectors.toMap(wa -> agentActions.get(wa.actionName.getText()), wa -> YodaExpressionEvaluationDeterministicAgentBehaviourContext.unpack(wa.weight.accept(expressionEvaluator))));
        return new YodaBehaviourElement((agentState, agentObservations) -> true, actionWeightMapping);
    }

    private List<YodaBehaviourElement> generateBehaviourElements(Map<String, YodaAction> agentActions, List<YodaModelParser.RuleCaseContext> cases) {
        return cases.stream().map(bc -> generateBehaviourElement(agentActions, bc)).collect(Collectors.toList());
    }

    private YodaBehaviourElement generateBehaviourElement(Map<String, YodaAction> agentActions, YodaModelParser.RuleCaseContext bc) {
        YodaExpressionEvaluator expressionEvaluator = new YodaExpressionEvaluator(functions, constantsAndParameters, variableRegistry);
        Function<YodaExpressionEvaluationContext, SibillaValue> guard = bc.guard.accept(expressionEvaluator);
        Map<YodaAction, ToDoubleBiFunction<YodaVariableMapping, YodaVariableMapping>> actionWeightMapping =
                bc.weightedAction().stream().collect(Collectors.toMap(wa -> agentActions.get(wa.actionName.getText()), wa -> YodaExpressionEvaluationDeterministicAgentBehaviourContext.unpack(wa.weight.accept(expressionEvaluator))));
        return new YodaBehaviourElement((agentState, agentObservations) -> guard.apply(new YodaExpressionEvaluationDeterministicAgentBehaviourContext(agentState, agentObservations)).booleanOf(), actionWeightMapping);
    }

    private Map<String, YodaAction> getAgentActions(List<YodaModelParser.ActionBodyContext> actionBodyContexts) {
        Map<String, YodaAction> actions = new HashMap<>();
        for (YodaModelParser.ActionBodyContext actionBodyContext: actionBodyContexts) {
            String actionName = actionBodyContext.actionName.getText();
            actions.put(actionName, YodaAction.actionOf(actionName, getUpdateFunctions(actionBodyContext.body)));
        }
        return actions;
    }

    private SibillaRandomBiFunction<YodaVariableMapping, YodaVariableMapping, List<YodaVariableUpdate>> getUpdateFunctions(List<YodaModelParser.AttributeUpdateContext> updates) {
        Function<YodaExpressionEvaluationContext, List<YodaVariableUpdate>> f = new YodaAttributeUpdateVisitor().blockUpdate(updates);
        return (rg, state, observations) -> f.apply(new YodaExpressionEvaluationAgentBehaviourContext(rg, state, observations));
    }



    private YodaVariableMapping getVariableMapping(List<YodaModelParser.NameDeclarationContext> nameDeclarationList) {
        YodaExpressionEvaluator evaluator = new YodaExpressionEvaluator(functions, constantsAndParameters, variableRegistry, registry::getGroup);
        return new YodaVariableMapping(
            nameDeclarationList.stream()
                    .collect(Collectors.toMap(
                            nd -> variableRegistry.get(nd.name.getText()),
                            nd -> nd.value.accept(evaluator).apply(YodaExpressionEvaluationContext.EMPTY_CONTEXT))
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
        Function<YodaExpressionEvaluationContext, List<YodaVariableUpdate>> update = new YodaAttributeUpdateVisitor().blockUpdate(agentAttributesUpdateContext.updates);
        definitions.setDynamics(agentName,
                (rg, dt, state, environment) -> environment.setAll(update.apply(new YodaExpressionEvaluationAgentDynamicContext(rg, dt, state, environment)))
                );
    }

    private void generateSensingFunctions(YodaModelParser.AgentAttributesUpdateContext agentAttributesUpdateContext) {
        YodaElementName agentName = registry.get(agentAttributesUpdateContext.agentName.getText());
        Function<YodaExpressionEvaluationContext, List<YodaVariableUpdate>> update = new YodaAttributeUpdateVisitor().blockUpdate(agentAttributesUpdateContext.updates);
        definitions.setSensing(agentName,
                (rg, system, agent) -> agent.getAgentObservations().setAll(update.apply(new YodaExpressionEvaluationSensingContext(rg, system, agent)))
        );
    }


    public YodaAgentsDefinitions getAgentsDefinitions() {
        return definitions;
    }

    public class YodaAttributeUpdateVisitor extends YodaModelBaseVisitor<Function<YodaExpressionEvaluationContext, List<YodaVariableUpdate>>> {

        public Function<YodaExpressionEvaluationContext, List<YodaVariableUpdate>> blockUpdate(List<YodaModelParser.AttributeUpdateContext> list) {
            return combine(list.stream().map(u -> u.accept(this)).toList());
        }


        @Override
        public Function<YodaExpressionEvaluationContext, List<YodaVariableUpdate>> visitAttributeUpdateLetBlock(YodaModelParser.AttributeUpdateLetBlockContext ctx) {
            YodaExpressionEvaluator evaluator = new YodaExpressionEvaluator(functions, constantsAndParameters, variableRegistry, registry::getGroup);
            YodaVariable[] variables = ctx.names.stream().map(Token::getText).map(variableRegistry::get).toArray(YodaVariable[]::new);
            YodaModelParser.ExprContext[] values = ctx.values.toArray(new YodaModelParser.ExprContext[0]);
            List<Pair<YodaVariable, Function<YodaExpressionEvaluationContext, SibillaValue>>> updates = IntStream.range(0, variables.length).mapToObj(i -> new Pair<>(variables[i], values[i].accept(evaluator))).toList();
            Function<YodaExpressionEvaluationContext, List<YodaVariableUpdate>> body = blockUpdate(ctx.body);
            return YodaExpressionEvaluationContext.getNestedContext(updates, body);
        }

        private Function<YodaExpressionEvaluationContext, List<YodaVariableUpdate>> combine(List<Function<YodaExpressionEvaluationContext, List<YodaVariableUpdate>>> list) {
            return eec -> list.stream().map(f -> f.apply(eec)).flatMap(Collection::stream).toList();
        }

        @Override
        public Function<YodaExpressionEvaluationContext, List<YodaVariableUpdate>> visitAttributeUpdateAssignment(YodaModelParser.AttributeUpdateAssignmentContext ctx) {
            YodaExpressionEvaluator evaluator = new YodaExpressionEvaluator(functions, constantsAndParameters, variableRegistry, registry::getGroup);
            Function<YodaExpressionEvaluationContext, SibillaValue> expression = ctx.value.accept(evaluator);
            YodaVariable variable = variableRegistry.get(ctx.fieldName.getText());
            return eec -> List.of(new YodaVariableUpdate(variable, expression.apply(eec)));
        }

        @Override
        public Function<YodaExpressionEvaluationContext, List<YodaVariableUpdate>> visitAttributeUpdateIfThenElse(YodaModelParser.AttributeUpdateIfThenElseContext ctx) {
            YodaExpressionEvaluator evaluator = new YodaExpressionEvaluator(functions, constantsAndParameters, variableRegistry, registry::getGroup);
            Function<YodaExpressionEvaluationContext, SibillaValue> guard = ctx.guard.accept(evaluator);
            Function<YodaExpressionEvaluationContext, List<YodaVariableUpdate>> thenBlock = blockUpdate(ctx.thenBlock);
            Function<YodaExpressionEvaluationContext, List<YodaVariableUpdate>> elseBlock = blockUpdate(ctx.elseBlock);
            return eec -> (guard.apply(eec).booleanOf()?thenBlock.apply(eec):elseBlock.apply(eec));
        }


    }
}
