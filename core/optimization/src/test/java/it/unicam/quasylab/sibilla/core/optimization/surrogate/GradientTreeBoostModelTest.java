package it.unicam.quasylab.sibilla.core.optimization.surrogate;

import it.unicam.quasylab.sibilla.core.optimization.sampling.LatinHyperCubeSamplingTask;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.ContinuousInterval;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Properties;
import java.util.function.ToDoubleFunction;

class GradientTreeBoostModelTest {


    @Test
    void testTrainingAndPredict(){
        ToDoubleFunction<Map<String,Double>> functionToLearn = (
                stringDoubleMap -> {
                    double x = stringDoubleMap.get("x");
                    double y = stringDoubleMap.get("y");
                    return 7 * ( x * y )/(Math.pow(Math.E,(Math.pow(x,2)+Math.pow(y,2))));
                }
        );
        TrainingSet trainingSet = new TrainingSet(
                new HyperRectangle(
                        new ContinuousInterval("x",-2.0,2.0),
                        new ContinuousInterval("y",-2.0,2.0)
                ),
                new LatinHyperCubeSamplingTask(),
                1000,
                functionToLearn)
                ;
        GradientTreeBoostModel rfr = new GradientTreeBoostModel(trainingSet,new Properties());
        rfr.fit();
        System.out.println(rfr.getInSampleMetrics().toString());

    }




    @Test
    void testComparisonRealAndSurrogate(){
        ToDoubleFunction<Map<String,Double>> functionToLearn = (
                stringDoubleMap -> {
                    double x = stringDoubleMap.get("x");
                    double y = stringDoubleMap.get("y");
                    return 7 * ( x * y )/(Math.pow(Math.E,(Math.pow(x,2)+Math.pow(y,2))));
                }
        );
        TrainingSet trainingSet = new TrainingSet(
                new HyperRectangle(
                        new ContinuousInterval("x",-2.0,2.0),
                        new ContinuousInterval("y",-2.0,2.0)
                ),
                new LatinHyperCubeSamplingTask(),
                1000,
                functionToLearn)
                ;
        GradientTreeBoostModel rfr = new GradientTreeBoostModel(trainingSet,new Properties());
        rfr.fit();
        System.out.println(rfr.getInSampleMetrics().toString());

    }

}