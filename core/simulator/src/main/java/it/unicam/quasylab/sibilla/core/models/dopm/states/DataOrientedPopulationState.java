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
import it.unicam.quasylab.sibilla.core.models.dopm.DataOrientedPopulationModel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;


public class DataOrientedPopulationState implements ImmutableState {

    private final List<Agent> agents;


    public DataOrientedPopulationState(List<Agent> agents) {
        this.agents = new ArrayList<>(agents);
    }

    public DataOrientedPopulationState(Agent agent) {
        this.agents = new ArrayList<>();
        this.agents.add(agent);
    }


    public List<Agent> getAgents() {
        return agents;
    }

    public double fractionOf(Predicate<Agent> predicate) {
        return agents.stream().filter(predicate).count() / (double)agents.size();
    }

    public double numberOf(Predicate<Agent> predicate) {
        return agents.stream().filter(predicate).count();
    }

    public DataOrientedPopulationState addAgent(Agent a) {
        List<Agent> cagents = new ArrayList<>(this.agents);
        cagents.add(a);
        return new DataOrientedPopulationState(cagents);
    }

}
