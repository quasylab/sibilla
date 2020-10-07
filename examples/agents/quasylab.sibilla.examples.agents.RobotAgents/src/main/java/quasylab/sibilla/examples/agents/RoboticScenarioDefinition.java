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

import java.util.HashMap;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

public class RoboticScenarioDefinition implements AgentModelBuilder<RobotArena> {
    public static final String ID_VAR = "id";
    public static final String REACH_VAR = "reach";
    public static final String DIRX_VAR = "dx";
    public static final String DIRY_VAR = "dy";
    public final static String X_VAR = "x";
    public final static String Y_VAR = "y";
    public static final String FRONT_SENSOR = "front_sensor";
    public static final String RIGHT_SENSOR = "right_sensor";
    public static final String LEFT_SENSOR = "left_sensor";
    public static final String BACK_SENSOR = "back_sensor";
    public static final String GOAL_SENSOR = "goal_sensor";
    private static final int ROBOT_OBSERVATIONS = 4;
    protected final int numberOfAgents;
    protected final int numberOfObstacles;
    protected final RandomGenerator rg;
    protected final int width;
    protected final int height;
    private IntFunction<AgentBehaviour> agentBehaviourIntFunction;
    private RobotArena world;

    public RoboticScenarioDefinition(IntFunction<AgentBehaviour> agentBehaviourIntFunction, int numberOfAgents, int numberOfObstacles, RandomGenerator rg, int width, int height) {
        this.numberOfAgents = numberOfAgents;
        this.numberOfObstacles = numberOfObstacles;
        this.rg = rg;
        this.width = width;
        this.height = height;
        this.agentBehaviourIntFunction = agentBehaviourIntFunction;
    }

    @Override
    public RobotArena getWorld() {
        if (world == null) {
            initialiseWorld();
        }
        return world;
    }

    @Override
    public void initialiseWorld() {
        world = RobotArena.generate(rg, width, height, numberOfObstacles);
    }

    public void setAgentBehaviourIntFunction(IntFunction<AgentBehaviour> agentBehaviourIntFunction) {
        this.agentBehaviourIntFunction = agentBehaviourIntFunction;
    }

    @Override
    public int getNumberOfAgents() {
        return numberOfAgents;
    }

    @Override
    public SystemEnvironment<RobotArena> getEnvironment() {
        return (rg, currentState, actions) -> {
            VariableMapping[] localStates = applyActions(rg, currentState, actions);
            VariableMapping[] newInfo = getNewInfo(currentState, localStates);
            return new SystemState<>(currentState.getWorld(), newInfo, localStates);
        };
    }

    public VariableMapping[] applyActions(RandomGenerator rg, SystemState<RobotArena> state, AgentAction[] actions) {
        VariableMapping[] toReturn = new VariableMapping[state.numberOfAgents()];
        IntStream.range(0, state.numberOfAgents()).forEach(i -> toReturn[i] = actions[i].performAction(rg, state.getLocal(i)));
        return toReturn;
    }

    public VariableMapping[] getNewInfo(SystemState<RobotArena> state, VariableMapping[] agentStates) {
        VariableMapping[] toReturn = new VariableMapping[agentStates.length];
        IntStream.range(0, agentStates.length).forEach(i -> toReturn[i] = getNewInfo(state.getInfo(i), agentStates[i]));
        return toReturn;
    }

    public VariableMapping getNewInfo(VariableMapping currentInfo, VariableMapping state) {
        double x = currentInfo.get("x");
        double y = currentInfo.get("y");

        return currentInfo.set(new SetVariable("x", x + state.get("dx")), new SetVariable("y", y + state.get("dy")));
    }

    @Override
    public OmegaFunction getOmegaFunction(int i) {
        return (OmegaFunction<RobotArena>) (r, agentState, currentState) -> {
            HashMap<String, Double> map = new HashMap<>();
            RobotArena world = currentState.getWorld();
            double x = currentState.getInfo(i, X_VAR);
            double y = currentState.getInfo(i, Y_VAR);
            map.put(FRONT_SENSOR, Math.max(world.thereIsAnObstacle(x, y + 1), thereIsAnAgentAt(i, x, y + 1, currentState)));
            map.put(LEFT_SENSOR, Math.max(world.thereIsAnObstacle(x - 1, y), thereIsAnAgentAt(i, x - 1, y, currentState)));
            map.put(RIGHT_SENSOR, Math.max(world.thereIsAnObstacle(x + 1, y), thereIsAnAgentAt(i, x + 1, y, currentState)));
            map.put(BACK_SENSOR, Math.max(world.thereIsAnObstacle(x, y - 1), thereIsAnAgentAt(i, x, y - 1, currentState)));
            map.put(GOAL_SENSOR, world.goalReached(x, y));
            return new VariableMapping(map);
        };
    }

    private double thereIsAnAgentAt(int i, double x, double y, SystemState<RobotArena> currentState) {
        for (int j = 0; j < currentState.numberOfAgents(); j++) {
            if (i != j) {
                if ((x == currentState.getInfo(j, X_VAR)) && (y == currentState.getInfo(j, Y_VAR))) {
                    return 1.0;
                }
            }
        }
        return 0.0;
    }

    @Override
    public AgentBehaviour getAgentBehaviour(int i) {
        return agentBehaviourIntFunction.apply(i);
    }

    @Override
    public VariableMapping getAgentState(RobotArena world, int i) {
        HashMap<String, Double> map = new HashMap<>();
        map.put(ID_VAR, (double) i);
        map.put(DIRX_VAR, 0.0);
        map.put(DIRY_VAR, 0.0);
        map.put(REACH_VAR, 0.0);
        return new VariableMapping(map);
    }

    @Override
    public VariableMapping getAgentInfo(RobotArena world, int i) {
        int gap = world.getWidth() / (numberOfAgents + 1);
        HashMap<String, Double> map = new HashMap<>();
        map.put(X_VAR, (double) i * gap);
        map.put(Y_VAR, 0.0);
        return new VariableMapping(map);
    }
}
