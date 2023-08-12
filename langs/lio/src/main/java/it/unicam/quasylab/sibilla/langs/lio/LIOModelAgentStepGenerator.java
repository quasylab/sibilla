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

package it.unicam.quasylab.sibilla.langs.lio;

import it.unicam.quasylab.sibilla.core.models.lio.LIOAgent;
import it.unicam.quasylab.sibilla.core.models.lio.LIOAgentDefinitions;
import it.unicam.quasylab.sibilla.core.models.lio.LIOAgentName;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import it.unicam.quasylab.sibilla.langs.util.ErrorCollector;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LIOModelAgentStepGenerator extends LIOModelAgentDependentChecker {

    public LIOModelAgentStepGenerator(ErrorCollector errors,
                                      LIOAgentDefinitions definition,
                                      Map<String, SibillaValue> constantsAndParameters) {
        super(errors, definition, constantsAndParameters);
    }

    @Override
    public Boolean visitElementState(LIOModelParser.ElementStateContext ctx) {
        String agentName = ctx.name.getText();
        Map<String, Integer> variableIndexes = getVariableIndexes(ctx.agentParameters);
        ctx.agentStep().forEach(step -> recordAgentStep(agentName, variableIndexes, step));
        return true;
    }

    private void recordAgentStep(String agentName,
                                 Map<String, Integer> variableIndexes,
                                 LIOModelParser.AgentStepContext step) {
        definition.addAgentStep(agentName,
                getAgentIndexPredicate(variableIndexes, step.guard),
                definition.getAction(step.performedAction.getText()),
                getStepAgentFunction(variableIndexes, step.nextState.getText(), step.stateArguments));
    }

    private Function<SibillaValue[], LIOAgent> getStepAgentFunction(Map<String, Integer> variableIndexes, String name, List<LIOModelParser.ExprContext> stateArguments) {
        if (stateArguments.isEmpty()) {
            LIOAgent nextAgent = definition.getAgent(name);
            return args -> nextAgent;
        } else {
            List<Function<SibillaValue[], SibillaValue>> argumentEvaluationFunction = stateArguments.stream().map(expr -> expr.accept(ParametricExpressionEvaluator.getAgentDependentExpressionEvaluator(this.errors, variableIndexes, constantsAndParameters))).collect(Collectors.toList());
            return args -> {
                LIOAgentName agentName = new LIOAgentName(name, argumentEvaluationFunction.stream().map(f -> f.apply(args)).toArray(SibillaValue[]::new));
                LIOAgent agent = definition.getAgent(agentName);
                if (agent == null) {
                    throw new IllegalStateException("Agent "+agentName+"is unknown");
                }
                return agent;
            };
        }
    }

}
