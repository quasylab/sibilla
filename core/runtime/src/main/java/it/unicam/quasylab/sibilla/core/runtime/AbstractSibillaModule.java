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

import it.unicam.quasylab.sibilla.core.models.State;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.runtime.command.*;
import it.unicam.quasylab.sibilla.core.simulator.DefaultRandomGenerator;
import it.unicam.quasylab.sibilla.core.simulator.SimulationEnvironment;
import it.unicam.quasylab.sibilla.core.simulator.SimulationManagerFactory;
import it.unicam.quasylab.sibilla.core.simulator.SimulationMonitor;
import it.unicam.quasylab.sibilla.core.simulator.sampling.FirstPassageTime;
import it.unicam.quasylab.sibilla.core.util.SimulationData;
import it.unicam.quasylab.sibilla.core.util.values.SibillaDouble;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import it.unicam.quasylab.sibilla.tools.stl.StlLoader;
import it.unicam.quasylab.sibilla.tools.stl.StlModelGenerationException;
import it.unicam.quasylab.sibilla.tools.tracing.TracingData;
import it.unicam.quasylab.sibilla.tools.tracing.TracingFunction;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.ToDoubleFunction;

import static it.unicam.quasylab.sibilla.core.runtime.Message.NO_MODULE_HAS_BEEN_LOADED;

public abstract class AbstractSibillaModule implements SibillaModule {

    private Set<String> enabledMeasures = new TreeSet<>();
    private boolean summary = true;
    private final SimulationEnvironment simulator = new SimulationEnvironment();
    protected final CommandAdapter commandAdapter = new CommandAdapter();
    private Configuration currentConfiguration;

    public AbstractSibillaModule() {
        initCommandHandler();
    }

    @Override
    public Optional<CommandResult> handle(Command command) throws CommandExecutionException {
        return commandAdapter.handle(command);
    }

    protected void initCommandHandler() {


    }

    protected abstract ModuleEngine<?> getModuleEngine();

    private ModuleEngine<?> checkForLoadedDefinition() {
        ModuleEngine<?> engine = getModuleEngine();
        if (engine == null) {
            throw new IllegalStateException(NO_MODULE_HAS_BEEN_LOADED);
        }
        return engine;
    }

    @Override
    public void setParameter(String name, double value) {
        checkForLoadedDefinition().setParameter(name, new SibillaDouble(value));
    }

    @Override
    public double getParameter(String name) {
        return checkForLoadedDefinition().getParameter(name).doubleOf();
    }

    @Override
    public String[] getParameters() {
        return checkForLoadedDefinition().getParameters();
    }

    @Override
    public Map<String, SibillaValue> getEvaluationEnvironment() {
        return checkForLoadedDefinition().getEnvironment();
    }

    @Override
    public void addMeasure(String name) {
        if (checkForLoadedDefinition().isAMeasure(name)) {
            this.enabledMeasures.add(name);
        } else {
            throw new IllegalArgumentException(String.format("Measure %s does not exists", name));
        }
    }

    @Override
    public void removeMeasure(String name) {
        if (this.enabledMeasures.contains(name)) {
            this.enabledMeasures.remove(name);
        } else {
            throw new IllegalArgumentException(String.format("Measure %s is not enabled", name));
        }
    }

    @Override
    public void addAllMeasures() {
        this.enabledMeasures.addAll(List.of(checkForLoadedDefinition().getMeasures()));
    }

    @Override
    public void removeAllMeasures() {
        this.enabledMeasures = new TreeSet<>();
    }


    @Override
    public Map<String, double[][]> simulate(SimulationMonitor monitor, RandomGenerator rg, long replica, double deadline, double dt) {
        return checkForLoadedDefinition().simulate(this.simulator, monitor, rg, replica, deadline, dt, this.enabledMeasures.toArray(new String[0]),summary);
    }

    @Override
    public FirstPassageTime firstPassageTime(SimulationMonitor monitor, RandomGenerator rg, long replica, double deadline, double dt, String predicateName) {
        return checkForLoadedDefinition().firstPassageTime(simulator, monitor, rg, replica, deadline, dt, predicateName);
    }

    @Override
    public double estimateReachability(SimulationMonitor monitor, RandomGenerator rg,  String targetCondition, double time, double pError, double delta) {
        return checkForLoadedDefinition().estimateReachability(simulator, monitor, rg, targetCondition, time, pError, delta);
    }

    @Override
    public double[] estimateReachability(SimulationMonitor monitor, RandomGenerator rg,  String targetCondition, double dt, double time, double pError, double delta) {
        return checkForLoadedDefinition().estimateReachability(simulator, monitor, rg, targetCondition, dt, time, pError, delta);
    }

