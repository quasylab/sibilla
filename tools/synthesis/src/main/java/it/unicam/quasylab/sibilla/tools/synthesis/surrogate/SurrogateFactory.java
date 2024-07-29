package it.unicam.quasylab.sibilla.tools.synthesis.surrogate;

import it.unicam.quasylab.sibilla.tools.synthesis.sampling.SamplingTask;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval.HyperRectangle;

import java.util.Map;
import java.util.Properties;
import java.util.function.ToDoubleFunction;

public interface SurrogateFactory {
    SurrogateModel getSurrogateModel(DataSet dataSet,double trainingPortion, Properties properties);
    SurrogateModel getSurrogateModel(ToDoubleFunction<Map<String,Double>> functionToBeSurrogate,
                                     SamplingTask samplingTask,
                                     HyperRectangle sampleSpace,
                                     int numberOfSamples,
                                     double trainingPortion,
                                     Properties properties);

}
