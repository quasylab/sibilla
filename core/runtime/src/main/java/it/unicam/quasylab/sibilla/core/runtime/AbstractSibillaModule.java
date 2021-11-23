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

import it.unicam.quasylab.sibilla.core.models.Model;
import it.unicam.quasylab.sibilla.core.models.ModelDefinition;
import it.unicam.quasylab.sibilla.core.models.State;
import it.unicam.quasylab.sibilla.core.models.StateSet;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.simulator.SimulationEnvironment;
import it.unicam.quasylab.sibilla.core.simulator.SimulationMonitor;
import it.unicam.quasylab.sibilla.core.simulator.sampling.FirstPassageTime;
import it.unicam.quasylab.sibilla.core.simulator.sampling.FirstPassageTimeResults;
import it.unicam.quasylab.sibilla.core.simulator.sampling.SamplingFunction;
import it.unicam.quasylab.sibilla.core.simulator.sampling.SimulationTimeSeries;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

public abstract class AbstractSibillaModule<S extends State> implements SibillaModule {

    private ModelDefinition<S> modelDefinition;
    private Model<S> model;
    private StateSet<S> states;
    private S state;
    private Set<String> enabledMeasures;
    private boolean summary = true;
    private SimulationEnvironment simulator = new SimulationEnvironment();

    protected void clearModuleState() {
        this.model = null;
        this.state = null;
        this.enabledMeasures = new TreeSet<>();
        this.states = null;
    }

    public void setModelDefinition(ModelDefinition<S> modelDefinition) {
        this.modelDefinition = modelDefinition;
    }

    private void checkForLoadedDefinition() {
        if (modelDefinition == null) {
            throw new IllegalStateException("No model has been loaded!");
        }
    }

    @Override
    public void setParameter(String name, double value) {
        checkForLoadedDefinition();
        this.modelDefinition.setParameter(name, value);
        clearModuleState();
    }

    @Override
    public double getParameter(String name) {
        checkForLoadedDefinition();
        return this.modelDefinition.getParameterValue(name);
    }

    @Override
    public String[] getParameters() {
        checkForLoadedDefinition();
        return this.modelDefinition.getModelParameters();
    }

    @Override
    public Map<String, Double> getEvaluationEnvironment() {
        checkForLoadedDefinition();
        return this.modelDefinition.getEnvironment().getParameterMap();
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
        SamplingFunction<S> samplingFunction = model.selectSamplingFunction(summary, deadline, dt, enabledMeasures.toArray(new String[0]));
        try {
            simulator.simulate(monitor, rg, model, state, samplingFunction::sample, replica, deadline);
            return samplingFunction.getSimulationTimeSeries(replica);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    public FirstPassageTimeResults firstPassageTime(SimulationMonitor monitor, RandomGenerator rg, int replica, double deadline, double dt, String predicateName) {
        checkForLoadedDefinition();
        loadModel();
        loadState();
        Predicate<S> predicate = model.getPredicate(predicateName);
        if (predicate == null) {
            throw new IllegalStateException("Predicate "+predicateName+" is unknown!");
        }
        FirstPassageTime<S> fpt = new FirstPassageTime<>(predicateName, predicate);
        try {
            simulator.simulate(monitor, rg, model, state, fpt, replica, deadline);
            return fpt.getResults();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public double estimateReachability(String targetCondition, double time, double pError, double delta) {
        checkForLoadedDefinition();
        loadModel();
        loadState();
        //Predicate<S> targetPredicate = model.getCondition(targetCondition);
        //return simulator.reachability(pError,delta,time, model, state, targetPredicate);
        return 0.0;
    }

    @Override
    public double estimateReachability(String transientCondition, String targetCondition, double time, double pError, double delta) {
        checkForLoadedDefinition();
        loadModel();
        loadState();
        //Predicate<S> transientPredicate = model.getCondition(transientCondition);
        //Predicate<S> targetPredicate = model.getCondition(targetCondition);
        //return simulator.reachability(pError,delta,time, model, state, transientPredicate, targetPredicate);
        return 0.0;
    }

    @Override
    public boolean isEnabledMeasure(String name) {
        return this.enabledMeasures.contains(name);
    }

    @Override
    public void clear() {
        this.model = null;
        clearModuleState();
    }

    @Override
    public void reset() {
        checkForLoadedDefinition();
        modelDefinition.reset();
        clearModuleState();
    }

    @Override
    public void reset(String name) {
        checkForLoadedDefinition();
        modelDefinition.reset(name);
        clearModuleState();
    }

    @Override
    public String[] getInitialConfigurations() {
        checkForLoadedDefinition();
        return this.modelDefinition.states();
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
        states = modelDefinition.getStates();
    }

    private void loadModel() {
        if (model == null) {
            model = modelDefinition.createModel();
        }
    }

    @Override
    public String[] getMeasures() {
        checkForLoadedDefinition();
        loadModel();
        return model.measures();
    }

    @Override
    public String[] getPredicates() {
        checkForLoadedDefinition();
        loadModel();
        return model.predicates();
    }

    @Override
    public void setMeasures(String... measures) {
        checkForLoadedDefinition();
        loadModel();
        checkMeasures(measures);
        this.enabledMeasures.addAll(List.of(measures));
    }

    @Override
    public void setSummaryStatistics(boolean summary) {
        this.summary = summary;
    }

    @Override
    public boolean isSummaryStatistics() {
        return summary;
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

    private void loadState() {
        if (state == null) {
            state = modelDefinition.state();
        }
    }
}
