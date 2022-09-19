package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.surrogateoptimization;

import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.OptimizationStrategy;
import it.unicam.quasylab.sibilla.core.optimization.sampling.HyperRectangle;
import it.unicam.quasylab.sibilla.core.optimization.sampling.LatinHyperCubeSampling;
import it.unicam.quasylab.sibilla.core.optimization.sampling.SamplingStrategy;
import it.unicam.quasylab.sibilla.core.optimization.surrogate.RandomForestSurrogate;
import it.unicam.quasylab.sibilla.core.optimization.surrogate.Surrogate;
import it.unicam.quasylab.sibilla.core.optimization.surrogate.TrainingSet;
import smile.validation.RegressionMetrics;
import tech.tablesaw.api.Table;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Predicate;

public class SurrogateOptimization implements OptimizationStrategy {

    Function<Map<String,Double>,Double> functionToOptimize;
    List<Predicate<Map<String,Double>>> constraints;
    OptimizationStrategy optimizationStrategy;
    Surrogate surrogate;
    TrainingSet trainingSet;
    SamplingStrategy samplingStrategy;
    RegressionMetrics metrics;

    public SurrogateOptimization(OptimizationStrategy optimizationStrategy,
                                 Surrogate surrogate,
                                 SamplingStrategy samplingStrategy,
                                 int trainingSetSize,
                                 Function<Map<String,Double>, Double> functionToOptimize,
                                 List<Predicate<Map<String,Double>>> constraints,
                                 HyperRectangle searchSpace,
                                 Properties surrogateProprieties,
                                 Properties optimizationAlgorithmProperties){

        if(optimizationStrategy instanceof SurrogateOptimization)
            throw new IllegalArgumentException("cannot use surrogate optimization as an optimization strategy in a surrogate optimization");
        this.functionToOptimize = functionToOptimize;
        this.constraints = constraints;
        this.optimizationStrategy = optimizationStrategy;
        this.trainingSet = new TrainingSet(samplingStrategy.getSampleTable(trainingSetSize, searchSpace), functionToOptimize);
        this.surrogate = surrogate;


    }
    @Override
    public Map<String, Double> minimize() {
        trainSurrogate();
        return null;
    }

    @Override
    public Map<String, Double> maximize() {
        trainSurrogate();
        return null;
    }

    public void trainSurrogate(){
        this.surrogate.fit(trainingSet);
        this.metrics = surrogate.getSurrogateMetrics();
    }
}
