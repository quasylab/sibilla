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

/**
 * 
 */
package it.unicam.quasylab.sibilla.examples.pm.crowds;

import it.unicam.quasylab.sibilla.core.models.pm.PopulationModel;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.simulator.SimulationEnvironment;
import it.unicam.quasylab.sibilla.core.simulator.sampling.SamplingFunction;

/**
 * @author loreti
 *
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {
        simulateChordModel(10,1000,1000,1000);
        simulateCrowdModel(10,1000,1000,1000);
        simulateMeshModel(5, 5,1000,1000,1000);
        simulateTierModel(5, 5,1000,1000,1000);
    }

    private static void simulateChordModel(int N, double deadline, int replica, int samplings) throws InterruptedException {
        ChordModel def = new ChordModel();
        def.setParameter("N",N);
        PopulationModel model = def.createModel();
        SimulationEnvironment simulator = new SimulationEnvironment();
        SamplingFunction<PopulationState> collection = model.getSamplingFunction(samplings,deadline/samplings);

        simulator.simulate(model,def.state(),collection,replica,deadline);
    }

    private static void simulateCrowdModel(int N, double deadline, int replica, int samplings) throws InterruptedException {
        CrowdDefinition def = new CrowdDefinition();
        def.setParameter("N",N);
        PopulationModel model = def.createModel();
        SimulationEnvironment simulator = new SimulationEnvironment();
        SamplingFunction<PopulationState> collection = model.getSamplingFunction(samplings,deadline/samplings);

        simulator.simulate(model,def.state(),collection,replica,deadline);
    }

    private static void simulateMeshModel(int H, int N, double deadline, int replica, int samplings) throws InterruptedException {
        MeshModel def = new MeshModel();
        def.setParameter("N",N);
        def.setParameter("H",H);
        PopulationModel model = def.createModel();
        SimulationEnvironment simulator = new SimulationEnvironment();
        SamplingFunction<PopulationState> collection = model.getSamplingFunction(samplings,deadline/samplings);

        simulator.simulate(model,def.state(),collection,replica,deadline);
    }

    private static void simulateTierModel(int H, int N, double deadline, int replica, int samplings) throws InterruptedException {
        TierModel def = new TierModel();
        def.setParameter("N",N);
        def.setParameter("H",H);
        PopulationModel model = def.createModel();
        SimulationEnvironment simulator = new SimulationEnvironment();
        SamplingFunction<PopulationState> collection = model.getSamplingFunction(samplings,deadline/samplings);

        simulator.simulate(model,def.state(),collection,replica,deadline);
    }

}
