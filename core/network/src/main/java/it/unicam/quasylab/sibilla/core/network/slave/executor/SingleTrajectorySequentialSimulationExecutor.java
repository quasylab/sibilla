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

package it.unicam.quasylab.sibilla.core.network.slave.executor;

import it.unicam.quasylab.sibilla.core.models.Model;
import it.unicam.quasylab.sibilla.core.network.ComputationResult;
import it.unicam.quasylab.sibilla.core.network.NetworkTask;
import it.unicam.quasylab.sibilla.core.network.communication.TCPNetworkManager;
import it.unicam.quasylab.sibilla.core.network.serialization.ComputationResultSerializerType;
import it.unicam.quasylab.sibilla.core.simulator.SimulationTask;
import it.unicam.quasylab.sibilla.core.simulator.Trajectory;

import java.util.LinkedList;
import java.util.List;

public class SingleTrajectorySequentialSimulationExecutor extends SimulationExecutor {
    public SingleTrajectorySequentialSimulationExecutor(ExecutorType exType, ComputationResultSerializerType crSerializerType) {
        super(exType, crSerializerType);
    }

    @Override
    public void simulate(NetworkTask networkTask, TCPNetworkManager master) {
        List<? extends SimulationTask<?>> tasks = networkTask.getTasks();
        Model model = tasks.get(0).getUnit().getModel();

        LinkedList<Trajectory> trajectories = new LinkedList<>();

        //FIXME!
//        this.computationBenchmark.run(() -> {
//            for (int i = 0; i < tasks.size(); i++) {
//                Trajectory trajectory = tasks.get(i).get();
//                trajectories.add(trajectory);
//            }
//            return List.of((double) tasks.size());
//        });

        for (Trajectory singleTrajectory : trajectories) {
            sendResult(new ComputationResult(new LinkedList<>(List.of(singleTrajectory))), master, model);
        }

        /*
        for (int i = 0; i < tasks.size(); i++) {
            Trajectory trajectory = tasks.get(i).get();
            sendResult(new ComputationResult(new LinkedList<>(List.of(trajectory))), master, model);
        }
         */

    }
}
