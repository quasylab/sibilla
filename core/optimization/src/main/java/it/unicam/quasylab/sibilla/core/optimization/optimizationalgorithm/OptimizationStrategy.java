package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm;
import java.util.Map;

public interface OptimizationStrategy {
    Map<String,Double> minimize();
    Map<String,Double> maximize();
}
