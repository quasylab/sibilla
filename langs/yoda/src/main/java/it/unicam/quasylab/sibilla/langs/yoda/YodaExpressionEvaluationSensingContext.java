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

import it.unicam.quasylab.sibilla.core.models.yoda.YodaAgent;
import it.unicam.quasylab.sibilla.core.models.yoda.YodaElementName;
import it.unicam.quasylab.sibilla.core.models.yoda.YodaSystemState;
import it.unicam.quasylab.sibilla.core.models.yoda.YodaVariable;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Set;
import java.util.function.Function;

public class YodaExpressionEvaluationSensingContext implements YodaExpressionEvaluationContext {
    private final RandomGenerator rg;
    private final YodaSystemState system;
    private final YodaAgent agent;

    public YodaExpressionEvaluationSensingContext(RandomGenerator rg, YodaSystemState system, YodaAgent agent) {
        this.rg = rg;
        this.system = system;
        this.agent = agent;
    }

    @Override
    public SibillaValue get(YodaVariable var) {
        return agent.get(var);
    }

    @Override
    public SibillaValue rnd() {
        return SibillaValue.of(rg.nextDouble());
    }

    @Override
    public SibillaValue min(Set<YodaElementName> group, Function<YodaExpressionEvaluationContext, SibillaValue> guard, Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return system.min(agent, group, other -> guard.apply(new YodaExpressionEvaluationAgentPredicateContext(agent, other)).booleanOf(), other -> expression.apply(new YodaExpressionEvaluationAgentPredicateContext(agent, other)).doubleOf());
    }

    @Override
    public SibillaValue min(Function<YodaExpressionEvaluationContext, SibillaValue> guard, Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return system.min(agent, other -> guard.apply(new YodaExpressionEvaluationAgentPredicateContext(agent, other)).booleanOf(), other -> expression.apply(new YodaExpressionEvaluationAgentPredicateContext(agent, other)).doubleOf());
    }

    @Override
    public SibillaValue min(Set<YodaElementName> group, Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return system.min(agent, group, other -> expression.apply(new YodaExpressionEvaluationAgentPredicateContext(agent, other)).doubleOf());
    }

    @Override
    public SibillaValue min(Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return system.min(agent, other -> expression.apply(new YodaExpressionEvaluationAgentPredicateContext(agent, other)).doubleOf());
    }

    @Override
    public SibillaValue max(Set<YodaElementName> group, Function<YodaExpressionEvaluationContext, SibillaValue> guard, Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return system.max(agent, group, other -> guard.apply(new YodaExpressionEvaluationAgentPredicateContext(agent, other)).booleanOf(), other -> expression.apply(new YodaExpressionEvaluationAgentPredicateContext(agent, other)).doubleOf());
    }

    @Override
    public SibillaValue max(Function<YodaExpressionEvaluationContext, SibillaValue> guard, Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return system.max(agent, other -> guard.apply(new YodaExpressionEvaluationAgentPredicateContext(agent, other)).booleanOf(), other -> expression.apply(new YodaExpressionEvaluationAgentPredicateContext(agent, other)).doubleOf());
    }

    @Override
    public SibillaValue max(Set<YodaElementName> group, Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return system.max(agent, group, other -> expression.apply(new YodaExpressionEvaluationAgentPredicateContext(agent, other)).doubleOf());
    }

    @Override
    public SibillaValue max(Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return system.max(agent, other -> expression.apply(new YodaExpressionEvaluationAgentPredicateContext(agent, other)).doubleOf());
    }

    @Override
    public SibillaValue mean(Set<YodaElementName> group, Function<YodaExpressionEvaluationContext, SibillaValue> guard, Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return system.mean(agent, group, other -> guard.apply(new YodaExpressionEvaluationAgentPredicateContext(agent, other)).booleanOf(), other -> expression.apply(new YodaExpressionEvaluationAgentPredicateContext(agent, other)).doubleOf());
    }

    @Override
    public SibillaValue mean(Function<YodaExpressionEvaluationContext, SibillaValue> guard, Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return system.mean(agent, other -> guard.apply(new YodaExpressionEvaluationAgentPredicateContext(agent, other)).booleanOf(), other -> expression.apply(new YodaExpressionEvaluationAgentPredicateContext(agent, other)).doubleOf());
    }

    @Override
    public SibillaValue mean(Set<YodaElementName> group, Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return system.mean(agent, group, other -> expression.apply(new YodaExpressionEvaluationAgentPredicateContext(agent, other)).doubleOf());
    }

    @Override
    public SibillaValue mean(Function<YodaExpressionEvaluationContext, SibillaValue> expression) {
        return system.mean(agent, other -> expression.apply(new YodaExpressionEvaluationAgentPredicateContext(agent, other)).doubleOf());
    }

    @Override
    public SibillaValue rnd(SibillaValue from, SibillaValue to) {
        return SibillaValue.of(from.doubleOf()+rg.nextDouble()*(to.doubleOf()-from.doubleOf()));
    }

    @Override
    public SibillaValue exists(Set<YodaElementName> group, Function<YodaExpressionEvaluationContext, SibillaValue> predicate) {
        return SibillaValue.of(system.exists(group, other -> predicate.apply(new YodaExpressionEvaluationAgentPredicateContext(agent, other)).booleanOf()));
    }

    @Override
    public SibillaValue exists(Function<YodaExpressionEvaluationContext, SibillaValue> predicate) {
        return SibillaValue.of(system.exists(other -> predicate.apply(new YodaExpressionEvaluationAgentPredicateContext(agent, other)).booleanOf()));
    }

    @Override
    public SibillaValue forAll(Set<YodaElementName> group, Function<YodaExpressionEvaluationContext, SibillaValue> predicate) {
        return SibillaValue.of(system.forall(group, other -> predicate.apply(new YodaExpressionEvaluationAgentPredicateContext(agent, other)).booleanOf()));
    }

    @Override
    public SibillaValue forAll(Function<YodaExpressionEvaluationContext, SibillaValue> predicate) {
        return SibillaValue.of(system.forall(other -> predicate.apply(new YodaExpressionEvaluationAgentPredicateContext(agent, other)).booleanOf()));
    }

    @Override
    public SibillaValue it(YodaVariable name) {
        return agent.get(name);
    }

    @Override
    public SibillaValue count(Set<YodaElementName> group, Function<YodaExpressionEvaluationContext, SibillaValue> guard) {
        return system.count(group, other -> guard.apply(new YodaExpressionEvaluationAgentPredicateContext(agent, other)).booleanOf());
    }

    @Override
    public SibillaValue count(Set<YodaElementName> group) {
        return YodaExpressionEvaluationContext.super.count(group);
    }
}
