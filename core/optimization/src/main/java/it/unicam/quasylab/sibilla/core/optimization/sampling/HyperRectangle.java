package it.unicam.quasylab.sibilla.core.optimization.sampling;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * An HyperRectangle is an n-dimensional rectangle representing a portion of the space through intervals
 *
 * @author      Lorenzo Matteucci
 */
public class HyperRectangle {

    final private Interval[] intervals;

    public HyperRectangle(Interval ... intervals){
        if(!allIDsAreUnique(intervals))
            throw new IllegalArgumentException("there cannot be more intervals with the same identifier");
        else
            this.intervals = intervals;
    }

    public Interval getInterval(int index){
        return intervals[index];
    }


    public int getDimensionality() {
        return intervals.length;
    }

    private boolean allIDsAreUnique(Interval ... intervals){
        String[] idArr = Arrays.stream(intervals).map(Interval::getId).toArray(String[]::new);
        Set<String> idSet = new HashSet<>(Arrays.asList(idArr));
        return (idSet.size() == idArr.length);
    }


    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder("HyperRectangle - dimensionality: " + intervals.length + "\n");
        for (Interval i :intervals) {
            ret.append("[ ").append(i.getLowerBound()).append(" ... ").append(i.getUpperBound()).append(" ] \n");
        }
        return ret.toString();
    }
}