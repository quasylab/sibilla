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
package it.unicam.quasylab.sibilla.core.models.dopm.states;

import it.unicam.quasylab.sibilla.core.models.ImmutableState;
import it.unicam.quasylab.sibilla.core.models.dopm.expressions.ExpressionContext;
import it.unicam.quasylab.sibilla.core.models.dopm.states.transitions.reactions.AgentDelta;
import it.unicam.quasylab.sibilla.core.models.dopm.states.transitions.reactions.InputReaction;
import it.unicam.quasylab.sibilla.core.models.dopm.states.transitions.Trigger;
import it.unicam.quasylab.sibilla.core.models.dopm.states.transitions.reactions.NoReaction;
import it.unicam.quasylab.sibilla.core.models.dopm.states.transitions.reactions.Reaction;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataOrientedPopulationState implements ImmutableState {
    private final Map<Agent, Long> agents;
    private final Long populationSize;

    public DataOrientedPopulationState(Map<Agent, Long> agents) {
        this.agents = agents;
        this.populationSize = agents.values().stream().reduce(0L, Long::sum);
    }

    public DataOrientedPopulationState() {
        this.agents = new HashMap<>();
        this.populationSize = 0L;
    }

    public DataOrientedPopulationState applyRule(Trigger t, RandomGenerator randomGenerator) {
        Map<Agent, Long> newOccupancies = new HashMap<>(this.agents);
        newOccupancies.put(t.getSender(), newOccupancies.get(t.getSender()) - 1);
        return new DataOrientedPopulationState(
                Stream.concat(
                    newOccupancies
                        .entrySet()
                        .stream()
                        .filter(entry -> entry.getValue() > 0)
                        .map(entry -> getAgentReaction(entry.getKey(), entry.getValue(), t))
                        .flatMap(reaction -> reaction.sampleDeltas(t.getSender(), this, randomGenerator)),
                    t.sampleDeltas(this, randomGenerator)
                )
                .collect(Collectors.groupingBy(AgentDelta::agent, Collectors.summingLong(AgentDelta::delta)))
        );
    }

    private Reaction getAgentReaction(Agent agent, Long numberOf, Trigger trigger) {
        return trigger
                .getRule()
                .getInputs()
                .stream()
                .filter(i ->
                    i.senderPredicate().test(new ExpressionContext(null, trigger.getSender().values(), this)) &&
                    i.predicate().test(agent.species(), new ExpressionContext(agent.values(), this))
                )
                .findFirst()
                .map(i -> (Reaction)new InputReaction(agent, numberOf, i))
                .orElse(new NoReaction(agent, numberOf));
    }

    public Map<Agent,Long> getAgents() {
        return agents;
    }

    public double fractionOf(BiPredicate<Integer, ExpressionContext> predicate) {
        return this.numberOf(predicate) / (double)populationSize;
    }

    public double numberOf(BiPredicate<Integer, ExpressionContext> predicate) {
        return agents.entrySet()
                .stream()
                .filter(e -> predicate.test(e.getKey().species(), new ExpressionContext(e.getKey().values(), this)))
                .map(Map.Entry::getValue)
                .reduce(0L, Long::sum);
    }
}
