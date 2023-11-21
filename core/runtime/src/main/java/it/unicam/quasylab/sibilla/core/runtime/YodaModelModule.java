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


import it.unicam.quasylab.sibilla.core.models.yoda.YodaSystemState;
import it.unicam.quasylab.sibilla.langs.yoda.YodaModelGenerationException;
import it.unicam.quasylab.sibilla.langs.yoda.YodaModelGenerator;

import java.io.File;
import java.io.IOException;

public class YodaModelModule extends AbstractSibillaModule{

    public final static String MODULE_NAME = "yoda";

    private ModuleEngine<YodaSystemState> moduleEngine;

    @Override
    public String getModuleName() {
        return MODULE_NAME;
    }

    @Override
    public void load(File file) throws CommandExecutionException {
        try {
            generateModuleEngine(new YodaModelGenerator(file));
        } catch (YodaModelGenerationException e){
            throw new CommandExecutionException(e.getErrorMessages());
        } catch (IOException e) {
            throw new CommandExecutionException(e.getMessage());
        }
    }

    @Override
    public void load(String code) throws CommandExecutionException {
        try {
            generateModuleEngine(new YodaModelGenerator(code));
        } catch (YodaModelGenerationException e){
            throw new CommandExecutionException(e.getMessage());

        }
    }

    //TODO
    @Override
    public String info() {
        return null;
    }

    private void generateModuleEngine(YodaModelGenerator yodaModelGenerator) throws YodaModelGenerationException {
        this.moduleEngine = new ModuleEngine<>(yodaModelGenerator.getYodaModelDefinition());
    }


    @Override
    protected ModuleEngine<?> getModuleEngine() {
        return moduleEngine;
    }
}
