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
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.function.BiFunction;
import java.util.function.Function;

public class YodaExpressionEvaluationAgentContext implements YodaExpressionEvaluationContext {

    private final RandomGenerator rg;

    private final YodaAgent agent;

    public YodaExpressionEvaluationAgentContext(RandomGenerator rg, YodaAgent agent) {
        this.rg = rg;
        this.agent = agent;
    }

    public static BiFunction<RandomGenerator, YodaAgent, SibillaValue> unpack(Function<YodaExpressionEvaluationContext, SibillaValue> f) {
        return (rg, agent) -> f.apply(new YodaExpressionEvaluationAgentContext(rg, agent));
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
    public SibillaValue rnd(SibillaValue from, SibillaValue to) {
        return SibillaValue.of(from.doubleOf()+rg.nextDouble()*(to.doubleOf()-from.doubleOf()));
    }

    @Override
    public SibillaValue it(YodaVariable name) {
        return agent.get(name);
    }

}
