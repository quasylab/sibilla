package it.unicam.quasylab.sibilla.core.optimization.sampling;

import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.ContinuousInterval;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.DiscreteSetInterval;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.DiscreteStepInterval;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;
import org.junit.jupiter.api.Test;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
/**
 * Test for Latin HyperCube Sampling
 *
 * @author      Lorenzo Matteucci
 */
class LatinHyperCubeSamplingTest {

    @Test
    void testNumberOfRowAndColumn() {
        HyperRectangle hr = new HyperRectangle(
                new ContinuousInterval(1.0,10.0),
                new ContinuousInterval(1.0,10.0),
                new ContinuousInterval(1.0,10.0)
        );
        int numberOfSamples = 50;
        Table sampleSet = new LatinHyperCubeSamplingTask().getSampleTable(numberOfSamples,hr);
        assertTrue( sampleSet.rowCount() == numberOfSamples &&
                sampleSet.columnCount() == hr.getDimensionality());
    }


    @Test
    void testDifferentKindOfInterval() {
        HyperRectangle hr = new HyperRectangle(
                new DiscreteSetInterval(1,2,3),
                new DiscreteStepInterval(1.0,10.0,1),
                new ContinuousInterval(1.0,10.0)
        );
        int numberOfSamples = 10;
        Table sampleSet = new LatinHyperCubeSamplingTask().getSampleTable(numberOfSamples,hr);
        assertEquals(sampleSet.rowCount(), numberOfSamples);
    }

    @Test
    void testTableContinuousIntervalSeed() {
        HyperRectangle hr = new HyperRectangle(
                new ContinuousInterval(0.0, 5.0),
                new ContinuousInterval(0.0, 5.0)
        );
        int numberOfSamples = 4;
        Table sampleSet = new LatinHyperCubeSamplingTask().getSampleTable(numberOfSamples, hr, 123456);
        System.out.println(sampleSet);

        double[] expectedValueV0 = {2.4082019746999923, 3.0799193651004826, 0.737258509790663, 4.3460591263784805};
        double[] expectedValueV1 = {0.12903200433554973, 4.153242450992566, 3.194171546175148, 2.35975181048092};

        for (int i = 0; i < numberOfSamples; i++) {
            assertEquals(expectedValueV0[i], sampleSet.row(i).getDouble(hr.getInterval(0).getId()),0.1);
            assertEquals(expectedValueV1[i], sampleSet.row(i).getDouble(hr.getInterval(1).getId()),0.1);
        }
    }

    @Test
    void testMapContinuousIntervalSeed() {
        HyperRectangle hr = new HyperRectangle(
                new ContinuousInterval("Value_0",0.0, 5.0),
                new ContinuousInterval("Value_1",0.0, 5.0)
        );
        int numberOfSamples = 4;
        List<Map<String, Double>> sampleSet = new LatinHyperCubeSamplingTask().getSamplesAsMap(numberOfSamples, hr, 123456);

        List<Map<String, Double>> expectedValues = new ArrayList<>();

        expectedValues.add(Map.of("Value_0", 2.4082019746999923, "Value_1", 0.5799193651004826));
        expectedValues.add(Map.of("Value_0", 3.237258509790663, "Value_1", 4.3460591263784805));
        expectedValues.add(Map.of("Value_0", 0.12903200433554973, "Value_1", 2.903242450992565));
        expectedValues.add(Map.of("Value_0", 4.444171546175148, "Value_1", 2.35975181048092));

        for (int i = 0; i < numberOfSamples; i++) {
            assertEquals(expectedValues.get(i).get("Value_0"), sampleSet.get(i).get("Value_0"),0.1);
            assertEquals(expectedValues.get(i).get("Value_1"), sampleSet.get(i).get("Value_1"),0.1);
        }
    }


    @Test
    void testListArrayContinuousIntervalSeed() {
        HyperRectangle hr = new HyperRectangle(
                new ContinuousInterval(0.0, 5.0),
                new ContinuousInterval(0.0, 5.0)
        );
        int numberOfSamples = 4;
        List<double[]> sampleSet = new LatinHyperCubeSamplingTask().getSamplesAsArray(numberOfSamples, hr);
        for (double[] sample : sampleSet) {
            assertTrue(sample[0] >= hr.getIntervals()[0].getLowerBound());
            assertTrue(sample[0] <= hr.getIntervals()[0].getUpperBound());
            assertTrue(sample[1] >= hr.getIntervals()[1].getLowerBound());
            assertTrue(sample[1] <= hr.getIntervals()[1].getUpperBound());
        }
        List<double[]> sampleSetWithSeed = new LatinHyperCubeSamplingTask().getSamplesAsArray(numberOfSamples, hr, 123456);
        double[][] expectedValues = {
                {2.4082019746999923, 0.5799193651004826},
                {3.237258509790663, 4.3460591263784805},
                {0.12903200433554973, 2.903242450992565},
                {4.444171546175148, 2.35975181048092}
        };

        for (int i = 0; i < numberOfSamples; i++) {
            double[] sample = sampleSetWithSeed.get(i);
            double[] expectedValue = expectedValues[i];

            assertEquals(expectedValue[0], sample[0], 0.0001);
            assertEquals(expectedValue[1], sample[1], 0.0001);
        }

    }


}