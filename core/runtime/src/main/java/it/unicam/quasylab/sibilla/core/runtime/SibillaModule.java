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

import it.unicam.quasylab.sibilla.core.runtime.command.Command;
import it.unicam.quasylab.sibilla.core.runtime.command.CommandHandler;
import it.unicam.quasylab.sibilla.core.runtime.command.CommandResult;
import it.unicam.quasylab.sibilla.core.simulator.SimulationEnvironment;
import it.unicam.quasylab.sibilla.core.simulator.SimulationManagerFactory;
import it.unicam.quasylab.sibilla.core.simulator.SimulationMonitor;
import it.unicam.quasylab.sibilla.core.simulator.sampling.FirstPassageTime;
import it.unicam.quasylab.sibilla.core.util.SimulationData;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import it.unicam.quasylab.sibilla.tools.stl.StlModelGenerationException;
import it.unicam.quasylab.sibilla.tools.tracing.TracingData;
import it.unicam.quasylab.sibilla.tools.tracing.TracingFunction;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;

/**
 * A Module identifies a component used to handle a specific language in Sibilla.
 */
public interface SibillaModule extends CommandHandler {

    List<SibillaModule> MODULES = List.of(new PopulationModelModule(), new LIOModelModule(), new YodaModelModule(), new DataOrientedPopulationModelModule(), new ENBAModule());

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
    Map<String, SibillaValue> getEvaluationEnvironment();

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
     * Get the configuration that was set
     *
     * @return returns the configuration that was set
     */
    Configuration getCurrentConfiguration();

    /**
     * Set initial configuration for simulations and property checking.
     *
     * @param name configuration name.
     * @param args configuration parameters.
     * @return true or false depending if the configuration has been successfully created.
     */
    boolean setConfiguration(String name, double ... args);


    /**
     * Returns the array of measures defined in the module.
     *
     * @return the array of measures defined in the module.
     */
    String[] getMeasures();

    /**
     * Returns the array of predicates defined in the module.
     *
     * @return the array of predicates defined in the module.
     */
    String[] getPredicates();

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
    Map<String, double[][]> simulate(SimulationMonitor monitor, RandomGenerator rg, long replica, double deadline, double dt);


    /**
     * Estimates the first passage time to the given predicate.
     *
     * @param monitor
     * @param rg
     * @param replica
     * @param deadline
     * @param dt
     * @param predicateName
     * @return
     */
    FirstPassageTime firstPassageTime(SimulationMonitor monitor, RandomGenerator rg, long replica, double deadline, double dt, String predicateName);


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

    /**
     * Set the kind of statics of collected data. If the parameter is true, summary statistics is used
     * and in the results of simulation only mean and standard deviation are reported. When the parameter is false,
     * descriptive statistics are used. In this case, for each collected measure are reported min value, max value,
     * first, second and third quartile, mean and standard deviation. Note that, when descriptive statistics is
     * used a larger amount of memory is needed.
     *
     * @param isSummary true if summary statistics are used, false for descriptive ones.
     */
    void setSummaryStatistics(boolean isSummary);


    /**
     * Return true if summary statistics are used and false when descriptive statistics are used.
     *
     * @return true if summary statistics are used and false when descriptive statistics are used.
     */
    boolean isSummaryStatistics();

    /**
     * Estimate the probability to reach a state satisfying the target condition within time units. A statistical
     * model checking algorithm is used that guarantees that the difference between the obtained result and
     * the exact one is greater than delta with a probability that is less or equal to p_error.
     *
     * @param targetCondition name of the condition representing the target state.
     * @param time reaching time.
     * @param pError error probability.
     * @param delta estimation error.
     * @return the probability to reach a state satisfuing the target condition within time units-
     */
    double estimateReachability(SimulationMonitor monitor, RandomGenerator rg,  String targetCondition, double time, double pError, double delta);


    /**
     * Estimate the probability to reach a state satisfying the target condition within time units while only state
     * satisfying the transientCondition are traversed. A statistical
     * model checking algorithm is used that guarantees that the difference between the obtained result and
     * the exact one is greater than delta with a probability that is less or equal to p_error.
     *
     * @param transientCondition name of the condition representing the target state.
     * @param targetCondition name of the condition representing the target state.
     * @param time reaching time.
     * @param pError error probability.
     * @param delta estimation error.
     * @return the probability to reach a state satisfuing the target condition within time units-
     */
    double estimateReachability(SimulationMonitor monitor, RandomGenerator rg, String transientCondition, String targetCondition, double time, double pError, double delta);


