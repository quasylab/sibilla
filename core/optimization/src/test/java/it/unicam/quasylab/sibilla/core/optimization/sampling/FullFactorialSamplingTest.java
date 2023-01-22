package it.unicam.quasylab.sibilla.core.optimization.sampling;

import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.ContinuousInterval;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.DiscreteStepInterval;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;
import org.junit.jupiter.api.Test;
import tech.tablesaw.api.Table;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for Full Factorial Sampling
 *
 * @author      Lorenzo Matteucci
 */
@SuppressWarnings({"UnusedDeclaration"})
class FullFactorialSamplingTest {
    @Test
    void testNumberOfSamplesOverContinuousInterval() {
        HyperRectangle hr = new HyperRectangle(
                new ContinuousInterval(1.0,10.0),
                new ContinuousInterval(1.0,10.0),
                new ContinuousInterval(1.0,10.0),
                new ContinuousInterval(1.0,10.0)
        );
        int numberOfSamplesPerDimension = 3;
        Table sampleSet = new FullFactorialSampling().getSampleTable(3,hr);
        assertEquals(
                sampleSet.rowCount(),
                Math.pow(numberOfSamplesPerDimension, hr.getDimensionality())
        );
    }

    @Test
    void testNumberOfSamplesOverMixedInterval() {
        HyperRectangle hrMixed = new HyperRectangle(
                new DiscreteStepInterval("v1",-1,1,1.0),
                new ContinuousInterval("v2",-40,65),
                new ContinuousInterval("v3",-130,200),
                new DiscreteStepInterval("v4",-1,1,1.0)
        );
        HyperRectangle hrAllContinuous = new HyperRectangle(
                new ContinuousInterval("v1",-1,1),
                new ContinuousInterval("v2",-40,65),
                new ContinuousInterval("v3",-130,200),
                new ContinuousInterval("v4",-1,1)
        );
        int numberOfSamplesPerDimension = 3;
        Table sampleSetMix = new FullFactorialSampling().getSampleTable(3,hrMixed);
        Table sampleSetCont = new FullFactorialSampling().getSampleTable(3,hrAllContinuous);


        assertTrue(sampleSetCont.rowCount() > sampleSetMix.rowCount());
    }

}