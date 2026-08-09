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

import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.states.AgentState;
import it.unicam.quasylab.sibilla.langs.dopm.generators.DataOrientedPopulationModelGenerator;
import it.unicam.quasylab.sibilla.langs.dopm.generators.exceptions.ModelGenerationException;
import it.unicam.quasylab.sibilla.langs.dopm.errors.ModelBuildingError;

import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;


public class DataOrientedPopulationModelModule extends AbstractSibillaModule {

    public final static String MODULE_NAME = "dopm";

    private ModuleEngine<AgentState> moduleEngine;

    @Override
    public String getModuleName() {
        return MODULE_NAME;
    }

    @Override
    public void load(File file) throws CommandExecutionException {
        try {
            generateModuleEngine(new DataOrientedPopulationModelGenerator(file));
        } catch (ModelGenerationException e) {
            throw new CommandExecutionException(e.getErrors().stream().sequential().map(ModelBuildingError::toString).collect(Collectors.toList()));
        } catch (IOException e) {
            throw new CommandExecutionException(e.getMessage());
        }
    }

    @Override
    public void load(String code) throws CommandExecutionException {
        try {
            generateModuleEngine(new DataOrientedPopulationModelGenerator(code));
        } catch (ModelGenerationException e) {
            throw new CommandExecutionException(e.getErrors().stream().sequential().map(ModelBuildingError::toString).collect(Collectors.toList()));
        }
    }

    private void generateModuleEngine(DataOrientedPopulationModelGenerator pmg) throws it.unicam.quasylab.sibilla.langs.dopm.generators.exceptions.ModelGenerationException {
        this.moduleEngine = new ModuleEngine<>(pmg.getPopulationModelDefinition());
    }

    @Override
    public String info() {
        //TODO: Implement this!
        return null;
    }

    @Override
    protected ModuleEngine<?> getModuleEngine() {
        return moduleEngine;
    }


}