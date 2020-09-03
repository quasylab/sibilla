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
import quasylab.sibilla.core.models.Action;
import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.models.ModelDefinition;
import quasylab.sibilla.core.models.TimeStep;
import quasylab.sibilla.core.simulator.sampling.Measure;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

public abstract class AgentModel implements Model<SystemState> {

    private final AgentDefinition[] agents;
    private final OmegaFunction[] omega;
    private final SystemEnvironment environment;

    protected AgentModel(AgentDefinition[] agents, OmegaFunction[] omega, SystemEnvironment environment) {
        this.agents = agents;
        this.omega = omega;
        this.environment = environment;
    }


    @Override
    public TimeStep<SystemState> next(RandomGenerator r, double now, SystemState state) {
        AgentAction[] actions = new AgentAction[agents.length];
        IntStream.range(0,agents.length).forEach(i -> actions[i] = getAgentAction(r,now,i,state));
        return new TimeStep<SystemState>(now+1.0,environment.apply(r,state,actions));
    }

    private AgentAction getAgentAction(RandomGenerator r, double now, int i, SystemState state) {
        double[] observations = omega[i].getObservations(r,state.getLocal(i),state);
        return agents[i].getAction(r,now,state.getLocal(i),observations);
    }

    @Override
    public List<Action<SystemState>> actions(RandomGenerator r, double time, SystemState state) {
        return null;
    }

    @Override
    public ModelDefinition<SystemState> getModelDefinition() {
        return null;
    }

    @Override
    public int stateByteArraySize() {
        return 0;
    }

    @Override
    public byte[] serializeState(SystemState state) throws IOException {
        return new byte[0];
    }

    @Override
    public SystemState deserializeState(byte[] bytes) throws IOException {
        return null;
    }

    @Override
    public SystemState deserializeState(ByteArrayInputStream toDeserializeFrom) throws IOException {
        return null;
    }

    @Override
    public String[] measures() {
        return new String[0];
    }

    @Override
    public double measure(String m, SystemState state) {
        return 0;
    }

    @Override
    public Measure<SystemState> getMeasure(String m) {
        return null;
    }
}
