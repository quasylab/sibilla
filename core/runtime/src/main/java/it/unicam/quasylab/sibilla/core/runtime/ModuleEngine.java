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
import it.unicam.quasylab.sibilla.core.models.ParametricDataSet;
import it.unicam.quasylab.sibilla.core.models.State;
import it.unicam.quasylab.sibilla.core.simulator.SimulationEnvironment;
import it.unicam.quasylab.sibilla.core.simulator.SimulationMonitor;
import it.unicam.quasylab.sibilla.core.simulator.sampling.FirstPassageTime;
import it.unicam.quasylab.sibilla.core.simulator.sampling.FirstPassageTimeResults;
import it.unicam.quasylab.sibilla.core.simulator.sampling.SamplingFunction;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class ModuleEngine<S extends State> {

    private final ModelDefinition<S>          modelDefinition;
    private ParametricDataSet<Function<RandomGenerator,S>> states;
    private Model<S>                    currentModel;
    private Function<RandomGenerator,S> state;


    public ModuleEngine(ModelDefinition<S> modelDefinition) {
        this.modelDefinition = modelDefinition;
    }

    protected void clear() {
        this.currentModel = null;
        this.state = null;
        this.states = null;
    }

    public void setParameter(String name, SibillaValue value) {
        this.modelDefinition.setParameter(name, value);
        clear();
    }

    public SibillaValue getParameter(String name) {
        return this.modelDefinition.getParameterValue(name);
    }

    public String[] getParameters() {
        return this.modelDefinition.getModelParameters();
    }

    public Map<String, SibillaValue> getEvaluationEnvironment() {
        return this.modelDefinition.getEnvironment().getParameterMap();
    }

    public String[] getMeasures() {
        loadModel();
        return currentModel.measures();
    }

    private void loadModel() {
        if (currentModel == null) {
            currentModel = modelDefinition.createModel();
        }
    }

    public String[] getPredicates() {
        loadModel();
        return currentModel.predicates();
    }

    private void loadStates() {
        states = modelDefinition.getStates();
    }

    public String[] getInitialConfigurations() {
        return this.modelDefinition.states();
    }


    public String getConfigurationInfo(String name) {
        loadModel();
        loadStates();
        return states.getInfo(name);
    }

    public boolean setConfiguration(String name, double... args) {
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

    private void loadState() {
        if (state == null) {
            state = modelDefinition.state();
        }
    }

    public Map<String, double[][]> simulate(SimulationEnvironment simulationEnvironment,
                                            SimulationMonitor monitor,
                                            RandomGenerator rg,
                                            long replica,
                                            double deadline,
                                            double dt,
                                            String[] measures,
                                            boolean summary) {
        loadModel();
        loadState();
        SamplingFunction<S> samplingFunction = currentModel.selectSamplingFunction(summary, deadline, dt, measures);
        try {
            simulationEnvironment.simulate(monitor, rg, currentModel, state, samplingFunction::getSamplingHandler, replica, deadline);
            return samplingFunction.getSimulationTimeSeries();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    public FirstPassageTimeResults firstPassageTime(SimulationEnvironment simulationEnvironment,
                                                    SimulationMonitor monitor,
                                                    RandomGenerator rg,
                                                    long replica,
                                                    double deadline,
                                                    double dt,
                                                    String predicateName) {
        loadModel();
        loadState();
        Predicate<? super S> predicate = currentModel.getPredicate(predicateName);
        if (predicate == null) {
            throw new IllegalStateException("Predicate "+predicateName+" is unknown!");
        }
        FirstPassageTime<S> fpt = new FirstPassageTime<>(predicateName, predicate);
        try {
            simulationEnvironment.simulate(monitor, rg, currentModel, state, fpt, replica, deadline);
            return fpt.getResults();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    public double estimateReachability(SimulationEnvironment simulationEnvironment,
                                       SimulationMonitor monitor,
                                       RandomGenerator rg,
                                       String targetName,
                                       double time,
                                       double pError,
                                       double delta) {
        loadModel();
        loadState();
        Predicate<? super S> targetPredicate = currentModel.getPredicate(targetName);
        try {
            return simulationEnvironment.reachability(monitor, rg, pError,delta,time, currentModel, state, s -> true, targetPredicate::test);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public double estimateReachability(SimulationEnvironment simulationEnvironment,
                                       SimulationMonitor monitor,
                                       RandomGenerator rg,
                                       String transientCondition,
                                       String targetCondition,
                                       double time,
                                       double pError,
                                       double delta) {
        loadModel();
        loadState();
        Predicate<? super S> transientPredicate = currentModel.getPredicate(transientCondition);
        Predicate<? super S> targetPredicate = currentModel.getPredicate(targetCondition);
        try {
            return simulationEnvironment.reachability(monitor, rg, pError,delta,time, currentModel, state, transientPredicate::test, targetPredicate::test);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }


    public Map<String, SibillaValue> getEnvironment() {
        return modelDefinition.getEnvironment().getParameterMap();
    }

    public void reset() {
        this.modelDefinition.reset();
        clear();
    }

    public void reset(String name) {
        this.modelDefinition.reset(name);
        clear();
    }
}
