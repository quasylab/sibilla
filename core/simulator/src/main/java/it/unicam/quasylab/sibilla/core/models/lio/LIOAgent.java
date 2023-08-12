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

package it.unicam.quasylab.sibilla.core.models.lio;

import it.unicam.quasylab.sibilla.core.tools.ProbabilityVector;
import it.unicam.quasylab.sibilla.core.util.Pair;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collector;

/**
 * Describes the behaviour of a single agent in a network of interactive objects.
 */
public final class LIOAgent {

    private final int index;
    private final LIOAgentName name;

    private final List<Pair<LIOAgentAction, LIOAgent>> actions;

    /**
     * Create a new agent with the given name and given index.
     *
     * @param name agent name.
     * @param index agent index.
     */
    public LIOAgent(LIOAgentName name, int index) {
        this.index = index;
        this.name = name;
        this.actions = new LinkedList<>();
    }

    /**
     * Add a new action to the agent.
     * @param action action performed.
     * @param next next agent state.
     */
    public void addAction(LIOAgentAction action, LIOAgent next) {
        actions.add(new Pair<>(action,next));
    }

    /**
     * Compute the List of pairs used to compute agent next state starting from a function
     * associating each action with a probability value.
     *
     * @param distribution a function associating each action with a probability value.
     * @return List of pairs used to compute agent next state
     */
    public ProbabilityVector<LIOAgent> probabilityVector(LIOActionsProbability distribution) {
        ProbabilityVector<LIOAgent> result =  actions.stream()
                .map(p -> new Pair<>(distribution.probabilityOf(p.getKey()),p.getValue()))
                .filter(p -> p.getKey() > 0.0 )
                .collect(Collector.of(ProbabilityVector::new, (v,p) -> v.add(p.getValue(),
                        p.getKey()),
                        (v1, v2) -> {v1.add(v2); return v1;}));
        return result.complete(this);
    }

    /**
     * Return agent index.
     *
     * @return agent index.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Return agent name.
     *
     * @return agent name.
     */
    public LIOAgentName getName() {
        return name;
    }

    @Override
    public String toString() {
        return name+"("+index+")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LIOAgent agent = (LIOAgent) o;
        return index == agent.index;
    }

    @Override
    public int hashCode() {
        return index;
    }


}
