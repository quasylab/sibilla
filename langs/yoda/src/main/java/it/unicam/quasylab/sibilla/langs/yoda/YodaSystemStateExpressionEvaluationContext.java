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

import it.unicam.quasylab.sibilla.core.models.yoda.YodaElementName;
import it.unicam.quasylab.sibilla.core.models.yoda.YodaSystemState;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

import java.util.Set;
import java.util.function.Function;

class YodaSystemStateExpressionEvaluationContext implements YodaExpressionEvaluationContext<YodaSystemState> {
    @Override
    public SibillaValue min(YodaSystemState context, Set<YodaElementName> group, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> guard, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> expression) {
        return context.min(group, YodaGroupExpressionEvaluationParameters.getPredicate(guard), YodaGroupExpressionEvaluationParameters.getDoubleFunction(expression));
    }

    @Override
    public SibillaValue min(YodaSystemState context, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> guard, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> expression) {
        return context.min(YodaGroupExpressionEvaluationParameters.getPredicate(guard), YodaGroupExpressionEvaluationParameters.getDoubleFunction(expression));
    }

    @Override
    public SibillaValue min(YodaSystemState context, Set<YodaElementName> group, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> expression) {
        return context.min(group, YodaGroupExpressionEvaluationParameters.getDoubleFunction(expression));
    }

    @Override
    public SibillaValue min(YodaSystemState context, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> expression) {
        return context.min(YodaGroupExpressionEvaluationParameters.getDoubleFunction(expression));
    }

    @Override
    public SibillaValue max(YodaSystemState context, Set<YodaElementName> group, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> guard, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> expression) {
        return context.max(group, YodaGroupExpressionEvaluationParameters.getPredicate(guard), YodaGroupExpressionEvaluationParameters.getDoubleFunction(expression));
    }

    @Override
    public SibillaValue max(YodaSystemState context, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> guard, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> expression) {
        return context.max(YodaGroupExpressionEvaluationParameters.getPredicate(guard), YodaGroupExpressionEvaluationParameters.getDoubleFunction(expression));
    }

    @Override
    public SibillaValue max(YodaSystemState context, Set<YodaElementName> group, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> expression) {
        return context.max(group, YodaGroupExpressionEvaluationParameters.getDoubleFunction(expression));
    }

    @Override
    public SibillaValue max(YodaSystemState context, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> expression) {
        return context.max(YodaGroupExpressionEvaluationParameters.getDoubleFunction(expression));
    }

    @Override
    public SibillaValue mean(YodaSystemState context, Set<YodaElementName> group, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> guard, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> expression) {
        return context.mean(group, YodaGroupExpressionEvaluationParameters.getPredicate(guard), YodaGroupExpressionEvaluationParameters.getDoubleFunction(expression));
    }

    @Override
    public SibillaValue mean(YodaSystemState context, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> guard, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> expression) {
        return context.mean(YodaGroupExpressionEvaluationParameters.getPredicate(guard), YodaGroupExpressionEvaluationParameters.getDoubleFunction(expression));
    }

    @Override
    public SibillaValue mean(YodaSystemState context, Set<YodaElementName> group, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> expression) {
        return context.mean(group, YodaGroupExpressionEvaluationParameters.getDoubleFunction(expression));
    }

    @Override
    public SibillaValue mean(YodaSystemState context, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> expression) {
        return context.mean(YodaGroupExpressionEvaluationParameters.getDoubleFunction(expression));
    }

    @Override
    public SibillaValue exists(YodaSystemState context, Set<YodaElementName> group, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> predicate) {
        return SibillaValue.of(context.exists(group, YodaGroupExpressionEvaluationParameters.getPredicate(predicate)));
    }

    @Override
    public SibillaValue exists(YodaSystemState context, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> predicate) {
        return SibillaValue.of(context.exists(YodaGroupExpressionEvaluationParameters.getPredicate(predicate)));
    }

    @Override
    public SibillaValue forAll(YodaSystemState context, Set<YodaElementName> group, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> predicate) {
        return SibillaValue.of(context.forall(group, YodaGroupExpressionEvaluationParameters.getPredicate(predicate)));
    }

    @Override
    public SibillaValue forAll(YodaSystemState context, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> predicate) {
        return SibillaValue.of(context.forall(YodaGroupExpressionEvaluationParameters.getPredicate(predicate)));
    }
}
