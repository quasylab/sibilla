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
import it.unicam.quasylab.sibilla.core.models.yoda.YodaVariableRegistry;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

public class YodaBehaviourExpressionEvaluationParameters {

    public static final YodaExpressionEvaluationContext<YodaBehaviourExpressionEvaluationParameters> EVALUATION_CONTEXT = new YodaExpressionEvaluationContext<>() {
        @Override
        public SibillaValue get(YodaBehaviourExpressionEvaluationParameters context, YodaVariable var) {
            if (context.agentState.isDefined(var)) {
                return context.agentState.getValue(var);
            }
            return context.agentObservations.getValue(var);
        }

        @Override
        public SibillaValue itGet(YodaBehaviourExpressionEvaluationParameters context, YodaVariable name) {
            return get(context, name);
        }
    };
    private final YodaVariableMapping agentState;

    private final YodaVariableMapping agentObservations;

    public YodaBehaviourExpressionEvaluationParameters(YodaVariableMapping agentState, YodaVariableMapping agentObservations) {
        this.agentState = agentState;
        this.agentObservations = agentObservations;
    }
}
