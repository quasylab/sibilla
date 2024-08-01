package it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.sa.cooling;

public class LinearCoolingSchedule implements CoolingSchedule {
    private final double coolingRate;

    public LinearCoolingSchedule(double coolingRate) {
        this.coolingRate = coolingRate;
    }

    @Override
    public double cool(double currentTemperature, int currentIteration, int maxIterations) {
        return currentTemperature * (1 - coolingRate);
    }
}
