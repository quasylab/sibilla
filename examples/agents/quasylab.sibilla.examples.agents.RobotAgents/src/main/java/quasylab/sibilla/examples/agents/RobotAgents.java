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
import quasylab.sibilla.core.simulator.sampling.SamplePredicate;

import java.util.Arrays;
import java.util.stream.IntStream;

public class RobotAgents {

    public final static int HEIGHT = 100;
    public final static int WIDTH = 100;
    public static final int ID_VAR = 0;
    public static final int REACH_VAR = 1;
    public static final int DIRX_VAR = 2;
    public static final int DIRY_VAR = 3;

    public final static int X_VAR = 0;
    public final static int Y_VAR = 1;

    private static final int ROBOT_OBSERVATIONS = 4;

    public static final int FRONT_SENSOR = 0;
    public static final int RIGHT_SENSOR = 1;
    public static final int LEFT_SENSOR = 2;
    public static final int BACK_SENSOR = 3;

    private static AgentAction UP = new ChangeDirectionAction("UP",0,+1);
    private static AgentAction DOWN = new ChangeDirectionAction("DOWN",0,-1);
    private static AgentAction RIGHT = new ChangeDirectionAction("RIGHT",+1,0);
    private static AgentAction LEFT = new ChangeDirectionAction("LEFT",-1,0);
    private static AgentAction STAND = new ChangeDirectionAction("STAND",0,0);

    private static AgentBehaviour DETERMINISTIC_ROBOT_BEHAVIOUR = new AgentBehaviour() {

        @Override
        public AgentAction step(RandomGenerator rg, double now, double[] currentState, double[] observations) {
            if (currentState[REACH_VAR]==0) {
                if (observations[FRONT_SENSOR] == 0) {
                    return UP;
                }
                if (observations[RIGHT_SENSOR] == 0) {
                    return RIGHT;
                }
                if (observations[LEFT_SENSOR] == 0) {
                    return LEFT;
                }
            }
            return STAND;
        }

    };


    private static SystemEnvironment ENVIRONMENT = new SystemEnvironment() {

        @Override
        public SystemState apply(RandomGenerator rg, SystemState currentState, AgentAction[] actions) {
            double[][] localStates = applyActions(rg,currentState,actions);
            double[][] newInfo = getNewInfo(currentState,localStates);

            return getSystemState(localStates,newInfo);
        }


    };

    private static SystemState getSystemState(double[][] localStates, double[][] newInfo) {
        return new SystemState(new double[0], newInfo,localStates);
    }

    private static AgentDefinition ROBOT = new AgentDefinition(DETERMINISTIC_ROBOT_BEHAVIOUR,4);


    public static double[] getAgentState(int id, boolean reached, int dx, int dy) {
        return new double[] {id, (reached?1.0:0.0), dx, dy};
    }

    public static double[] getRobotObservations(int i, SystemState currentState) {
        double[] observations = new double[4];
        observations[FRONT_SENSOR] = sense(i,0,1,currentState);
        observations[LEFT_SENSOR] = sense(i,-1,0,currentState);
        observations[RIGHT_SENSOR] = sense(i,1,0,currentState);
        observations[BACK_SENSOR] = sense(i,0,-1,currentState);
        return observations;
    }

    private static double sense(int i, int dx, int dy, SystemState currentState) {
        double x = currentState.getInfo(i,X_VAR);
        double y = currentState.getInfo(i,Y_VAR);
        if ((x+dx<0)||(x+dx==WIDTH)||(x+dy<0)||(x+dy==HEIGHT)) { return 1.0; }
        for( int j=0 ; j<currentState.numberOfAgents() ; j++ ) {
            if (i !=j) {
                if ((x+dx==currentState.getInfo(j,X_VAR))&&(y+dy==currentState.getInfo(j,Y_VAR))) {
                    return 1.0;
                }
            }
        }
        return 0.0;
    }

    public static double[] getNewInfo(double[] current, double[] state) {
        double[] toReturn = Arrays.copyOf(current,current.length);
        toReturn[X_VAR] += state[DIRX_VAR];
        toReturn[Y_VAR] += state[DIRY_VAR];
        return toReturn;
    }

    public static double[][] getNewInfo(SystemState state, double[][] agentStates) {
        double[][] toReturn = new double[agentStates.length][];
        IntStream.range(0,agentStates.length).forEach(i -> toReturn[i] = getNewInfo(state.getLocal(i),agentStates[i]));
        return toReturn;
    }

    public static double[][] applyActions(RandomGenerator rg, SystemState state, AgentAction[] actions) {
        double[][] toReturn = new double[state.numberOfAgents()][];
        IntStream.range(0,state.numberOfAgents()).forEach(i -> toReturn[i] = actions[i].performAction(rg,state.getLocal(i)) );
        return toReturn;
    }



    public static void main(String[] args) {
        RandomGenerator rg = new DefaultRandomGenerator();
        int N = 10;
        double deadline = 100.0;
        AgentModel model = new AgentModel(getAgents(N),getOmega(N),ENVIRONMENT);
        Trajectory<SystemState> trajectory = simulate(rg,model,getInitialState(N),deadline);
        //Use trajectory....
    }

    private static Trajectory<SystemState> simulate(RandomGenerator rg, AgentModel model, SystemState initialState, double deadline) {
        SimulationUnit<SystemState> simulationUnit = new SimulationUnit<>(model,initialState, SamplePredicate.timeDeadlinePredicate(deadline));
        SimulationTask<SystemState> task = new SimulationTask<>(rg,simulationUnit);
        return task.getTrajectory();
    }

    private static SystemState getInitialState(int n) {
        //Add here the code used to generate an initial state with n robots.
        return null;
    }

    private static OmegaFunction[] getOmega(int n) {
        OmegaFunction[] omega = new OmegaFunction[n];
        IntStream.range(0,n).forEach(i -> omega[i] = ((r, agentState, currentState) -> getRobotObservations(i,currentState)));
        return omega;
    }

    private static AgentDefinition[] getAgents(int n) {
        AgentDefinition[] agents = new AgentDefinition[n];
        IntStream.range(0,n).forEach(i -> agents[i] = new AgentDefinition(DETERMINISTIC_ROBOT_BEHAVIOUR,ROBOT_OBSERVATIONS));
        return agents;
    }


}
