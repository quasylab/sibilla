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
import org.apache.commons.math3.random.RandomGenerator;

import java.util.function.BiFunction;
import java.util.function.Function;

public class YodaAgentExpressionEvaluationParameters {

    private final RandomGenerator rg;

    private final YodaVariableMapping vm;

    public YodaAgentExpressionEvaluationParameters(RandomGenerator rg, YodaVariableMapping vm) {
        this.rg = rg;
        this.vm = vm;
    }

    public final static YodaExpressionEvaluationContext<YodaAgentExpressionEvaluationParameters> EVALUATION_CONTEXT = new YodaExpressionEvaluationContext<>() {

        @Override
        public SibillaValue get(YodaAgentExpressionEvaluationParameters context, YodaVariable var) {
            return context.vm.getValue(var);
        }

        @Override
        public SibillaValue rnd(YodaAgentExpressionEvaluationParameters context) {
            return SibillaValue.of(context.rg.nextDouble());
        }

        @Override
        public SibillaValue rnd(YodaAgentExpressionEvaluationParameters context, SibillaValue from, SibillaValue to) {
            return SibillaValue.of(context.rg.nextInt(to.intOf()-from.intOf())+ from.intOf());
        }

        @Override
        public SibillaValue itGet(YodaAgentExpressionEvaluationParameters context, YodaVariable var) {
            return context.vm.getValue(var);
        }
    };

    public static BiFunction<RandomGenerator, YodaVariableMapping, SibillaValue> unpack(Function<YodaAgentExpressionEvaluationParameters, SibillaValue> f) {
        return (rg, yvm) -> f.apply(new YodaAgentExpressionEvaluationParameters(rg, yvm));
    }

}
