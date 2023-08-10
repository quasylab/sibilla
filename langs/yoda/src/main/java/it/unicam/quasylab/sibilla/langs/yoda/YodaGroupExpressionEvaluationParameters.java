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

import it.unicam.quasylab.sibilla.core.models.yoda.YodaAgent;
import it.unicam.quasylab.sibilla.core.models.yoda.YodaVariable;
import it.unicam.quasylab.sibilla.core.models.yoda.YodaVariableMapping;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public class YodaGroupExpressionEvaluationParameters {

    private final YodaAgent agent;

    private final YodaVariableMapping otherResolvingFunction;

    public static Predicate<YodaVariableMapping> getPredicate(YodaAgent agent, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> f) {
        return yvm -> f.apply(new YodaGroupExpressionEvaluationParameters(agent, yvm)).booleanOf();
    }

    public static Predicate<YodaVariableMapping> getPredicate(Function<YodaGroupExpressionEvaluationParameters, SibillaValue> f) {
        return yvm -> f.apply(new YodaGroupExpressionEvaluationParameters(yvm)).booleanOf();
    }

    public static ToDoubleFunction<YodaVariableMapping> getDoubleFunction(YodaAgent agent, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> f) {
        return yvm -> f.apply(new YodaGroupExpressionEvaluationParameters(agent, yvm)).doubleOf();
    }

    public static ToDoubleFunction<YodaVariableMapping> getDoubleFunction(Function<YodaGroupExpressionEvaluationParameters, SibillaValue> f) {
        return yvm -> f.apply(new YodaGroupExpressionEvaluationParameters(yvm)).doubleOf();
    }

    public YodaGroupExpressionEvaluationParameters(YodaVariableMapping otherResolvingFunction) {
        this(null, otherResolvingFunction);
    }

    public YodaGroupExpressionEvaluationParameters(YodaAgent agent, YodaVariableMapping otherResolvingFunction) {
        this.agent = agent;
        this.otherResolvingFunction = otherResolvingFunction;
    }

    public SibillaValue itGet(YodaVariable var) {
        if (agent == null) {
            return SibillaValue.ERROR_VALUE;
        } else {
            return agent.get(var);
        }
    }

    public SibillaValue get(YodaVariable var) {
        return otherResolvingFunction.getValue(var);
    }

    public static YodaExpressionEvaluationContext<YodaGroupExpressionEvaluationParameters> EXPRESSION_EVALUATION_CONTEXT = new YodaExpressionEvaluationContext<>() {
        @Override
        public SibillaValue get(YodaGroupExpressionEvaluationParameters context, YodaVariable var) {
            return context.get(var);
        }

        @Override
        public SibillaValue itGet(YodaGroupExpressionEvaluationParameters context, YodaVariable name) {
            return context.itGet(name);
        }
    };

}
