package it.unicam.quasylab.sibilla.core.optimization.sampling;

public class LatinHyperCubeSamplingFactory implements SamplingFactory {
    @Override
    public SamplingTask getSamplingTask() {
        return new LatinHyperCubeSamplingTask();
    }
}
