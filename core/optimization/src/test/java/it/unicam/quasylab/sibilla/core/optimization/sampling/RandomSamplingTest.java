package it.unicam.quasylab.sibilla.core.optimization.sampling;

import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.ContinuousInterval;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.DiscreteSetInterval;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.DiscreteStepInterval;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;
import org.junit.jupiter.api.Test;
import tech.tablesaw.api.Table;

import static org.junit.jupiter.api.Assertions.*;
/**
 * Test for Latin HyperCube Sampling
 *
 * @author      Lorenzo Matteucci
 */
class RandomSamplingTest {
    @Test
    void testNumberOfRowAndColumn() {
        HyperRectangle hr = new HyperRectangle(
                new ContinuousInterval(1.0,10.0),
                new DiscreteStepInterval(1.0,10.0,1.0),
                new DiscreteSetInterval(3,5,9)
        );
        int numberOfSamples = 50;
        Table sampleSet = new RandomSamplingTask().getSampleTable(numberOfSamples,hr);
        assertTrue( sampleSet.rowCount() == numberOfSamples &&
                sampleSet.columnCount() == hr.getDimensionality());
    }


    @Test
    void testNumberOfRowAndColumnWithSeed() {
        HyperRectangle hr = new HyperRectangle(
                new ContinuousInterval(1.0,10.0),
                new DiscreteStepInterval(1.0,10.0,1.0),
                new DiscreteSetInterval(3,5,9)
        );
        int numberOfSamples = 5;
        Table sampleSet = new RandomSamplingTask().getSampleTable(numberOfSamples,hr,123456789);
        assertTrue( sampleSet.rowCount() == numberOfSamples &&
                sampleSet.columnCount() == hr.getDimensionality());

        System.out.println(sampleSet);
        double expectedValueV0 = 6.783891834031845;
        double expectedValueV1 = 4;
        double expectedValueV2 = 9;
        assertEquals(expectedValueV0, sampleSet.row(0).getDouble(hr.getInterval(0).getId()),0.1);
        assertEquals(expectedValueV1, sampleSet.row(0).getDouble(hr.getInterval(1).getId()),0.1);
        assertEquals(expectedValueV2, sampleSet.row(0).getDouble(hr.getInterval(2).getId()),0.1);

    }


}