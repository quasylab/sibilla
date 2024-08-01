package it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.sa.cooling;

public class ExponentialCoolingSchedule implements CoolingSchedule{
    private final double coolingRate;

    public ExponentialCoolingSchedule(double coolingRate) {
        this.coolingRate = coolingRate;
    }

    @Override
    public double cool(double currentTemperature, int currentIteration, int maxIterations) {
        return currentTemperature * Math.exp(-coolingRate * currentIteration);
    }
}
