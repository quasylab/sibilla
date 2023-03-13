package it.unicam.quasylab.sibilla.core.optimization.sampling;

public class RandomSamplingAlgorithm implements SamplingAlgorithm{
    @Override
    public SamplingTask getSamplingTask() {
        return new RandomSamplingTask();
    }
}
