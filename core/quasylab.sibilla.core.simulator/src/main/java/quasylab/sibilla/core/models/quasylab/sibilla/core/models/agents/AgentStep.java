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

import java.util.Arrays;

public class AgentStep {

    private final double[] state;
    private final double[] observations;
    private final AgentAction action;

    public AgentStep(double[] state, double[] observations, AgentAction action) {
        this.state = state;
        this.observations = observations;
        this.action = action;
    }

    public double[] getState() {
        return state;
    }

    public double[] getObservations() {
        return observations;
    }

    public AgentAction getAction() {
        return action;
    }

    public boolean sameConditions(double[] state, double[] observations) {
        return Arrays.equals(state,this.state)&&Arrays.equals(observations,this.observations);
    }
}
