package it.unicam.quasylab.sibilla.core.optimization.sampling;


import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Table;

import java.util.stream.IntStream;

/**
 *
 * Latin hypercube sampling (LHS) is a popular sampling method that is
 * built on a random process but is more effective and efficient than pure
 * random sampling. Random sampling scales better than full factorial
 * searches, but it tends to exhibit clustering and requires many points
 * to reach the desired distribution
 * In random sampling, each sample is independent of past samples, but
 * in LHS, we choose all samples beforehand to ensure a well-spread
 * distribution.
 *
 *
 * @author      Lorenzo Matteucci (lorenzo.matteucci@unicam.it)
 */

public class LatinHyperCubeSampling implements SamplingStrategy {
    @Override
    public Table getSampleTable(int numberOfSamples, HyperRectangle hr) {
        int[][] shuffledMatrix = getShuffledMatrix(hr.getDimensionality(),numberOfSamples);
        DoubleColumn[] columns = new DoubleColumn[hr.getDimensionality()];
        for (int i = 0; i < hr.getDimensionality(); i++) {
            double[] columnArray = new double[numberOfSamples];
            for (int j = 0; j <numberOfSamples ; j++) {
                double val = hr.getInterval(i).getLowerBound() + ((shuffledMatrix[i][j] - random.nextDouble())/(numberOfSamples)) * (hr.getInterval(i).getUpperBound() - hr.getInterval(i).getLowerBound() );
                columnArray[j] = hr.getInterval(i).isContinuous() ? val : Math.round(val);
            }
            columns[i] = DoubleColumn.create(hr.getInterval(i).getId(),columnArray);
        }
        return Table.create().addColumns(columns);
    }
    private int[][] getShuffledMatrix(int row, int column){
        int[][] shuffledMatrix = new int[row][column];
        int[] orderedArray = IntStream.range(1,column+1).toArray();
        for (int i = 0; i < row; i++) {
            shuffledMatrix[i] = shuffledArray(orderedArray);
        }
        return shuffledMatrix;
    }

    private int[] shuffledArray(int[] array){
        int[] shuffledArray = array.clone();
        for (int i = 0; i < array.length; i++) {
            int randomIndexToSwap = random.nextInt(shuffledArray.length);
            int temp = shuffledArray[randomIndexToSwap];
            shuffledArray[randomIndexToSwap] = shuffledArray[i];
            shuffledArray[i] = temp;
        }
        return shuffledArray;
    }
}

