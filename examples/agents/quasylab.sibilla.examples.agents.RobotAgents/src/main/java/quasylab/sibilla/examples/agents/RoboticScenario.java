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
import quasylab.sibilla.core.models.quasylab.sibilla.core.models.agents.*;
import quasylab.sibilla.core.simulator.*;

import java.io.File;
import java.util.LinkedList;
import java.util.function.IntFunction;

public class RoboticScenario {

    private RandomGenerator rg;

    private File outputDir;

    public RoboticScenario(DefaultRandomGenerator rg) {
        this.rg = rg;
        this.outputDir = new File(".");
    }

    public static void main(String[] argv) throws InterruptedException {
        RoboticScenario rs = new RoboticScenario(new DefaultRandomGenerator());
        rs.testTwinGeneration("test1", 1,10,10,10,i-> new DeterministicRobotBehaviour(),100,100.0);
        rs.testTwinGeneration("test2", 1,10,10,10,i-> new ProbabilitsticRobotBehaviour(),100,100.0);
        rs.testTwinGeneration("test3", 1, 10, 10, 10, i-> new RandomisedRobotBehaviour(), 100, 100.0);
    }

    public void testTwinGeneration(String label, int numberOfAgents, int numberOfObstacles, int height, int widht, IntFunction<AgentBehaviour> agentBehaviour, int iterations, double deadline) throws InterruptedException {
        RoboticScenarioDefinition def = new RoboticScenarioDefinition(agentBehaviour,numberOfAgents,numberOfObstacles,rg,widht,height);
        SingleLogBuilder logBuilder = new SingleLogBuilder();
        LoggerWrapper<RobotArena> logger = getLoggedDefinition(def,logBuilder);
        LinkedList<Trajectory<SystemState<RobotArena>>> trainingTrajectories = generateData(logger,iterations,deadline);
        saveTrajectories(label, trainingTrajectories);
        def.initialiseWorld();
        Trajectory<SystemState<RobotArena>> originalTrajectory = getTrajectory(def,deadline);
        def.setAgentBehaviourIntFunction(i -> new AgentTwin(logBuilder.getLogger()));
        Trajectory<SystemState<RobotArena>> twinTrajectory = getTrajectory(def,deadline);
        saveTrajectory(label, originalTrajectory);
        saveTrajectory(label, twinTrajectory);
    }

    private void saveTrajectories(String label, LinkedList<Trajectory<SystemState<RobotArena>>> trajectories) {

    }

    private void saveTrajectory(String label, Trajectory<SystemState<RobotArena>> trajectory) {
    }




    private  AgentModelBuilder<RobotArena> getDeterministicDefinition(RandomGenerator rg, int width, int height, int numberOfAgents, int numberOfObstacles) {
        return new RoboticScenarioDefinition(
                i -> new DeterministicRobotBehaviour(),
                width,
                height,
                rg,
                numberOfAgents,
                numberOfObstacles
        );
    }

    private  LoggerWrapper<RobotArena> getLoggedDefinition(AgentModelBuilder<RobotArena> def, AgentLogBuilder logBuilder) {
        return new LoggerWrapper<>(def,logBuilder);
    }

    private  LinkedList<Trajectory<SystemState<RobotArena>>> generateData(AgentModelBuilder<RobotArena> def, int iterations, double deadline) throws InterruptedException {
        LinkedList<Trajectory<SystemState<RobotArena>>> trajectories = new LinkedList<>();
        for(int i=0; i<iterations; i++) {
            trajectories.add(getTrajectory(def, deadline));
        }
        return trajectories;
    }

    private  Trajectory<SystemState<RobotArena>> getTrajectory(AgentModelBuilder<RobotArena> def, double deadline) {
        def.initialiseWorld();
        AgentModel<RobotArena> model = def.getAgentModel();
        SystemState<RobotArena> state = def.getState();
        SimulationUnit<SystemState<RobotArena>> simulationUnit = new SimulationUnit<SystemState<RobotArena>>(model,state,(t,s) -> t>=deadline);
        SimulationTask<SystemState<RobotArena>> task = new SimulationTask<>(rg,simulationUnit);
        return task.get();
    }



}
