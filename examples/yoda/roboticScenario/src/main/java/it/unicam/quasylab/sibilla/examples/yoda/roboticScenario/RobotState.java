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
import java.util.Random;

public class RobotState implements YodaVariableMapping {

    public static YodaVariable POSX_VAR = new YodaVariable(0, "posX", YodaType.INTEGER_TYPE);
    public static YodaVariable POSY_VAR = new YodaVariable(1, "posY", YodaType.INTEGER_TYPE);
    public static YodaVariable ACCX_VAR = new YodaVariable(2, "accX", YodaType.INTEGER_TYPE);
    public static YodaVariable ACCY_VAR = new YodaVariable(3, "accY", YodaType.INTEGER_TYPE);

    public static YodaValue POSX_VAL = YodaValue.integerOf(5);
    public static YodaValue POSY_VAL = YodaValue.integerOf(0);
    public static YodaValue ACCX_VAL = YodaValue.integerOf(0);
    public static YodaValue ACCY_VAL = YodaValue.integerOf(0);


    public static Map<YodaVariable, YodaValue> positionMap;
    static {
        //YodaValue posx = YodaValue.integerOf((int) (Math.random()*10));
        positionMap = new HashMap<>();
        positionMap.put(POSX_VAR, POSX_VAL); //To be used if a static position x is required
        //positionMap.put(POSX_VAR, posx);   //To be used if a random position x is required
        positionMap.put(POSY_VAR, POSY_VAL);
    }
    public static YodaVariableMapping AGENT_POS_MAP = new RobotState(positionMap);

    public static Map<YodaVariable, YodaValue> accelerationMap;
    static {
        accelerationMap = new HashMap<>();
        accelerationMap.put(ACCX_VAR, ACCX_VAL);
        accelerationMap.put(ACCY_VAR, ACCY_VAL);
    }
    public static YodaVariableMapping AGENT_ACC_MAP = new RobotState(accelerationMap);




    private Map<YodaVariable, YodaValue> map;

    public RobotState(Map<YodaVariable, YodaValue> map) {
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
        return new RobotState(newMap);
    }


    @Override
    public int size() {
        return map.size();
    }
}
