package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.poll;


import java.util.*;

public abstract class AbstractPollMethod implements PollMethod{

    protected abstract int[][] getPositiveBasis(int dimension, double deltaMesh, Random random);
    @Override
    public List<Map<String, Double>> getPolledPoints(Map<String, Double> point, double deltaMesh, Random random) {
        int[][] matrix = getPositiveBasis(point.size(),deltaMesh, random);
        ArrayList<Map<String, Double>> result = new ArrayList<>();
        Set<String> keys = new TreeSet<>(point.keySet());
        for (int col = 0; col < matrix[0].length; col++) {
            Map<String, Double> temp = new HashMap<>();
            int i=0;
            for (String key:keys) {
                temp.put(key, point.get(key) + deltaMesh * matrix[i++][col]);
            }
            result.add(temp);
        }
        return result;
    }

}
