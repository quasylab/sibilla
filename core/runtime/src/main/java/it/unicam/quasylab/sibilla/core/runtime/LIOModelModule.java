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

import it.unicam.quasylab.sibilla.core.models.EvaluationEnvironment;
import it.unicam.quasylab.sibilla.core.models.lio.AgentsDefinition;
import it.unicam.quasylab.sibilla.core.models.lio.LIOModelDefinition;
import it.unicam.quasylab.sibilla.core.models.lio.LIOState;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.simulator.SimulationMonitor;
import it.unicam.quasylab.sibilla.core.simulator.sampling.FirstPassageTimeResults;
import it.unicam.quasylab.sibilla.langs.lio.LIOModelGenerator;
import it.unicam.quasylab.sibilla.langs.lio.LIOModelParseError;
import it.unicam.quasylab.sibilla.langs.pm.ModelBuildingError;
import it.unicam.quasylab.sibilla.langs.pm.ModelGenerationException;
import it.unicam.quasylab.sibilla.langs.pm.PopulationModelGenerator;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


public class LIOModelModule extends AbstractSibillaModule {

    private LIOModelGenerator modelGenerator;

    public enum LIOModes {
        INDIVIDUALS,
        MASS;
    }

    private LIOModes mode = LIOModes.INDIVIDUALS;


    public final static String MODULE_NAME = "lio";

    @Override
    public String getModuleName() {
        return MODULE_NAME;
    }

    @Override
    public void load(File file) throws CommandExecutionException {
        try {
            setModelGenerator(new LIOModelGenerator(file));
        } catch (IOException | LIOModelParseError e) {
            throw new CommandExecutionException(e.getMessage());
        }
    }

    @Override
    public void load(String code) throws CommandExecutionException {
        try {
            setModelGenerator(new LIOModelGenerator(code));
        } catch (Exception e) {
            throw new CommandExecutionException(e.getMessage());
        }
    }

    private void setModelGenerator(LIOModelGenerator modelGenerator) {
        this.modelGenerator = modelGenerator;
        generateDefinition();
    }

    @Override
    public String info() {
        //TODO: Implement this!
        return null;
    }

    @Override
    public String[] getModes() {
        return Arrays.stream(LIOModes.values()).map(Enum::toString).toArray(String[]::new);
    }

    @Override
    public void setMode(String name) {
        this.mode = LIOModes.valueOf(name);
    }

    @Override
    public String getMode() {
        return this.mode.name();
    }

    private void generateDefinition() {
        switch (mode) {
            case MASS: setModelDefinition(modelGenerator.getMassModelDefinition());
            case INDIVIDUALS: setModelDefinition(modelGenerator.getIndividualModelDefinition());
        }
    }



}
