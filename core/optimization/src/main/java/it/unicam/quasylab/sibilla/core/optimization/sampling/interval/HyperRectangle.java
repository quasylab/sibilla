package it.unicam.quasylab.sibilla.core.optimization.sampling.interval;

import java.util.*;

import static it.unicam.quasylab.sibilla.core.optimization.Constants.*;

/**
 * An HyperRectangle is an n-dimensional rectangle representing a portion of the space through intervals
 *
 * @author      Lorenzo Matteucci
 */
public class HyperRectangle{

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

    public Map<String,Interval> getIntervalsAsMap(){
        Map<String,Interval> map = new HashMap<>();
        for (Interval i: this.intervals) {
            map.put(i.getId(),i);
        }
        return map;
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
        boolean isContainable = true;
        if(values.size() != this.intervals.length)
            return false;
        Map<String,Interval> intervalMap = getIntervalsAsMap();
        for (String k :values.keySet()) {
            if(!intervalMap.get(k).contains(values.get(k))){
                isContainable= false;
                break;
            }
        }
        return isContainable;
    }

    public boolean couldContain(double[] values){
        boolean isContainable = true;
        if(values.length != this.intervals.length)
            return false;
        for (int i = 0; i < intervals.length; i++) {
            if(!intervals[i].contains(values[i])){
                isContainable= false;
                break;
            }
        }
        return isContainable;
    }


    public Map<String,Double> getRandomValue(){
        Map<String,Double> randomValue = new HashMap<>();
        for (Interval i:this.intervals) {
            randomValue.put(i.getId(),i.getRandomValue());
        }
        return randomValue;
    }

    public void changeCenter(double[] newCenterArray){
        if(newCenterArray.length != intervals.length)
            throw new IllegalArgumentException(EXCEPT_ILLEGAL_CENTER_SIZE);
        for (int i = 0; i < intervals.length; i++) {
            this.intervals[i].changeCenter(newCenterArray[i]);
        }
    }

    public void changeCenter(Map<String,Double> newCenterMap){
        if(newCenterMap.size() != intervals.length)
            throw new IllegalArgumentException(EXCEPT_ILLEGAL_CENTER_SIZE);
        for (Interval i:this.intervals) {
            i.changeCenter(newCenterMap.get(i.getId()));
        }
    }

    private boolean allIDsAreUnique(Interval ... intervals){
        String[] idArr = Arrays.stream(intervals).map(Interval::getId).toArray(String[]::new);
        Set<String> idSet = new HashSet<>(Arrays.asList(idArr));
        return (idSet.size() == idArr.length);
    }


    public HyperRectangle getCopy(){
        return new HyperRectangle(
                Arrays.stream(this.intervals)
                .map(Interval::getDeepCopy)
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