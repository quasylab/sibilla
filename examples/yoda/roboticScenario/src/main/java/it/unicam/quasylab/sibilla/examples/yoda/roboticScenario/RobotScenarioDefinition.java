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
import org.apache.commons.math3.random.RandomGenerator;

import java.util.List;

public class RobotScenarioDefinition implements YodaModelBuilder<Grid> {

    protected final String label;
    protected final int width;
    protected final int height;
    protected final int numberOfObstacles;
    //protected final int numberOfAgents;
    protected final List<YodaAgent> agents;
    protected final RandomGenerator rg;

    public Grid grid;

    public YodaSystemState<Grid> yodaSysState;

    public RobotScenarioDefinition(String label, int width, int height, int numberOfObstacles, List<YodaAgent> agents, RandomGenerator rg) {
        this.label = label;
        this.width = width;
        this.height = height;
        this.numberOfObstacles = numberOfObstacles;
        this.agents = agents;
        this.rg = rg;
        //this.numberOfAgents = numberOfAgents;
    }

    @Override
    public Grid getScene() {
        if (grid == null){
            initialiseScene();
        }
        return grid;
    }

    @Override
    public void initialiseScene() {

        //grid = Grid.generate(rg, width, height, numberOfObstacles); //obstacles randomly placed


    }

    @Override
    public void initialiseScene(int sceneType) {
        switch (sceneType) {
            case 1 : grid = Grid.generateThroughColumn(rg, width, height, numberOfObstacles); //Obstacles placed randomly for each column
                break;
            case 2 : grid = Grid.generateDiagonal(rg, width, height,numberOfObstacles); //Obstacles placed in diagonals and one horizontal
                break;
            case 3 : grid = Grid.generateCulDeSac(rg,width, height); //Obstacles placed in random cul-de-sac
                break;
        }
        //
        //

    }

    @Override
    public int getNumberOfAgent() {
        return agents.size();
    }

    @Override
    public int getNumberOfObstacles() {
        return numberOfObstacles;
    }

    @Override
    public void addAgent(YodaAgent yodaAgent){
        agents.add(yodaAgent);
    }

    @Override
    public List<YodaAgent> getAgents(){
        return agents;
    }

    @Override
    public YodaVariableMapping getAgentsInfo(int i) {
        return agents.get(i).getAgentInformation();
    }

    //TODO
    @Override
    public YodaVariableMapping getGlobalState() {
        return null;
    }

    //TODO
    @Override
    public GlobalStateUpdateFunction getGlobalStateUpdateFunction() {
        return null;
    }
}
