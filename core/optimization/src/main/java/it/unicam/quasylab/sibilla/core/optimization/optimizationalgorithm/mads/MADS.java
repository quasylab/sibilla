package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads;

import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.OptimizationTask;

@SuppressWarnings("all")
//TODO
public interface MADS {
    void initialisation();

    void search();

    void poll();

    void  termination();
}
