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
import it.unicam.quasylab.sibilla.core.models.State;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.function.IntPredicate;

/**
 * A LIOState indicates a state of a network of interactive objects.
 */
public interface LIOState extends ImmutableState {

    /**
     * Sample next agent state from given a row of probability transition matrix.
     *
     * @param randomGenerator random generator
     * @param probabilityRow probability matrix row associated with the agent
     * @param self self agent index
     * @return next agent state
     */
    static int doSample(RandomGenerator randomGenerator, double[] probabilityRow, int self) {
        double p = randomGenerator.nextDouble();
        double sum = 0.0;
        for(int i=0 ; i<probabilityRow.length; i++) {
            sum += probabilityRow[i];
            if (p<sum) {
                return i;
            }
        }

        return self;
    }

    /**
     * Return the number of agents in the system.
     *
     * @return the number of agents in the system.
     */
    int size();

    /**
     * Return the fraction of agents in the given state.
     *
     * @param stateIndex state index.
     * @return the fraction of agents in the given state.
     */
    double fractionOf(int stateIndex);

    /**
     * Return the fraction of agents in the given state.
     *
     * @param a agent state.
     * @return the fraction of agents in the given state.
     */
    default double fractionOf(Agent a) {
        return fractionOf(a.getIndex());
    }

    /**
     * Returns the fraction of agents in a state satisfying the given predicate.
     *
     * @param predicate state predicate.
     * @return  the fraction of agents in a state satisfying the given predicate.
     */
    double fractionOf(IntPredicate predicate);

    /**
     * Return the number of agents in the given state.
     *
     * @param stateIndex state index.
     * @return the number of agents in the given state.
     */
    double numberOf(int stateIndex);

    /**
     * Return the number of agents in the given state.
     *
     * @param a agent state.
     * @return the number of agents in the given state.
     */
    default double numberOf(Agent a) { return numberOf(a.getIndex()); }

    /**
     * Returns the number of agents in a state satisfying the given predicate.
     *
     * @param predicate state predicate.
     * @return  the number of agents in a state satisfying the given predicate.
     */
    double numberOf(IntPredicate predicate);

    /**
     * Given a random generator and the probability matrix, this method returns a sampling of next
     * state.
     *
     * @param randomGenerator random generator
     * @param matrix probability matrix
     * @return a sampling of next state.
     */
    LIOState step(RandomGenerator randomGenerator, double[][] matrix);


}
