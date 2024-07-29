package it.unicam.quasylab.sibilla.tools.synthesis.surrogate;

import it.unicam.quasylab.sibilla.tools.synthesis.sampling.FullFactorialSamplingTask;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.RandomSamplingTask;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.SamplingStrategyRegistry;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval.ContinuousInterval;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval.DiscreteStepInterval;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval.HyperRectangle;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import tech.tablesaw.api.Row;

import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;

import static it.unicam.quasylab.sibilla.tools.synthesis.Commons.DEFAULT_COLUMN_RESULT_NAME;
import static org.junit.jupiter.api.Assertions.*;
/**
 * Test for Training Set
 *
 * @author      Lorenzo Matteucci
 */
@SuppressWarnings({"UnusedDeclaration"})
class DataSetTest {

    @Disabled
    @Test
    void testCreationTrainingSet(){
        HyperRectangle hr = new HyperRectangle(
                new ContinuousInterval("x",0.0,5.0),
                new ContinuousInterval("y",5.0,10.0),
                new ContinuousInterval("z",10.0,15.0)
        );
        ToDoubleFunction<Map<String,Double>> function = (
                stringDoubleMap -> stringDoubleMap.get("x") + stringDoubleMap.get("y")
        );
        DataSet ts = new DataSet(
                hr,
                SamplingStrategyRegistry.getInstance().get("lhs").getSamplingTask(),
                1000,
                function);

        System.out.println(ts);
    }

    @Disabled
    @Test
    void testTrainingSet(){
        HyperRectangle hr = new HyperRectangle(
                new ContinuousInterval("x",0.0,5.0),
                new ContinuousInterval("y",5.0,10.0)
        );
        ToDoubleFunction<Map<String,Double>> function = (
                stringDoubleMap -> stringDoubleMap.get("x") + stringDoubleMap.get("y")
        );
        DataSet ts = new DataSet(hr,new RandomSamplingTask(),50,function);
        boolean sumCondition = true;
        for (int i = 0; i < ts.rowCount(); i++) {
            Row row = ts.row(i);
            sumCondition &= (row.getDouble("x") + row.getDouble("y") )
                            == row.getDouble(DEFAULT_COLUMN_RESULT_NAME);
        }
        assertTrue(sumCondition);
    }

    @Disabled
    @Test
    void testSummary(){
        HyperRectangle hr = new HyperRectangle(
                new ContinuousInterval("x",0.0,5.0),
                new ContinuousInterval("y",5.0,10.0)
        );
        ToDoubleFunction<Map<String,Double>> function = (
                map -> map.get("x") + map.get("y")
        );
        DataSet ts = new DataSet(hr,new RandomSamplingTask(),50,function);
        assertTrue(ts.getResultSD() != 0);
        assertTrue(ts.getResultMean() >= 5.0 && ts.getResultMean() <= 15.0);
    }

    @Test
    void testMode(){
        HyperRectangle hr = new HyperRectangle(
                new ContinuousInterval("x",0.0,1.0)
        );
        ToDoubleFunction<Map<String,Double>> function = ( map -> map.get("x") > 0.8 ? 0.0 : 1.0 );
        DataSet ts = new DataSet(hr,new RandomSamplingTask(),50,function);
        assertEquals(1.0, ts.getResultMode());
    }

    @Test
    void testModeSDis0(){
        HyperRectangle hr = new HyperRectangle(
                new ContinuousInterval("x",0.0,1.0)
        );
        ToDoubleFunction<Map<String,Double>> function = ( map -> 1.0 );
        DataSet ts = new DataSet(hr,new RandomSamplingTask(),50,function);
        assertEquals(0.0, ts.getResultSD());
        assertEquals(1.0, ts.getResultMode());
    }

    @Test
    void testFilter(){
        HyperRectangle searchSpace1 = new HyperRectangle(
                new ContinuousInterval("x",-20.0,20.0),
                new ContinuousInterval("y",-50.0,50.0)
        );

        HyperRectangle searchSpace2 = new HyperRectangle(
                new ContinuousInterval("x",-10.0,10.0),
                new ContinuousInterval("y",-25.0,25.0)
        );
        ToDoubleFunction<Map<String,Double>>  function = (
                map -> map.get("x") + map.get("y")
        );
        DataSet ts =  new DataSet(searchSpace1,new RandomSamplingTask(),50,function);
        DataSet newTS = ts.filterBy(searchSpace2);
        assertTrue(ts.rowCount() > newTS.rowCount());
    }

    @Test
    void appendTrainingSet(){
        HyperRectangle searchSpace = new HyperRectangle(
                new DiscreteStepInterval("x",-2.0,2.0,1.0),
                new DiscreteStepInterval("y",-5.0,5.0,1.0)
        );

        ToDoubleFunction<Map<String,Double>>  function = (
                map -> map.get("x") + map.get("y")
        );

        DataSet ts1 = new DataSet(searchSpace,new RandomSamplingTask(),2,function);
        DataSet ts2 = new DataSet(searchSpace,new RandomSamplingTask(),2,function);
        DataSet ts3 = ts1.appendTrainingSet(ts2);


    }