    @Override
    public double estimateReachability(SimulationMonitor monitor, RandomGenerator rg, String transientCondition, String targetCondition, double time, double pError, double delta) {
        return checkForLoadedDefinition().estimateReachability(simulator, monitor, rg, transientCondition, targetCondition, time, pError, delta);
    }

    @Override
    public void setSimulationManagerFactory(SimulationManagerFactory factory) {
        this.simulator.setSimulationManagerFactory(factory);
    }

    @Override
    public List<SimulationData> trace( RandomGenerator rg, double dealine) {
        return checkForLoadedDefinition().trace(simulator, rg, dealine);
    }

    @Override
    public Map<String, List<TracingData>> trace(RandomGenerator rg, TracingFunction tracingFunction, double deadline) {
        return checkForLoadedDefinition().trace(simulator, rg, tracingFunction, deadline);
    }

    @Override
    public boolean isEnabledMeasure(String name) {
        return this.enabledMeasures.contains(name);
    }

    @Override
    public void clear() {
        ModuleEngine<?> engine = getModuleEngine();
        if (engine != null) {
            engine.clear();
        }
        this.enabledMeasures = new TreeSet<>();
    }

    @Override
    public void reset() {
        ModuleEngine<?> engine = getModuleEngine();
        if (engine != null) {
            engine.reset();
        }
    }

    @Override
    public void reset(String name) {
        ModuleEngine<?> engine = getModuleEngine();
        if (engine != null) {
            engine.reset(name);
        }
    }

    @Override
    public String[] getInitialConfigurations() {
        return checkForLoadedDefinition().getInitialConfigurations();
    }



    @Override
    public String getConfigurationInfo(String name) {
        return checkForLoadedDefinition().getConfigurationInfo(name);
    }

    @Override
    public Configuration getCurrentConfiguration() {
        return currentConfiguration;
    }

    @Override
    public boolean setConfiguration(String name, double... args) {
        this.currentConfiguration = new Configuration(name, args);
        return checkForLoadedDefinition().setConfiguration(name, args);
    }



    @Override
    public String[] getMeasures() {
        return checkForLoadedDefinition().getMeasures();
    }

    @Override
    public String[] getPredicates() {
        return checkForLoadedDefinition().getPredicates();
    }

    @Override
    public void setMeasures(String... measures) {
        checkForLoadedDefinition();
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


    @Override
    public void loadFormulas(File file) throws IOException{
        checkForLoadedDefinition().generateMonitor(file);
    }

    @Override
    public void loadFormulas(String code){
        checkForLoadedDefinition().generateMonitor(code);
    }

    @Override
    public Map<String, double[][]> qualitativeMonitoring(RandomGenerator rg, String[] formulaName, Map<String, Double>[] formulaArgs,double deadline, double dt, int replica) throws StlModelGenerationException {
        return checkForLoadedDefinition().qualitativeMonitoring(this.simulator, rg, formulaName, formulaArgs,deadline, dt, replica);
    }

    @Override
    public Map<String, double[][]> quantitativeMonitoring(RandomGenerator rg, String[] formulaName, Map<String, Double>[] formulaArgs, double deadline, double dt, int replica) throws StlModelGenerationException {
        return checkForLoadedDefinition().quantitativeMonitoring(this.simulator, rg, formulaName, formulaArgs, deadline,dt, replica);
    }

    @Override
    public double meanRobustnessAtTime0( RandomGenerator rg, String formulaName, Map<String, Double> formulaParameters, int replica) throws StlModelGenerationException {
        return checkForLoadedDefinition().meanRobustnessAtTime0(this.simulator, rg, formulaName, formulaParameters, replica);
    }

    @Override
    public double[] meanAndSdRobustnessAtTime0(RandomGenerator rg, String formulaName,Map<String, Double> formulaParameters, int replica) throws StlModelGenerationException {
        return checkForLoadedDefinition().meanAndSdRobustnessAtTime0(this.simulator, rg, formulaName, formulaParameters, replica);
    }

    @Override
    public double expectedProbabilityAtTime0( RandomGenerator rg, String formulaName, Map<String, Double> formulaParameters, int replica) throws StlModelGenerationException {
        return checkForLoadedDefinition().expectedProbabilityAtTime0(this.simulator, rg, formulaName, formulaParameters, replica);
    }

    @Override
    public Map<String, Map<String, ToDoubleFunction<Map<String, Double>>>> getFormulaMonitors() throws StlModelGenerationException {
        return checkForLoadedDefinition().getFormulaMonitors();
    }

}