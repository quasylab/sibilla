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

import java.util.Set;
import java.util.function.Function;

public class YodaSensingFunctionEvaluationParameters {

    private final RandomGenerator rg;

    private final YodaSystemState systemState;

    private final YodaAgent agent;


    public static YodaAttributeSensingFunction unpack(Function<YodaSensingFunctionEvaluationParameters, SibillaValue> f) {
        return (rg, system, agent) -> f.apply(new YodaSensingFunctionEvaluationParameters(rg, system, agent));
    }

    public YodaSensingFunctionEvaluationParameters(RandomGenerator rg, YodaSystemState systemState, YodaAgent agent) {
        this.rg = rg;
        this.systemState = systemState;
        this.agent = agent;
    }

    public final static YodaExpressionEvaluationContext<YodaSensingFunctionEvaluationParameters> EVALUATION_CONTEXT = new YodaExpressionEvaluationContext<YodaSensingFunctionEvaluationParameters>() {
        @Override
        public SibillaValue get(YodaSensingFunctionEvaluationParameters context, YodaVariable var) {
            return context.agent.get(var);
        }

        @Override
        public SibillaValue rnd(YodaSensingFunctionEvaluationParameters context) {
            return SibillaValue.of(context.rg.nextDouble());
        }

        @Override
        public SibillaValue min(YodaSensingFunctionEvaluationParameters context,
                                Set<YodaElementName> group,
                                Function<YodaGroupExpressionEvaluationParameters, SibillaValue> guard,
                                Function<YodaGroupExpressionEvaluationParameters, SibillaValue> expression) {
            return context.systemState.min(
                    context.agent,
                    group,
                    YodaGroupExpressionEvaluationParameters.getPredicate(context.agent, guard),
                    YodaGroupExpressionEvaluationParameters.getDoubleFunction(context.agent, expression)
            );
        }

        @Override
        public SibillaValue min(YodaSensingFunctionEvaluationParameters context,
                                Function<YodaGroupExpressionEvaluationParameters, SibillaValue> guard,
                                Function<YodaGroupExpressionEvaluationParameters, SibillaValue> expression) {
            return context.systemState.min(
                    context.agent,
                    YodaGroupExpressionEvaluationParameters.getPredicate(context.agent, guard),
                    YodaGroupExpressionEvaluationParameters.getDoubleFunction(context.agent, expression)
            );
        }

        @Override
        public SibillaValue min(YodaSensingFunctionEvaluationParameters context,
                                Set<YodaElementName> group,
                                Function<YodaGroupExpressionEvaluationParameters, SibillaValue> expression) {
            return context.systemState.min(
                    context.agent,
                    group,
                    YodaGroupExpressionEvaluationParameters.getDoubleFunction(context.agent, expression)
            );
        }

        @Override
        public SibillaValue min(YodaSensingFunctionEvaluationParameters context,
                                Function<YodaGroupExpressionEvaluationParameters, SibillaValue> expression) {
            return context.systemState.min(
                    context.agent,
                    YodaGroupExpressionEvaluationParameters.getDoubleFunction(context.agent, expression)
            );
        }

        @Override
        public SibillaValue max(YodaSensingFunctionEvaluationParameters context,
                                Set<YodaElementName> group,
                                Function<YodaGroupExpressionEvaluationParameters, SibillaValue> guard,
                                Function<YodaGroupExpressionEvaluationParameters, SibillaValue> expression) {
            return context.systemState.max(
                    context.agent,
                    group,
                    YodaGroupExpressionEvaluationParameters.getPredicate(context.agent, guard),
                    YodaGroupExpressionEvaluationParameters.getDoubleFunction(context.agent, expression)
            );
        }

        @Override
        public SibillaValue max(YodaSensingFunctionEvaluationParameters context,
                                Function<YodaGroupExpressionEvaluationParameters, SibillaValue> guard,
                                Function<YodaGroupExpressionEvaluationParameters, SibillaValue> expression) {
            return context.systemState.max(
                    context.agent,
                    YodaGroupExpressionEvaluationParameters.getPredicate(context.agent, guard),
                    YodaGroupExpressionEvaluationParameters.getDoubleFunction(context.agent, expression)
            );
        }

        @Override
        public SibillaValue max(YodaSensingFunctionEvaluationParameters context,
                                Set<YodaElementName> group,
                                Function<YodaGroupExpressionEvaluationParameters, SibillaValue> expression) {
            return context.systemState.max(
                    context.agent,
                    group,
                    YodaGroupExpressionEvaluationParameters.getDoubleFunction(context.agent, expression)
            );
        }

        @Override
        public SibillaValue max(YodaSensingFunctionEvaluationParameters context,
                                Function<YodaGroupExpressionEvaluationParameters, SibillaValue> expression) {
            return context.systemState.max(
                    context.agent,
                    YodaGroupExpressionEvaluationParameters.getDoubleFunction(context.agent, expression)
            );
        }

        @Override
        public SibillaValue mean(YodaSensingFunctionEvaluationParameters context,
                                 Set<YodaElementName> group,
                                 Function<YodaGroupExpressionEvaluationParameters, SibillaValue> guard,
                                 Function<YodaGroupExpressionEvaluationParameters, SibillaValue> expression) {
            return context.systemState.mean(
                    context.agent,
                    group,
                    YodaGroupExpressionEvaluationParameters.getPredicate(context.agent, guard),
                    YodaGroupExpressionEvaluationParameters.getDoubleFunction(context.agent, expression)
            );
        }

        @Override
        public SibillaValue mean(YodaSensingFunctionEvaluationParameters context,
                                 Function<YodaGroupExpressionEvaluationParameters, SibillaValue> guard,
                                 Function<YodaGroupExpressionEvaluationParameters, SibillaValue> expression) {
            return context.systemState.mean(
                    context.agent,
                    YodaGroupExpressionEvaluationParameters.getPredicate(context.agent, guard),
                    YodaGroupExpressionEvaluationParameters.getDoubleFunction(context.agent, expression)
            );
        }

        @Override
        public SibillaValue mean(YodaSensingFunctionEvaluationParameters context, Set<YodaElementName> group,
                                 Function<YodaGroupExpressionEvaluationParameters, SibillaValue> expression) {
            return context.systemState.mean(
                    context.agent,
                    group,
                    YodaGroupExpressionEvaluationParameters.getDoubleFunction(context.agent, expression)
            );
        }

        @Override
        public SibillaValue mean(YodaSensingFunctionEvaluationParameters context,
                                 Function<YodaGroupExpressionEvaluationParameters, SibillaValue> expression) {
            return context.systemState.mean(
                    context.agent,
                    YodaGroupExpressionEvaluationParameters.getDoubleFunction(context.agent, expression)
            );
        }

        @Override
        public SibillaValue rnd(YodaSensingFunctionEvaluationParameters context, SibillaValue from, SibillaValue to) {
            return YodaExpressionEvaluationContext.super.rnd(context, from, to);
        }

        @Override
        public SibillaValue exists(YodaSensingFunctionEvaluationParameters context,
                                   Set<YodaElementName> group,
                                   Function<YodaGroupExpressionEvaluationParameters, SibillaValue> predicate) {
            return SibillaValue.of(context.systemState.exists(
                    context.agent,
                    group,
                    YodaGroupExpressionEvaluationParameters.getPredicate(context.agent, predicate))
            );
        }

        @Override
        public SibillaValue exists(YodaSensingFunctionEvaluationParameters context,
                                   Function<YodaGroupExpressionEvaluationParameters, SibillaValue> predicate) {
            return SibillaValue.of(context.systemState.exists(
                    context.agent,
                    YodaGroupExpressionEvaluationParameters.getPredicate(context.agent, predicate))
            );
        }

        @Override
        public SibillaValue forAll(YodaSensingFunctionEvaluationParameters context,
                                   Set<YodaElementName> group,
                                   Function<YodaGroupExpressionEvaluationParameters, SibillaValue> predicate) {
            return SibillaValue.of(context.systemState.forall(
                    context.agent,
                    group,
                    YodaGroupExpressionEvaluationParameters.getPredicate(context.agent, predicate))
            );
        }

        @Override
        public SibillaValue forAll(YodaSensingFunctionEvaluationParameters context,
                                   Function<YodaGroupExpressionEvaluationParameters, SibillaValue> predicate) {
            return SibillaValue.of(context.systemState.exists(
                    context.agent,
                    YodaGroupExpressionEvaluationParameters.getPredicate(context.agent, predicate))
            );
        }

        @Override
        public SibillaValue itGet(YodaSensingFunctionEvaluationParameters context, YodaVariable name) {
            return context.agent.get(name);
        }
    };

}
