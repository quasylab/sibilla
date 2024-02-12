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

package it.unicam.quasylab.sibilla.core.models.carma.targets.dopm;


import it.unicam.quasylab.sibilla.core.models.EvaluationEnvironment;
import it.unicam.quasylab.sibilla.core.models.Model;
import it.unicam.quasylab.sibilla.core.models.ModelDefinition;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.Rule;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.states.AgentState;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class DataOrientedPopulationModelDefinition implements ModelDefinition<AgentState> {

    private Map<String, Function<RandomGenerator, AgentState>> states;
    private Map<String, Measure<AgentState>> measures;
    private Map<String, Predicate<AgentState>> predicates;
    private List<Rule> rules;
    private DataOrientedPopulationModel model;

    public DataOrientedPopulationModelDefinition(Map<String, Function<RandomGenerator, AgentState>> states, Map<String, Measure<AgentState>> measures, Map<String, Predicate<AgentState>> predicates, List<Rule> rules) {
        this.states = states;
        this.measures = measures;
        this.predicates = predicates;
        this.rules = rules;
        this.model = new DataOrientedPopulationModel(this.measures, this.predicates, this.rules);
    }

    @Override
    public void reset() {

    }

    @Override
    public void reset(String name) {

    }

    @Override
    public SibillaValue getParameterValue(String name) {
        return SibillaValue.ERROR_VALUE;
    }

    @Override
    public EvaluationEnvironment getEnvironment() {
        return new EvaluationEnvironment();
    }

    @Override
    public int defaultConfigurationArity() {
        return 0;
    }

    @Override
    public int configurationArity(String name) {
        return 0;
    }

    @Override
    public String[] configurations() {
        if(this.states != null) {
            return states.keySet().toArray(new String[0]);
        } else{
            return new String[0];
        }
    }

    @Override
    public Function<RandomGenerator, AgentState> getConfiguration(String name, double... args) {
        if(this.states != null && states.containsKey(name)) {
            return states.get(name);
        } else {
            return (r) -> new AgentState();
        }
    }

    @Override
    public Function<RandomGenerator, AgentState> getDefaultConfiguration(double... args) {
        if(this.states != null) {
            return states.entrySet().iterator().next().getValue();
        } else {
            return (r) -> new AgentState();
        }
    }

    @Override
    public String getStateInfo(String name) {
        if(this.states != null && states.containsKey(name)) {
            return "State exists";
        } else {
            return "Non-existent state";
        }
    }

    @Override
    public Model<AgentState> createModel() {
        return this.model;
    }

    @Override
    public boolean isAnInitialConfiguration(String name) {
        return states != null && states.containsKey(name);
    }
}
