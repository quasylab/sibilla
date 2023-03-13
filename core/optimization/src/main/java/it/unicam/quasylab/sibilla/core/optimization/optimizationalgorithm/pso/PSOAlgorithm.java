package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.pso;

import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.OptimizationAlgorithm;
import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.OptimizationTask;

public class PSOAlgorithm implements OptimizationAlgorithm {
    @Override
    public OptimizationTask getOptimizationTask() {
        return new PSOTask();
    }
}
