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

import it.unicam.quasylab.sibilla.core.models.markov.MarkovChainDefinition;
import it.unicam.quasylab.sibilla.core.models.markov.MarkovChainModel;
import it.unicam.quasylab.sibilla.core.models.util.MappingState;
import it.unicam.quasylab.sibilla.core.simulator.SimulationMonitor;
import it.unicam.quasylab.sibilla.core.simulator.sampling.SimulationTimeSeries;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MarkovChainModelModule extends AbstractSibillaModule<MappingState> {

    public final static String MODULE_NAME = "markov_chains";


    @Override
    public String getModuleName() {
        return MODULE_NAME;
    }

    @Override
    public void load(File file) throws CommandExecutionException {

    }

    @Override
    public void load(String code) throws CommandExecutionException {

    }

    @Override
    public String info() {
        return null;
    }

    @Override
    public String[] getModes() {
        return super.getModes();
    }

    @Override
    public void setMode(String name) {
        super.setMode(name);
    }

    @Override
    public String getMode() {
        return super.getMode();
    }


}
