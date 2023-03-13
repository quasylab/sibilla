package it.unicam.quasylab.sibilla.core.optimization.surrogate;

import it.unicam.quasylab.sibilla.core.optimization.sampling.SamplingTask;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;

import java.util.Map;
import java.util.Properties;
import java.util.function.ToDoubleFunction;

public class RBFNetwork implements SurrogateFactory {
    @Override
    public SurrogateModel getSurrogateModel(TrainingSet trainingSet, Properties properties) {
        return new RBFNetworkModel(trainingSet,properties);
    }

    @Override
    public SurrogateModel getSurrogateModel(ToDoubleFunction<Map<String, Double>> functionToBeSurrogate, SamplingTask samplingTask, HyperRectangle sampleSpace, int numberOfSamples, Properties properties) {
        return new RBFNetworkModel(functionToBeSurrogate,samplingTask,sampleSpace,numberOfSamples,properties);
    }
}
