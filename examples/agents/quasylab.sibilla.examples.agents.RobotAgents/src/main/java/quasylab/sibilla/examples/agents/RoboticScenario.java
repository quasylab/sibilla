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

package quasylab.sibilla.examples.agents;


import org.apache.commons.math3.random.RandomGenerator;
import quasylab.sibilla.core.models.quasylab.sibilla.core.models.agents.AgentModel;
import quasylab.sibilla.core.models.quasylab.sibilla.core.models.agents.SystemState;
import quasylab.sibilla.core.simulator.*;
import quasylab.sibilla.core.simulator.sampling.SamplingCollection;

public class RoboticScenario {

    public static void main(String[] argv) throws InterruptedException {
        RandomGenerator rg = new DefaultRandomGenerator();
        runDeterministicScenario(rg, 5,10, 1,10, 100, 30);
    }

    private static void runDeterministicScenario(RandomGenerator rg, int width, int height, int numberOfAgents, int numberOfObstacles, int iterations, double deadline) throws InterruptedException {
        RoboticScenarioDeterministicDefinition def = new RoboticScenarioDeterministicDefinition(
                rg,
                width,
                height,
                numberOfAgents,
                numberOfObstacles
                );
        AgentModel<RobotArena> model = def.getAgentModel();
        SystemState<RobotArena> state = def.getState();
        SimulationUnit<SystemState<RobotArena>> simulationUnit = new SimulationUnit<SystemState<RobotArena>>(model,state,(t,s) -> t>=deadline);
        SimulationTask<SystemState<RobotArena>> task = new SimulationTask<>(rg,simulationUnit);
        Trajectory<SystemState<RobotArena>> systemStateTrajectory = task.get();
        System.out.println(systemStateTrajectory.getEnd());
        showTrajectory(systemStateTrajectory);
    }

    private static void showTrajectory(Trajectory<SystemState<RobotArena>> systemStateTrajectory) {
    }


}
