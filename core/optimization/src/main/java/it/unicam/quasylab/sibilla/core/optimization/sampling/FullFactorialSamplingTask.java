package it.unicam.quasylab.sibilla.core.optimization.sampling;


import com.google.common.collect.Sets;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

/**
 * The full factorial sampling plan places a grid of evenly spaced points over the search space.
 * This approach is easy to implement, does not rely on randomness, and covers the space, but it uses
 * a large number of points<br>
 * <b>NOTE : </b> For this sampling method you choose how many samples for dimension are wanted and NOT how many
 * samples you want
 *
 * @author      Lorenzo Matteucci
 */
public class FullFactorialSamplingTask implements SamplingTask {

    /**
     *
     * methods used to generate sets of points in a hyper-rectangle, it could be used
     * either for training or for prediction.
     *
     * @param  numberOfSamplesPerDimension  number samples for each dimension<br>
     *                                      <b>ATTENTION : </b> For n dimensions with m samples per dimension, we have m raised to n total samples
     * @param  hyperRectangle an n-dimensional rectangle representing a portion of the space through intervals
     * @return the samples' table of type <a href=https://github.com/jtablesaw/tablesaw">Table</a>
     * @see    <a href=https://jtablesaw.github.io/tablesaw/gettingstarted.html">tablesaw</a>
     *
     * @author      Lorenzo Matteucci (lorenzo.matteucci@unicam.it)
     */
    @Override
    public Table getSampleTable(int numberOfSamplesPerDimension, HyperRectangle hyperRectangle) {
        return getTableFromSetOfList(Sets.cartesianProduct(getSetsOfPointsPerDimension(numberOfSamplesPerDimension,hyperRectangle)),hyperRectangle);
    }

    @Override
    public Table getSampleTable(int numberOfSamplesPerDimension, HyperRectangle hyperRectangle, long seed) {
        return getSampleTable(numberOfSamplesPerDimension, hyperRectangle);
    }

    private List<Set<Double>> getSetsOfPointsPerDimension(int n , HyperRectangle hr){
        List<Set<Double>> setsOfPointsPerDimension = new ArrayList<>();
        for (int i = 0; i < hr.getDimensionality(); i++) {
            double currentLow = hr.getInterval(i).getLowerBound();
            double gapBetweenPoints = hr.getInterval(i).length() / n;
            setsOfPointsPerDimension.add(
                    DoubleStream.
                            iterate(currentLow, p -> p + gapBetweenPoints).
                            limit(n).
                            boxed().collect(Collectors.toSet())
            );
        }
        return setsOfPointsPerDimension;
    }

    private Table getTableFromSetOfList(Set<List<Double>> set,HyperRectangle hr){
        int numberOfColumn = set.iterator().next().size();
        DoubleColumn[] columnList = new DoubleColumn[numberOfColumn];
        for (int i = 0; i < numberOfColumn; i++) {
            columnList[i] = DoubleColumn.create(hr.getInterval(i).getId());
        }
        for (List<Double> list: set) {
            for (int i = 0; i < list.size(); i++) {
                double value = list.get(i);
                columnList[i].append(hr.getInterval(i).isContinuous() ? value : hr.getInterval(i).getClosestValueTo(value));
            }
        }
        return Table.create(columnList).dropDuplicateRows();
    }



}

