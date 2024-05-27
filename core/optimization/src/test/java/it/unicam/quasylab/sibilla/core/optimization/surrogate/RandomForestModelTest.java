package it.unicam.quasylab.sibilla.core.optimization.surrogate;

import it.unicam.quasylab.sibilla.core.optimization.sampling.LatinHyperCubeSamplingTask;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.ContinuousInterval;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Properties;
import java.util.function.ToDoubleFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test for Random Forest Surrogate
 *
 * @author      Lorenzo Matteucci
 */
@SuppressWarnings({"UnusedDeclaration"})
class RandomForestModelTest {

    @Test
    void testTrainingAndPredictSeed(){
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
                functionToLearn,
                123L
        );
        RandomForestModel rfr = new RandomForestModel(dataSet,0.85, new Properties(), 123L);
        rfr.fit();


        double expectedMSE = 0.006546784965538491;
        double actualMSE = rfr.getTrainingSetMetrics().mse;
        assertEquals(expectedMSE, actualMSE, 0.01);
    }


    @Test
    void testTrainingAndPredictSeedVer2(){
        ToDoubleFunction<Map<String,Double>> functionToLearn = (
                stringDoubleMap -> {
                    double x = stringDoubleMap.get("x");
                    double y = stringDoubleMap.get("y");
                    return 7 * ( x * y )/(Math.pow(Math.E,(Math.pow(x,2)+Math.pow(y,2))));
                }
        );

        RandomForestModel rfr = new RandomForestModel(functionToLearn, new LatinHyperCubeSamplingTask(), new HyperRectangle(
                new ContinuousInterval("x",-2.0,2.0),
                new ContinuousInterval("y",-2.0,2.0)
        ), 1000,0.85, new Properties(), 123L);
        rfr.fit();


        double expectedMSE = 0.006546784965538491;
        double actualMSE = rfr.getTrainingSetMetrics().mse;
        assertEquals(expectedMSE, actualMSE, 0.01);
    }


    @Test
    void testOutOfSample(){
        ToDoubleFunction<Map<String,Double>> functionToLearn = (
                stringDoubleMap -> {
                    double x = stringDoubleMap.get("x");
                    double y = stringDoubleMap.get("y");
                    return 7 * ( x * y )/(Math.pow(Math.E,(Math.pow(x,2)+Math.pow(y,2))));
                }
        );

        DataSet dataSet = new DataSet(
                new HyperRectangle(
                        new ContinuousInterval("x",-10.0,10.0),
                        new ContinuousInterval("y",-10.0,10.0)
                ),
                new LatinHyperCubeSamplingTask(),
                100,
                functionToLearn
        );



        RandomForestModel rfr = new RandomForestModel(dataSet, 0.85,new Properties());
        rfr.fit();

    }




}