    /**
     * Load a set of formulas from the given file.
     *
     * @param file the file containing the formulas to load.
     */
    void loadFormulas(File file) throws CommandExecutionException, IOException, StlModelGenerationException;


    /**
     * Load a set of formulas from the given string.
     *
     * @param code the string containing the set of formulas.
     */
    void loadFormulas(String code) throws CommandExecutionException, StlModelGenerationException;


    /**
     * Sets the factory to use to instantiate the simulation manager. By default, a {@link it.unicam.quasylab.sibilla.core.simulator.SequentialSimulationManager}
     * is used.
     *
     * @param factory the factory to use to instantiate the simulation manager.
     */
    void setSimulationManagerFactory(SimulationManagerFactory factory);

    /**
     * Performs qualitative monitoring using a specified formula.
     *
     * @param rg The random generator used to sample trajectories.
     * @param formulaName The name of the formula used for monitoring.
     * @param formulaArgs The arguments for the formula.
     * @param deadline The deadline.
     * @param dt The time step increment.
     * @param replica The number of replicas to run.
     * @return A map where the key is the formula name and the value is a double[][] array, where:
     *         - results[i][0] is the time step
     *         - results[i][1] is the probability of the formula being satisfied at the time step
     * @throws StlModelGenerationException If there is an error generating the formula monitor.
     */
    Map<String, double[][]> qualitativeMonitoring(RandomGenerator rg, String[] formulaName, Map<String, Double>[] formulaArgs,double deadline, double dt, int replica) throws StlModelGenerationException;
    /**
     * Performs quantitative monitoring using a single formula.
     *
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
    Map<String, double[][]> quantitativeMonitoring(RandomGenerator rg, String[] formulaName, Map<String, Double>[] formulaArgs,double deadline, double dt, int replica) throws StlModelGenerationException;

    /**
     * Calculates the mean robustness at time 0 based on the provided parameters.
     *
     * @param rg The random generator
     * @param formulaName The name of the formula
     * @param formulaParameters The arguments for the formula.
     * @param replica The replica number
     * @return The mean robustness at time 0
     * @throws StlModelGenerationException If an error occurs during STL model generation
     */
    double meanRobustnessAtTime0( RandomGenerator rg, String formulaName, Map<String, Double> formulaParameters, int replica) throws StlModelGenerationException;

    /**
     * Calculate the mean and standard deviation robustness at time 0.
     *
     * @param rg The random generator.
     * @param formulaName The name of the formula.
     * @param formulaParameters The parameters for the formula.
     * @param replica The number of replicas.
     * @return An array containing the mean and standard deviation robustness at time 0.
     * @throws StlModelGenerationException If there is an issue with STL model generation.
     */
    double[] meanAndSdRobustnessAtTime0( RandomGenerator rg, String formulaName, Map<String, Double> formulaParameters, int replica) throws StlModelGenerationException;

    /**
     * Calculates the expected probability at time 0 based on a qualitative monitor.
     *
     * @param rg          the random generator
     * @param formulaName the name of the formula
     * @param formulaParameters The arguments for the formula.
     * @param replica     the number of replicas
     * @return the expected probability at time 0
     * @throws StlModelGenerationException if there is an error generating the STL model
     */
    double expectedProbabilityAtTime0( RandomGenerator rg, String formulaName, Map<String, Double> formulaParameters, int replica) throws StlModelGenerationException;


    /**
     * Returns the formula monitors.
     *
     * The first map uses the formula ID as the key. The second map maps each parameter
     * to its evaluation function.
     *
     * @return a map where the key is the formula ID, and the value is another map
     *         that maps parameter names to their evaluation functions.
     * @throws StlModelGenerationException if there is an error generating the STL model.
     */
    Map<String, Map<String, ToDoubleFunction<Map<String, Double>>>> getFormulaMonitors() throws StlModelGenerationException;

    List<SimulationData> trace(RandomGenerator rg, double deadline);

    Map<String, List<TracingData>> trace(RandomGenerator rg, TracingFunction tracingFunction, double deadline);

}
