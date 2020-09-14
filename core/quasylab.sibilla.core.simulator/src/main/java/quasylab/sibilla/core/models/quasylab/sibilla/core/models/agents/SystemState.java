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

import quasylab.sibilla.core.models.State;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;

public class SystemState implements State {

    /**
     * Global variables describing the global system state.
     */
    private final double[] global;

    /**
     * Each agent is associated with a set of variables that are
     * used to describe in the environment (for instance, the agent
     * position).
     */
    private final double[][] agentInfo;

    /**
     * Local variables used by each agent to select its state.
     */
    private final double[][] local;

    public SystemState(double[] global, double[][] agentInfo, double[][] local) {
        this.global = global;
        this.agentInfo = agentInfo;
        this.local = local;
    }


    public double getGlobal(int i) {
        return global[i];
    }

    public double getLocal(int a, int i) {
        return local[a][i];
    }

    public double[] getLocal(int a) {
        return local[a];
    }

    public int size() {
        return global.length;
    }

    public int numberOfAgents() {
        return local.length;
    }

    public int size(int a) {
        return local[a].length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SystemState that = (SystemState) o;
        return Arrays.equals(global, that.global) &&
                Arrays.equals(local, that.local);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(global);
        result = 31 * result + Arrays.hashCode(local);
        return result;
    }

    public double getInfo(int a, int i) {
        return agentInfo[a][i];
    }

    /*@Override
    public void writeExternal(ObjectOutput out) throws IOException {

    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

    }*/
}
