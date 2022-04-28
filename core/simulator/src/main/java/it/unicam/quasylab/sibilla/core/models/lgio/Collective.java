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

package it.unicam.quasylab.sibilla.core.models.lgio;

import it.unicam.quasylab.sibilla.core.models.ImmutableState;
import it.unicam.quasylab.sibilla.core.models.State;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Collective implements State, ImmutableState {

    private final List<Agent> agents;

    public Collective(List<Agent> agents) {
        this.agents = agents;
    }

    public double fractionOf(Predicate<Agent> pred) {
        return numberOf(pred)/((double) agents.size());
    }

    public double numberOf(Predicate<Agent> pred) {
        return agents.stream().filter(pred).count();
    }

    public Collective step(RandomGenerator rg, Function<Agent, ActionProbabilityFunction> probabilityFunction) {
        return new Collective(
                agents.stream().map(a -> a.step(rg, probabilityFunction)).collect(Collectors.toList())
        );
    }

}
