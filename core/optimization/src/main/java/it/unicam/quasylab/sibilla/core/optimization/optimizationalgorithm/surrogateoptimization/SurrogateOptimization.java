package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.surrogateoptimization;

import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.OptimizationStrategy;
import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.OptimizationStrategyFactory;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.Interval;
import it.unicam.quasylab.sibilla.core.optimization.surrogate.Surrogate;
import it.unicam.quasylab.sibilla.core.optimization.surrogate.SurrogateFactory;
import it.unicam.quasylab.sibilla.core.optimization.surrogate.TrainingSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Surrogate optimization attempts to find a global minimum of an objective function using
 * few objective function evaluations.
 * A surrogate of the function is constructed using
 * few objective function evaluations, then the optimization algorithm is
 * applied to the surrogate
 *
 * @author Lorenzo Matteucci
 */
public class SurrogateOptimization implements OptimizationStrategy {
    final private String surrogateName;
    final private String optimizationName;
    final private String samplingName;
    final private int trainingSetSize;
    final private Function<Map<String,Double>,Double> functionToBeSurrogate;
    final private List<Predicate<Map<String,Double>>> constraints;
    final private HyperRectangle searchSpace;
    final private Properties properties;


    public SurrogateOptimization(Function<Map<String,Double>,Double> functionToBeSurrogate,
                                 List<Predicate<Map<String,Double>>> constraints,
                                 HyperRectangle searchSpace,
                                 Properties properties){
        this(
                properties.getProperty("surrogate.optimization.surrogate.name","rfr"),
                properties.getProperty("surrogate.optimization.optimization.name","pso"),
                properties.getProperty("surrogate.optimization.sampling.name","lhs"),
                Integer.parseInt(properties.getProperty("surrogate.optimization.training.set.size","1000")),
                functionToBeSurrogate,
                constraints,
                searchSpace,
                properties
        );
       }

    public SurrogateOptimization(String surrogateName,
                                 String optimizationName,
                                 String samplingName,
                                 int trainingSetSize,
                                 Function<Map<String, Double>, Double> functionToBeSurrogate,
                                 List<Predicate<Map<String, Double>>> constraints,
                                 HyperRectangle searchSpace,
                                 Properties properties) {
        this.surrogateName = surrogateName;
        this.optimizationName = optimizationName;
        this.samplingName = samplingName;
        this.trainingSetSize = trainingSetSize;
        this.functionToBeSurrogate = functionToBeSurrogate;
        this.constraints = constraints;
        this.searchSpace = searchSpace;
        this.properties = properties;
    }


    private Function<Map<String,Double>,Double> generateSurrogateFunction(){
        TrainingSet ts = new TrainingSet(this.searchSpace,this.samplingName,this.trainingSetSize,this.functionToBeSurrogate);
        Surrogate surrogate = SurrogateFactory.getSurrogate(this.surrogateName,this.properties);
        surrogate.fit(ts);
        return map -> surrogate.predict(map.values().toArray(new Double[0]));
    }

    @Override
    public Map<String, Double> minimize() {
        return OptimizationStrategyFactory.getOptimizationStrategy(
                this.optimizationName,
                this.generateSurrogateFunction(),
                this.constraints,
                this.searchSpace,
                this.properties)
                .minimize();
    }

    @Override
    public Map<String, Double> maximize() {
        return OptimizationStrategyFactory.getOptimizationStrategy(
                        this.optimizationName,
                        this.generateSurrogateFunction(),
                        this.constraints,
                        this.searchSpace,
                        this.properties)
                .maximize();
    }

    public void setSearchSpaceAsConstraints(){
        List<Predicate<Map<String,Double>>> constraints = new ArrayList<>();
        for (Interval i :searchSpace.getIntervals()) {
            constraints.add( map -> i.getLowerBound() <= map.get(i.getId()) && map.get(i.getId()) <= i.getUpperBound() );
        }
        this.constraints.addAll(constraints);
    }


}
