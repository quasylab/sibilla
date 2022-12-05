package it.unicam.quasylab.sibilla.core.optimization.sampling;

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
                new ContinuousInterval(1.0,10.0),
                new ContinuousInterval(1.0,10.0)
        );
        int numberOfSamples = 50;
        Table sampleSet = new RandomSampling().getSampleTable(numberOfSamples,hr);
        assertTrue( sampleSet.rowCount() == numberOfSamples &&
                sampleSet.columnCount() == hr.getDimensionality());
    }
}