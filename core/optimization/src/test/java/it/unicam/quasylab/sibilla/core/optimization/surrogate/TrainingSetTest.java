package it.unicam.quasylab.sibilla.core.optimization.surrogate;

import it.unicam.quasylab.sibilla.core.optimization.sampling.HyperRectangle;
import it.unicam.quasylab.sibilla.core.optimization.sampling.Interval;
import org.junit.jupiter.api.Test;
import tech.tablesaw.api.Row;

import java.util.Map;
import java.util.function.Function;

import static it.unicam.quasylab.sibilla.core.optimization.Constants.DEFAULT_COLUMN_RESULT_NAME;
import static org.junit.jupiter.api.Assertions.*;
/**
 * Test for Training Set
 *
 * @author      Lorenzo Matteucci
 */
@SuppressWarnings({"UnusedDeclaration"})
class TrainingSetTest {

    @Test
    void testTrainingSet(){
        HyperRectangle hr = new HyperRectangle(
                new Interval("x",0.0,5.0),
                new Interval("y",5.0,10.0)
        );
        Function<Map<String,Double>,Double> function = (
                stringDoubleMap -> stringDoubleMap.get("x") + stringDoubleMap.get("y")
        );
        TrainingSet ts = new TrainingSet(hr,"rs",50,function);
        boolean sumCondition = true;
        for (int i = 0; i < ts.rowCount(); i++) {
            Row row = ts.row(i);
            sumCondition &= (row.getDouble("x") + row.getDouble("y") )
                            == row.getDouble(DEFAULT_COLUMN_RESULT_NAME);
        }
        assertTrue(sumCondition);
    }

    @Test
    void testSummary(){
        HyperRectangle hr = new HyperRectangle(
                new Interval("x",0.0,5.0),
                new Interval("y",5.0,10.0)
        );
        Function<Map<String,Double>,Double> function = (
                map -> map.get("x") + map.get("y")
        );
        TrainingSet ts = new TrainingSet(hr,"rs",50,function);
        assertTrue(ts.getResultSD() != 0);
        assertTrue(ts.getResultMean() >= 5.0 && ts.getResultMean() <= 15.0);
    }

    @Test
    void testMode(){
        HyperRectangle hr = new HyperRectangle(
                new Interval("x",0.0,1.0)
        );
        Function<Map<String,Double>,Double> function = ( map -> map.get("x") > 0.8 ? 0.0 : 1.0 );
        TrainingSet ts = new TrainingSet(hr,"rs",50,function);
        assertEquals(1.0, ts.getResultMode());
    }

    @Test
    void testModeSDis0(){
        HyperRectangle hr = new HyperRectangle(
                new Interval("x",0.0,1.0)
        );
        Function<Map<String,Double>,Double> function = ( map -> 1.0 );
        TrainingSet ts = new TrainingSet(hr,"rs",50,function);
        assertEquals(0.0, ts.getResultSD());
        assertEquals(1.0, ts.getResultMode());
    }

    @Test
    void testFilter(){
        HyperRectangle searchSpace1 = new HyperRectangle(
                new Interval("x",-20.0,20.0),
                new Interval("y",-50.0,50.0)
        );

        HyperRectangle searchSpace2 = new HyperRectangle(
                new Interval("x",-10.0,10.0),
                new Interval("y",-25.0,25.0)
        );
        Function<Map<String,Double>,Double> function = (
                map -> map.get("x") + map.get("y")
        );
        TrainingSet ts =  new TrainingSet(searchSpace1,"rs",50,function);
        TrainingSet newTS = ts.filterBy(searchSpace2);
        assertTrue(ts.rowCount() > newTS.rowCount());
    }

    @Test
    void appendTrainingSet(){
        HyperRectangle searchSpace = new HyperRectangle(
                new Interval("x",-2.0,2.0,false),
                new Interval("y",-5.0,5.0,false)
        );

        Function<Map<String,Double>,Double> function = (
                map -> map.get("x") + map.get("y")
        );

        TrainingSet ts1 = new TrainingSet(searchSpace,"rs",2,function);
        TrainingSet ts2 = new TrainingSet(searchSpace,"rs",2,function);
        TrainingSet ts3 = ts1.appendTrainingSet(ts2);

        System.out.println("ts1 - "+ts1.rowCount());
        System.out.println("ts2 - "+ts2.rowCount());
        System.out.println("ts3 - "+ts3.rowCount());

        System.out.println("- - - - - - - - - - - - - - -");

        System.out.println(ts1);
        System.out.println(ts2);
        System.out.println(ts3);

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