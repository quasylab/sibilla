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

package it.unicam.quasylab.sibilla.core.models.lgio;

import org.apache.commons.math3.random.RandomGenerator;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class Agent {

    private static int agentCounter = 0;

    private final int id;

    private final AgentBehaviour behaviour;
    private final AgentAttributes attributes;


    private Agent(int id, AgentBehaviour behaviour, AgentAttributes attributes) {
        this.id = id;
        this.behaviour = behaviour;
        this.attributes = attributes;
    }

    public Agent(AgentBehaviour behaviour, AgentAttributes attributes) {
        this(agentCounter++, behaviour, attributes);
    }


    public Agent step(RandomGenerator rg, Function<Agent, ActionProbabilityFunction> probabilityFunction) {
        Optional<AgentStep> oStep = this.behaviour.select(rg, this.attributes, probabilityFunction.apply(this));
        if (oStep.isPresent()) {
            AgentStep step = oStep.get();
            return new Agent(id, step.getNextBehaviour(), step.getAttributeUpdateFunction().apply(rg,  this.attributes));
        } else {
            return this;
        }
    }

    @Override
    public String toString() {
        return behaviour.toString()+attributes.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Agent agent = (Agent) o;
        return id == agent.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
