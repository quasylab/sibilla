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

package it.unicam.quasylab.sibilla.examples.yoda.roboticScenario;

import it.unicam.quasylab.sibilla.core.models.yoda.*;
import it.unicam.quasylab.sibilla.core.simulator.DefaultRandomGenerator;
import it.unicam.quasylab.sibilla.core.simulator.SimulationTask;
import it.unicam.quasylab.sibilla.core.simulator.SimulationUnit;
import it.unicam.quasylab.sibilla.core.simulator.Trajectory;
import it.unicam.quasylab.sibilla.core.simulator.sampling.TrajectoryCollector;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

public class RobotScenario {
    private RandomGenerator rg;

    private File outputDir;


    /*private static List<YodaAgent> AGENTS = new LinkedList<>();
    static {
        //AGENTS.add(RobotDefinition.T800); //T800 is a deterministic robot
        AGENTS.add(RobotDefinition.R2D2);  //Walle is a non deterministic robot
        AGENTS.add(RobotDefinition.CHOPPER);
        AGENTS.add(RobotDefinition.BD1);
    }*/

    public static void main(String[] args) throws  InterruptedException, FileNotFoundException {
        RobotScenario robotScenario = new RobotScenario(new DefaultRandomGenerator());
        //robotScenario.startSimulation("Test1", 10, 10, 15, AGENTS, 100, 100.0);
        robotScenario.startSimulation("Test2", 100, 100, 100, RobotDefinition.getRobotAgents(100, 0, 100, RobotBehaviour.NON_DETERMINISTIC), 1, 100, 150.0);
        robotScenario.startSimulation("Test3", 100, 100, 100, RobotDefinition.getRobotAgents(100, 0, 100, RobotBehaviour.NON_DETERMINISTIC), 2, 100, 150.0);
        robotScenario.startSimulation("Test4", 100, 100, 100, RobotDefinition.getRobotAgents(100, 0, 100, RobotBehaviour.NON_DETERMINISTIC), 3, 100, 150.0);
    }



    public RobotScenario(DefaultRandomGenerator rg) {
        this.rg = rg;
        this.outputDir = new File("./data");
        if (!this.outputDir.exists()) {
            this.outputDir.mkdir();
        }
    }

    public void startSimulation(String label, int width, int height, int numberOfObstacles, List<YodaAgent> agents, int gridType, int iterations, double deadline) throws FileNotFoundException {
        RobotScenarioDefinition def = new RobotScenarioDefinition(label, width, height, numberOfObstacles, agents, rg);
        //def.initialiseScene();
        def.initialiseScene(gridType);
        Trajectory<YodaSystemState<Grid>> robotTrajectory = getTrajectory(def, deadline);
        File cwd = new File(this.outputDir,label + "_result");
        cwd.mkdir();
        saveTrajectory(cwd, label+"_robot",  robotTrajectory, agents.size());
    }


    private void saveTrajectories(File outputDir, String label, LinkedList<Trajectory<YodaSystemState<Grid>>> trajectories) throws FileNotFoundException {
        int counter = 0;
        for (Trajectory<YodaSystemState<Grid>> t: trajectories) {
            saveDataOfAgents(new PrintWriter(new File(outputDir, label+"_"+(counter++)+".csv")), t);
        }
    }

    private void saveTrajectory(File outputDir, String label, Trajectory<YodaSystemState<Grid>> trajectory, int agentsNumb) throws FileNotFoundException {
        saveDataOfScene(new PrintWriter(new File(outputDir, label+"_arena.csv")), trajectory.getData().get(0).getValue().getScene());
        File agentsFolder = new File(outputDir, "robots");
        agentsFolder.mkdir();
        for (int i=0; i<agentsNumb; i++){
            File agentFile = new File(agentsFolder, "robot_"+i+".csv");
            saveDataOfAgents(new PrintWriter(agentFile), trajectory, i);
        }
        //saveDataOfAgents(new PrintWriter(new File(agentsFolder, label+"_agents.csv")), trajectory);
    }

    private void saveDataOfAgents(PrintWriter printWriter, Trajectory<YodaSystemState<Grid>> trajectory, int i) {
        trajectory.getData().stream().forEach(t-> {
            YodaSystemState<Grid> scenario = t.getValue();
            int posx = scenario.getAgentsInfo(i, RobotState.POSX_VAR).integerValue().map(YodaValue.IntegerValue::value).orElse(0);
            int posy = scenario.getAgentsInfo(i, RobotState.POSY_VAR).integerValue().map(YodaValue.IntegerValue::value).orElse(0);
            printWriter.printf("%d;%d", posx, posy);
            printWriter.printf("\n");
        });
        printWriter.flush();
        printWriter.close();
    }

    private void saveDataOfAgents(PrintWriter printWriter, Trajectory<YodaSystemState<Grid>> trajectory) {
        trajectory.getData().stream().sequential().forEach(t -> {
            //printWriter.printf("%f", t.getTime());
            YodaSystemState<Grid> scenario = t.getValue();
            for(int i=0; i<scenario.getAgents().size(); i++){
                int posx = scenario.getAgentsInfo(i, RobotState.POSX_VAR).integerValue().map(YodaValue.IntegerValue::value).orElse(0);
                int posy = scenario.getAgentsInfo(i, RobotState.POSY_VAR).integerValue().map(YodaValue.IntegerValue::value).orElse(0);
                printWriter.printf("%d;%d;", posx, posy);
            }
            printWriter.printf("\n");
        });
        printWriter.flush();
        printWriter.close();
    }

    private void saveDataOfScene(PrintWriter printWriter, Grid scene) {
        printWriter.printf("%d\n", scene.getWidthInt());
        printWriter.printf("%d\n", scene.getHeightInt());
        scene.getObstacles().stream().forEach(o -> printWriter.printf("%d; %d\n", o.getPosx(), o.getPosy()));
        printWriter.flush();
        printWriter.close();
    }

    private LinkedList<Trajectory<YodaSystemState<Grid>>> generateData(YodaModelBuilder<Grid> def, int iterations, double deadline) {
        LinkedList<Trajectory<YodaSystemState<Grid>>> trajectories = new LinkedList<>();
        for (int i=0; i<iterations; i++) {
            def.initialiseScene();
            trajectories.add(getTrajectory(def, deadline));
        }
        return trajectories;
    }

    private Trajectory<YodaSystemState<Grid>> getTrajectory(YodaModelBuilder<Grid> def, double deadline) {
        TrajectoryCollector<YodaSystemState<Grid>> collector = new TrajectoryCollector<>();
        YodaSystemState<Grid> state = def.getState();
        YodaModel<Grid> model = def.getYodaModel();

        SimulationUnit<YodaSystemState<Grid>> simulationUnit = new SimulationUnit<YodaSystemState<Grid>>(model,state,collector,(t, s) -> t>=deadline);
        SimulationTask<YodaSystemState<Grid>> task = new SimulationTask<>(rg, simulationUnit);
        task.get();
        return collector.getTrajectory();
    }
}
