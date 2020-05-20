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

import quasylab.sibilla.core.past.State;
import quasylab.sibilla.core.simulator.SimulationTask;

import java.io.Serializable;
import java.util.List;

/**
 * Class that stores a list of tasks sent through network.
 *
 * @param <S> The {@link quasylab.sibilla.core.past.State} of the simulation model.
 * @author Belenchia Matteo
 * @author Stelluti Francesco Pio
 * @author Zamponi Marco
 */
public class NetworkTask<S extends State> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * List of tasks to be executed by a slave server
     */
    private final List<SimulationTask<S>> tasks;

    /**
     * Creates a NetworkTask object from a list of tasks
     *
     * @param tasks list of tasks to be executed by a slave server
     */
    public NetworkTask(List<SimulationTask<S>> tasks) {
        this.tasks = tasks;
    }

    /**
     * Returns the list of tasks to be executed by a slave server
     *
     * @return list of tasks to be executed by a slave server
     */
    public List<SimulationTask<S>> getTasks() {
        return tasks;
    }

}