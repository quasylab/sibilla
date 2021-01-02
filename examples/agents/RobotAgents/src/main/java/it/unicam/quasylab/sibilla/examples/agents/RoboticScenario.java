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

package it.unicam.quasylab.sibilla.examples.agents;


import it.unicam.quasylab.sibilla.core.models.quasylab.sibilla.core.models.agents.*;
import it.unicam.quasylab.sibilla.core.simulator.DefaultRandomGenerator;
import it.unicam.quasylab.sibilla.core.simulator.SimulationTask;
import it.unicam.quasylab.sibilla.core.simulator.SimulationUnit;
import it.unicam.quasylab.sibilla.core.simulator.Trajectory;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.function.IntFunction;

public class RoboticScenario {

    private RandomGenerator rg;

    private File outputDir;

    public RoboticScenario(DefaultRandomGenerator rg) {
        this.rg = rg;
        this.outputDir = new File("./data");
        if (!this.outputDir.exists()) {
            this.outputDir.mkdir();
        }
    }

    public static void main(String[] argv) throws InterruptedException, FileNotFoundException {
        RoboticScenario rs = new RoboticScenario(new DefaultRandomGenerator());
        rs.testTwinGeneration("deterministic", 1,15,10,10,i-> new DeterministicRobotBehaviour(),100,100.0);
        rs.testTwinGeneration("probabilistic", 1,15,10,10,i-> new ProbabilitsticRobotBehaviour(),100,500.0);
        rs.testTwinGeneration("nondeterministic", 1, 15, 10, 10, i-> new NondeterministicRobotBehaviour(), 100, 100.0);
        rs.testTwinGeneration("prioritised", 1, 15, 10, 10, true, i-> new PrioritisedRobotBehaviour(), 100, 100.0);
    }

    public void testTwinGeneration(String label, int numberOfAgents, int numberOfObstacles, int height, int width, IntFunction<AgentBehaviour> agentBehaviour, int iterations, double deadline) throws InterruptedException, FileNotFoundException {
        testTwinGeneration(label,numberOfAgents,numberOfObstacles,height,width,false,agentBehaviour,iterations,deadline);
    }

    public void testTwinGeneration(String label, int numberOfAgents, int numberOfObstacles, int height, int widht, boolean withFlagDirection, IntFunction<AgentBehaviour> agentBehaviour, int iterations, double deadline) throws InterruptedException, FileNotFoundException {
        RoboticScenarioDefinition def = new RoboticScenarioDefinition(agentBehaviour,numberOfAgents,numberOfObstacles,rg,widht,height,withFlagDirection);
        SingleLogBuilder logBuilder = new SingleLogBuilder();
        LoggerWrapper<RobotArena> logger = getLoggedDefinition(def,logBuilder);
        LinkedList<Trajectory<SystemState<RobotArena>>> trainingTrajectories = generateData(logger,iterations,deadline);
        File cwd = new File(this.outputDir,label+"_training");
        cwd.mkdir();
        saveTrajectories(cwd, label, trainingTrajectories);
        def.initialiseWorld();
        Trajectory<SystemState<RobotArena>> originalTrajectory = getTrajectory(def,deadline);
        def.setAgentBehaviourIntFunction(i -> new AgentTwin(logBuilder.getLogger()));
        Trajectory<SystemState<RobotArena>> twinTrajectory = getTrajectory(def,deadline);
        cwd = new File(this.outputDir,label+"_result");
        cwd.mkdir();
        saveTrajectory(cwd, label+"_original", originalTrajectory);
        saveTrajectory(cwd, label+"_twin", twinTrajectory);
    }

    private void saveTrajectories(File outputDir, String label, LinkedList<Trajectory<SystemState<RobotArena>>> trajectories) throws FileNotFoundException{
        int counter = 0;
        for(Trajectory<SystemState<RobotArena>> t: trajectories) {
            saveDataOfAgents(new PrintWriter(new File(outputDir,label+"_"+(counter++)+".data")),t);
        }
    }

    private void saveTrajectory(File outputDir, String label, Trajectory<SystemState<RobotArena>> trajectory) throws FileNotFoundException {
        saveDataOfArena(new PrintWriter(new File(outputDir,label+"_arena.data")),trajectory.getData().get(0).getValue().getWorld());
        saveDataOfAgents(new PrintWriter(new File(outputDir, label+"_agents.data")),trajectory);
    }

    private void saveDataOfArena(PrintWriter pw, RobotArena arena) {
        pw.printf("%d\n",arena.getWidth());
        pw.printf("%d\n",arena.getHeight());
        arena.getObstacles().stream().sequential().forEach(o -> pw.printf("%d;%d\n",o.getXPos(),o.getYPos()));
        pw.flush();
        pw.close();
    }

    private void saveDataOfAgents(PrintWriter pw, Trajectory<SystemState<RobotArena>> trajectory) {
        trajectory.getData().stream().sequential().forEach(s -> {
            pw.printf("%f",s.getTime());
            SystemState<RobotArena> state = s.getValue();
            for(int i=0 ; i<state.numberOfAgents() ; i++) {
                pw.printf(";%f;%f",state.getInfo(i,RoboticScenarioDefinition.X_VAR),state.getInfo(i,RoboticScenarioDefinition.Y_VAR));
            }
            pw.println();
        });
        pw.flush();
        pw.close();
    }

    private AgentModelBuilder<RobotArena> getDeterministicDefinition(RandomGenerator rg, int width, int height, int numberOfAgents, int numberOfObstacles) {
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
            def.initialiseWorld();
            trajectories.add(getTrajectory(def, deadline));
        }
        return trajectories;
    }

    private  Trajectory<SystemState<RobotArena>> getTrajectory(AgentModelBuilder<RobotArena> def, double deadline) {
        AgentModel<RobotArena> model = def.getAgentModel();
        SystemState<RobotArena> state = def.getState();
        SimulationUnit<SystemState<RobotArena>> simulationUnit = new SimulationUnit<SystemState<RobotArena>>(model,state,(t, s) -> t>=deadline);
        SimulationTask<SystemState<RobotArena>> task = new SimulationTask<>(rg,simulationUnit);
        return task.get();
    }



}
