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

import java.util.function.Function;

/**
 * This class represents a definition of a LIO Model.
 */
public class LIOModelDefinition<S extends LIOState<S>> extends AbstractModelDefinition<S> {

    private final Function<EvaluationEnvironment,LIOModel<S>> definitionGenerator;
    private final Function<EvaluationEnvironment, ParametricDataSet<Function<RandomGenerator, S>>>  stateGenerator;
    private LIOModel<S> cachedModel;
    private ParametricDataSet<Function<RandomGenerator, S>> cachedStates;

    public LIOModelDefinition(EvaluationEnvironment environment,
                              Function<EvaluationEnvironment, LIOModel<S>> definitionGenerator,
                              Function<EvaluationEnvironment, ParametricDataSet<Function<RandomGenerator, S>>> stateGenerator) {
        super(environment);
        this.definitionGenerator = definitionGenerator;
        this.stateGenerator = stateGenerator;
    }


    @Override
    protected void clearCache() {
        this.cachedModel = null;
        this.cachedStates = null;
    }

    @Override
    public ParametricDataSet<Function<RandomGenerator, S>> getStates() {
        if (cachedStates==null) {
            cachedStates = stateGenerator.apply(getEnvironment());
        }
        return cachedStates;
    }


    @Override
    public LIOModel<S> createModel() {
        if (cachedModel==null) {
            cachedModel = definitionGenerator.apply(getEnvironment());
        }
        return cachedModel;
    }
}
