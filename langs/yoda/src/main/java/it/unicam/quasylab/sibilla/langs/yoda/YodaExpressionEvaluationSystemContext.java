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
import it.unicam.quasylab.sibilla.core.models.yoda.YodaSystemState;
import it.unicam.quasylab.sibilla.core.models.yoda.YodaVariable;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

import java.util.Set;
import java.util.function.Function;

public class YodaExpressionEvaluationSystemContext implements YodaExpressionEvaluationContext {


    private final YodaSystemState sys;

    public YodaExpressionEvaluationSystemContext(YodaSystemState sys) {
        this.sys = sys;
    }


    @Override
    public SibillaValue min(Set<YodaElementName> group, Function<YodaExpressionEvaluationContext, SibillaValue> guard, Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return sys.min(group, element -> guard.apply(new YodaExpressionEvaluationAgentPredicateContext(element, element)).booleanOf(), element -> expression.apply(new YodaExpressionEvaluationAgentPredicateContext(element, element)).doubleOf());
    }

    @Override
    public SibillaValue min(Function<YodaExpressionEvaluationContext, SibillaValue> guard, Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return sys.min(element -> guard.apply(new YodaExpressionEvaluationAgentPredicateContext(element, element)).booleanOf(), element -> expression.apply(new YodaExpressionEvaluationAgentPredicateContext(element, element)).doubleOf());
    }

    @Override
    public SibillaValue min(Set<YodaElementName> group, Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return sys.min(group, element -> expression.apply(new YodaExpressionEvaluationAgentPredicateContext(element, element)).doubleOf());
    }

    @Override
    public SibillaValue min(Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return sys.min(element -> expression.apply(new YodaExpressionEvaluationAgentPredicateContext(element, element)).doubleOf());
    }

    @Override
    public SibillaValue max(Set<YodaElementName> group, Function<YodaExpressionEvaluationContext, SibillaValue> guard, Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return sys.max(group, element -> guard.apply(new YodaExpressionEvaluationAgentPredicateContext(element, element)).booleanOf(), element -> expression.apply(new YodaExpressionEvaluationAgentPredicateContext(element, element)).doubleOf());
    }

    @Override
    public SibillaValue max(Function<YodaExpressionEvaluationContext, SibillaValue> guard, Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return sys.max(element -> guard.apply(new YodaExpressionEvaluationAgentPredicateContext(element, element)).booleanOf(), element -> expression.apply(new YodaExpressionEvaluationAgentPredicateContext(element, element)).doubleOf());
    }

    @Override
    public SibillaValue max(Set<YodaElementName> group, Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return sys.max(group, element -> expression.apply(new YodaExpressionEvaluationAgentPredicateContext(element, element)).doubleOf());
    }

    @Override
    public SibillaValue max(Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return sys.max(element -> expression.apply(new YodaExpressionEvaluationAgentPredicateContext(element, element)).doubleOf());
    }

    @Override
    public SibillaValue mean(Set<YodaElementName> group, Function<YodaExpressionEvaluationContext, SibillaValue> guard, Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return sys.mean(group, element -> guard.apply(new YodaExpressionEvaluationAgentPredicateContext(element, element)).booleanOf(), element -> expression.apply(new YodaExpressionEvaluationAgentPredicateContext(element, element)).doubleOf());
    }

    @Override
    public SibillaValue mean(Function<YodaExpressionEvaluationContext, SibillaValue> guard, Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return sys.mean(element -> guard.apply(new YodaExpressionEvaluationAgentPredicateContext(element, element)).booleanOf(), element -> expression.apply(new YodaExpressionEvaluationAgentPredicateContext(element, element)).doubleOf());
    }

    @Override
    public SibillaValue mean(Set<YodaElementName> group, Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return sys.mean(group, element -> expression.apply(new YodaExpressionEvaluationAgentPredicateContext(element, element)).doubleOf());
    }

    @Override
    public SibillaValue mean(Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return sys.mean(element -> expression.apply(new YodaExpressionEvaluationAgentPredicateContext(element, element)).doubleOf());
    }

    @Override
    public SibillaValue exists(Set<YodaElementName> group, Function<YodaExpressionEvaluationContext, SibillaValue> predicate) {
        return SibillaValue.of(sys.exists(group, element -> predicate.apply(new YodaExpressionEvaluationAgentPredicateContext(element, element)).booleanOf()));
    }

    @Override
    public SibillaValue exists(Function<YodaExpressionEvaluationContext, SibillaValue> predicate) {
        return SibillaValue.of(sys.exists(element -> predicate.apply(new YodaExpressionEvaluationAgentPredicateContext(element, element)).booleanOf()));
    }

    @Override
    public SibillaValue forAll(Set<YodaElementName> group, Function<YodaExpressionEvaluationContext, SibillaValue> predicate) {
        return SibillaValue.of(sys.forall(group, element -> predicate.apply(new YodaExpressionEvaluationAgentPredicateContext(element, element)).booleanOf()));
    }

    @Override
    public SibillaValue forAll(Function<YodaExpressionEvaluationContext, SibillaValue> predicate) {
        return SibillaValue.of(sys.forall( element -> predicate.apply(new YodaExpressionEvaluationAgentPredicateContext(element, element)).booleanOf()));
    }
}
