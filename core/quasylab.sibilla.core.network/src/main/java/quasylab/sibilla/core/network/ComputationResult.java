/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 * Copyright (C) 2020.
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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package quasylab.sibilla.core.network;

import quasylab.sibilla.core.models.State;
import quasylab.sibilla.core.simulator.Trajectory;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Class that stores the results of a simulation executed by a slave.
 *
 * @param <S> The {@link State} of the simulation model.
 * @author Belenchia Matteo
 * @author Stelluti Francesco Pio
 * @author Zamponi Marco
 */
public class ComputationResult<S extends State> implements Serializable {
    private static final long serialVersionUID = -545122842766553412L;

    /**
     * List of trajectory that contains the results of a simulation.
     */
    private final LinkedList<Trajectory<S>> results;

    /**
     * Creates a new ComputationResult object with the list of trajectories passed in input
     *
     * @param results list of trajectories that compose the result of a simulation
     */
    public ComputationResult(LinkedList<Trajectory<S>> results) {
        this.results = results;
    }

    /**
     * Returns the list of trajectories of a simulation
     *
     * @return list of trajectories that compose the result of a simulation
     */
    public List<Trajectory<S>> getResults() {
        return results;
    }

}