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
import it.unicam.quasylab.sibilla.core.simulator.Trajectory;
import it.unicam.quasylab.sibilla.core.simulator.sampling.FirstPassageTimeHandlerSupplier;
import it.unicam.quasylab.sibilla.core.simulator.sampling.FirstPassageTime;
import it.unicam.quasylab.sibilla.core.simulator.sampling.SamplingFunction;
import it.unicam.quasylab.sibilla.tools.stl.QualitativeMonitor;
import it.unicam.quasylab.sibilla.tools.stl.QuantitativeMonitor;
import it.unicam.quasylab.sibilla.core.util.SimulationData;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import it.unicam.quasylab.sibilla.tools.stl.StlModelGenerationException;
import it.unicam.quasylab.sibilla.tools.tracing.TracingData;
import it.unicam.quasylab.sibilla.tools.tracing.TracingFunction;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

public class ModuleEngine<S extends State> {

    private final ModelDefinition<S>          modelDefinition;
    private ParametricDataSet<Function<RandomGenerator,S>> states;
    private Model<S>                    currentModel;
    protected Function<RandomGenerator,S> state;

    protected String selectedConfigurationName;
    private double[] selectedConfigurationArgs;

    protected SibillaSTLMonitorGenerator<S> stlMonitorGenerator;

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
        loadModel();
        return this.modelDefinition.getParameterValue(name);
    }

    public String[] getParameters() {
        loadModel();
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


    public String[] getInitialConfigurations() {
        return this.modelDefinition.configurations();
    }


    public String getConfigurationInfo(String name) {
        loadModel();
        return modelDefinition.getStateInfo(name);
    }

    public boolean setConfiguration(String name, double... args) {
        loadModel();
        if (!modelDefinition.isAnInitialConfiguration(name)) {
            throw new IllegalStateException("Unknown configuration "+name);
        }
        if (modelDefinition.configurationArity(name)!=args.length) {
            throw new IllegalStateException(String.format("Wrong number of parameters for state %s (expected %d are %d)",name,states.arity(name),args.length));
        }
        state = getConfiguration(name, args);
        selectedConfigurationName = name;
        selectedConfigurationArgs = args;
        return true;
    }

    protected Function<RandomGenerator,S> getConfiguration(String name, double ... args) {
        return modelDefinition.getConfiguration(name, args);
    }

    protected void setDefaultConfiguration() {
        if (state == null) {
            state = getDefaultConfiguration();
        }
    }

    protected Function<RandomGenerator,S> getDefaultConfiguration(double ... args) {
        return modelDefinition.getDefaultConfiguration(args);
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
        setDefaultConfiguration();
        SamplingFunction<S> samplingFunction = currentModel.selectSamplingFunction(summary, deadline, dt, measures);
        try {
            simulationEnvironment.simulate(monitor, rg, currentModel, state, samplingFunction::getSamplingHandler, replica, deadline);
            return samplingFunction.getSimulationTimeSeries();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    public FirstPassageTime firstPassageTime(SimulationEnvironment simulationEnvironment,
                                             SimulationMonitor monitor,
                                             RandomGenerator rg,
                                             long replica,
                                             double deadline,
                                             double dt,
                                             String predicateName) {
        loadModel();
        setDefaultConfiguration();
        Predicate<? super S> predicate = currentModel.getPredicate(predicateName);
        if (predicate == null) {
            throw new IllegalStateException("Predicate "+predicateName+" is unknown!");
        }
        FirstPassageTimeHandlerSupplier<S> fpt = new FirstPassageTimeHandlerSupplier<>(predicateName, predicate);
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
        setDefaultConfiguration();
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
        setDefaultConfiguration();
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

    public Map<String, ToDoubleFunction<S>> getMeasuresFunctionMapping() {
        return currentModel.measuresFunctionMapping();
    }

    public boolean isAMeasure(String name) {
        loadModel();
        return this.currentModel.getMeasure(name) != null;
    }

    public Map<String, double[][]>  qualitativeMonitoring(SimulationEnvironment se,
                                                          RandomGenerator rg,
                                                          String[] formulaName,
                                                          double[][] formulaArgs,
                                                          double deadline,
                                                          double dt,
                                                          int replica) throws StlModelGenerationException {
        Map<String, double[][]> result = new TreeMap<>();
        for (int i = 0; i < formulaName.length; i++) {
            QualitativeMonitor<S> formulaMonitor = stlMonitorGenerator.getQualitativeMonitor(formulaName[i], formulaArgs[i]);
            Supplier<Trajectory<S>> trajectoryProvider = () -> se.sampleTrajectory(rg, currentModel, state.apply(rg), deadline);
            result.put(formulaName[i], QualitativeMonitor.computeTimeSeriesProbabilities(formulaMonitor,trajectoryProvider,replica,dt,deadline));
        }
        return  result;

    }


    /**
     * Performs quantitative monitoring using a single formula.
     *
     * @param simulationEnvironment The simulation environment used to generate trajectories.
     * @param rg The random generator used to sample trajectories.
     * @param formulaName The name of the formula used for monitoring.
     * @param formulaArgs The arguments for the formula.
     * @param deadline The deadline.
     * @param dt The time step increment.
     * @param replica The number of replicas to run.
     * @return A map where the key is the formula name and the value is a double[][] array, where:
     *         - results[i][0] is the time step
     *         - results[i][1] is the mean robustness at the time step
     *         - results[i][2] is the standard deviation of robustness at the time step
     * @throws StlModelGenerationException If there is an error generating the formula monitor.
     */
    public Map<String, double[][]> quantitativeMonitoring(SimulationEnvironment simulationEnvironment,
                                                          RandomGenerator rg,
                                                          String[] formulaName,
                                                          double[][] formulaArgs,
                                                          double deadline,
                                                          double dt,
                                                          int replica) throws StlModelGenerationException {
        loadModel();
        setDefaultConfiguration();
        Map<String, double[][]> result = new TreeMap<>();
        for (int i = 0; i < formulaName.length; i++) {
            QuantitativeMonitor<S> formulaMonitor = stlMonitorGenerator.getQuantitativeMonitor(formulaName[i], formulaArgs[i]);
            Supplier<Trajectory<S>> trajectoryProvider = () -> {
                Trajectory<S> trajectory = simulationEnvironment.sampleTrajectory(rg, currentModel, state.apply(rg), deadline);
                trajectory.setEnd(deadline);
                return trajectory;
            };
            //simulationEnvironment.sampleTrajectory(rg, currentModel, state.apply(rg), deadline);
            result.put(formulaName[i], QuantitativeMonitor.meanAndStandardDeviationRobustness(formulaMonitor,trajectoryProvider,replica,dt,deadline));
        }
        return  result;

    }



    public List<SimulationData> trace(SimulationEnvironment simulationEnvironment,
                                      RandomGenerator rg,
                                      double deadline) {
        loadModel();
        setDefaultConfiguration();
        S initialState = state.apply(rg);
        Trajectory<S> trajectory = simulationEnvironment.sampleTrajectory(rg, currentModel, initialState, deadline);
        Map<String, Map<String, ToDoubleFunction<S>>> traceFunctions = currentModel.trace(initialState);
        return traceFunctions.entrySet().stream().map(e -> SimulationData.getDataSet(e.getKey(), trajectory, e.getValue())).toList();
    }

    public Map<String, List<TracingData>> trace(SimulationEnvironment simulationEnvironment,
                                   RandomGenerator rg,
                                   TracingFunction tracingFunction,
                                   double deadline) {
        loadModel();
        setDefaultConfiguration();
        S initialState = state.apply(rg);
        Trajectory<S> trajectory = simulationEnvironment.sampleTrajectory(rg, currentModel, initialState, deadline);
        Map<String, Function<S, Function<String, SibillaValue>>> traceFunctions = currentModel.getNameSolver(initialState);
        Map<String, List<TracingData>> result = new HashMap<>();
        traceFunctions.forEach((name, np) -> result.put(name, trajectory.stream().map(s -> tracingFunction.apply(np.apply(s.getValue()), s.getTime())).toList()));
        return result;
    }

    public void generateMonitor(String code) {
        this.stlMonitorGenerator = new SibillaSTLMonitorGenerator<>(code, getMeasuresFunctionMapping());
    }

    public void generateMonitor(File file) throws  IOException {
        this.stlMonitorGenerator = new SibillaSTLMonitorGenerator<>(file, getMeasuresFunctionMapping());
    }
}
