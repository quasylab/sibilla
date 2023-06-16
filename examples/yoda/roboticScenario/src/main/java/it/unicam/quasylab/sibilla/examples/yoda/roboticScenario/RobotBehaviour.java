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

import it.unicam.quasylab.sibilla.core.models.yoda.YodaAction;
import it.unicam.quasylab.sibilla.core.models.yoda.YodaBehaviour;
import it.unicam.quasylab.sibilla.core.models.yoda.YodaValue;
import it.unicam.quasylab.sibilla.core.models.yoda.YodaVariableMapping;
import it.unicam.quasylab.sibilla.core.simulator.util.WeightedLinkedList;
import it.unicam.quasylab.sibilla.core.simulator.util.WeightedStructure;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.LinkedList;
import java.util.List;

public class RobotBehaviour {

    public static List<YodaAction> actionList;
    static {
        actionList = new LinkedList<>();
        actionList.add(RobotAction.GO_NORTH);
        actionList.add(RobotAction.GO_SOUTH);
        actionList.add(RobotAction.GO_EAST);
        actionList.add(RobotAction.GO_WEST);
        actionList.add(RobotAction.STAND_STILL);
    }

    public static YodaBehaviour DETERMINISTIC = YodaBehaviour.behaviourOf("Deterministic", actionList, RobotBehaviour::deterministicBehaviour);
    public static YodaBehaviour NON_DETERMINISTIC = YodaBehaviour.behaviourOf("NonDeterministic", actionList, RobotBehaviour::nonDeterministicBehaviour);
    public static YodaBehaviour FOUR_DIRECTIONS = YodaBehaviour.behaviourOf("FourDirection", actionList, RobotBehaviour::fourDirectionsBehaviour);




    public static WeightedStructure<YodaAction> deterministicBehaviour(RandomGenerator rg, YodaVariableMapping currentState, YodaVariableMapping observations){
        WeightedStructure<YodaAction> possibleActions = new WeightedLinkedList<>();

        if (observations.getValue(RobotObservation.GOAL_VAR).equals(YodaValue.TRUE)){
            possibleActions.add(1.0, RobotAction.STAND_STILL);
            return possibleActions;
        }
        if (observations.getValue(RobotObservation.NORTH_VAR).equals(YodaValue.FALSE)) {
            possibleActions.add(1.0, RobotAction.GO_NORTH);
        }
        if (observations.getValue(RobotObservation.NORTH_VAR).equals(YodaValue.TRUE) && observations.getValue(RobotObservation.EAST_VAR).equals(YodaValue.FALSE)) {
            possibleActions.add(1.0, RobotAction.GO_EAST);
        }
        if (observations.getValue(RobotObservation.NORTH_VAR).equals(YodaValue.TRUE) && observations.getValue(RobotObservation.EAST_VAR).equals(YodaValue.TRUE)){
            possibleActions.add(1.0, RobotAction.GO_WEST);
        }
        return possibleActions;
    }

    public static WeightedStructure<YodaAction> nonDeterministicBehaviour(RandomGenerator rg, YodaVariableMapping currentState, YodaVariableMapping observations){
        WeightedStructure<YodaAction> possibleActions = new WeightedLinkedList<>();

        if (observations.getValue(RobotObservation.GOAL_VAR).equals(YodaValue.TRUE)) {
            possibleActions.add(1.0, RobotAction.STAND_STILL);
            return possibleActions;
        }
        if (observations.getValue(RobotObservation.NORTH_VAR).equals(YodaValue.FALSE)) {
            possibleActions.add(1.0, RobotAction.GO_NORTH);
        }
        if (observations.getValue(RobotObservation.NORTH_VAR).equals(YodaValue.TRUE) && observations.getValue(RobotObservation.EAST_VAR).equals(YodaValue.FALSE)) {
            possibleActions.add(1.0, RobotAction.GO_EAST);
        }
        if (observations.getValue(RobotObservation.NORTH_VAR).equals(YodaValue.TRUE) && observations.getValue(RobotObservation.WEST_VAR).equals(YodaValue.FALSE)) {
            possibleActions.add(1.0, RobotAction.GO_WEST);
        }

        return possibleActions;
    }

    public static WeightedStructure<YodaAction> fourDirectionsBehaviour(RandomGenerator randomGenerator, YodaVariableMapping currentState, YodaVariableMapping observations) {
        WeightedStructure<YodaAction> possibleActions = new WeightedLinkedList<>();

        if (observations.getValue(RobotObservation.GOAL_VAR).equals(YodaValue.TRUE)) {
            possibleActions.add(1.0, RobotAction.STAND_STILL);
            return possibleActions;
        }
        if (observations.getValue(RobotObservation.NORTH_VAR).equals(YodaValue.FALSE)) {
            possibleActions.add(2.0, RobotAction.GO_NORTH);
        }
        if (observations.getValue(RobotObservation.EAST_VAR).equals(YodaValue.FALSE)) {
            possibleActions.add(1.0, RobotAction.GO_EAST);
        }
        if (observations.getValue(RobotObservation.WEST_VAR).equals(YodaValue.FALSE)) {
            possibleActions.add(1.0, RobotAction.GO_WEST);
        }
        if (observations.getValue(RobotObservation.SOUTH_VAR).equals(YodaValue.FALSE)) {
            possibleActions.add(1.0, RobotAction.GO_SOUTH);
        }

        return possibleActions;
    }
}
