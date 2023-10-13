package it.unicam.quasylab.sibilla.core.optimization.surrogate;

import it.unicam.quasylab.sibilla.core.optimization.sampling.LatinHyperCubeSamplingTask;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.ContinuousInterval;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Properties;
import java.util.function.ToDoubleFunction;

import static it.unicam.quasylab.sibilla.core.optimization.CommonForTesting.getCsvFromTable;

/**
 * Test for Random Forest Surrogate
 *
 * @author      Lorenzo Matteucci
 */
@SuppressWarnings({"UnusedDeclaration"})
class RandomForestModelTest {

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
        RandomForestModel rfr = new RandomForestModel(dataSet,0.85, new Properties());
        rfr.fit();
        System.out.println("metrics 1 ");
        System.out.println(rfr.getTrainingSetMetrics().toString());
//        System.out.println(rfr.getInSampleMetrics().getTruthVsPredictedTable());
//        Properties newProp = new Properties();
//        newProp.put("rf.trees","10000");
//        newProp.put("rf.max_depth","1000");
//        newProp.put("not.model.property","100");
//        rfr.setProperties(newProp);
//        rfr.fit();
//        System.out.println("metrics 2 ");
//        System.out.println(rfr.getInSampleMetrics().toString());
//        System.out.println(rfr.getInSampleMetrics().getTruthVsPredictedTable());
        String csvString = getCsvFromTable(rfr.getTrainingSetMetrics().getTruthVsPredictedTable());
        System.out.println(csvString);
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

        System.out.println("metrics  in Sample ");
        System.out.println(rfr.getTrainingSetMetrics().toString());

        System.out.println("metrics out Sample ");
        System.out.println(rfr.getTestSetMetrics().toString());

    }

//    @Test
//    void testTrainingSetDifferentProperties(){
//
//        TrainingSet trainingSet = new TrainingSet(
//                new HyperRectangle(
//                        new ContinuousInterval("x",-5.0,5.0),
//                        new ContinuousInterval("y",-5.0,5.0)
//                ),
//                new LatinHyperCubeSamplingTask(),
//                10000,
//                EGG_HOLDER_FUNCTION)
//                ;
//        RandomForestModel rfr = new RandomForestModel(trainingSet,new Properties());
//        rfr.fit();
//        System.out.println(rfr.getInSampleMetrics().toString());
//
//        Properties newProp = new Properties();
//        newProp.put("surrogate.random.forest.trees","1000");
//        newProp.put("surrogate.random.forest.max_depth","500");
//        newProp.put("not.surrogate.properties","asjdn1oi2359990");
//        rfr.setProperties(newProp);
//        rfr.fit();
//        System.out.println(rfr.getInSampleMetrics().toString());
//    }
//
//    @Test
//    void testTrainingSetDifferentProperties(){
//
//        TrainingSet trainingSet = new TrainingSet(
//                new HyperRectangle(
//                        new ContinuousInterval("x",-5.0,5.0),
//                        new ContinuousInterval("y",-5.0,5.0)
//                ),
//                new LatinHyperCubeSamplingTask(),
//                10000,
//                EGG_HOLDER_FUNCTION)
//                ;
//        RandomForestModel rfr = new RandomForestModel();
//        rfr.fit(trainingSet);
//        //System.out.println("metrics 1 ");
//        //System.out.println(rfr.getInSampleMetrics().toString());
//
//        Properties newProp = new Properties();
//        newProp.put("surrogate.random.forest.trees","1000");
//        newProp.put("surrogate.random.forest.max_depth","100");
//        newProp.put("not.surrogate.properties","100");
//        rfr.setProperties(newProp);
//        rfr.fit(trainingSet);
//        //System.out.println("metrics 2 ");
//        //System.out.println(rfr.getInSampleMetrics().toString());
//    }




}