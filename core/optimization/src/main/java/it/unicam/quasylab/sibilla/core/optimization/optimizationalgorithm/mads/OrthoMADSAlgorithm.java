package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads;

import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.OptimizationAlgorithm;
import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.OptimizationTask;

public class OrthoMADSAlgorithm implements OptimizationAlgorithm {
    @Override
    public OptimizationTask getOptimizationTask() {
        return new OrthoMADSTask();
    }
}
