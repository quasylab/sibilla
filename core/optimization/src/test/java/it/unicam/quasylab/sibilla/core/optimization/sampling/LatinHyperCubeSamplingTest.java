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
class LatinHyperCubeSamplingTest {

    @Test
    void testNumberOfRowAndColumn() {
        HyperRectangle hr = new HyperRectangle(
                new ContinuousInterval(1.0,10.0),
                new ContinuousInterval(1.0,10.0),
                new ContinuousInterval(1.0,10.0)
        );
        int numberOfSamples = 50;
        Table sampleSet = new LatinHyperCubeSampling().getSampleTable(numberOfSamples,hr);
        assertTrue( sampleSet.rowCount() == numberOfSamples &&
                sampleSet.columnCount() == hr.getDimensionality());
    }


    @Test
    void visualTest() {
        HyperRectangle hr = new HyperRectangle(
                new DiscreteSetInterval(1,2,3),
                new DiscreteStepInterval(1.0,10.0,1)
        );
        int numberOfSamples = 10;
        Table sampleSet = new LatinHyperCubeSampling().getSampleTable(numberOfSamples,hr);
        //System.out.println(getCSVFromTable(sampleSet));
    }


    public String getCSVFromTable(Table table){
        StringBuilder csv = new StringBuilder();

        for (int i = 0; i < table.columnNames().size()-1; i++) {
            csv.append(table.columnNames().get(i)).append(",");
        }
        csv.append(table.columnNames().get(table.columnNames().size()-1)).append("\n");
        for (int i = 0; i < table.rowCount(); i++) {
            for (int j = 0; j < table.columnCount()-1; j++) {
                csv.append(table.row(i).getDouble(j)).append(",");
            }
            csv.append(table.row(i).getDouble(table.columnCount()-1)).append("\n");
        }
        return csv.toString();
    }

}