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

package it.unicam.quasylab.sibilla.langs.slam;

import it.unicam.quasylab.sibilla.core.models.slam.*;
import it.unicam.quasylab.sibilla.core.models.slam.agents.*;
import it.unicam.quasylab.sibilla.core.models.slam.data.*;
import it.unicam.quasylab.sibilla.core.util.datastructures.Pair;
import it.unicam.quasylab.sibilla.core.util.values.SibillaBoolean;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import org.antlr.v4.runtime.Token;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.*;
import java.util.stream.Collectors;

public class SlamAgentDefinitionsGenerator {


    private final SlamAgentDefinitions agentDefinitions = new SlamAgentDefinitions();
    private final VariableRegistry variableRegistry;
    private final Function<String, Optional<SibillaValue>> globalAssignments;
    private final MessageRepository messageRepository;

    public SlamAgentDefinitionsGenerator(VariableRegistry variableRegistry, Function<String, Optional<SibillaValue>> globalAssignments, MessageRepository messageRepository) {
        this.variableRegistry = variableRegistry;
        this.globalAssignments = globalAssignments;
        this.messageRepository = messageRepository;
    }



    private void generateAgentBehaviour(SlamModelParser.DeclarationAgentContext ctx) {
        if (agentDefinitions.contains(ctx.name.getText())) {
            throw new SlamInternalRuntimeException(ParseUtil.duplicatedAgentName(ctx.name));
        }
        AgentName agentName = agentDefinitions.addAgent(ctx.name.getText(), ctx.params.stream().map(p -> SlamType.getTypeOf(p.type.getText())).toArray(SlamType[]::new));
        SlamAgentPrototype prototype = agentDefinitions.getPrototype(agentName.getAgentName());
        prototype.setStoreProvider(getStoreProvider(getVariables(ctx.params), ctx.attributes));
        prototype.setPerceptionFunction(getPerceptionFunction(ctx.views));
        prototype.setAgentBehaviour(generateAgentStates(ctx.states));
        prototype.setTimePassingFunctionProvider(generateTimePassingFunction(ctx.commands));
    }

    private AgentTimePassingFunction generateTimePassingFunction(List<SlamModelParser.AgentCommandAssignmentContext> commands) {
        List<Pair<AgentVariable, Function<SlamExpressionEvaluationParameters, SibillaValue>>> assignments = commands.stream().map(a -> Pair.of(getVariable(a.name), a.expr().accept(new SlamExpressionEvaluator(ExpressionContext.AGENT_TIME_UPDATE, globalAssignments, variableRegistry, agentDefinitions)))).collect(Collectors.toList());
        return (rg, dt, m) -> {
            AgentStore store = m;
            for (Pair<AgentVariable, Function<SlamExpressionEvaluationParameters, SibillaValue>> a : assignments)
                store = store.set(a.getKey(), SlamExpressionEvaluationParameters.evalDtExpression(rg, m, dt, a.getValue()));
            return store;
        };
    }

    private SlamAgentBehaviour generateAgentStates(List<SlamModelParser.AgentStateDeclarationContext> states) {
        SlamAgentBehaviour agentBehaviour = new SlamAgentBehaviour();
        recordStates(agentBehaviour, states);
        generateStatesBehaviour(agentBehaviour, states);
        return agentBehaviour;
    }

    private void generateStatesBehaviour(SlamAgentBehaviour agentBehaviour, List<SlamModelParser.AgentStateDeclarationContext> states) {
        for (SlamModelParser.AgentStateDeclarationContext state : states) {
            generateStateBehaviour(agentBehaviour, state);
        }
    }

    private void generateStateBehaviour(SlamAgentBehaviour agentBehaviour, SlamModelParser.AgentStateDeclarationContext state) {
        String name = state.name.getText();
        if (state.isInit != null) {
            agentBehaviour.setInitialState(name);
        }
        if (state.sojournTimeExpression != null) {
            agentBehaviour.setTimeDependentStep(name, generateSojournTimeFunction(state.sojournTimeExpression), generateStateStep(agentBehaviour, state.activityBlock()));
        }
        if (state.commands != null) {
            agentBehaviour.setTimePassingFunction(name, generateTimePassingFunction(state.commands));
        }
        state.handlers.forEach(smh -> agentBehaviour.addMessageHandler(name, generateMessageHandler(agentBehaviour, smh)));
    }

