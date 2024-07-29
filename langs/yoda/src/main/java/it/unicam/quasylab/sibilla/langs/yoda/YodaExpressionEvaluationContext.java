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
import it.unicam.quasylab.sibilla.core.models.yoda.YodaVariableMapping;
import it.unicam.quasylab.sibilla.core.models.yoda.YodaVariableUpdate;
import it.unicam.quasylab.sibilla.core.util.datastructures.Pair;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * This interface is implemented to provide an evaluation context for a yoda expression.
 *
 */
public interface YodaExpressionEvaluationContext {

    YodaExpressionEvaluationContext EMPTY_CONTEXT = new YodaExpressionEvaluationContext() {};

    /**
     * Returns the value associated to the given variable in this context.
     *
     * @param var variable to extract
     * @return the value of the given variable in the given context.
     */
    default SibillaValue get(YodaVariable var) {
        return SibillaValue.ERROR_VALUE;
    }

    /**
     * Returns a random value generated in the interval [0,1).
     *
     * @return a random value generated in the interval [0,1).
     */
    default SibillaValue rnd()  {
        return SibillaValue.ERROR_VALUE;
    }

    /**
     * Returns the minimal value among the ones obtained by evaluating the given expression on the set of agents belonging to the given group and satisfying the given predicate.
     *
     * @param group a set of agent names
     * @param guard a predicate used to select the agents
     * @param expression the expression to evaluate
     * @return the minimal value among the ones obtained by evaluating the given expression on the set of agents belonging to the given group and satisfying the given predicate.
     */
    default SibillaValue min(Set<YodaElementName> group, Function<YodaExpressionEvaluationContext, SibillaValue> guard, Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue min(Function<YodaExpressionEvaluationContext, SibillaValue> guard, Function<YodaExpressionEvaluationContext, SibillaValue> expression)  {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue min(Set<YodaElementName> group, Function<YodaExpressionEvaluationContext, SibillaValue> expression)  {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue min(Function<YodaExpressionEvaluationContext, SibillaValue> expression)  {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue max(Set<YodaElementName> group, Function<YodaExpressionEvaluationContext, SibillaValue> guard, Function<YodaExpressionEvaluationContext, SibillaValue> expression)  {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue max(Function<YodaExpressionEvaluationContext, SibillaValue> guard, Function<YodaExpressionEvaluationContext, SibillaValue> expression)  {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue max(Set<YodaElementName> group, Function<YodaExpressionEvaluationContext, SibillaValue> expression)  {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue  max(Function<YodaExpressionEvaluationContext, SibillaValue> expression)  {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue mean(Set<YodaElementName> group, Function<YodaExpressionEvaluationContext, SibillaValue> guard, Function<YodaExpressionEvaluationContext, SibillaValue> expression)  {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue mean(Function<YodaExpressionEvaluationContext, SibillaValue> guard, Function<YodaExpressionEvaluationContext, SibillaValue> expression)  {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue mean(Set<YodaElementName> group, Function<YodaExpressionEvaluationContext, SibillaValue> expression)  {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue mean(Function<YodaExpressionEvaluationContext, SibillaValue> expression)  {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue sum(Set<YodaElementName> group, Function<YodaExpressionEvaluationContext, SibillaValue> guard, Function<YodaExpressionEvaluationContext, SibillaValue> expression)  {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue sum(Set<YodaElementName> group, Function<YodaExpressionEvaluationContext, SibillaValue> expression)  {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue sum(Function<YodaExpressionEvaluationContext, SibillaValue> guard, Function<YodaExpressionEvaluationContext, SibillaValue> expression)  {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue sum(Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return SibillaValue.ERROR_VALUE;
    }


    default SibillaValue rnd(SibillaValue from, SibillaValue to)  {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue exists(Set<YodaElementName> group, Function<YodaExpressionEvaluationContext, SibillaValue> predicate)  {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue exists(Function<YodaExpressionEvaluationContext, SibillaValue> predicate)  {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue forAll(Set<YodaElementName> group, Function<YodaExpressionEvaluationContext, SibillaValue> predicate)  {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue forAll(Function<YodaExpressionEvaluationContext, SibillaValue> predicate)  {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue it(YodaVariable name)  {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue count(Set<YodaElementName> group, Function<YodaExpressionEvaluationContext, SibillaValue> guard) {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue count(Set<YodaElementName> group) {
        return SibillaValue.ERROR_VALUE;
    }

    default SibillaValue dt() {
        return SibillaValue.ERROR_VALUE;
    }

    static <T> Function<YodaExpressionEvaluationContext, T> getNestedContext(
            List<Pair<YodaVariable, Function<YodaExpressionEvaluationContext, SibillaValue>>> localVariables,
            Function<YodaExpressionEvaluationContext, T> f
            ) {
        return eec -> f.apply(
                new YodaExpressionEvaluationLetContext(new YodaVariableMapping().setAll(localVariables.stream().map(p -> new YodaVariableUpdate(p.getKey(), p.getValue().apply(eec))).toList()),
                eec
        ));
    }


}
