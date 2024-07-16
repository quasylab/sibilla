package it.unicam.quasylab.sibilla.tools.synthesis.sampling;

import it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval.ContinuousInterval;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval.DiscreteStepInterval;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval.HyperRectangle;
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
        Table sampleSet = new FullFactorialSamplingTask().getSampleTable(3,hr);
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
        Table sampleSetMix = new FullFactorialSamplingTask().getSampleTable(3,hrMixed);
        Table sampleSetCont = new FullFactorialSamplingTask().getSampleTable(3,hrAllContinuous);


        assertTrue(sampleSetCont.rowCount() > sampleSetMix.rowCount());
    }


    @Test
    void testGetSampleTable() {
        // Create a HyperRectangle with intervals for each dimension
        HyperRectangle hyperRectangle = new HyperRectangle(
                new ContinuousInterval("A", -10, 10),
                new ContinuousInterval("B", -20, 20),
                new ContinuousInterval("C", -2, 2)
        );

        // Create an instance of FullFactorialSamplingTask
        FullFactorialSamplingTask samplingTask = new FullFactorialSamplingTask();

        // Call the getSampleTable method with the desired number of samples per dimension
        int numberOfSamplesPerDimension = 3;
        Table sampleTable = samplingTask.getSampleTable(numberOfSamplesPerDimension, hyperRectangle);

        // Verify the expected number of rows in the sample table
        int expectedNumberOfRows = 3 * 3 * 3; // 3 samples per dimension
        assertEquals(expectedNumberOfRows, sampleTable.rowCount());

        // Verify the expected number of columns in the sample table
        int expectedNumberOfColumns = 3; // 3 dimensions
        assertEquals(expectedNumberOfColumns, sampleTable.columnCount());
    }

}