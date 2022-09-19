package it.unicam.quasylab.sibilla.core.optimization.sampling;

import java.util.*;

import static it.unicam.quasylab.sibilla.core.optimization.Constants.*;

/**
 * An HyperRectangle is an n-dimensional rectangle representing a portion of the space through intervals
 *
 * @author      Lorenzo Matteucci
 */
public class HyperRectangle implements Cloneable{

    final private Interval[] intervals;

    public HyperRectangle(Interval ... intervals){
        if(!allIDsAreUnique(intervals))
            throw new IllegalArgumentException(EXCEPT_INTERVALS_WITH_SAME_ID);
        else
            this.intervals = intervals;
    }

    public Interval getInterval(int index){
        return intervals[index];
    }

    public Interval[] getIntervals() {
        return this.intervals;
    }

    public Map<String,Interval> getIntervalsMappedByID(){
        Map<String,Interval> map = new HashMap<>();
        for (Interval i : intervals) {
            map.put(i.getId(),i);
        }
        return map;
    }

    public int getDimensionality() {
        return intervals.length;
    }

    public void scale(double scaleFactor){
        Arrays.stream(intervals).forEach( i -> i.scale(scaleFactor));
    }

    public boolean couldContain(Map<String,Double> values){
        boolean isContainable = false;

        if (values.size()!=intervals.length){
            for (var entry : values.entrySet()) {
                System.out.println(entry.getKey() + "/" + entry.getValue());
            }
        }
        return isContainable;
    }

    private boolean allIDsAreUnique(Interval ... intervals){
        String[] idArr = Arrays.stream(intervals).map(Interval::getId).toArray(String[]::new);
        Set<String> idSet = new HashSet<>(Arrays.asList(idArr));
        return (idSet.size() == idArr.length);
    }

    public HyperRectangle getCopy(){
        return new HyperRectangle(
                Arrays.stream(this.intervals)
                .map( i-> new Interval(i.getId(),i.getLowerBound(),i.getUpperBound(),i.isContinuous()))
                .toArray(Interval[]::new)
        );
    }

    public HyperRectangle getScaledCopy(double scaleFactor){
        HyperRectangle scaledOne = this.getCopy();
        scaledOne.scale(scaleFactor);
        return scaledOne;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder("HyperRectangle - dimensionality: " + intervals.length + "\n");
        for (Interval i :intervals) {
            ret.append("[ ").append(i.getLowerBound()).append(" ... ").append(i.getUpperBound()).append(" ] \n");
        }
        return ret.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HyperRectangle that = (HyperRectangle) o;
        return Arrays.equals(intervals, that.intervals);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(intervals);
    }




}