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

import it.unicam.quasylab.sibilla.core.models.AbstractModelDefinition;
import it.unicam.quasylab.sibilla.core.models.EvaluationEnvironment;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * This class represents a definition of a LIO Model.
 */
public class LIOModelDefinition extends AbstractModelDefinition<LIOState> {

    private final Function<EvaluationEnvironment, LIOAgentDefinitions> definitionGenerator;

    private final BiFunction<EvaluationEnvironment, LIOAgentDefinitions, LIOModel> modelGenerator;

    private final BiFunction<EvaluationEnvironment, LIOAgentDefinitions, LIOConfigurationsSupplier>  stateGenerator;
    private LIOModel cachedModel;
    private LIOConfigurationsSupplier cachedStates;
    private LIOAgentDefinitions agentsDefinition;

    public LIOModelDefinition(EvaluationEnvironment environment, Function<EvaluationEnvironment, LIOAgentDefinitions> definitionGenerator, BiFunction<EvaluationEnvironment, LIOAgentDefinitions, LIOModel> modelGenerator, BiFunction<EvaluationEnvironment, LIOAgentDefinitions, LIOConfigurationsSupplier> stateGenerator) {
        super(environment);
        this.definitionGenerator = definitionGenerator;
        this.modelGenerator = modelGenerator;
        this.stateGenerator = stateGenerator;
    }


    @Override
    protected void clearCache() {
        this.cachedModel = null;
        this.cachedStates = null;
        this.agentsDefinition = null;
    }

    public LIOConfigurationsSupplier getStates() {
        if (cachedStates==null) {
            cachedStates = stateGenerator.apply(getEnvironment(), getAgentsDefinitions());
        }
        return cachedStates;
    }

    private LIOAgentDefinitions getAgentsDefinitions() {
        if (this.agentsDefinition == null) {
            this.agentsDefinition = definitionGenerator.apply(getEnvironment());
        }
        return this.agentsDefinition;
    }


    @Override
    public LIOModel createModel() {
        if (cachedModel==null) {
            cachedModel = modelGenerator.apply(getEnvironment(), getAgentsDefinitions());
        }
        return cachedModel;
    }

    @Override
    public boolean isAnInitialConfiguration(String name) {
        return getStates().isDefined(name);
    }

    @Override
    public Function<RandomGenerator, LIOState> getConfiguration(String name, double[] args) {
        return getMassConfiguration(name, args);
    }

    private Function<RandomGenerator, LIOState> getMassConfiguration(String name, double[] args) {
        LIOState state = getStates().getConfigurationOfCountingElements(name, args);
        return rg -> state;
    }


    public boolean isAState(String name) {
        return getStates().isDefined(name);
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
    public Function<RandomGenerator,LIOState> getDefaultConfiguration(double... args) {
        return getStates().getDefault(args);
    }

    @Override
    public String getStateInfo(String name) {
        return getStates().getInfo(name);
    }

    public Function<RandomGenerator, LIOState> getConfigurationOfIndividuals(String name, double[] args) {
        LIOState state = getStates().getConfigurationOfIndividuals(name, args);
        return rg -> state;
    }

    public Function<RandomGenerator, LIOState> getConfigurationOfCountingElements(String name, double[] args) {
        LIOState state = getStates().getConfigurationOfCountingElements(name, args);
        return rg -> state;
    }

    public Function<RandomGenerator, LIOState> getConfigurationOfFluid(String name, double[] args) {
        LIOMeanFieldState state = new LIOMeanFieldState(getStates().getConfigurationOfIndividuals(name, args));
        return rg -> state;
    }

    public String getDefaultConfigurationName() {
        return getStates().getDefaultConfigurationName();
    }
}
