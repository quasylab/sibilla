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

import it.unicam.quasylab.sibilla.core.models.InteractiveModel;
import it.unicam.quasylab.sibilla.core.models.Action;
import it.unicam.quasylab.sibilla.core.models.TimeStep;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

public class AgentModel<W extends World> implements InteractiveModel<SystemState<W>> {

    private final AgentBehaviour[] agents;
    private final OmegaFunction[] omega;
    private final SystemEnvironment<W> environment;

    public AgentModel(AgentBehaviour[] agents, OmegaFunction[] omega, SystemEnvironment<W> environment) {
        this.agents = agents;
        this.omega = omega;
        this.environment = environment;
    }


    @Override
    public TimeStep<SystemState<W>> next(RandomGenerator r, double now, SystemState<W> state) {
        AgentAction[] actions = new AgentAction[agents.length];
        IntStream.range(0,agents.length).forEach(i -> actions[i] = getAgentAction(r,now,i,state));
        return new TimeStep<SystemState<W>>(1.0,environment.apply(r,state,actions));
    }

    private AgentAction getAgentAction(RandomGenerator r, double now, int i, SystemState<W> state) {
        VariableMapping observations = omega[i].getObservations(r,state.getLocal(i),state);
        return agents[i].step(r,now,state.getLocal(i),observations);
    }

    @Override
    public List<Action<SystemState<W>>> actions(RandomGenerator r, double time, SystemState<W> state) {
        return null;
    }

    @Override
    public int stateByteArraySize() {
        return 0;
    }

    @Override
    public byte[] serializeState(SystemState<W> state) throws IOException {
        return new byte[0];
    }

    @Override
    public void serializeState(ByteArrayOutputStream toSerializeInto, SystemState<W> state) throws IOException {

    }

    @Override
    public SystemState<W> deserializeState(byte[] bytes) throws IOException {
        return null;
    }

    @Override
    public SystemState<W> deserializeState(ByteArrayInputStream toDeserializeFrom) throws IOException {
        return null;
    }

    @Override
    public String[] measures() {
        return new String[0];
    }

    @Override
    public double measure(String m, SystemState<W> state) {
        return 0;
    }

    @Override
    public Measure<SystemState<W>> getMeasure(String m) {
        return null;
    }
}
