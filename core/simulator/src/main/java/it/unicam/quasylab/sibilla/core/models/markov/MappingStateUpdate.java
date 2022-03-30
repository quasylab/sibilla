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

package it.unicam.quasylab.sibilla.core.models.markov;

import it.unicam.quasylab.sibilla.core.models.util.MappingState;
import it.unicam.quasylab.sibilla.core.simulator.util.WeightedElement;

import java.util.HashMap;
import java.util.Map;
import java.util.function.*;

/**
 * This class is used to identify an update of a MarkovChainModel state.
 */
public class MappingStateUpdate {

    /**
     * This predicate checks if the update is enabled or not.
     */
    private final Predicate<MappingState> guard;

    /**
     * Associates the update with a weight. The result must be a value greater than
     * 0 if the predicate guard is evaluated to true.
     */
    private final ToDoubleFunction<MappingState> weightFunction;

    /**
     * A hashmap associating each variable with its update.
     */
    private final Map<Integer,ToIntFunction<MappingState>> update;

    /**
     * Create a new MappingStateUpdate.
     *
     * @param guard a predicate used to check if the update is enabled or not.
     * @param weightFunction a function associating each mapping state with a rate.
     * @param update the updated.
     */
    public MappingStateUpdate(
            Predicate<MappingState> guard,
            ToDoubleFunction<MappingState>  weightFunction,
            Map<Integer, ToIntFunction<MappingState>> update) {
        this.guard = guard;
        this.weightFunction = weightFunction;
        this.update = update;
    }

    /**
     * Check if the update is enabled at the current state.
     *
     * @param state current state.
     * @return true if the update is enabled at the current state.
     */
    public boolean isEnabled(MappingState state) {
        return (guard == null)||(guard.test(state));
    }

    /**
     * Weight of the update at the current state.
     *
     * @param state current state.
     * @return weight of the update at the current state.
     */
    public double weightOf(MappingState state) {
        return weightFunction.applyAsDouble(state);
    }

    /**
     * Apply the update to the current state.
     *
     * @param state current state.
     * @return new state after the update.
     */
    public MappingState apply(MappingState state) {
        return state.apply(this.update);
    }
}
