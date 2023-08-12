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

package it.unicam.quasylab.sibilla.core.runtime;


import it.unicam.quasylab.sibilla.core.models.lio.LIOModelDefinition;
import it.unicam.quasylab.sibilla.core.models.lio.LIOState;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.function.Function;

public class LIOModuleEngine extends ModuleEngine<LIOState> {

    private final LIOModelDefinition modelDefinition;
    private LIOModelModule.LIOModes mode;

    public LIOModuleEngine(LIOModelModule.LIOModes mode, LIOModelDefinition modelDefinition) {
        super(modelDefinition);
        this.modelDefinition = modelDefinition;
        this.mode = mode;
    }

    @Override
    protected Function<RandomGenerator, LIOState> getConfiguration(String name, double[] args) {
        switch (mode) {
            case INDIVIDUALS: return modelDefinition.getConfigurationOfIndividuals(name, args);
            case MASS: return modelDefinition.getConfigurationOfCountingElements(name, args);
            case FLUID: return modelDefinition.getConfigurationOfFluid(name, args);
        }
        return null;
    }

    @Override
    protected Function<RandomGenerator, LIOState> getDefaultConfiguration(double... args) {
        return getConfiguration(this.modelDefinition.getDefaultConfigurationName(), args);
    }

    /**
     * Sets the mode of this engine to the given value.
     *
     * @param mode the mode to set.
     */
    public void setMode(LIOModelModule.LIOModes mode) {
        this.mode = mode;
        this.clear();
    }


}
