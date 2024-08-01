package it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.sa;

import it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.OptimizationAlgorithm;
import it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.OptimizationTask;

public class SimulatedAnnealingAlgorithm implements OptimizationAlgorithm {
    @Override
    public OptimizationTask getOptimizationTask() {
        return new SimulatedAnnealingTask();
    }
}
