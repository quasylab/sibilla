package it.unicam.quasylab.sibilla.tools.synthesis.sampling;

public class FullFactorialSamplingFactory implements SamplingFactory {
    @Override
    public SamplingTask getSamplingTask() {
        return new FullFactorialSamplingTask();
    }
}
