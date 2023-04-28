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

import it.unicam.quasylab.sibilla.core.models.ModelDefinition;
import it.unicam.quasylab.sibilla.core.simulator.SimulationEnvironment;
import it.unicam.quasylab.sibilla.core.simulator.SimulationMonitor;
import it.unicam.quasylab.sibilla.core.simulator.sampling.FirstPassageTimeResults;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public abstract class AbstractSibillaModule implements SibillaModule {

    protected ModuleEngine<?> moduleEngine;
    private Set<String> enabledMeasures;
    private boolean summary = true;
    private final SimulationEnvironment simulator = new SimulationEnvironment();

    private Configuration currentConfiguration;


    protected final void setModelDefinition(ModelDefinition<?> moduleEngine) {
        this.moduleEngine = new ModuleEngine<>(moduleEngine);
        this.clear();
    }

    private void checkForLoadedDefinition() {
        if (moduleEngine == null) {
            throw new IllegalStateException("No model has been loaded!");
        }
    }

    @Override
    public void setParameter(String name, double value) {
        checkForLoadedDefinition();
        this.moduleEngine.setParameter(name, value);
    }

    @Override
    public double getParameter(String name) {
        checkForLoadedDefinition();
        return this.moduleEngine.getParameter(name);
    }

    @Override
    public String[] getParameters() {
        checkForLoadedDefinition();
        return this.moduleEngine.getParameters();
    }

    @Override
    public Map<String, Double> getEvaluationEnvironment() {
        checkForLoadedDefinition();
        return this.moduleEngine.getEnvironment();
    }

    @Override
    public void addMeasure(String name) {
        checkForLoadedDefinition();
        this.enabledMeasures.add(name);
    }

    @Override
    public void removeMeasure(String name) {
        this.enabledMeasures.remove(name);
    }

    @Override
    public void addAllMeasures() {
        checkForLoadedDefinition();
        this.enabledMeasures.addAll(List.of(moduleEngine.getMeasures()));
    }

    @Override
    public void removeAllMeasures() {
        this.enabledMeasures = new TreeSet<>();
    }

    @Override
    public Map<String, double[][]> simulate(SimulationMonitor monitor, RandomGenerator rg, long replica, double deadline, double dt) {
        checkForLoadedDefinition();
        return moduleEngine.simulate(this.simulator, monitor, rg, replica, deadline, dt, this.enabledMeasures.toArray(new String[0]),summary);
    }

    @Override
    public FirstPassageTimeResults firstPassageTime(SimulationMonitor monitor, RandomGenerator rg, long replica, double deadline, double dt, String predicateName) {
        checkForLoadedDefinition();
        return moduleEngine.firstPassageTime(simulator, monitor, rg, replica, deadline, dt, predicateName);
    }

    @Override
    public double estimateReachability(SimulationMonitor monitor, RandomGenerator rg,  String targetCondition, double time, double pError, double delta) {
        checkForLoadedDefinition();
        return moduleEngine.estimateReachability(simulator, monitor, rg, targetCondition, time, pError, delta);
    }

    @Override
    public double estimateReachability(SimulationMonitor monitor, RandomGenerator rg, String transientCondition, String targetCondition, double time, double pError, double delta) {
        checkForLoadedDefinition();
        return moduleEngine.estimateReachability(simulator, monitor, rg, transientCondition, targetCondition, time, pError, delta);
    }

    @Override
    public boolean isEnabledMeasure(String name) {
        return this.enabledMeasures.contains(name);
    }

    @Override
    public void clear() {
        if (this.moduleEngine != null) {
            this.moduleEngine.clear();
        }
        this.enabledMeasures = new TreeSet<>();
    }

    @Override
    public void reset() {
        checkForLoadedDefinition();
        this.moduleEngine.reset();
    }

    @Override
    public void reset(String name) {
        checkForLoadedDefinition();
        moduleEngine.reset(name);
    }

    @Override
    public String[] getInitialConfigurations() {
        checkForLoadedDefinition();
        return this.moduleEngine.getInitialConfigurations();
    }



    @Override
    public String getConfigurationInfo(String name) {
        checkForLoadedDefinition();
        return this.moduleEngine.getConfigurationInfo(name);
    }

    @Override
    public boolean setConfiguration(String name, double... args) {
        this.currentConfiguration = new Configuration(name,args);
        return moduleEngine.setConfiguration(name, args);
    }

    @Override
    public Configuration getCurrentConfiguration() {
        return currentConfiguration;
    }

    @Override
    public String[] getMeasures() {
        checkForLoadedDefinition();
        return moduleEngine.getMeasures();
    }

    @Override
    public String[] getPredicates() {
        checkForLoadedDefinition();
        return moduleEngine.getPredicates();
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
