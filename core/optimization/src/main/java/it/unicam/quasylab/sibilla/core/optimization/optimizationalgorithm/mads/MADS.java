package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads;

import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.OptimizationStrategy;

public interface MADS extends OptimizationStrategy {
    void initialisation();

    void search();

    void poll();

    void  termination();
}
