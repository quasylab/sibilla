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

import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;

public class YodaExpressionEvaluationAgentDynamicContext implements YodaExpressionEvaluationContext {


    private final YodaVariableMapping agentState;

    private final YodaVariableMapping agentObservations;
    private final RandomGenerator rg;

    public YodaExpressionEvaluationAgentDynamicContext(RandomGenerator rg, YodaVariableMapping agentState, YodaVariableMapping agentObservations) {
        this.rg = rg;
        this.agentState = agentState;
        this.agentObservations = agentObservations;
    }

    @Override
    public SibillaValue get(YodaVariable var) {
        if (agentState.isDefined(var)) {
            return agentState.getValue(var);
        }
        return agentObservations.getValue(var);
    }

    @Override
    public SibillaValue it(YodaVariable name) {
        return get(name);
    }

    @Override
    public SibillaValue rnd() {
        return SibillaValue.of(rg.nextDouble());
    }

    @Override
    public SibillaValue rnd(SibillaValue from, SibillaValue to) {
        return SibillaValue.of(from.doubleOf()+rg.nextDouble()*(to.doubleOf()-from.doubleOf()));
    }
}
