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
import it.unicam.quasylab.sibilla.core.models.ParametricDataSet;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * This class represents a definition of a LIO Model.
 */
public class LIOModelDefinition extends AbstractModelDefinition<LIOState> {

    private final Function<EvaluationEnvironment, AgentsDefinition> definitionGenerator;

    private final BiFunction<EvaluationEnvironment, AgentsDefinition, LIOModel> modelGenerator;

    private final BiFunction<EvaluationEnvironment, AgentsDefinition, ParametricDataSet<Function<RandomGenerator, LIOState>>>  stateGenerator;
    private LIOModel cachedModel;
    private ParametricDataSet<Function<RandomGenerator, LIOState>> cachedStates;
    private AgentsDefinition agentsDefinition;

    public LIOModelDefinition(EvaluationEnvironment environment, Function<EvaluationEnvironment, AgentsDefinition> definitionGenerator, BiFunction<EvaluationEnvironment, AgentsDefinition, LIOModel> modelGenerator, BiFunction<EvaluationEnvironment, AgentsDefinition, ParametricDataSet<Function<RandomGenerator, LIOState>>> stateGenerator) {
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

    @Override
    public ParametricDataSet<Function<RandomGenerator, LIOState>> getStates() {
        if (cachedStates==null) {
            cachedStates = stateGenerator.apply(getEnvironment(), getAgentsDefinitions());
        }
        return cachedStates;
    }

    private AgentsDefinition getAgentsDefinitions() {
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
}
