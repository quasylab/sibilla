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
import it.unicam.quasylab.sibilla.core.models.yoda.YodaVariable;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

import java.util.Set;
import java.util.function.Function;

/**
 * This interface is implemented to provide an evaluation context for a yoda expression.
 *
 * @param <T> type of objects used to extract elements needed to evaluate an expression.
 */
public interface YodaExpressionEvaluationContext<T> {

    /**
     * Returns the value of the given variable in the given context.
     *
     * @param context context where the expression is evaluated
     * @param var variable to extract
     * @return the value of the given variable in the given context.
     */
    default SibillaValue get(T context, YodaVariable var) {
        return SibillaValue.ERROR_VALUE;
    }

    /**
     * Returns a random value generated in the interval [0,1).
     * @return a random value generated in the interval [0,1).
     */
    default SibillaValue rnd(T context)  {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue min(T context, Set<YodaElementName> group, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> guard, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> expression) {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue min(T context, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> guard, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> expression)  {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue min(T context, Set<YodaElementName> group, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> expression)  {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue min(T context, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> expression)  {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue max(T context, Set<YodaElementName> group, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> guard, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> expression)  {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue max(T context, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> guard, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> expression)  {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue max(T context, Set<YodaElementName> group, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> expression)  {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue  max(T context, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> expression)  {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue mean(T context, Set<YodaElementName> group, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> guard, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> expression)  {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue mean(T context, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> guard, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> expression)  {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue mean(T context, Set<YodaElementName> group, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> expression)  {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue mean(T context, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> expression)  {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue rnd(T context, SibillaValue from, SibillaValue to)  {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue exists(T context, Set<YodaElementName> group, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> predicate)  {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue exists(T context, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> predicate)  {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue forAll(T context, Set<YodaElementName> group, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> predicate)  {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue forAll(T context, Function<YodaGroupExpressionEvaluationParameters, SibillaValue> predicate)  {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue itGet(T arg, YodaVariable name)  {
        return SibillaValue.ERROR_VALUE;
    }

}
