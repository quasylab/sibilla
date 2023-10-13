package it.unicam.quasylab.sibilla.core.optimization.sampling;

public class FullFactorialSamplingFactory implements SamplingFactory {
    @Override
    public SamplingTask getSamplingTask() {
        return new FullFactorialSamplingTask();
    }
}
