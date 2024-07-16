package it.unicam.quasylab.sibilla.tools.synthesis.surrogate;

import it.unicam.quasylab.sibilla.tools.synthesis.sampling.LatinHyperCubeSamplingTask;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval.ContinuousInterval;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval.HyperRectangle;
import org.junit.jupiter.api.Disabled;
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
        DataSet dataSet = new DataSet(
                new HyperRectangle(
                        new ContinuousInterval("x",-2.0,2.0),
                        new ContinuousInterval("y",-2.0,2.0)
                ),
                new LatinHyperCubeSamplingTask(),
                1000,
                functionToLearn)
                ;
        GradientTreeBoostModel rfr = new GradientTreeBoostModel(dataSet,0.85,new Properties());
        rfr.fit();
    }



    @Disabled
    @Test
    void testComparisonRealAndSurrogate(){
        ToDoubleFunction<Map<String,Double>> functionToLearn = (
                stringDoubleMap -> {
                    double x = stringDoubleMap.get("x");
                    double y = stringDoubleMap.get("y");
                    return 7 * ( x * y )/(Math.pow(Math.E,(Math.pow(x,2)+Math.pow(y,2))));
                }
        );
        DataSet dataSet = new DataSet(
                new HyperRectangle(
                        new ContinuousInterval("x",-2.0,2.0),
                        new ContinuousInterval("y",-2.0,2.0)
                ),
                new LatinHyperCubeSamplingTask(),
                1000,
                functionToLearn)
                ;
        GradientTreeBoostModel rfr = new GradientTreeBoostModel(dataSet,0.85,new Properties());
        rfr.fit();
        System.out.println(rfr.getInSampleMetrics().toString());

    }

}