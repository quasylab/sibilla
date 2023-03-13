package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.poll;

import java.util.*;

public abstract class AbstractPollMethod implements PollMethod{

    protected abstract int[][] getPositiveBasis(int dimension, double deltaMesh);


    @Override
    public List<Map<String, Double>> getPolledPoints(Map<String, Double> point, double deltaMesh) {
        int[][] matrix = getPositiveBasis(point.size(),deltaMesh);
        ArrayList<Map<String, Double>> result = new ArrayList<>();
        Set<String> keys = point.keySet();
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
