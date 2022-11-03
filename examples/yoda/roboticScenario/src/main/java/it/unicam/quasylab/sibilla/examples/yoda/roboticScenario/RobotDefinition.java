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

/**
 * @author nicoladelgiudice
 *
 */
public class RobotDefinition {
    private static YodaVariableMapping KNOWLEDGE = RobotState.AGENT_ACC_MAP;
    private static YodaVariableMapping INFORMATION = RobotState.AGENT_POS_MAP;
    private static YodaVariableMapping OBSERVATIONS = RobotObservation.AGENT_OBS_MAP;
    private static YodaBehaviour DET_BEHAVIOUR = RobotBehaviour.DETERMINISTIC;
    private static OmegaFunction OMEGA = RobotDefinition::computeOmega;
    private static AgentInfoUpdateFunction INFO_UPDATE = RobotDefinition::computeInfoUpdate;
    private RandomGenerator rg;


    public static YodaAgent T800 = new YodaAgent(0, "T800", KNOWLEDGE,INFORMATION, OBSERVATIONS, DET_BEHAVIOUR, OMEGA, INFO_UPDATE);


    private static YodaVariableMapping computeOmega(RandomGenerator rg, YodaSystemState yodaSystemState, YodaAgent yodaAgent) {
        YodaVariableMapping agentPos = yodaAgent.getAgentInformation();
        YodaVariableMapping observations = yodaAgent.getAgentObservations();
        YodaScene scene = yodaSystemState.getScene();
        int x = agentPos.getValue(RobotState.POSX_VAR).integerValue().map(YodaValue.IntegerValue::value).orElse(0);
        int y = agentPos.getValue(RobotState.POSY_VAR).integerValue().map(YodaValue.IntegerValue::value).orElse(0);
        observations.setValue(RobotObservation.NORTH_VAR, new YodaValue.BooleanValue(scene.thereIsSomething(x, y+1)));
        observations.setValue(RobotObservation.SOUTH_VAR, new YodaValue.BooleanValue(scene.thereIsSomething(x, y-1)));
        observations.setValue(RobotObservation.EAST_VAR, new YodaValue.BooleanValue(scene.thereIsSomething(x+1, y)));
        observations.setValue(RobotObservation.WEST_VAR, new YodaValue.BooleanValue(scene.thereIsSomething(x-1, y)));
        observations.setValue(RobotObservation.GOAL_VAR, new YodaValue.BooleanValue(y == scene.getHeightInt()));
        return observations;
    }

    private static YodaVariableMapping computeInfoUpdate(RandomGenerator randomGenerator, YodaVariableMapping knowledge, YodaVariableMapping information) {
        int posX = information.getValue(RobotState.POSX_VAR).integerValue().map(YodaValue.IntegerValue::value).orElse(0);
        int posY = information.getValue(RobotState.POSY_VAR).integerValue().map(YodaValue.IntegerValue::value).orElse(0);
        int accX = knowledge.getValue(RobotState.ACCX_VAR).integerValue().map(YodaValue.IntegerValue::value).orElse(0);
        int accY = knowledge.getValue(RobotState.ACCY_VAR).integerValue().map(YodaValue.IntegerValue::value).orElse(0);
        information.setValue(RobotState.POSX_VAR, new YodaValue.IntegerValue(posX + accX));
        information.setValue(RobotState.POSY_VAR, new YodaValue.IntegerValue(posY + accY));
        return information;
    }


}
