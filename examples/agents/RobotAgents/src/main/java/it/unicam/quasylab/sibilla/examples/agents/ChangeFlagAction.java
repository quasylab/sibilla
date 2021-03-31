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
import it.unicam.quasylab.sibilla.core.models.quasylab.sibilla.core.models.agents.AgentActionWrapper;
import it.unicam.quasylab.sibilla.core.models.quasylab.sibilla.core.models.agents.SetVariable;
import it.unicam.quasylab.sibilla.core.models.quasylab.sibilla.core.models.agents.VariableMapping;

public class ChangeFlagAction extends AgentActionWrapper {

    public final static ChangeFlagAction LEFT_AND_CHANGE_FLAG_ACTION = new ChangeFlagAction("goLeft",false,ChangeDirectionAction.LEFT);
    public final static ChangeFlagAction RIGHT_AND_CHANGE_FLAG_ACTION = new ChangeFlagAction("goRight",true,ChangeDirectionAction.RIGHT);


    private final boolean goRight;

    public ChangeFlagAction(String name, boolean goRight, AgentAction action) {
        super(name, action);
        this.goRight = goRight;
    }

    @Override
    protected VariableMapping extendResult(RandomGenerator rg, VariableMapping currentState, VariableMapping innerActionResult) {
        return innerActionResult.set(new SetVariable(RoboticScenarioDefinition.DIR_FLAG_VARIABLE,(goRight?1.0:0.0)));
    }
}
