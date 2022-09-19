package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm;

import it.unicam.quasylab.sibilla.core.optimization.sampling.HyperRectangle;

import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

public interface OptimizationStrategy {
    Map<String,Double> minimize();
    Map<String,Double> maximize();

    //void setOptimizationProblem(Function<Map<String,Double>,Double> functionToOptimize, HyperRectangle searchSpace, Properties properties);
}
