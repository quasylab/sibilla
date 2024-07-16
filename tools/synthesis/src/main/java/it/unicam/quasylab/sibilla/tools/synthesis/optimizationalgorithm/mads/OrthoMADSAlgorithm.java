package it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.mads;

import it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.OptimizationAlgorithm;
import it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.OptimizationTask;

public class OrthoMADSAlgorithm implements OptimizationAlgorithm {
    @Override
    public OptimizationTask getOptimizationTask() {
        return new OrthoMADSTask();
    }
}
