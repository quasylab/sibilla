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

package it.unicam.quasylab.sibilla.core.models.dopm;


import it.unicam.quasylab.sibilla.core.models.EvaluationEnvironment;
import it.unicam.quasylab.sibilla.core.models.Model;
import it.unicam.quasylab.sibilla.core.models.ModelDefinition;
import it.unicam.quasylab.sibilla.core.models.dopm.rules.Rule;
import it.unicam.quasylab.sibilla.core.models.dopm.states.DataOrientedPopulationState;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class DataOrientedPopulationModelDefinition implements ModelDefinition<DataOrientedPopulationState> {


    private Map<String, Function<RandomGenerator, DataOrientedPopulationState>> states;
    private Map<String, Measure<DataOrientedPopulationState>> measures;
    private Map<String, Predicate<DataOrientedPopulationState>> predicates;
    private Map<String, Rule> rules;

    private DataOrientedPopulationModel model;

    public DataOrientedPopulationModelDefinition(Map<String, Function<RandomGenerator, DataOrientedPopulationState>> states, Map<String, Measure<DataOrientedPopulationState>> measures, Map<String, Predicate<DataOrientedPopulationState>> predicates, Map<String, Rule> rules) {
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
    public Function<RandomGenerator, DataOrientedPopulationState> getConfiguration(String name, double... args) {
        if(this.states != null && states.containsKey(name)) {
            return states.get(name);
        } else {
            return (r) -> new DataOrientedPopulationState(new ArrayList<>());
        }
    }

    @Override
    public Function<RandomGenerator, DataOrientedPopulationState> getDefaultConfiguration(double... args) {
        if(this.states != null) {
            return states.entrySet().iterator().next().getValue();
        } else {
            return (r) -> new DataOrientedPopulationState(new ArrayList<>());
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
    public Model<DataOrientedPopulationState> createModel() {
        return this.model;
    }

    @Override
    public boolean isAnInitialConfiguration(String name) {
        return states != null && states.containsKey(name);
    }
}
