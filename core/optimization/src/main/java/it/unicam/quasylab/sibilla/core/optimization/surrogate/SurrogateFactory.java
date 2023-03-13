package it.unicam.quasylab.sibilla.core.optimization.surrogate;

import it.unicam.quasylab.sibilla.core.optimization.sampling.SamplingTask;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;

import java.util.Map;
import java.util.Properties;
import java.util.function.ToDoubleFunction;

public interface SurrogateFactory {
    SurrogateModel getSurrogateModel(TrainingSet trainingSet, Properties properties);
    SurrogateModel getSurrogateModel(ToDoubleFunction<Map<String,Double>> functionToBeSurrogate,
                                     SamplingTask samplingTask,
                                     HyperRectangle sampleSpace,
                                     int numberOfSamples,
                                     Properties properties);

}
