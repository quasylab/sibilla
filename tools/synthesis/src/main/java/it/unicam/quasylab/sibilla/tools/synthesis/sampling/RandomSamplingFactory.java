package it.unicam.quasylab.sibilla.tools.synthesis.sampling;

public class RandomSamplingFactory implements SamplingFactory {
    @Override
    public SamplingTask getSamplingTask() {
        return new RandomSamplingTask();
    }
}