    private ToDoubleBiFunction<RandomGenerator, AgentStore> generateSojournTimeFunction(SlamModelParser.ExprContext sojournTimeExpression) {
        Function<SlamExpressionEvaluationParameters, SibillaValue> evaluationFunction = sojournTimeExpression.accept(new SlamExpressionEvaluator(ExpressionContext.AGENT_SOJOURN_TIME, globalAssignments, variableRegistry, agentDefinitions));
        return (rg, s) -> SlamExpressionEvaluationParameters.evalAgentExpression(rg, s, evaluationFunction).doubleOf();
    }

    private SlamAgentStep generateStateStep(SlamAgentBehaviour agentBehaviour, SlamModelParser.ActivityBlockContext activityBlockContext) {
        return activityBlockContext.accept(new AgentActivityBlockGenerator(agentBehaviour));
    }

    private MessageHandler generateMessageHandler(SlamAgentBehaviour agentBehaviour, SlamModelParser.StateMessageHandlerContext smh) {
        BiFunction<AgentStore,SibillaValue[],AgentStore> patternAssignment = getPatternAssignment(smh.content);
        BiPredicate<AgentStore, SlamAgent> senderPredicate;
        if (smh.agentGuard != null) {
            senderPredicate = smh.agentGuard.accept(new AgentPatternGenerator(globalAssignments, variableRegistry, agentDefinitions));
        } else {
            senderPredicate = (store, agent) -> true;
        }
        Function<SlamExpressionEvaluationParameters, SibillaValue> guard;
        if (smh.guard != null) {
            guard = smh.guard.accept(new SlamExpressionEvaluator(ExpressionContext.AGENT_MESSAGE_HANDLER, globalAssignments, variableRegistry, agentDefinitions));
        } else {
            guard  = arg -> SibillaBoolean.TRUE;
        }
        SlamAgentStep triggeredStep = smh.activityBlock().accept(new AgentActivityBlockGenerator(agentBehaviour));
        return new MessageHandler(getMessageTag(smh.tag), patternAssignment, senderPredicate, (rg, store) -> SlamExpressionEvaluationParameters.evalAgentExpression(rg, store, guard).booleanOf(), triggeredStep);
    }

    private MessageTag getMessageTag(Token tag) {
        if (!messageRepository.exists(tag.getText())) {
            throw new SlamInternalRuntimeException(ParseUtil.unknownSymbolMessage(tag));
        }
        return messageRepository.getTag(tag.getText());
    }

    private BiFunction<AgentStore, SibillaValue[], AgentStore> getPatternAssignment(List<SlamModelParser.ValuePatternContext> content) {
        int counter = 0;
        List<BiFunction<AgentStore, SibillaValue[], AgentStore>> assignments = new LinkedList<>();
        for (SlamModelParser.ValuePatternContext pattern : content) {
            assignments.add(pattern.accept(new MessageHandlerTemplateAssignments(counter++)));
        }
        return (s, args) -> {
            AgentStore result = s;
            for (BiFunction<AgentStore, SibillaValue[], AgentStore> a : assignments) {
                result = a.apply(result, args);
            }
            return result;
        };
    }

    private void recordStates(SlamAgentBehaviour agentBehaviour, List<SlamModelParser.AgentStateDeclarationContext> states) {
        for (SlamModelParser.AgentStateDeclarationContext state : states) {
            agentBehaviour.addState(state.name.getText());
        }
    }

    private PerceptionFunction getPerceptionFunction(List<SlamModelParser.AttributeDeclarationContext> views) {
        Map<AgentVariable, PerceptionFunctionExpression> assignments = views.stream().collect(Collectors.toMap(v -> getVariable(v.name), v -> getPerceptionFunctionElement(v.expr())));
        return (rg, se, s) -> PerceptionFunctionExpression.perceive(assignments, rg, se, s);
    }

    private AgentVariable getVariable(Token name) {
        Optional<AgentVariable> optionalAgentVariable = variableRegistry.getVariable(name.getText());
        if (optionalAgentVariable.isPresent()) {
            return optionalAgentVariable.get();
        }
        throw new SlamInternalRuntimeException(ParseUtil.unknownSymbolMessage(name));
    }

