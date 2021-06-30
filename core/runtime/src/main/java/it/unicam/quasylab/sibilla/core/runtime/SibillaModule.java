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

import it.unicam.quasylab.sibilla.core.simulator.SimulationMonitor;
import it.unicam.quasylab.sibilla.core.simulator.sampling.SimulationTimeSeries;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * A Module identifies a component used to handle a specific language in Sibilla.
 */
public interface SibillaModule {

    public final static List<SibillaModule> MODULES = List.of(new PopulationModelModule());

    /**
     * Return the module name.
     *
     * @return the module name.
     */
    String getModuleName();

    /**
     * Load a specification from file.
     *
     * @param file specification file.
     */
    void load(File file) throws CommandExecutionException;

    /**
     * Load a specification from a string.
     *
     * @param code specificaiton code.
     */
    void load(String code) throws CommandExecutionException;

    /**
     * Return info about the module state.
     *
     * @return info about the module state.
     */
    String info();

    /**
     * Set a parameter to a given value.
     *
     * @param name parameter name.
     * @param value parameter value.
     */
    void setParameter(String name, double value);

    /**
     * Return the value of a given parameter.
     *
     * @param name parameter name.
     * @return the value of the given parameter.
     */
    double getParameter(String name);

    /**
     * Get the array of current model parameter.
     *
     * @return the array of current model parameter.
     */
    String[] getParameters();

    /**
     * Return the EvaluationEnvironment of the current model.
     *
     * @return the EvaluationEnvironment of the current model.
     */
    Map<String, Double> getEvaluationEnvironment();

    /**
     * Cancels all the data loaded in the module.
     */
    void clear();

    /**
     * Reset all the parameters to their default value.
     */
    void reset();

    /**
     * Reset the given parameters to its default value.
     */
    void reset(String name);

    /**
     * Return the array with the names of the initial configurations available in the
     * current module.
     *
     * @return the array with the names of the initial configurations available in the
     * current module.
     */
    String[] getInitialConfigurations();


    /**
     * Return a string providing info about the given configuration.
     *
     * @param name configuration name.
     * @return a string providing info about the given configuration.
     */
    String getConfigurationInfo(String name);

    /**
     * Set initial configuration for simulations and property checking.
     *
     * @param name configuration name.
     * @param args configuration parameters.
     * @return true or false depending if the configuration has been successfully created.
     */
    boolean setConfiguration(String name, double ... args);


    /**
     * Return the array of measures defined in the module.
     *
     * @return the array of measures defined in the module.
     */
    String[] getMeasures();

    /**
     * Set the measures to sample in a simulation.
     *
     * @param measures the measures to sample.
     */
    void setMeasures(String ... measures);

    /**
     * Add a measure to the ones collected in simulation.
     *
     * @param name measure name.
     */
    void addMeasure(String name);

    /**
     * Remove a measure from the ones collected in simulation.
     *
     * @param name measure name.
     */
    void removeMeasure(String name);

    /**
     * Add all the available measures to the ones collected in simulation.
     */
    void addAllMeasures();

    /**
     * Remove all measures from the ones collected from simulation.
     */
    void removeAllMeasures();

    /**
     * Execute a simulation task.
     *
     * @param monitor simulation monitor.
     *  @param replica number of simulation runs.
     * @param deadline simulation deadline.
     * @param dt sampling interval.
     * @return
     */
    List<SimulationTimeSeries> simulate(SimulationMonitor monitor, RandomGenerator rg, int replica, double deadline, double dt);

    /**
     * Return the module modes.
     *
     * @return the module modes.
     */
    default String[] getModes() {
        return new String[0];
    }

    /**
     * Set module mode.
     *
     * @param name mode name.
     */
    default void setMode(String name) {}

    /**
     * Return the current module mode.
     *
     * @return the current module mode.
     */
    default String getMode() {
        return null;
    }


    /**
     * Return true if the given measure is enabled.
     *
     * @param name measure name.
     * @return true if the given measure is enabled.
     */
    boolean isEnabledMeasure(String name);
}
