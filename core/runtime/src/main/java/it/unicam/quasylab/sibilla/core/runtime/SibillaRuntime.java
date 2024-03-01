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

import it.unicam.quasylab.sibilla.core.simulator.DefaultRandomGenerator;
import it.unicam.quasylab.sibilla.core.simulator.SimulationManagerFactory;
import it.unicam.quasylab.sibilla.core.simulator.SimulationMonitor;
import it.unicam.quasylab.sibilla.core.simulator.sampling.FirstPassageTimeResults;
import it.unicam.quasylab.sibilla.core.util.SimulationData;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import it.unicam.quasylab.sibilla.tools.tracing.TraceSpecificationEvaluator;
import it.unicam.quasylab.sibilla.tools.tracing.TracingData;
import it.unicam.quasylab.sibilla.tools.tracing.TracingFunction;
import org.apache.commons.math3.random.RandomGenerator;
import tech.tablesaw.api.Table;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public final class SibillaRuntime {

    private static final String UNKNOWN_MODULE_MESSAGE = "Module %s is unknown!";
    private static final String NO_MODULE_HAS_BEEN_LOADED =  "No module has been loaded!";
    private final Map<String,SibillaModule> moduleIndex = new TreeMap<>();
    private SibillaModule currentModule;
    private final Map<String, Map<String,double[][]>> simulations = new TreeMap<>();
    private Map<String,double[][]> lastSimulation;
    private final RandomGenerator rg = new DefaultRandomGenerator();
    private long replica = 1;
    private double deadline = Double.NaN;
    private double dt = Double.NaN;

    public SibillaRuntime() {
        initModules();
    }

    private void initModules() {
        for (SibillaModule m: SibillaModule.MODULES) {
            moduleIndex.put(m.getModuleName(),m);
            if (currentModule == null) {
                currentModule =  m;
            }
        }
        optimizationModule = new OptimizationModule();
    }

    /**
     * Return an array with the names of all enabled modules.
     *
     * @return an array with the names of all enabled modules.
     */
    public String[] getModules() {
        return moduleIndex.keySet().stream().sorted().toArray(String[]::new);
    }


    /**
     * Load the module with the given name.
     */
    public void loadModule(String name) throws CommandExecutionException {
        SibillaModule module = moduleIndex.get(name);
        if (module == null) {
            throw new CommandExecutionException(String.format(UNKNOWN_MODULE_MESSAGE,name));
        }
        this.currentModule.clear();
        this.currentModule = module;
    }

    /**
     * Load a specification from file.
     *
     * @param file specification file.
     */
    public void load(File file) throws CommandExecutionException {
        currentModule.load(file);
    }

    /**
     * Loads a specification from the give file.
     *
     * @param fileName file name.
     * @throws CommandExecutionException
     */
    public void loadFromFile(String fileName) throws CommandExecutionException {
        currentModule.load(new File(fileName));
    }

    /**
     * Load a specification from a string.
     *
     * @param code specificaiton code.
     */
    public void load(String code) throws CommandExecutionException {
        currentModule.load(code);
    }

    /**
     * Return info about the module state.
     *
     * @return info about the module state.
     */
    public String info() {
        return currentModule.info();
    }

    /**
     * Set a parameter to a given value.
     *
     * @param name parameter name.
     * @param value parameter value.
     */
    public void setParameter(String name, double value) {
        currentModule.setParameter(name, value);
    }

    /**
     * Return the value of a given parameter.
     *
     * @param name parameter name.
     * @return the value of the given parameter.
     */
    public double getParameter(String name) {
        return currentModule.getParameter(name);
    }

    /**
     * Get the array of current model parameter.
     *
     * @return the array of current model parameter.
     */
    public String[] getParameters() {
        return currentModule.getParameters();
    }

    /**
     * Return the EvaluationEnvironment of the current model.
     *
     * @return the EvaluationEnvironment of the current model.
     */
    public Map<String, SibillaValue> getEvaluationEnvironment() {
        return currentModule.getEvaluationEnvironment();
    }

    /**
     * Cancels all the data loaded in the module.
     */
    public void clear() {
        currentModule.clear();
    }

    /**
     * Reset all the parameters to their default value.
     */
    public void reset() {
        currentModule.reset();
    }

    /**
     * Reset the given parameters to its default value.
     */
    public void reset(String name) {
        currentModule.reset(name);
    }

    /**
     * Return the array with the names of the initial configurations available in the
     * current module.
     *
     * @return the array with the names of the initial configurations available in the
     * current module.
     */
    public String[] getInitialConfigurations() {
        return currentModule.getInitialConfigurations();
    }


    /**
     * Return a string providing info about the given configuration.
     *
     * @param name configuration name.
     * @return a string providing info about the given configuration.
     */
    public String getConfigurationInfo(String name) {
        return currentModule.getConfigurationInfo(name);
    }

    /**
     * Set initial configuration for simulations and property checking.
     *
     * @param name configuration name.
     * @param args configuration parameters.
     * @return true or false depending if the configuration has been successfully created.
     */
    public boolean setConfiguration(String name, double ... args) throws CommandExecutionException {
        checkLoadedModule();
        return currentModule.setConfiguration(name,args);
    }

    private void checkLoadedModule() throws CommandExecutionException {
        if (currentModule == null) {
            throw new CommandExecutionException(NO_MODULE_HAS_BEEN_LOADED);
        }
    }


    /**
     * Return the array of measures defined in the module.
     *
     * @return the array of measures defined in the module.
     */
    public String[] getMeasures() {
        return currentModule.getMeasures();
    }

    public boolean isEnabledMeasure(String name) {
        return currentModule.isEnabledMeasure(name);
    }

    /**
     * Set the measures to sample in a simulation.
     *
     * @param measures the measures to sample.
     */
    public void setMeasures(String ... measures) {
        currentModule.setMeasures(measures);
    }

    /**
     * Add a measure to the ones collected in simulation.
     *
     * @param name measure name.
     */
    public void addMeasure(String name) {
        currentModule.addMeasure(name);
    }

    /**
     * Remove a measure from the ones collected in simulation.
     *
     * @param name measure name.
     */
    public void removeMeasure(String name) {
        currentModule.removeMeasure(name);
    }

    /**
     * Add all the available measures to the ones collected in simulation.
     */
    public void addAllMeasures() {
        currentModule.addAllMeasures();
    }

    /**
     * Remove all measures from the ones collected from simulation.
     */
    public void removeAllMeasures() {
        currentModule.removeAllMeasures();
    }

    /**
     * Run a simulation and save results with the given label.
     */
    public Map<String, double[][]> simulate(SimulationMonitor monitor, String label) throws CommandExecutionException {
        checkDeadline();
        checkDt();
        lastSimulation = currentModule.simulate(monitor,rg,replica,deadline,dt);
        if (label != null) {
            simulations.put(label, lastSimulation);
        }
        return lastSimulation;
    }

    /**
     * Use descriptive statistics.
     */
    public void useDescriptiveStatistics() {
        this.currentModule.setSummaryStatistics(false);
    }

    /**
     * Use summary statistics.
     */
    public void useSummaryStatistics() {
        this.currentModule.setSummaryStatistics(true);
    }

    /**
     * Return true if a descriptive statistics is used.
     *
     * @return true if a descriptive statistics is used.
     */
    public boolean isDescriptiveStatistics() {
        return !this.currentModule.isSummaryStatistics();
    }

    /**
     * Return true if a summary statistics is used.
     *
     * @return true if a summary statistics is used.
     */
    public boolean isSummaryStatistics() {
        return this.currentModule.isSummaryStatistics();
    }

    /**
     * Return a string that describes the kind of used statistics.
     *
     * @return a string that describes the kind of used statistics.
     */
    public String getStatistics() {
       if (this.currentModule.isSummaryStatistics()) {
           return "summary";
       } else {
           return "descriptive";
       }
    }

    /**
     * Run a simulation and save results with the given label.
     * @return
     */
    public Map<String, double[][]> simulate(String label) throws CommandExecutionException {
        return simulate(null,label);
    }

    private void checkDt() throws CommandExecutionException {
        if (Double.isNaN(dt)) {
            throw new CommandExecutionException("No sampling time has been set!");
        }
    }

    private void checkDeadline() throws CommandExecutionException {
        if (Double.isNaN(deadline)) {
            throw new CommandExecutionException("No simulation deadline has been set!");
        }
    }

    private void checkReplica() throws CommandExecutionException {
        if (replica<1) {
            throw new CommandExecutionException("No number of replicas has been set!");
        }
    }

    /**
     * Set simulation deadline.
     *
     * @param deadline simulation deadline.
     * @throws CommandExecutionException when the deadline is a non positive value.
     */
    public void setDeadline(double deadline) throws CommandExecutionException {
        if (deadline<=0) {
            throw new CommandExecutionException("Simulation deadline must be a positive value!");
        }
        this.deadline = deadline;
    }

    /**
     * Return current simulation deadline.
     *
     * @return current simulation deadline.
     */
    public double getDeadline() {
        return deadline;
    }

    /**
     * Set sampling time.
     *
     * @param dt sampling time.
     * @throws CommandExecutionException when the given sampling time is a non positive value.
     */
    public void setDt(double dt) throws CommandExecutionException {
        if (dt<=0) {
            throw new CommandExecutionException("Sampling time must be a positive value!");
        }
        this.dt = dt;
    }

    /**
     * Return the module modes.
     *
     * @return the module modes.
     */
    public String[] getModes() {
        return currentModule.getModes();
    }

    /**
     * Set module mode.
     *
     * @param name mode name.
     */
    public void setMode(String name) {
        currentModule.setMode(name);
    }

    /**
     * Return the current module mode.
     *
     * @return the current module mode.
     */
    public String getMode() {
        return currentModule.getMode();
    }

    /**
     * Set a seed for the rundom generator.
     *
     * @param seed the seed to use with the random generator.
     */
    public void setSeed(long seed) {
        this.rg.setSeed(seed);
    }

    /**
     * Generate and set a new seed that can be used to replicate experiments.
     *
     * @return a new generated seed.
     */
    public long getSeed() {
        long seed = rg.nextLong();
        setSeed(seed);
        return seed;
    }

    /**
     * Save last result to a given folder. Data files, in CSV format, are stored
     * in the given output format. The name of each save file, having extension .csv,
     * consists of the string prefix, the name of the series, and the postfix.
     *
     * @param outputFolder folder where data are saved
     * @param prefix prefix of generated file names
     * @param postfix postfix of generate file names
     * @throws IOException if an error while saving data occurs
     * @throws CommandExecutionException if an error occurred while executing this method
     */
    public void save(String outputFolder, String prefix, String postfix) throws IOException, CommandExecutionException {
        CSVWriter writer = new CSVWriter(outputFolder, prefix, postfix);
        if (lastSimulation != null) {
            writer.write(lastSimulation);
        } else {
            throw new CommandExecutionException("No simulation is available!");
        }
    }



    /**
     * Save the sample set to a given folder. Data files, in CSV format, are stored
     * in the given output format. The name of each save file, having extension .csv,
     * consists of the string prefix, the name of the series, and the postfix.
     *
     * @param outputFolder folder where data are saved
     * @param prefix prefix of generated file names
     * @param postfix postfix of generate file names
     * @throws IOException if an error while saving data occurs
     * @throws CommandExecutionException if an error occurred while executing this method
     */
    public void saveTable(String name, String outputFolder, String prefix, String postfix) throws IOException, CommandExecutionException {
        if (name == null) { name = "samples"; }
        if (outputFolder == null) { outputFolder = System.getProperty("user.dir"); }
        if (prefix == null) { prefix = ""; }
        if (postfix == null) { postfix = ""; }
        CSVWriter writer = new CSVWriter(outputFolder, prefix, postfix);
        if (this.optimizationModule.getTrainingSet() != null) {
            writer.write(name,this.optimizationModule.getTrainingSet());
        } else {
            throw new CommandExecutionException("No sample is available!");
        }
    }

    /**
     * Saves the results associated with the given label to the given folder. Data files, in CSV format, are stored
     * in the given output format. The name of each save file, having extension .csv,
     * consists of the string prefix, the name of the series, and the postfix.
     *
     * @param outputFolder folder where data are saved
     * @param prefix prefix of generated file names
     * @param postfix postfix of generate file names
     * @throws IOException if an error while saving data occurs
     * @throws CommandExecutionException if an error occurred while executing this method
     */
    public void save(String label, String outputFolder, String prefix, String postfix) throws IOException, CommandExecutionException {
        if (outputFolder == null) { outputFolder = System.getProperty("user.dir"); }
        if (prefix == null) { prefix = ""; }
        if (postfix == null) { postfix = ""; }
        if (label == null) {
            save(outputFolder,prefix,postfix);
        } else {
            Map<String, double[][]> series = this.simulations.get(label);
            if (series != null) {
                CSVWriter writer = new CSVWriter(outputFolder, prefix, postfix);
                writer.write(series);
            } else {
                throw new CommandExecutionException("Simulation "+label+" is unknown!");
            }
        }
    }

    /**
     * Set the number of replications.
     *
     * @param replica the number of replications.
     */
    public void setReplica(long replica) {
        this.replica = replica;
    }

    public double getDt() {
        return dt;
    }

    public long getReplica() {
        return replica;
    }

    public String printData(String label) {
        Map<String, double[][]> series = this.simulations.get(label);
        if (series != null) {
            StringWriter sw = new StringWriter();
            for (Map.Entry<String, double[][]> ts: series.entrySet()) {
                sw.write(ts.getKey()+"\n");
                sw.write(CSVWriter.getCSVString(ts.getValue()));
            }
            return sw.getBuffer().toString();
        } else {
            return "";
        }
    }

    public String[] getPredicates() {
        return currentModule.getPredicates();
    }

    public FirstPassageTimeResults firstPassageTime(SimulationMonitor monitor, String predicateName) throws CommandExecutionException {
        checkDeadline();
        checkReplica();
        return currentModule.firstPassageTime(monitor,rg,replica,deadline,dt,predicateName);
    }

    public double computeProbReach(SimulationMonitor monitor, String goal, double alpha, double eps) throws CommandExecutionException {
        checkDeadline();
        return currentModule.estimateReachability(monitor, rg, goal, deadline, alpha, eps);
    }

    public double computeProbReach(SimulationMonitor monitor, String condition, String goal, double alpha, double eps) throws CommandExecutionException {
        checkDeadline();
        return currentModule.estimateReachability(monitor, rg, condition, goal, deadline, alpha, eps);
    }


    public void load(URL resource) throws CommandExecutionException {
        try {
            load(new File(resource.toURI()));
        } catch (URISyntaxException e) {
            throw new CommandExecutionException(e.getMessage());
        }
    }
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    //  - - - - - - -  O P T I M I Z A T I O N   S E C T I O N  - - - - - - - - - - - - - -
    //  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    private OptimizationModule optimizationModule;


    public void setOptimizationStrategy(String name) throws CommandExecutionException {
        try {
            this.optimizationModule.setOptimizationStrategy(name);
        }catch (IllegalArgumentException e){
            throw new CommandExecutionException(String.format(e.getMessage(),name));
        }
    }

    public void setSurrogateStrategy(String name) throws CommandExecutionException {
        try {
            this.optimizationModule.setSurrogateStrategy(name);
        }catch (IllegalArgumentException e){
            throw new CommandExecutionException(String.format(e.getMessage(),name));
        }
    }

    public void usingSurrogate(boolean usingSurrogate){
        this.optimizationModule.useASurrogateFunction(usingSurrogate);
    }


    public void setSurrogateProperty(String key, String value) throws CommandExecutionException {
        try {
            this.optimizationModule.setSurrogateProperty(key,value);
        }catch (IllegalArgumentException e){
            throw new CommandExecutionException(String.format(e.getMessage(),key));
        }
    }

    public void setOptimizationProperty(String key, String value) throws CommandExecutionException {
        try {
            this.optimizationModule.setOptimizationProperty(key,value);
        }catch (IllegalArgumentException e){
            throw new CommandExecutionException(String.format(e.getMessage(),key));
        }
    }

    public void addSpaceInterval(String variableName, double lowerBound, double upperBound){
        this.optimizationModule.addInterval(variableName,lowerBound,upperBound);
    }

    public void addConstraint(Predicate<Map<String,Double>> predicate){
        this.optimizationModule.addConstraint(predicate);
    }

    public void setDataSetSize(int dataSetSize){
        this.optimizationModule.setDataSetSize(dataSetSize);
    }

    public void setTrainingSetProportion(double proportion){
        this.optimizationModule.setTrainingDatasetPortion(proportion);
    }

    public void setSamplingStrategy(String samplingName){
        this.optimizationModule.setSamplingStrategy(samplingName);
    }


    public void setOptimizationAsMinimization(boolean isMinimization){
        this.optimizationModule.isAMinimizationProblem(isMinimization);
    }



    public void setProbReachAsObjectiveFunction(SimulationMonitor monitor, String goal, double alpha, double eps){
        this.optimizationModule.setObjectiveFunction(
                map -> {
                    map.keySet().forEach(key -> this.setParameter(key,map.get(key)));
                    this.addAllMeasures();
                    //System.out.println("testing for parameters : " + map);
                    try {
                        this.setConfiguration(this.currentModule.getCurrentConfiguration().name(),this.currentModule.getCurrentConfiguration().args());
                        return this.computeProbReach(monitor,goal,alpha,eps);
                    } catch (CommandExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    public void setProbReachAsObjectiveFunctionWithCondition(SimulationMonitor monitor,String condition, String goal, double alpha, double eps){
        this.optimizationModule.setObjectiveFunction(
                map -> {
                    map.keySet().forEach(key -> this.setParameter(key,map.get(key)));
                    this.addAllMeasures();
                    try {
                        this.setConfiguration(this.currentModule.getCurrentConfiguration().name(),this.currentModule.getCurrentConfiguration().args());
                        return this.computeProbReach(monitor,condition,goal,alpha,eps);
                    } catch (CommandExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    public void setFirstPassageTimeAsObjectiveFunction(SimulationMonitor monitor, String predicateName){
        this.optimizationModule.setObjectiveFunction(
            map -> {
                map.keySet().forEach(key -> this.setParameter(key,map.get(key)));
                this.addAllMeasures();
                try {
                    return this.firstPassageTime(monitor,predicateName).getMean();
                } catch (CommandExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        );
    }

    public void setObjectiveFunction(ToDoubleFunction<Map<String,Double>> function){
        this.optimizationModule.setObjectiveFunction(function);
    }

    public void performOptimization() {
        this.optimizationModule.performOptimization();
    }

    public String getOptimizationInfo(){
        return this.optimizationModule.info();
    }

    public String getTrainingSetInfo(){ return this.optimizationModule.infoTrainingSet(); }

    public void generateTrainingSet(){
        this.optimizationModule.generateTrainingSet();
    }
    public Table getTrainingSet() {
        return this.optimizationModule.getTrainingSet();
    }
    public void resetOptimizationSettings(){ this.optimizationModule.initialise();}

    public Map<String,Double> getOptimalSolution(){
        return this.optimizationModule.getOptimalCoordinates();
    }

    public double evaluateObjectiveFunction(Map<String,Double> value){
        return this.optimizationModule.evaluateObjectiveFunction(value);
    }
    public double evaluateSurrogateFunction(Map<String,Double> value){
        return this.optimizationModule.evaluateSurrogateFunction(value);
    }


    public void setSimulationManagerFactory(SimulationManagerFactory factory) throws CommandExecutionException {
        checkLoadedModule();
        this.currentModule.setSimulationManagerFactory(factory);
    }

    public void loadStlFormulas() {

    }


    public void trace(String outputFolder, boolean header) throws CommandExecutionException, IOException {
        checkLoadedModule();
        checkDeadline();
        CSVWriter writer = new CSVWriter(outputFolder);
        List<SimulationData> trace = this.currentModule.trace(this.rg, this.deadline);
        for (SimulationData simulationData : trace) {
            writer.write(simulationData, header);
        }
    }

    public void trace(String tracingSpecification, String outputFolder, boolean header) throws CommandExecutionException, IOException {
        trace(new File(tracingSpecification), outputFolder, header);
    }

    public void trace(URL tracingSpecification, String outputFolder, boolean header) throws CommandExecutionException, IOException, URISyntaxException {
        trace(new File(tracingSpecification.toURI()), outputFolder, header);
    }

    public void trace(File tracingSpecification, String outputFolder, boolean header) throws CommandExecutionException, IOException {
        checkLoadedModule();
        checkDeadline();
        TracingFunction tracingFunction = TraceSpecificationEvaluator.load(tracingSpecification);
        CSVWriter writer = new CSVWriter(outputFolder);
        Map<String, List<TracingData>> trace = this.currentModule.trace(this.rg, tracingFunction, this.deadline);
        for (Map.Entry<String, List<TracingData>> simulationData : trace.entrySet()) {
            writer.write(simulationData.getKey(), simulationData.getValue(), header);
        }
    }
}
