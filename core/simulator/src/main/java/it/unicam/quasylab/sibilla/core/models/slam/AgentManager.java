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

package it.unicam.quasylab.sibilla.core.models.slam;

import it.unicam.quasylab.sibilla.core.models.State;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Instances of this class represents an environment where a number of agents are operating.
 */
public final class AgentManager implements State {

    private ArrayList<Agent> agents;

    public AgentManager(Supplier<Agent>[] agents) {
        this(Arrays.stream(agents).map(Supplier::get).collect(Collectors.toList()));
    }

    public AgentManager(List<Agent> agents) {
        this.agents = new ArrayList<>(agents);
    }

    public synchronized double getMinOf(ToDoubleFunction<AgentMemory> expr) {
        return agents.stream().mapToDouble(a -> a.eval(expr)).min().orElse(Double.NaN);
    }

    public synchronized double getMaxOf(ToDoubleFunction<AgentMemory> expr) {
        return agents.stream().mapToDouble(a -> a.eval(expr)).max().orElse(Double.NaN);
    }

    public synchronized double getMeanOf(ToDoubleFunction<AgentMemory> expr) {
        return agents.stream().mapToDouble(a -> a.eval(expr)).average().orElse(Double.NaN);
    }

    public synchronized boolean exists(Predicate<AgentMemory> p) {
        return agents.stream().anyMatch(a -> a.test(p));
    }

    public synchronized boolean forAll(Predicate<AgentMemory> p) {
        return agents.stream().allMatch(a -> a.test(p));
    }

    public Stream<Agent> stream() {
        return agents.stream();
    }
}
