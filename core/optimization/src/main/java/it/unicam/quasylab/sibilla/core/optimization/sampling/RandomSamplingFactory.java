package it.unicam.quasylab.sibilla.core.optimization.sampling;

public class RandomSamplingFactory implements SamplingFactory {
    @Override
    public SamplingTask getSamplingTask() {
        return new RandomSamplingTask();
    }
}
