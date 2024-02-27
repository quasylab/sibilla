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

import it.unicam.quasylab.sibilla.core.models.yoda.YodaVariable;
import it.unicam.quasylab.sibilla.core.models.yoda.YodaVariableMapping;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;

public class YodaExpressionEvaluationDeterministicAgentBehaviourContext implements YodaExpressionEvaluationContext {


    private final YodaVariableMapping agentState;

    private final YodaVariableMapping agentObservations;

    public YodaExpressionEvaluationDeterministicAgentBehaviourContext(YodaVariableMapping agentState, YodaVariableMapping agentObservations) {
        this.agentState = agentState;
        this.agentObservations = agentObservations;
    }

    public static ToDoubleBiFunction<YodaVariableMapping, YodaVariableMapping> unpack(Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return (state, observations) -> expression.apply(new YodaExpressionEvaluationDeterministicAgentBehaviourContext(state, observations)).doubleOf();
    }

    @Override
    public SibillaValue get(YodaVariable var) {
        if (agentState.isDefined(var)) {
            return agentState.getValue(var);
        }
        return agentObservations.getValue(var);
    }

    @Override
    public SibillaValue it(YodaVariable name) {
        return get(name);
    }

}
