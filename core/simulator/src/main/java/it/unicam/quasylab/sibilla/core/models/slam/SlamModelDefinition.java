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

package it.unicam.quasylab.sibilla.core.models.slam;

import it.unicam.quasylab.sibilla.core.models.AbstractModelDefinition;
import it.unicam.quasylab.sibilla.core.models.EvaluationEnvironment;
import it.unicam.quasylab.sibilla.core.models.Model;
import it.unicam.quasylab.sibilla.core.models.ParametricDataSet;
import it.unicam.quasylab.sibilla.core.models.slam.agents.SlamAgentDefinitions;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class SlamModelDefinition extends AbstractModelDefinition<SlamState> {


    private final Function<EvaluationEnvironment, SlamAgentDefinitions> agentsDefinitionBuilder;

    private final MessageRepository messageRepository;
    private final BiFunction<EvaluationEnvironment, SlamAgentDefinitions, Map<String, Measure<? super SlamState>>> measureTableBuilder;

    private final BiFunction<EvaluationEnvironment, SlamAgentDefinitions, Map<String, Predicate<? super SlamState>>> predicateTableBuilder;

    private final BiFunction<EvaluationEnvironment, SlamAgentDefinitions, ParametricDataSet<Function<RandomGenerator, SlamState>>> stateGenerator;

    private SlamAgentDefinitions agentsDefinition;
    private Map<String, Measure<? super SlamState>> measureTable;
    private Map<String, Predicate<? super SlamState>> predicateTable;
    private ParametricDataSet<Function<RandomGenerator, SlamState>> states;

    public SlamModelDefinition(Function<EvaluationEnvironment, SlamAgentDefinitions> agentsDefinitionBuilder, MessageRepository messageRepository, BiFunction<EvaluationEnvironment, SlamAgentDefinitions, Map<String, Measure<? super SlamState>>> measureTableBuilder, BiFunction<EvaluationEnvironment, SlamAgentDefinitions, Map<String, Predicate<? super SlamState>>> predicateTableBuilder, BiFunction<EvaluationEnvironment, SlamAgentDefinitions, ParametricDataSet<Function<RandomGenerator, SlamState>>> stateGenerator) {
        this.agentsDefinitionBuilder = agentsDefinitionBuilder;
        this.messageRepository = messageRepository;
        this.measureTableBuilder = measureTableBuilder;
        this.predicateTableBuilder = predicateTableBuilder;
        this.stateGenerator = stateGenerator;
    }

    @Override
    protected void clearCache() {
        this.agentsDefinition = null;
        this.measureTable = null;
        this.predicateTable = null;
        this.states = null;
    }

    public ParametricDataSet<Function<RandomGenerator, SlamState>> getStates() {
        if (states == null) {
            states = stateGenerator.apply(getEnvironment(), getAgentsDefinition());
        }
        return states;
    }

    @Override
    public Model<SlamState> createModel() {
        return new SlamModel(getAgentsDefinition(), getMessageRepository(), getMeasuresTable(), getPredicatesTable());
    }

    private Map<String, Predicate<? super SlamState>> getPredicatesTable() {
        if (predicateTable == null) {
            predicateTable = predicateTableBuilder.apply(getEnvironment(), getAgentsDefinition());
        }
        return predicateTable;
    }

    private Map<String, Measure<? super SlamState>> getMeasuresTable() {
        if (measureTable == null) {
            measureTable = measureTableBuilder.apply(getEnvironment(), getAgentsDefinition());
        }
        return measureTable;
    }

    private MessageRepository getMessageRepository() {
        return messageRepository;
    }

    private SlamAgentDefinitions getAgentsDefinition() {
        if (this.agentsDefinition == null) {
            this.agentsDefinition = this.agentsDefinitionBuilder.apply(getEnvironment());
        }
        return agentsDefinition;
    }

    @Override
    public boolean isAnInitialConfiguration(String name) {
        return getStates().isDefined(name);
    }

    @Override
    public Function<RandomGenerator, SlamState> getConfiguration(String name, double[] args) {
        return null;
    }


    @Override
    public int defaultConfigurationArity() {
        return getStates().arity();
    }

    @Override
    public int configurationArity(String name) {
        return getStates().arity(name);
    }

    @Override
    public String[] configurations() {
        return getStates().states();
    }

    @Override
    public Function<RandomGenerator,SlamState> getDefaultConfiguration(double... args) {
        return getStates().get(args);
    }

    @Override
    public String getStateInfo(String name) {
        return getStates().getInfo(name);
    }
}
