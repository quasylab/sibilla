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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

/**
 * @author nicoladelgiudice
 *
 */
public class RobotDefinition {
    private static YodaVariableMapping KNOWLEDGE = RobotState.AGENT_ACC_MAP;
    private static YodaVariableMapping INFORMATION1 = RobotState.AGENT_POS_MAP1;
    private static YodaVariableMapping INFORMATION2 = RobotState.AGENT_POS_MAP2;
    private static YodaVariableMapping INFORMATION3 = RobotState.AGENT_POS_MAP3;
    private static YodaVariableMapping OBSERVATIONS = RobotObservation.AGENT_OBS_MAP;
    private static YodaBehaviour DET_BEHAVIOUR = RobotBehaviour.DETERMINISTIC;
    private static YodaBehaviour NON_BEHAVIOUR = RobotBehaviour.NON_DETERMINISTIC;
    private static OmegaFunction OMEGA = RobotDefinition::computeOmega;
    private static AgentInfoUpdateFunction INFO_UPDATE = RobotDefinition::computeInfoUpdate;
    private RandomGenerator rg;


    //public static YodaAgent T800 = new YodaAgent(0, "T800", KNOWLEDGE, INFORMATION, OBSERVATIONS, DET_BEHAVIOUR, OMEGA, INFO_UPDATE);
    public static YodaAgent R2D2 = new YodaAgent(1,"R2D2", KNOWLEDGE, INFORMATION1, OBSERVATIONS, NON_BEHAVIOUR, OMEGA, INFO_UPDATE);
    public static YodaAgent CHOPPER = new YodaAgent(2, "C1-10P", KNOWLEDGE, INFORMATION2, OBSERVATIONS, NON_BEHAVIOUR, OMEGA, INFO_UPDATE);
    public static YodaAgent BD1 = new YodaAgent(3, "BD-1", KNOWLEDGE, INFORMATION3, OBSERVATIONS, NON_BEHAVIOUR, OMEGA, INFO_UPDATE);

    private static YodaVariableMapping computeOmega(RandomGenerator rg, YodaSystemState yodaSystemState, YodaAgent yodaAgent) {
        YodaVariableMapping agentPos = yodaAgent.getAgentInformation();
        YodaVariableMapping newObservations = yodaAgent.getAgentObservations().copy();
        YodaScene scene = yodaSystemState.getScene();
        int x = agentPos.getValue(RobotState.POSX_VAR).integerValue().map(YodaValue.IntegerValue::value).orElse(0);
        int y = agentPos.getValue(RobotState.POSY_VAR).integerValue().map(YodaValue.IntegerValue::value).orElse(0);
        newObservations.setValue(RobotObservation.NORTH_VAR, new YodaValue.BooleanValue(scene.thereIsSomething(x, y+1)));
        newObservations.setValue(RobotObservation.SOUTH_VAR, new YodaValue.BooleanValue(scene.thereIsSomething(x, y-1)));
        newObservations.setValue(RobotObservation.EAST_VAR, new YodaValue.BooleanValue(scene.thereIsSomething(x+1, y)));
        newObservations.setValue(RobotObservation.WEST_VAR, new YodaValue.BooleanValue(scene.thereIsSomething(x-1, y)));
        newObservations.setValue(RobotObservation.GOAL_VAR, new YodaValue.BooleanValue(y == scene.getHeightInt()));
        return newObservations;
    }

    private static YodaVariableMapping computeInfoUpdate(RandomGenerator randomGenerator, YodaVariableMapping knowledge, YodaVariableMapping information) {
        YodaVariableMapping newInfo = information.copy();
        int posX = information.getValue(RobotState.POSX_VAR).integerValue().map(YodaValue.IntegerValue::value).orElse(0);
        int posY = information.getValue(RobotState.POSY_VAR).integerValue().map(YodaValue.IntegerValue::value).orElse(0);
        int accX = knowledge.getValue(RobotState.ACCX_VAR).integerValue().map(YodaValue.IntegerValue::value).orElse(0);
        int accY = knowledge.getValue(RobotState.ACCY_VAR).integerValue().map(YodaValue.IntegerValue::value).orElse(0);
        newInfo.setValue(RobotState.POSX_VAR, new YodaValue.IntegerValue(posX + accX));
        newInfo.setValue(RobotState.POSY_VAR, new YodaValue.IntegerValue(posY + accY));
        return newInfo;
    }

    public static LinkedList<YodaAgent> getRobotAgents(int size, int from, int to, YodaBehaviour behaviour) {
        Random random = new Random(1000);
        int dt = (to - from) / size;
        LinkedList<YodaAgent> agents = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            agents.add(new YodaAgent(i, "R" + i, KNOWLEDGE, getInformation(from + random.nextInt(to-from)), OBSERVATIONS, behaviour, OMEGA, INFO_UPDATE));
        }
        return agents;
    }


    private static YodaVariableMapping getInformation(int xVal) {
        HashMap<YodaVariable, YodaValue> map = new HashMap<>();
        map.put(RobotState.POSX_VAR, YodaValue.integerOf(xVal));
        map.put(RobotState.POSY_VAR, RobotState.POSY_VAL);
        YodaVariableMapping variableMapping = new RobotState(map);
        return variableMapping;
    }

}
