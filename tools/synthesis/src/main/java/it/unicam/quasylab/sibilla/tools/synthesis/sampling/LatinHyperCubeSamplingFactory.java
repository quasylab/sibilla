package it.unicam.quasylab.sibilla.tools.synthesis.sampling;

public class LatinHyperCubeSamplingFactory implements SamplingFactory {
    @Override
    public SamplingTask getSamplingTask() {
        return new LatinHyperCubeSamplingTask();
    }
}
