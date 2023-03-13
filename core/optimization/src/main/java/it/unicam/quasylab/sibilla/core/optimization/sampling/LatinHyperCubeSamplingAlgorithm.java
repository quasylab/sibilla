package it.unicam.quasylab.sibilla.core.optimization.sampling;

public class LatinHyperCubeSamplingAlgorithm implements SamplingAlgorithm{
    @Override
    public SamplingTask getSamplingTask() {
        return new LatinHyperCubeSamplingTask();
    }
}
