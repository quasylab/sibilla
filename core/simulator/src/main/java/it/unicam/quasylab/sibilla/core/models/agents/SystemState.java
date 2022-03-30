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

package it.unicam.quasylab.sibilla.core.models.agents;

import it.unicam.quasylab.sibilla.core.models.ImmutableState;
import it.unicam.quasylab.sibilla.core.models.State;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.Objects;

public class SystemState<W extends World> implements ImmutableState {

    /**
     * Global variables describing the global system state.
     */
    private final W world;

    /**
     * Each agent is associated with a set of variables that are
     * used to describe in the environment (for instance, the agent
     * position).
     */
    private final VariableMapping[]  agentInfo;

    /**
     * Local variables used by each agent to select its state.
     */
    private final VariableMapping[] local;

    public SystemState(W world, VariableMapping[] agentInfo, VariableMapping[] local) {
        this.world = world;
        this.agentInfo = agentInfo;
        this.local = local;
    }


    public W getWorld() {
        return world;
    }

    public double getLocal(int a, String variable) {
        return local[a].get(variable);
    }

    public VariableMapping getLocal(int a) {
        return local[a];
    }

    public int numberOfAgents() {
        return local.length;
    }

    public int size(int a) {
        return local[a].size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SystemState that = (SystemState) o;
        return Objects.equals(world, that.world) &&
                Arrays.equals(local, that.local);
    }

    @Override
    public int hashCode() {
        int result = world.hashCode();
        result = 31 * result + Arrays.hashCode(local);
        return result;
    }

    public double getInfo(int a, String variable) {
        return agentInfo[a].get(variable);
    }

    public VariableMapping getInfo(int a) {
        return agentInfo[a];
    }

    @Override
    public String toString() {
        return "SystemState{" +
                "world=" + world +
                ", agentInfo=" + Arrays.toString(agentInfo) +
                ", local=" + Arrays.toString(local) +
                '}';
    }
}
