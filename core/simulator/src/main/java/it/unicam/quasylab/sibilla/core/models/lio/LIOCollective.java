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

package it.unicam.quasylab.sibilla.core.models.lio;

import it.unicam.quasylab.sibilla.core.models.ImmutableState;
import java.util.function.Predicate;

/**
 * A LIOState indicates a state of a network of interactive objects.
 */
public interface LIOCollective extends ImmutableState {


    /**
     * Return the number of agents in the system.
     *
     * @return the number of agents in the system.
     */
    int size();

    /**
     * Return the fraction of agents in the given state.
     *
     * @param a agent state.
     * @return the fraction of agents in the given state.
     */
    default double fractionOf(Agent a) {
        return this.numberOf(a)/size();
    }

    /**
     * Returns the fraction of agents in a state satisfying the given predicate.
     *
     * @param predicate state predicate.
     * @return  the fraction of agents in a state satisfying the given predicate.
     */
    default double fractionOf(Predicate<Agent> predicate) {
        return this.numberOf(predicate)/size();
    }


    /**
     * Return the number of agents in the given state.
     *
     * @param a agent state.
     * @return the number of agents in the given state.
     */
    double numberOf(Agent a);

    /**
     * Returns the number of agents in a state satisfying the given predicate.
     *
     * @param predicate state predicate.
     * @return  the number of agents in a state satisfying the given predicate.
     */
    double numberOf(Predicate<Agent> predicate);


}
