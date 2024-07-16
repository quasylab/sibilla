package it.unicam.quasylab.sibilla.tools.synthesis.surrogate;

import it.unicam.quasylab.sibilla.tools.synthesis.sampling.SamplingTask;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval.HyperRectangle;

import java.util.Map;
import java.util.Properties;
import java.util.function.ToDoubleFunction;

public class RBFNetwork implements SurrogateFactory {
    @Override
    public SurrogateModel getSurrogateModel(DataSet dataSet, double trainingPortion, Properties properties) {
        return new RBFNetworkModel(dataSet, trainingPortion,properties);
    }

    @Override
    public SurrogateModel getSurrogateModel(ToDoubleFunction<Map<String, Double>> functionToBeSurrogate, SamplingTask samplingTask, HyperRectangle sampleSpace, int numberOfSamples,double trainingPortion, Properties properties) {
        return new RBFNetworkModel(functionToBeSurrogate,samplingTask,sampleSpace,numberOfSamples, trainingPortion,properties);
    }
}
