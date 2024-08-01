package it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.sa.cooling;

public class LogarithmicCoolingSchedule implements CoolingSchedule {
    @Override
    public double cool(double currentTemperature, int currentIteration, int maxIterations) {
        return currentTemperature / (1 + Math.log(1 + currentIteration));
    }
}
