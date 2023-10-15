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

import it.unicam.quasylab.sibilla.core.models.slam.SlamModelDefinition;
import it.unicam.quasylab.sibilla.core.models.slam.SlamState;
import it.unicam.quasylab.sibilla.langs.slam.SlamModelGenerationException;
import it.unicam.quasylab.sibilla.langs.slam.SlamModelGenerator;

import java.io.File;
import java.io.IOException;

public class SlamModelModule extends AbstractSibillaModule  {

    public final static String MODULE_NAME = "slam";

    private ModuleEngine<SlamState> slamStateModuleEngine;

    @Override
    public String getModuleName() {
        return MODULE_NAME;
    }

    @Override
    public void load(File file) throws CommandExecutionException {
        try {
            generateModuleEngine(new SlamModelGenerator(file));
        } catch (IOException|SlamModelGenerationException e) {
            throw new CommandExecutionException(e.getMessage());
        }
    }

    @Override
    public void load(String code) throws CommandExecutionException {
        try {
            generateModuleEngine(new SlamModelGenerator(code));
        } catch (SlamModelGenerationException e) {
            throw new CommandExecutionException(e.getMessage());
        }
    }

    private void generateModuleEngine(SlamModelGenerator slamModelGenerator) throws SlamModelGenerationException {
        this.slamStateModuleEngine = new ModuleEngine<>(slamModelGenerator.getDefinition());
    }

    @Override
    public String info() {
        //TODO: Add info here.
        return "SLAM ";
    }

    @Override
    protected ModuleEngine<?> getModuleEngine() {
        return slamStateModuleEngine;

    }
}
