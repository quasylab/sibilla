/*
 *  Sibilla:  a Java framework designed to support analysis of Collective
 *  Adaptive Systems.
 *
 *              Copyright (C) ${YEAR}.
 *
 *  See the NOTICE file distributed with this work for additional information
 *  regarding copyright ownership.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *    or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package it.unicam.quasylab.sibilla.langs.yoda;

import it.unicam.quasylab.sibilla.core.models.yoda.YodaElementName;
import it.unicam.quasylab.sibilla.core.models.yoda.YodaVariable;
import it.unicam.quasylab.sibilla.core.models.yoda.YodaVariableMapping;
import it.unicam.quasylab.sibilla.core.util.datastructures.Pair;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class YodaExpressionEvaluationLetContext implements YodaExpressionEvaluationContext {

    private final YodaVariableMapping localVariableMapping;

    private final YodaExpressionEvaluationContext nestedContext;

    public YodaExpressionEvaluationLetContext(YodaVariableMapping localVariableMapping, YodaExpressionEvaluationContext nestedContext) {
        this.localVariableMapping = localVariableMapping;
        this.nestedContext = nestedContext;
    }

    @Override
    public SibillaValue get(YodaVariable var) {
        if (localVariableMapping.isDefined(var)) {
            return localVariableMapping.getValue(var);
        }
        return nestedContext.get(var);
    }

    @Override
    public SibillaValue rnd() {
        return nestedContext.rnd();
    }

    @Override
    public SibillaValue min(Set<YodaElementName> group, Function<YodaExpressionEvaluationContext, SibillaValue> guard, Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return nestedContext.min(group, guard, expression);
    }

    @Override
    public SibillaValue min(Function<YodaExpressionEvaluationContext, SibillaValue> guard, Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return nestedContext.min(guard, expression);
    }

    @Override
    public SibillaValue min(Set<YodaElementName> group, Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return nestedContext.min(group, expression);
    }

    @Override
    public SibillaValue min(Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return nestedContext.min(expression);
    }

    @Override
    public SibillaValue max(Set<YodaElementName> group, Function<YodaExpressionEvaluationContext, SibillaValue> guard, Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return nestedContext.max(group, guard, expression);
    }

    @Override
    public SibillaValue max(Function<YodaExpressionEvaluationContext, SibillaValue> guard, Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return nestedContext.max(guard, expression);
    }

    @Override
    public SibillaValue max(Set<YodaElementName> group, Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return nestedContext.max(group, expression);
    }

    @Override
    public SibillaValue max(Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return nestedContext.max(expression);
    }

    @Override
    public SibillaValue mean(Set<YodaElementName> group, Function<YodaExpressionEvaluationContext, SibillaValue> guard, Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return nestedContext.mean(group, guard, expression);
    }

    @Override
    public SibillaValue mean(Function<YodaExpressionEvaluationContext, SibillaValue> guard, Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return nestedContext.mean(guard, expression);
    }

    @Override
    public SibillaValue mean(Set<YodaElementName> group, Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return nestedContext.mean(group, expression);
    }

    @Override
    public SibillaValue mean(Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return nestedContext.mean(expression);
    }

    @Override
    public SibillaValue sum(Set<YodaElementName> group, Function<YodaExpressionEvaluationContext, SibillaValue> guard, Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return nestedContext.mean(group, guard, expression);
    }

    @Override
    public SibillaValue sum(Function<YodaExpressionEvaluationContext, SibillaValue> guard, Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return nestedContext.mean(guard, expression);
    }

    @Override
    public SibillaValue sum(Set<YodaElementName> group, Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return nestedContext.mean(group, expression);
    }

    @Override
    public SibillaValue sum(Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return nestedContext.mean(expression);
    }

    @Override
    public SibillaValue rnd(SibillaValue from, SibillaValue to) {
        return nestedContext.rnd(from, to);
    }

    @Override
    public SibillaValue exists(Set<YodaElementName> group, Function<YodaExpressionEvaluationContext, SibillaValue> predicate) {
        return nestedContext.exists(group, predicate);
    }

    @Override
    public SibillaValue exists(Function<YodaExpressionEvaluationContext, SibillaValue> predicate) {
        return nestedContext.exists(predicate);
    }

    @Override
    public SibillaValue forAll(Set<YodaElementName> group, Function<YodaExpressionEvaluationContext, SibillaValue> predicate) {
        return nestedContext.forAll(group, predicate);
    }

    @Override
    public SibillaValue forAll(Function<YodaExpressionEvaluationContext, SibillaValue> predicate) {
        return nestedContext.forAll(predicate);
    }

    @Override
    public SibillaValue it(YodaVariable name) {
        return nestedContext.it(name);
    }

    @Override
    public SibillaValue count(Set<YodaElementName> group, Function<YodaExpressionEvaluationContext, SibillaValue> guard) {
        return nestedContext.count(group, guard);
    }

    @Override
    public SibillaValue count(Set<YodaElementName> group) {
        return nestedContext.count(group);
    }

    public static <T> Function<YodaExpressionEvaluationContext, T> let(List<Pair<YodaVariable, Function<YodaExpressionEvaluationContext, SibillaValue>>> localVariables, Function<YodaExpressionEvaluationContext, T> f) {
        return YodaExpressionEvaluationContext.getNestedContext(localVariables, f);
    }


}
