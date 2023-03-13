package it.unicam.quasylab.sibilla.core.runtime;

import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.OptimizationAlgorithmRegistry;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.Interval;
import it.unicam.quasylab.sibilla.core.optimization.surrogate.SurrogateModelRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public class OptimizationModule {


    SibillaRuntime sr;
    private String surrogateName;
    private String optimizationName;
    private String samplingName;
    private int trainingSetSize;
    private Map<String,Function<Map<String, Double>, Double> > functions;
    private Map<String,Predicate<Map<String, Double>>> constraints;
    private Map<String,Interval> intervals;
    private HyperRectangle searchSpace;
    private Properties properties;

    public OptimizationModule(){
        this.functions = new HashMap<>();
        this.constraints = new HashMap<>();
        this.intervals = new HashMap<>();
    }

    public OptimizationModule(SibillaRuntime sr){
        this.sr = sr;
        this.functions = new HashMap<>();
        this.constraints = new HashMap<>();
        this.intervals = new HashMap<>();
    }

    public void setSurrogateName(String surrogateName) {
        for (String name:SurrogateModelRegistry.getInstance().getSurrogates()) {
            if(surrogateName.equals(name)){
                this.surrogateName = surrogateName;
                return;
            }
        }
    }

    public void setOptimizationName(String optimizationName) {
        for (String name:OptimizationAlgorithmRegistry.getInstance().getAlgorithms()) {
            if(optimizationName.equals(name)){
                this.optimizationName = optimizationName;
                return;
            }
        }
    }

    public void setSamplingName(String samplingName) {
        this.samplingName = samplingName;
    }

    public void setTrainingSetSize(int trainingSetSize) {
        this.trainingSetSize = trainingSetSize;
    }

    public void addFunctionToOptimize(String id, Function<Map<String, Double>, Double> functionToOptimize){
        functions.put(id,functionToOptimize);
    }

    public void addConstraints(String id,Predicate<Map<String, Double>> constraint){
        this.constraints.put(id,constraint);
    }

    public void addInterval(Interval interval){
        this.intervals.put(interval.getId(),interval);
    }

    public void setSearchSpace(HyperRectangle searchSpace) {
        this.searchSpace = searchSpace;
    }

    public void setSearchSpace(Interval ... intervals) {
        this.searchSpace = new HyperRectangle(intervals);
    }

    public void addOptimizationAlgorithmProperty(){

    }



}
