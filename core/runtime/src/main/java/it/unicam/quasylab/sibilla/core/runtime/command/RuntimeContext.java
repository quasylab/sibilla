package it.unicam.quasylab.sibilla.core.runtime.command;

import it.unicam.quasylab.sibilla.core.runtime.OptimizationModule;
import it.unicam.quasylab.sibilla.core.runtime.SibillaModule;
import it.unicam.quasylab.sibilla.core.simulator.DefaultRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

public class RuntimeContext {

    private final Map<String, SibillaModule> moduleIndex = new TreeMap<>();
    private SibillaModule currentModule;
    private final Map<String, Map<String,double[][]>> simulations = new TreeMap<>();
    private Map<String,double[][]> lastSimulation;
    private final RandomGenerator rg = new DefaultRandomGenerator();
    private long replica = 1;
    private double deadline = Double.NaN;
    private double dt = Double.NaN;
    private OptimizationModule optimizationModule;



    protected Map<String, SibillaModule> getModuleIndex() {
        return moduleIndex;
    }

    protected SibillaModule getCurrentModule() {
        return currentModule;
    }

    protected void setCurrentModule(SibillaModule currentModule) {
        this.currentModule = currentModule;
    }

    protected Map<String, Map<String, double[][]>> getSimulations() {
        return simulations;
    }

    protected Map<String, double[][]> getLastSimulation() {
        return lastSimulation;
    }

    protected void setLastSimulation(Map<String, double[][]> lastSimulation) {
        this.lastSimulation = lastSimulation;
    }

    protected RandomGenerator getRg() {
        return rg;
    }

    protected long getReplica() {
        return replica;
    }

    protected void setReplica(long replica) {
        this.replica = replica;
    }

    protected double getDeadline() {
        return deadline;
    }

    protected void setDeadline(double deadline) {
        this.deadline = deadline;
    }

    protected double getDt() {
        return dt;
    }

    protected void setDt(double dt) {
        this.dt = dt;
    }

    protected OptimizationModule getOptimizationModule() {
        return optimizationModule;
    }

    protected void setOptimizationModule(OptimizationModule optimizationModule) {
        this.optimizationModule = optimizationModule;
    }

    /**
     * Return an array with the names of all enabled modules.
     *
     * @return an array with the names of all enabled modules.
     */
    public String[] getModules() {
        return moduleIndex.keySet().stream().sorted().toArray(String[]::new);
    }



}
