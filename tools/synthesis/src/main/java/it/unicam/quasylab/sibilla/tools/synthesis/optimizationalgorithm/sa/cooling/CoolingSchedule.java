package it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.sa.cooling;

public interface CoolingSchedule {
    double cool(double currentTemperature, int currentIteration, int maxIterations);
}