    @Test
    void testArray(){
        HyperRectangle searchSpace = new HyperRectangle(
                new DiscreteStepInterval("x",0.0,5.0,1.0),
                new DiscreteStepInterval("y",15.0,25.0,1.0)
        );

        ToDoubleFunction<Map<String,Double>> function = m -> m.get("x") + m.get("y");

        DataSet ts = new DataSet(searchSpace,new FullFactorialSamplingTask(),5,function);

        double[][] dataInput = ts.getDataMatrix();
        double[] dataOutput = ts.getResultValues();
        String[] names = ts.getColumnNames();

        assertEquals(0.0, dataInput[0][0]);
        assertEquals(17.0, dataInput[0][1]);
        assertEquals(2.0, dataInput[13][0]);
        assertEquals(23.0, dataInput[13][1]);

        assertEquals(19.0,dataOutput[1]);
        assertEquals(18.0,dataOutput[5]);

        assertEquals("x",names[0]);
        assertEquals("y",names[1]);

    }

    @Disabled
    @Test
    void testMap(){
        HyperRectangle searchSpace = new HyperRectangle(
                new DiscreteStepInterval("x",0.0,5.0,1.0),
                new DiscreteStepInterval("y",5.0,10.0,1.0)
        );

        ToDoubleFunction<Map<String,Double>>  function = (
                map -> map.get("x") + map.get("y")
        );

        DataSet ts = new DataSet(searchSpace,new RandomSamplingTask(),10,function);



        List<Map<String,Double>> tsAsMap = ts.toMapList();

        ts.stream().forEach(System.out::println);

        //System.out.println("ts1 - "+ts1.rowCount());
        //System.out.println("ts2 - "+ts2.rowCount());
        //System.out.println("ts3 - "+ts3.rowCount());

        //System.out.println("- - - - - - - - - - - - - - -");

        //System.out.println(ts1);
        //System.out.println(ts2);
        //System.out.println(ts3);

    }

    @Disabled
    @Test
    public void testSplit() {

        HyperRectangle searchSpace = new HyperRectangle(
                new DiscreteStepInterval("x",0.0,5.0,1.0),
                new DiscreteStepInterval("y",5.0,10.0,1.0)
        );

        ToDoubleFunction<Map<String,Double>>  function = (
                map -> map.get("x") + map.get("y")
        );

        DataSet ts = new DataSet(searchSpace,new RandomSamplingTask(),10,function,123L);
        DataSet[] split = ts.trainTestSplit(0.9);

        assertEquals(9,split[0].rowCount());
        assertEquals(1,split[1].rowCount());

        System.out.println();
        System.out.println(ts);
        System.out.println();
        System.out.println(split[0]);
        System.out.println();
        System.out.println(split[1]);
        System.out.println();
    }

    @Disabled
    @Test
    public void testSplitSeed() {

        HyperRectangle searchSpace = new HyperRectangle(
                new DiscreteStepInterval("x",0.0,5.0,1.0),
                new DiscreteStepInterval("y",5.0,10.0,1.0)
        );

        ToDoubleFunction<Map<String,Double>>  function = (
                map -> map.get("x") + map.get("y")
        );

        DataSet ts = new DataSet(searchSpace,new RandomSamplingTask(),10,function,123L);
        DataSet[] split = ts.trainTestSplit(0.6,123L);

        //assertEquals(9,split[0].rowCount());
        //assertEquals(1,split[1].rowCount());

        System.out.println();
        System.out.println(ts);
        System.out.println();
        System.out.println(split[0]);
        System.out.println();
        System.out.println(split[1]);
        System.out.println();
    }

//    @Test
//    void testDropNumberOfResultRowsEqualTo(){
//        HyperRectangle searchSpace = new HyperRectangle(
//                new Interval("x",-2.0,2.0),
//                new Interval("y",-5.0,5.0)
//        );
//
//
//        Function<Map<String,Double>,Double> function_0 = (
//                map -> 0.0
//        );
//
//        Function<Map<String,Double>,Double> function_1 = (
//                map -> 1.0
//        );
//
//
//        TrainingSet ts_0 = new TrainingSet(searchSpace,"rs",500,function_0);
//        TrainingSet ts_1 = new TrainingSet(searchSpace,"rs",500,function_1);
//
//        TrainingSet merged = ts_0.appendTrainingSet(ts_1);
//
//        TrainingSet mergedWithDrop = merged.dropNumberOfResultRowsEqualTo(1.0,250);
//
//        assertEquals(750, mergedWithDrop.rowCount());
//    }

}