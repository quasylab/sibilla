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

import it.unicam.quasylab.sibilla.core.models.yoda.YodaType;
import it.unicam.quasylab.sibilla.core.models.yoda.YodaValue;
import it.unicam.quasylab.sibilla.core.models.yoda.YodaVariable;
import it.unicam.quasylab.sibilla.core.models.yoda.YodaVariableMapping;

import java.util.HashMap;
import java.util.Map;

public class RobotObservation implements YodaVariableMapping {

    public static YodaVariable NORTH_VAR = new YodaVariable(0, "north", YodaType.BOOLEAN_TYPE);
    public static YodaVariable SOUTH_VAR = new YodaVariable(1, "south", YodaType.BOOLEAN_TYPE);
    public static YodaVariable EAST_VAR = new YodaVariable(2, "east", YodaType.BOOLEAN_TYPE);
    public static YodaVariable WEST_VAR = new YodaVariable(3, "west", YodaType.BOOLEAN_TYPE);
    public static YodaVariable GOAL_VAR = new YodaVariable(4, "goal", YodaType.BOOLEAN_TYPE);

    public static YodaValue NORTH_VAL = new YodaValue.BooleanValue(false);
    public static YodaValue SOUTH_VAL = new YodaValue.BooleanValue(false);
    public static YodaValue EAST_VAL = new YodaValue.BooleanValue(false);
    public static YodaValue WEST_VAL = new YodaValue.BooleanValue(false);
    public static YodaValue GOAL_VAL = new YodaValue.BooleanValue(false);

    public static Map<YodaVariable, YodaValue> observationMap;
    static {
        observationMap = new HashMap<>();
        observationMap.put(NORTH_VAR, NORTH_VAL);
        observationMap.put(SOUTH_VAR, SOUTH_VAL);
        observationMap.put(EAST_VAR, EAST_VAL);
        observationMap.put(WEST_VAR, WEST_VAL);
        observationMap.put(GOAL_VAR, GOAL_VAL);
    }
    public static YodaVariableMapping AGENT_OBS_MAP = new RobotObservation(observationMap);

    private Map<YodaVariable, YodaValue> map;
    public RobotObservation(Map<YodaVariable, YodaValue> map) {
        this.map = map;
    }

    @Override
    public YodaValue getValue(YodaVariable variable) {
        return map.get(variable);
    }

    @Override
    public void setValue(YodaVariable variable, YodaValue value) {
        this.map.put(variable, value);
    }

    @Override
    public void addElement(YodaVariable variable, YodaValue value) {
        this.map.put(variable, value);
    }

    @Override
    public YodaVariableMapping copy() {
        Map<YodaVariable, YodaValue> newMap = new HashMap<>(map);
        return new RobotObservation(newMap);
    }

    @Override
    public int size() {
        return map.size();
    }
}
