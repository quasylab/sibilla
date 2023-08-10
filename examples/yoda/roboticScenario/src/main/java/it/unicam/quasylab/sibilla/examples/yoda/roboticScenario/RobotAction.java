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
import it.unicam.quasylab.sibilla.core.models.yoda.YodaValue;
import it.unicam.quasylab.sibilla.core.models.yoda.YodaVariableMapping;
import org.apache.commons.math3.random.RandomGenerator;

public class RobotAction {

    /*

    public static YodaAction GO_NORTH = YodaAction.actionOf("GoToNorth", RobotAction::goToNorth);
    public static YodaAction GO_SOUTH = YodaAction.actionOf("GoToSouth", RobotAction::goToSouth);
    public static YodaAction GO_EAST = YodaAction.actionOf("GoToEast", RobotAction::goToEast);
    public static YodaAction GO_WEST = YodaAction.actionOf("GoToWest", RobotAction::goToWest);
    public static YodaAction STAND_STILL = YodaAction.actionOf("StandStill", RobotAction::standStill);


    public static YodaVariableMapping goToNorth(RandomGenerator rg, YodaVariableMapping currentState) {
        currentState.setValue(RobotState.ACCX_VAR, YodaValue.integerOf(0));
        currentState.setValue(RobotState.ACCY_VAR, YodaValue.integerOf(1));
        return currentState;
    }
    public static YodaVariableMapping goToSouth(RandomGenerator rg, YodaVariableMapping currentState) {
        currentState.setValue(RobotState.ACCX_VAR, YodaValue.integerOf(0));
        currentState.setValue(RobotState.ACCY_VAR, YodaValue.integerOf(-1));
        return currentState;
    }

    public static YodaVariableMapping goToEast(RandomGenerator rg, YodaVariableMapping currentState) {
        currentState.setValue(RobotState.ACCX_VAR, YodaValue.integerOf(1));
        currentState.setValue(RobotState.ACCY_VAR, YodaValue.integerOf(0));
        return currentState;
    }

    public static YodaVariableMapping goToWest(RandomGenerator rg, YodaVariableMapping currentState) {
        currentState.setValue(RobotState.ACCX_VAR, YodaValue.integerOf(-1));
        currentState.setValue(RobotState.ACCY_VAR, YodaValue.integerOf(0));
        return currentState;
    }

    public static YodaVariableMapping standStill(RandomGenerator rg, YodaVariableMapping currentState) {
        currentState.setValue(RobotState.ACCX_VAR, YodaValue.integerOf(0));
        currentState.setValue(RobotState.ACCY_VAR, YodaValue.integerOf(0));
        return currentState;
    }


     */
}
