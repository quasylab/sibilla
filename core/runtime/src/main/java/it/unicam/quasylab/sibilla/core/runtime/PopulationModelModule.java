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


public class PopulationModelModule implements SibillaModule {

    public final static String MODULE_NAME = "population";
    private PopulationModelDefinition populationModelDefinition;
    private PopulationModel model;
    private StateSet<PopulationState> states;
    private PopulationState state;
    private Set<String> enabledMeasures;
    private SimulationEnvironment simulator = new SimulationEnvironment();

    @Override
    public String getModuleName() {
        return MODULE_NAME;
    }

    private void clearModuleState( ) {
        this.model = null;
        this.state = null;
        this.enabledMeasures = new TreeSet<>();
        this.states = null;
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
        setPopulationModelDefinition(pmg.getPopulationModelDefinition());
    }

    private void setPopulationModelDefinition(PopulationModelDefinition populationModelDefinition) {
        this.populationModelDefinition = populationModelDefinition;
        clearModuleState();
    }

    @Override
    public String info() {
        //TODO: Implement this!
        return null;
    }

    @Override
    public void setParameter(String name, double value) {
        checkForLoadedDefinition();
        this.populationModelDefinition.setParameter(name, value);
        clearModuleState();
    }

    @Override
    public double getParameter(String name) {
        checkForLoadedDefinition();
        return this.populationModelDefinition.getValue(name);
    }

    @Override
    public String[] getParameters() {
        checkForLoadedDefinition();
        return this.populationModelDefinition.getModelParameters();
    }

    @Override
    public Map<String, Double> getEvaluationEnvironment() {
        checkForLoadedDefinition();
        return this.populationModelDefinition.getEnvironment().getParameterMap();
    }

    @Override
    public void clear() {
        this.populationModelDefinition = null;
        clearModuleState();
    }

    @Override
    public void reset() {
        checkForLoadedDefinition();
        populationModelDefinition.reset();
        clearModuleState();
    }

    @Override
    public void reset(String name) {
        checkForLoadedDefinition();
        populationModelDefinition.reset(name);
        clearModuleState();
    }

    @Override
    public String[] getInitialConfigurations() {
        checkForLoadedDefinition();
        return this.populationModelDefinition.states();
    }

    private void checkForLoadedDefinition() {
        if (populationModelDefinition == null) {
            throw new IllegalStateException("No model has been loaded!");
        }
    }

    @Override
    public String getConfigurationInfo(String name) {
        checkForLoadedDefinition();
        loadModel();
        loadStates();
        return states.getInfo(name);
    }

    @Override
    public boolean setConfiguration(String name, double... args) {
        checkForLoadedDefinition();
        loadModel();
        loadStates();
        if (!states.isDefined(name)) {
            throw new IllegalStateException("Unknown configuration "+name);
        }
        if (states.arity(name)!=args.length) {
            throw new IllegalStateException(String.format("Wrong number of parameters for state %s (expected %d are %d)",name,states.arity(name),args.length));
        }
        state = states.state(name, args);
        return true;
    }

    private void loadStates() {
        states = populationModelDefinition.getStates();
    }

    private void loadModel() {
        if (model == null) {
            model = populationModelDefinition.createModel();
        }
    }

    @Override
    public String[] getMeasures() {
        checkForLoadedDefinition();
        loadModel();
        return model.measures();
    }

    @Override
    public void setMeasures(String... measures) {
        checkForLoadedDefinition();
        loadModel();
        checkMeasures(measures);
        this.enabledMeasures.addAll(List.of(measures));
    }

    private void checkMeasures(String[] measures) {
        for (String m: measures) {
            checkMeasure(m);
        }
    }

    private void checkMeasure(String m) {
        if (model.getMeasure(m) == null) {
            throw new IllegalStateException("Unknown measure "+m+".");
        }
    }

    @Override
    public void addMeasure(String name) {
        checkForLoadedDefinition();
        loadModel();
        checkMeasure(name);
        this.enabledMeasures.add(name);
    }

    @Override
    public void removeMeasure(String name) {
        this.enabledMeasures.remove(name);
    }

    @Override
    public void addAllMeasures() {
        checkForLoadedDefinition();
        loadModel();
        this.enabledMeasures.addAll(List.of(model.measures()));
    }

    @Override
    public void removeAllMeasures() {
        this.enabledMeasures = new TreeSet<>();
    }

    @Override
    public List<SimulationTimeSeries> simulate(SimulationMonitor monitor, RandomGenerator rg, int replica, double deadline, double dt) {
        checkForLoadedDefinition();
        loadModel();
        loadState();
        SamplingFunction<PopulationState> samplingFunction = model.selectSamplingFunction(deadline, dt, enabledMeasures.toArray(new String[0]));
        try {
            simulator.simulate(monitor, rg, model,state, samplingFunction,replica,deadline);
            return samplingFunction.getSimulationTimeSeries(replica);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean isEnabledMeasure(String name) {
        return this.enabledMeasures.contains(name);
    }

    private void loadState() {
        if (state == null) {
            state = populationModelDefinition.state();
        }
    }
}
