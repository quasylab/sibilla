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
import it.unicam.quasylab.sibilla.core.models.State;
import it.unicam.quasylab.sibilla.core.models.dopm.DataOrientedPopulationModel;
import it.unicam.quasylab.sibilla.core.models.dopm.rules.transitions.InputTransition;
import it.unicam.quasylab.sibilla.core.past.ds.Tuple;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
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

    public DataOrientedPopulationState applyRule(RuleApplication ruleApplication, RandomGenerator randomGenerator) {
        Map<Agent, Long> newOccupancies = new HashMap<>(this.agents);

        Agent senderNew = ruleApplication.getRule().getOutput().getPost().apply(ruleApplication.getSender());
        newOccupancies.put(ruleApplication.getSender(), newOccupancies.get(ruleApplication.getSender()) - 1);
        newOccupancies.put(senderNew, newOccupancies.getOrDefault(senderNew, 0L) + 1);

        Set<Agent> usedAgents = new HashSet<>();

        for(InputTransition input : ruleApplication.getRule().getInputs()) {
            if(input.getSender_predicate().test(ruleApplication.getSender())) {
                for(Map.Entry<Agent, Long> e : this.agents.entrySet()) {
                    if(e.getValue() > 0 && !usedAgents.contains(e.getKey()) && input.getPredicate().test(e.getKey())) {
                        Long totalTransitioned = Stream.generate(randomGenerator::nextDouble)
                                .limit(e.getKey().equals(ruleApplication.getSender()) ? (e.getValue() - 1) : e.getValue())
                                .filter(result -> result <= input.getProbability().apply(this, e.getKey()))
                                .count();
                        if(totalTransitioned > 0) {
                            Agent newReceiver = input.getPost().apply(ruleApplication.getSender(), e.getKey());
                            newOccupancies.put(e.getKey(), newOccupancies.get(e.getKey()) - totalTransitioned);
                            newOccupancies.put(newReceiver, newOccupancies.getOrDefault(newReceiver,0L) + totalTransitioned);
                        }
                        usedAgents.add(e.getKey());
                    }
                }
            }
        }
        return new DataOrientedPopulationState(newOccupancies);
    }

    public Map<Agent,Long> getAgents() {
        return agents;
    }

    public double fractionOf(Predicate<Agent> predicate) {
        return this.numberOf(predicate) / (double)populationSize;
    }

    public double numberOf(Predicate<Agent> predicate) {
        return agents.keySet()
                .stream()
                .filter(predicate)
                .map(agents::get)
                .reduce(0L, Long::sum);
    }
}