    private PerceptionFunctionExpression getPerceptionFunctionElement(SlamModelParser.ExprContext expr) {
        return SlamExpressionEvaluationParameters.getPerceptionFunction(expr.accept(new SlamExpressionEvaluator(ExpressionContext.AGENT_VIEW, globalAssignments, variableRegistry, agentDefinitions)));
    }

    private Function<SibillaValue[], AgentStore> getStoreProvider(AgentVariable[] variables, List<SlamModelParser.AttributeDeclarationContext> attributes) {
        List<Pair<AgentVariable, Function<SlamExpressionEvaluationParameters, SibillaValue>>> assignments = attributes.stream().map(adc -> new Pair<>(variableRegistry.getVariable(adc.name.getText()).get(),
                adc.expr().accept(new SlamExpressionEvaluator(ExpressionContext.AGENT_ATTRIBUTE, globalAssignments, variableRegistry, agentDefinitions)))).collect(Collectors.toList());
        return args -> {
            AgentStore store = AgentStore.of(variables, args);
            for (Pair<AgentVariable, Function<SlamExpressionEvaluationParameters, SibillaValue>> pair: assignments) {
                store = store.set(pair.getKey(), SlamExpressionEvaluationParameters.evalAttribute(store, pair.getValue()));
            }
            return store;
        };
    }

    private AgentVariable[] getVariables(List<SlamModelParser.AgentParameterContext> params) {
        return params.stream()
                .map(p -> p.name.getText())
                .map(variableRegistry::getVariable)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toArray(AgentVariable[]::new);
    }

    public class MessageHandlerTemplateAssignments extends SlamModelBaseVisitor<BiFunction<AgentStore,SibillaValue[],AgentStore>> {

        private final int elementIndex;

        public MessageHandlerTemplateAssignments(int elementIndex) {
            this.elementIndex = elementIndex;
        }

        @Override
        public BiFunction<AgentStore, SibillaValue[], AgentStore> visitPatternAnyValue(SlamModelParser.PatternAnyValueContext ctx) {
            return (s,args) -> s;
        }

        @Override
        public BiFunction<AgentStore, SibillaValue[], AgentStore> visitPatternVariable(SlamModelParser.PatternVariableContext ctx) {
            AgentVariable variable = getVariable(ctx.name);
            return (s, args) -> s.set(variable, args[elementIndex]);
        }

        @Override
        protected BiFunction<AgentStore, SibillaValue[], AgentStore> defaultResult() {
            return (s, args) -> s;
        }

    }


    public class AgentActivityBlockGenerator extends SlamModelBaseVisitor<SlamAgentStep> {

        private final SlamAgentBehaviour agentBehaviour;

        public AgentActivityBlockGenerator(SlamAgentBehaviour agentBehaviour) {
            this.agentBehaviour = agentBehaviour;
        }

        @Override
        public SlamAgentStep visitNextStateBlock(SlamModelParser.NextStateBlockContext ctx) {
            return new SlamAgentDeterministicStep(ctx.agentCommandBlock().accept(new SlamCommandGenerator(variableRegistry, globalAssignments, messageRepository, agentDefinitions)), agentBehaviour.getAgentState(ctx.name.getText()));
        }

        @Override
        public SlamAgentStep visitProbabilitySelectionBlock(SlamModelParser.ProbabilitySelectionBlockContext ctx) {
            SlamAgentProbabilisticStep step = new SlamAgentProbabilisticStep();
            ctx.cases.forEach(c -> {
                Function<SlamExpressionEvaluationParameters, SibillaValue> weight = c.weight.accept(new SlamExpressionEvaluator(ExpressionContext.AGENT_COMMAND, globalAssignments, variableRegistry, agentDefinitions));
                if (c.guard == null) {
                    step.add((rg, store) -> SlamExpressionEvaluationParameters.evalAgentExpression(rg, store, weight), c.nextStateBlock().accept(this));
                } else {
                    Function<SlamExpressionEvaluationParameters, SibillaValue> guard = c.guard.accept(new SlamExpressionEvaluator(ExpressionContext.AGENT_COMMAND, globalAssignments, variableRegistry, agentDefinitions));
                    step.add((rg, store) -> SlamExpressionEvaluationParameters.evalAgentExpression(rg, store, weight),
                            (store) -> SlamExpressionEvaluationParameters.evalAgentExpression(store, guard),
                            c.nextStateBlock().accept(this));
                }
            });
            return step;
        }
    }
}
