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

import it.unicam.quasylab.sibilla.core.simulator.util.WeightedElement;
import it.unicam.quasylab.sibilla.core.simulator.util.WeightedStructure;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * This class identifies the state of agent behaviour.
 */
public class AgentBehaviour {

    private final String name;
    private final Map<AgentAction, AgentTransition> steps;

    /**
     * Creates a state behaviour with the given name
     *
     * @param name the name of the created behaviour.
     */
    public AgentBehaviour(String name) {
        this.name = name;
        this.steps = new HashMap<>();
    }

    /**
     * Add a step to this behaviour.
     *
     * @param action the action performed.
     * @param step the step performed with the action.
     */
    public void addStep(AgentAction action, AgentStep step) {
        addStep(null, action, step);
    }

    public void addStep(Predicate<AgentAttributes> guard, AgentAction action, AgentStep step) {
        this.steps.put(action, new AgentTransition(guard, action, step));
    }

    /**
     * Samples next action to execute.
     *
     * @param rg random generator used to sample random expressions.
     * @param probability function used to associate probability to actions.
     * @return the sampled step.
     */
    public Optional<AgentStep> select(RandomGenerator rg, AgentAttributes agentAttributes, ActionProbabilityFunction probability) {
        WeightedStructure<AgentStep> ws = this.steps.values()
                .stream()
                .filter(t -> t.isEnabled(agentAttributes))
                .map(t -> new WeightedElement<>(probability.get(t.getAction()), t.getStep())).collect(WeightedStructure.collector());
        double sampled = rg.nextDouble();
        if (sampled>ws.getTotalWeight()) {
            return Optional.empty();
        } else {
            return Optional.of(ws.select(sampled).getElement());
        }
    }

}
