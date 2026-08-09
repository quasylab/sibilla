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
package it.unicam.quasylab.sibilla.core.models.carma.targets.commons.states;

import it.unicam.quasylab.sibilla.core.models.ImmutableState;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.expressions.ExpressionContext;

import java.util.*;
import java.util.function.BiPredicate;

public class AgentState implements ImmutableState {
    private final Map<Agent, Long> agents;
    private final Long populationSize;

    public AgentState(Map<Agent, Long> agents) {
        this.agents = agents;
        this.populationSize = agents.values().stream().reduce(0L, Long::sum);
    }

    public AgentState() {
        this.agents = new HashMap<>();
        this.populationSize = 0L;
    }

    public Map<Agent,Long> getAgents() {
        return agents;
    }

    public double fractionOf(BiPredicate<Integer, ExpressionContext> predicate) {
        return this.numberOf(predicate) / (double)populationSize;
    }

    public double numberOf(BiPredicate<Integer, ExpressionContext> predicate) {
        return agents.entrySet()
                .stream()
                .filter(e -> predicate.test(e.getKey().species(), new ExpressionContext(e.getKey().values(), this)))
                .map(Map.Entry::getValue)
                .reduce(0L, Long::sum);
    }
}
