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

package it.unicam.quasylab.sibilla.core.models.markov;

import it.unicam.quasylab.sibilla.core.models.*;
import it.unicam.quasylab.sibilla.core.models.util.MappingState;
import it.unicam.quasylab.sibilla.core.models.util.VariableTable;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class MarkovChainDefinition<M extends MarkovChainModel> extends AbstractModelDefinition<MappingState> {

    private final MarkovChainBuilder<M> modelBuilder;
    private final BiFunction<EvaluationEnvironment, VariableTable, StateSet<MappingState>> stateSetBuilder;
    private final Function<EvaluationEnvironment, VariableTable> variablesBuilder;
    private final BiFunction<EvaluationEnvironment, VariableTable, List<MappingStateUpdate>> rulesBuilder;
    private final BiFunction<EvaluationEnvironment, VariableTable, Map<String, Measure<MappingState>>> measureBuilder;
    private Model<MappingState> model;
    private Map<String, Measure<MappingState>> measures;
    private StateSet<MappingState> states;
    private VariableTable variables;
    private List<MappingStateUpdate> rules;

    public MarkovChainDefinition(
            EvaluationEnvironment environment,
            BiFunction<EvaluationEnvironment, VariableTable, StateSet<MappingState>> stateSetBuilder,
            MarkovChainBuilder<M> modelBuilder,
            Function<EvaluationEnvironment, VariableTable> variablesBuilder,
            BiFunction<EvaluationEnvironment, VariableTable, List<MappingStateUpdate>> rulesBuilder,
            BiFunction<EvaluationEnvironment, VariableTable, Map<String, Measure<MappingState>>> measureBuilder) {
        super(environment);
        this.modelBuilder = modelBuilder;
        this.stateSetBuilder = stateSetBuilder;
        this.variablesBuilder = variablesBuilder;
        this.measureBuilder = measureBuilder;
        this.rulesBuilder = rulesBuilder;
    }


    @Override
    protected void clearCache() {
        this.model = null;
        this.measures = null;
        this.states = null;
        this.variables = null;
        this.rules = null;
    }

    @Override
    public StateSet<MappingState> getStates() {
        if (states == null) {
            states = stateSetBuilder.apply(getEnvironment(),getVariables());
        }
        return states;
    }


    @Override
    public Model<MappingState> createModel() {
        if (model == null) {
            model = modelBuilder.apply(getVariables(),getRules(),getMeasures());
        }
        return model;
    }

    private List<MappingStateUpdate> getRules() {
        if (rules == null) {
            rules = rulesBuilder.apply(getEnvironment(), getVariables());
        }
        return rules;
    }

    private VariableTable getVariables() {
        if (variables == null) {
            variables = variablesBuilder.apply(getEnvironment());
        }
        return variables;
    }

    private Map<String, Measure<MappingState>> getMeasures() {
        if (measures == null) {
            measures = measureBuilder.apply(getEnvironment(),getVariables());
        }
        return measures;
    }



}
