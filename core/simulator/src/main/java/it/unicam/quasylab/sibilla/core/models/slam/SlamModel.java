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

package it.unicam.quasylab.sibilla.core.models.slam;

import it.unicam.quasylab.sibilla.core.models.AbstractModel;
import it.unicam.quasylab.sibilla.core.models.SimulatorCursor;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents a SLAM model.
 */
public class SlamModel extends AbstractModel<SlamState> {

    private final AgentDefinition agentDefinitions;
    private final MessageRepository messageRepository;

    /**
     * Creates a new model with the given definitions, messages, measures and predicates.
     *
     * @param agentDefinitions prototypes of agents defined in the model;
     * @param messageRepository set of messages that agents can exchange;
     * @param measuresTable measures defined in the model;
     * @param predicatesTable predicates defined in the model.
     */
    public SlamModel(AgentDefinition agentDefinitions, MessageRepository messageRepository, Map<String, Measure<? super SlamState>> measuresTable, Map<String, Predicate<? super SlamState>> predicatesTable) {
        super(measuresTable, predicatesTable);
        this.agentDefinitions = agentDefinitions;
        this.messageRepository = messageRepository;
    }

    @Override
    public SimulatorCursor<SlamState> createSimulationCursor(RandomGenerator r, Function<RandomGenerator, SlamState> initialStateBuilder) {
        return new SlamSimulationCursor(r, initialStateBuilder);
    }

    @Override
    public int stateByteArraySize() {
        return 0;
    }

    @Override
    public byte[] byteOf(SlamState state) throws IOException {
        return new byte[0];
    }

    @Override
    public SlamState fromByte(byte[] bytes) throws IOException {
        return null;
    }

    /**
     * Returns the definitions of agent prototypes in this model.
     * @return the definitions of agent prototypes in this model.
     */
    public AgentDefinition getAgents() {
        return this.agentDefinitions;
    }

    /**
     * Returns the repository containing the set of messages that agents can exchange.
     *
     * @return the repository containing the set of messages that agents can exchange.
     */
    public MessageRepository getMessages() {
        return this.messageRepository;
    }
}
