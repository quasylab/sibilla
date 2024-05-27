package it.unicam.quasylab.sibilla.core.optimization.surrogate;

import it.unicam.quasylab.sibilla.core.optimization.sampling.SamplingTask;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;

import java.util.Map;
import java.util.Properties;
import java.util.function.ToDoubleFunction;

public class GaussianProcess implements SurrogateFactory{
    @Override
    public SurrogateModel getSurrogateModel(DataSet dataSet, double trainingPortion, Properties properties) {
        return new GaussianProcessModel(dataSet,trainingPortion,properties);
    }

    @Override
    public SurrogateModel getSurrogateModel(ToDoubleFunction<Map<String, Double>> functionToBeSurrogate, SamplingTask samplingTask, HyperRectangle sampleSpace, int numberOfSamples, double trainingPortion, Properties properties) {
        return new GaussianProcessModel(functionToBeSurrogate,samplingTask,sampleSpace,numberOfSamples,trainingPortion,properties);
    }
}