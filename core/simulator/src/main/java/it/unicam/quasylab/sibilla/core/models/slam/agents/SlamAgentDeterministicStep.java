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

package it.unicam.quasylab.sibilla.core.models.slam.agents;

import it.unicam.quasylab.sibilla.core.models.slam.data.AgentStore;
import it.unicam.quasylab.sibilla.core.models.slam.data.AgentVariable;
import it.unicam.quasylab.sibilla.core.util.datastructures.Pair;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.List;
import java.util.Optional;

/**
 * This functional interface is used to compute next configuration of an agent. This function takes
 * a random generator and an agent memory and returns an instance of AgentStepEffects.
 */
public class SlamAgentDeterministicStep implements SlamAgentStep {

    private final SlamAgentCommand stepCommand;

    private final SlamAgentState nextState;

    public SlamAgentDeterministicStep(SlamAgentCommand stepCommand, SlamAgentState nextState) {
        this.stepCommand = stepCommand;
        this.nextState = nextState;
    }

    @Override
    public Optional<SlamAgentStepEffect> apply(RandomGenerator rg, AgentStore m) {
        Pair<List<OutgoingMessage>, List<Pair<AgentVariable, SibillaValue>>> effects = this.stepCommand.execute(rg, m);
        return Optional.of(new SlamAgentStepEffect(nextState, effects.getKey(), m.set(effects.getValue())));
    }

}
