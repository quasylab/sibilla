package it.unicam.quasylab.sibilla.core.optimization.surrogate;

import it.unicam.quasylab.sibilla.core.optimization.sampling.FullFactorialSampling;
import it.unicam.quasylab.sibilla.core.optimization.sampling.HyperRectangle;
import it.unicam.quasylab.sibilla.core.optimization.sampling.Interval;
import it.unicam.quasylab.sibilla.core.optimization.sampling.RandomSampling;
import org.junit.jupiter.api.Test;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.util.Map;
import java.util.function.Function;

import static it.unicam.quasylab.sibilla.core.optimization.Constants.DEFAULT_COLUMN_RESULT_NAME;
import static org.junit.jupiter.api.Assertions.*;
@SuppressWarnings({"UnusedDeclaration"})
class TrainingSetTest {

    @Test
    void testTrainingSet(){
        HyperRectangle hr = new HyperRectangle(
                new Interval("x",0.0,5.0),
                new Interval("y",5.0,10.0)
        );
        int numberOfSamples = 50;
        Table sampleSet = new RandomSampling().getSampleTable(numberOfSamples,hr);
        Function<Map<String,Double>,Double> function = (
                stringDoubleMap -> stringDoubleMap.get("x") + stringDoubleMap.get("y")
        );
        Table ts = new TrainingSet(sampleSet,function);
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
        int numberOfSamples = 50;
        Table sampleSet = new RandomSampling().getSampleTable(numberOfSamples,hr);
        Function<Map<String,Double>,Double> function = (
                map -> map.get("x") + map.get("y")
        );
        TrainingSet ts = new TrainingSet(sampleSet,function);
        assertTrue(ts.getResultSD() != 0);
        assertTrue(ts.getResultMean() >= 5.0 && ts.getResultMean() <= 15.0);
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

        int numberOfSamples = 1000;
        Table sampleSet = new RandomSampling().getSampleTable(numberOfSamples,searchSpace1);
        Function<Map<String,Double>,Double> function = (
                map -> map.get("x") + map.get("y")
        );
        TrainingSet ts = new TrainingSet(sampleSet,function);
        TrainingSet newTS = ts.filterBy(searchSpace2);
        assertTrue(ts.rowCount() > newTS.rowCount());
    }

    @Test
    void appendTrainingSet(){
        HyperRectangle searchSpace = new HyperRectangle(
                new Interval("x",-2.0,2.0,false),
                new Interval("y",-5.0,5.0,false)
        );

        int numberOfSamples = 2;

        Table sampleSet1 = new RandomSampling().getSampleTable(numberOfSamples,searchSpace);
        Table sampleSet2 = new RandomSampling().getSampleTable(numberOfSamples,searchSpace);
        Function<Map<String,Double>,Double> function = (
                map -> map.get("x") + map.get("y")
        );

        TrainingSet ts1 = new TrainingSet(sampleSet1,function);
        TrainingSet ts2 = new TrainingSet(sampleSet2,function);
        TrainingSet ts3 = ts1.appendTrainingSet(ts2);

        System.out.println("ts1 - "+ts1.rowCount());
        System.out.println("ts2 - "+ts2.rowCount());
        System.out.println("ts3 - "+ts3.rowCount());

        System.out.println("- - - - - - - - - - - - - - -");

        System.out.println(ts1);
        System.out.println(ts2);
        System.out.println(ts3);

    }

}