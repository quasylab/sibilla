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
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

public class RobotState {

    public static YodaVariable POSX_VAR = new YodaVariable(0, "posX");
    public static YodaVariable POSY_VAR = new YodaVariable(1, "posY");
    public static YodaVariable ACCX_VAR = new YodaVariable(2, "accX");
    public static YodaVariable ACCY_VAR = new YodaVariable(3, "accY");

    public static SibillaValue POSX_VAL = SibillaValue.of(5);
    public static SibillaValue POSY_VAL = SibillaValue.of(0);
    public static SibillaValue ACCX_VAL = SibillaValue.of(0);
    public static SibillaValue ACCY_VAL = SibillaValue.of(0);


    public static Map<YodaVariable, SibillaValue> positionMap1;
    public static Map<YodaVariable, SibillaValue> positionMap2;
    public static Map<YodaVariable, SibillaValue> positionMap3;
    static {
        SibillaValue posx1 = SibillaValue.of((int) (Math.random()*10));
        SibillaValue posx2 = SibillaValue.of((int) (Math.random()*10));
        SibillaValue posx3 = SibillaValue.of((int) (Math.random()*10));
        positionMap1 = new HashMap<>();
        positionMap2 = new HashMap<>();
        positionMap3 = new HashMap<>();
        //positionMap.put(POSX_VAR, POSX_VAL); //To be used if a static position x is required
        positionMap1.put(POSX_VAR, posx1);   //To be used if a random position x is required
        positionMap1.put(POSY_VAR, POSY_VAL);
        positionMap2.put(POSX_VAR, posx2);   //To be used if a random position x is required
        positionMap2.put(POSY_VAR, POSY_VAL);
        positionMap3.put(POSX_VAR, posx3);   //To be used if a random position x is required
        positionMap3.put(POSY_VAR, POSY_VAL);

    }
    public static YodaVariableMapping AGENT_POS_MAP1 = new YodaVariableMapping(positionMap1);
    public static YodaVariableMapping AGENT_POS_MAP2 = new YodaVariableMapping(positionMap2);
    public static YodaVariableMapping AGENT_POS_MAP3 = new YodaVariableMapping(positionMap3);

    public static Map<YodaVariable, SibillaValue> accelerationMap;
    static {
        accelerationMap = new HashMap<>();
        accelerationMap.put(ACCX_VAR, ACCX_VAL);
        accelerationMap.put(ACCY_VAR, ACCY_VAL);
    }
    public static YodaVariableMapping AGENT_ACC_MAP = new YodaVariableMapping(accelerationMap);




    private final Map<YodaVariable, SibillaValue> map;

    public RobotState(Map<YodaVariable, SibillaValue> map) {
        this.map = map;
    }


}
