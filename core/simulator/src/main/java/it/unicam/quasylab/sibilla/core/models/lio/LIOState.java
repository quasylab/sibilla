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

import it.unicam.quasylab.sibilla.core.tools.ProbabilityMatrix;
import it.unicam.quasylab.sibilla.core.tools.ProbabilityVector;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * This interface is implemented by LIO representations that are able to perform a computational step.
 *
 * @param <S> type of LIO representations.
 */
public interface LIOState<S extends LIOCollective> extends LIOCollective {

    /**
     * Given a random generator and the probability matrix, this method returns a sampling of next
     * state.
     *
     * @param randomGenerator random generator
     * @param matrix probability matrix
     * @return a sampling of next state.
     */
    S step(RandomGenerator randomGenerator, ProbabilityMatrix<Agent> matrix);


    /**
     * Returns the probability distribution of states reachable from this one in one state.
     *
     * @param matrix probability matrix representing behaviour of each single agent.
     * @return the probability distribution of states reachable from this one in one state.
     */
    ProbabilityVector<S> next(ProbabilityMatrix<Agent> matrix);


    ProbabilityVector<S> next();


}
