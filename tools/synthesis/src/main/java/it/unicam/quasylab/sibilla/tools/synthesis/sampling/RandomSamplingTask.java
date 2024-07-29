package it.unicam.quasylab.sibilla.tools.synthesis.sampling;


import it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval.HyperRectangle;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Table;
/**
 *
 * This class simply creates random samples from a uniform distribution over the hyper rectangle.
 *
 * @author      Lorenzo Matteucci (lorenzo.matteucci@unicam.it)
 */
public class RandomSamplingTask implements SamplingTask {
    @Override
    public Table getSampleTable(int numberOfSamples, HyperRectangle hr) {
        return getSampleTable(numberOfSamples, hr, System.nanoTime());
    }

    @Override
    public Table getSampleTable(int numberOfSamples, HyperRectangle hr, long seed) {
        DoubleColumn[] columns = new DoubleColumn[hr.getDimensionality()];
        hr.setSeeds(seed);
        for (int i = 0; i < hr.getDimensionality(); i++) {
            double[] columnArray = new double[numberOfSamples];
            for (int j = 0; j < numberOfSamples; j++) {
                columnArray[j] = hr.getInterval(i).getRandomValue();
            }
            columns[i] = DoubleColumn.create(hr.getInterval(i).getId(), columnArray);
        }
        return Table.create().addColumns(columns);
    }
}

