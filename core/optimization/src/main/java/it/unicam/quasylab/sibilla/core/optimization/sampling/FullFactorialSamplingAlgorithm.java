package it.unicam.quasylab.sibilla.core.optimization.sampling;

public class FullFactorialSamplingAlgorithm implements SamplingAlgorithm{
    @Override
    public SamplingTask getSamplingTask() {
        return new FullFactorialSamplingTask();
    }
}
