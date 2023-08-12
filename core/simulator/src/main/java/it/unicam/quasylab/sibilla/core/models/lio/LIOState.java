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

import it.unicam.quasylab.sibilla.core.models.IndexedState;
import it.unicam.quasylab.sibilla.core.tools.ProbabilityMatrix;
import it.unicam.quasylab.sibilla.core.tools.ProbabilityVector;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * This interface is implemented by LIO representations that are able to perform a computational step.
 *
 */
public interface LIOState extends LIOCollective, IndexedState<LIOAgent> {

    /**
     * Given a random generator and the probability matrix, this method returns a sampling of next
     * state.
     *
     * @param randomGenerator random generator
     * @param matrix          probability matrix
     * @return a sampling of next state.
     */
    LIOState step(RandomGenerator randomGenerator, ProbabilityMatrix<LIOAgent> matrix);


    /**
     * Returns the probability distribution of the states reachable from this one state by using the given probability in one state.
     *
     * @param matrix probability matrix representing behaviour of each single agent.
     * @return the probability distribution of states reachable from this one in one state.
     */
    ProbabilityVector<? extends LIOState> next(ProbabilityMatrix<LIOAgent> matrix);


    ProbabilityVector<? extends LIOState> next();

    /**
     * Returns the agents definition in this state.
     *
     * @return the agents definition in this state.
     */
    LIOAgentDefinitions getAgentsDefinition();


    /**
     * Returns an array of doubles containing the fraction of population in each species.
     *
     * @return array of doubles containing the fraction of population in each species.
     */
    default LIOPopulationFraction getPopulationFractionVector() {
        return getAgentsDefinition().getPopulationFractionOf(this);
    }


    @Override
    default LIOAgent get(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    default int numberOfAgents() {
        throw new UnsupportedOperationException();
    }
}
