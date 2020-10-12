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

package quasylab.sibilla.core.models.quasylab.sibilla.core.models.agents;

import org.apache.commons.math3.random.RandomGenerator;

public abstract class AgentActionWrapper implements AgentAction {

    private final AgentAction action;
    private final String name;

    public AgentActionWrapper(String name, AgentAction action) {
        this.action = action;
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name+":"+this.action.getName();
    }

    @Override
    public VariableMapping performAction(RandomGenerator rg, VariableMapping currentState) {
        return extendResult(rg,currentState,this.action.performAction(rg,currentState));
    }

    protected abstract VariableMapping extendResult(RandomGenerator rg, VariableMapping currentState, VariableMapping innerActionResult);
}
