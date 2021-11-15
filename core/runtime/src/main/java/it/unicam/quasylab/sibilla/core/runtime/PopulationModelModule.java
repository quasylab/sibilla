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

import it.unicam.quasylab.sibilla.core.models.StateSet;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationModel;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationModelDefinition;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.simulator.SimulationEnvironment;
import it.unicam.quasylab.sibilla.core.simulator.SimulationMonitor;
import it.unicam.quasylab.sibilla.core.simulator.sampling.SamplingFunction;
import it.unicam.quasylab.sibilla.core.simulator.sampling.SimulationTimeSeries;
import it.unicam.quasylab.sibilla.langs.pm.ModelBuildingError;
import it.unicam.quasylab.sibilla.langs.pm.ModelGenerationException;
import it.unicam.quasylab.sibilla.langs.pm.PopulationModelGenerator;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;


public class PopulationModelModule extends AbstractSibillaModule<PopulationState> {

    public final static String MODULE_NAME = "population";

    @Override
    public String getModuleName() {
        return MODULE_NAME;
    }

    @Override
    public void load(File file) throws CommandExecutionException {
        try {
            generateDefinition(new PopulationModelGenerator(file));
        } catch (ModelGenerationException e) {
            throw new CommandExecutionException(e.getErrors().stream().sequential().map(ModelBuildingError::toString).collect(Collectors.toList()));
        } catch (IOException e) {
            throw new CommandExecutionException(e.getMessage());
        }
    }

    @Override
    public void load(String code) throws CommandExecutionException {
        try {
            generateDefinition(new PopulationModelGenerator(code));
        } catch (ModelGenerationException e) {
            throw new CommandExecutionException(e.getErrors().stream().sequential().map(ModelBuildingError::toString).collect(Collectors.toList()));
        }
    }

    private void generateDefinition(PopulationModelGenerator pmg) throws ModelGenerationException {
        setModelDefinition(pmg.getPopulationModelDefinition());
    }

    @Override
    public String info() {
        //TODO: Implement this!
        return null;
    }



}
