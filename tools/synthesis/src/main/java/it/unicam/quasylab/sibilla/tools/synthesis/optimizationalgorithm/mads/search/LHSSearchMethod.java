package it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.mads.search;

import it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.mads.mesh.Mesh;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval.HyperRectangle;

import java.util.*;
import java.util.stream.IntStream;

public class LHSSearchMethod implements SearchMethod {
    @Override
    public List<Map<String, Double>> generateTrialPoints(int numberOfPoints, Mesh mesh, Random random) {
        return  getTrialPoints(numberOfPoints,mesh.getMeshAsHyperRectangle(),random);
    }

    @Override
    public List<Map<String, Double>> generateTrialPoints(int numberOfPoints, Mesh mesh) {
       return getTrialPoints(numberOfPoints,mesh.getMeshAsHyperRectangle(),new Random(System.nanoTime()));
    }

    private List<Map<String, Double>> getTrialPoints(int numberOfSamples, HyperRectangle hr, Random random) {
        int[][] shuffledMatrix = getShuffledMatrix(hr.getDimensionality(),numberOfSamples,random);
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

    private int[][] getShuffledMatrix(int row, int column, Random random){
        int[][] shuffledMatrix = new int[row][column];
        int[] orderedArray = IntStream.range(1,column+1).toArray();
        for (int i = 0; i < row; i++) {
            shuffledMatrix[i] = shuffledArray(orderedArray, random);
        }
        return shuffledMatrix;
    }

    private int[] shuffledArray(int[] array,Random random){
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
