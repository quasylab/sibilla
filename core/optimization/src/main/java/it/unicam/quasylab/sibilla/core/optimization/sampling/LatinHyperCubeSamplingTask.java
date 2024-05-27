package it.unicam.quasylab.sibilla.core.optimization.sampling;


import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Table;

import java.util.*;
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

public class LatinHyperCubeSamplingTask implements SamplingTask {

    Random random = new Random();

    @Override
    public Table getSampleTable(int numberOfSamples, HyperRectangle hr) {
        return getSampleTable(numberOfSamples, hr, System.nanoTime());
    }

    @Override
    public Table getSampleTable(int numberOfSamples, HyperRectangle hr, long seed) {
        random.setSeed(seed);
        int[][] shuffledMatrix = getShuffledMatrix(hr.getDimensionality(), numberOfSamples);
        DoubleColumn[] columns = new DoubleColumn[hr.getDimensionality()];
        for (int i = 0; i < hr.getDimensionality(); i++) {
            double[] columnArray = new double[numberOfSamples];
            for (int j = 0; j < numberOfSamples; j++) {
                double value = hr.getInterval(i).getLowerBound() + ((shuffledMatrix[i][j] - random.nextDouble()) / (numberOfSamples)) * (hr.getInterval(i).getUpperBound() - hr.getInterval(i).getLowerBound());
                columnArray[j] = hr.getInterval(i).isContinuous() ? value : hr.getInterval(i).getClosestValueTo(value);
            }
            columns[i] = DoubleColumn.create(hr.getInterval(i).getId(), columnArray);
        }
        return Table.create().addColumns(columns);
    }


    public List<double[]> getSamplesAsArray(int numberOfSamples, HyperRectangle hr) {
        return getSamplesAsArray(numberOfSamples,hr,System.nanoTime());
    }

    public List<double[]> getSamplesAsArray(int numberOfSamples, HyperRectangle hr, long seed) {
        random.setSeed(seed);
        int[][] shuffledMatrix = getShuffledMatrix(hr.getDimensionality(),numberOfSamples);
        List<double[]> samples = new ArrayList<>();
        for (int j = 0; j <numberOfSamples ; j++) {
            double[] array = new double[hr.getDimensionality()];
            for (int i = 0; i < hr.getDimensionality(); i++) {
                array[i] = hr.getInterval(i).getLowerBound() + ((shuffledMatrix[i][j] - random.nextDouble())/(numberOfSamples)) * (hr.getInterval(i).getUpperBound() - hr.getInterval(i).getLowerBound() );
            }
            samples.add(array);
        }
        return samples;
    }


    public List<Map<String, Double>> getSamplesAsMap(int numberOfSamples, HyperRectangle hr) {
        return this.getSamplesAsMap(numberOfSamples,hr,System.nanoTime());
    }

    public List<Map<String, Double>> getSamplesAsMap(int numberOfSamples, HyperRectangle hr, long seed) {
        random.setSeed(seed);
        int[][] shuffledMatrix = getShuffledMatrix(hr.getDimensionality(),numberOfSamples);
        List<Map<String, Double>> samples = new ArrayList<>();
        for (int j = 0; j <numberOfSamples ; j++) {
            Map<String, Double>  map = new TreeMap<>();
            for (int i = 0; i < hr.getDimensionality(); i++) {
                double value = hr.getInterval(i).getLowerBound() + ((shuffledMatrix[i][j] - random.nextDouble())/(numberOfSamples)) * (hr.getInterval(i).getUpperBound() - hr.getInterval(i).getLowerBound() );
                String name = hr.getInterval(i).getId();
                map.put(name,value);
            }
            samples.add(map);
        }
        return samples;
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

