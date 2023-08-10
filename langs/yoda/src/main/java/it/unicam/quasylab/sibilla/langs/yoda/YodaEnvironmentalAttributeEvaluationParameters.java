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

import it.unicam.quasylab.sibilla.core.models.yoda.YodaAgentEnvironmentalAttributeEvaluationFunction;
import it.unicam.quasylab.sibilla.core.models.yoda.YodaAttributeSensingFunction;
import it.unicam.quasylab.sibilla.core.models.yoda.YodaVariable;
import it.unicam.quasylab.sibilla.core.models.yoda.YodaVariableMapping;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.function.Function;

public class YodaEnvironmentalAttributeEvaluationParameters {

    private final RandomGenerator rg;

    private final YodaVariableMapping currentState;

    private final YodaVariableMapping currentEnvironmentalAttrivutes;


    public YodaEnvironmentalAttributeEvaluationParameters(RandomGenerator rg, YodaVariableMapping currentState, YodaVariableMapping currentEnvironmentalAttrivutes) {
        this.rg = rg;
        this.currentState = currentState;
        this.currentEnvironmentalAttrivutes = currentEnvironmentalAttrivutes;
    }

    public static YodaAgentEnvironmentalAttributeEvaluationFunction unpack(Function<YodaEnvironmentalAttributeEvaluationParameters, SibillaValue> f) {
        return (rg, agentState, currentEnvironmentalAttributes) -> f.apply(new YodaEnvironmentalAttributeEvaluationParameters(rg, agentState, currentEnvironmentalAttributes));
    }


    public static YodaExpressionEvaluationContext<YodaEnvironmentalAttributeEvaluationParameters> EVALUATION_CONTEXT = new YodaExpressionEvaluationContext<>() {

        @Override
        public SibillaValue get(YodaEnvironmentalAttributeEvaluationParameters context, YodaVariable var) {
            if (context.currentState.isDefined(var)) {
                return context.currentState.getValue(var);
            }
            return context.currentEnvironmentalAttrivutes.getValue(var);
        }

        @Override
        public SibillaValue itGet(YodaEnvironmentalAttributeEvaluationParameters arg, YodaVariable name) {
            return get(arg, name);
        }
    };


}
