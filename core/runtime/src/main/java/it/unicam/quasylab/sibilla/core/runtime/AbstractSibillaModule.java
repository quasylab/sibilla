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

import it.unicam.quasylab.sibilla.core.simulator.SimulationEnvironment;
import it.unicam.quasylab.sibilla.core.simulator.SimulationManagerFactory;
import it.unicam.quasylab.sibilla.core.simulator.SimulationMonitor;
import it.unicam.quasylab.sibilla.core.simulator.sampling.FirstPassageTimeResults;
import it.unicam.quasylab.sibilla.core.util.SimulationData;
import it.unicam.quasylab.sibilla.core.util.values.SibillaDouble;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public abstract class AbstractSibillaModule implements SibillaModule {

    private Set<String> enabledMeasures = new TreeSet<>();
    private boolean summary = true;
    private final SimulationEnvironment simulator = new SimulationEnvironment();

    private Configuration currentConfiguration;

    protected abstract ModuleEngine<?> getModuleEngine();

    private ModuleEngine<?> checkForLoadedDefinition() {
        ModuleEngine<?> engine = getModuleEngine();
        if (engine == null) {
            throw new IllegalStateException("No model has been loaded!");
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
    public FirstPassageTimeResults firstPassageTime(SimulationMonitor monitor, RandomGenerator rg, long replica, double deadline, double dt, String predicateName) {
        return checkForLoadedDefinition().firstPassageTime(simulator, monitor, rg, replica, deadline, dt, predicateName);
    }

    @Override
    public double estimateReachability(SimulationMonitor monitor, RandomGenerator rg,  String targetCondition, double time, double pError, double delta) {
        return checkForLoadedDefinition().estimateReachability(simulator, monitor, rg, targetCondition, time, pError, delta);
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

}