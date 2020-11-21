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

package it.unicam.quasylab.sibilla.examples.agents;

import org.apache.commons.math3.random.RandomGenerator;
import it.unicam.quasylab.sibilla.core.models.quasylab.sibilla.core.models.agents.AgentAction;
import it.unicam.quasylab.sibilla.core.models.quasylab.sibilla.core.models.agents.SetVariable;
import it.unicam.quasylab.sibilla.core.models.quasylab.sibilla.core.models.agents.VariableMapping;


public class ChangeDirectionAction implements AgentAction {

    public static AgentAction UP = new ChangeDirectionAction("UP",0,+1);
    public static AgentAction DOWN = new ChangeDirectionAction("DOWN",0,-1);
    public static AgentAction RIGHT = new ChangeDirectionAction("RIGHT",+1,0);
    public static AgentAction LEFT = new ChangeDirectionAction("LEFT",-1,0);
    public static AgentAction STAND = new ChangeDirectionAction("STAND",0,0);


    private final String name;
    private final double dx;
    private final double dy;

    public ChangeDirectionAction(String name, double dx, double dy) {
        this.name = name;
        this.dx = dx;
        this.dy = dy;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public VariableMapping performAction(RandomGenerator rg, VariableMapping currentState) {
        return currentState.set(new SetVariable("dx",dx), new SetVariable("dy",dy));
    }
}
