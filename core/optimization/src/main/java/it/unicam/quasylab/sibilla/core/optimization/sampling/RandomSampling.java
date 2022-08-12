package it.unicam.quasylab.sibilla.core.optimization.sampling;


import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Table;
/**
 *
 * This class simply creates random samples from a uniform distribution over the hyper rectangle.
 *
 * @author      Lorenzo Matteucci (lorenzo.matteucci@unicam.it)
 */
public class RandomSampling implements SamplingMethod{
    @Override
    public Table getSampleTable(int numberOfSamples, HyperRectangle hr) {
        DoubleColumn[] columns = new DoubleColumn[hr.getDimensionality()];
        for (int i = 0; i < hr.getDimensionality(); i++) {
            double[] columnArray = new double[numberOfSamples];
            for (int j = 0; j <numberOfSamples ; j++) {
                double val = hr.getInterval(i).getLowerBound() + (hr.getInterval(i).getUpperBound() -hr.getInterval(i).getLowerBound()) * random.nextDouble();
                columnArray[j] = hr.getInterval(i).isContinuous() ? val : Math.round(val);
            }
            columns[i] = DoubleColumn.create(hr.getInterval(i).getId(),columnArray);
        }
        return Table.create().addColumns(columns);
    }
}

