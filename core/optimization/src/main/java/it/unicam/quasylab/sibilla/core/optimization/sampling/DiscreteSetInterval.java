package it.unicam.quasylab.sibilla.core.optimization.sampling;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class DiscreteSetInterval extends AbstractDiscreteInterval{


    private TreeSet<Double> orderedElementsSet= new TreeSet<>();


    public DiscreteSetInterval(String id, double ... intervalElements) {
        super(
                id,
                Arrays.stream(intervalElements).min().orElseThrow(() -> new IllegalStateException("no lower bound found")),
                Arrays.stream(intervalElements).max().orElseThrow(() -> new IllegalStateException("no upper bound found"))
        );
        Arrays.stream(intervalElements).forEach(this.orderedElementsSet::add);
        super.sequenceOfElement = orderedElementsSet.stream().toList();
    }

    public DiscreteSetInterval(double ... intervalElements) {
        super(
                Arrays.stream(intervalElements).min().orElseThrow(() -> new IllegalStateException("no lower bound found")),
                Arrays.stream(intervalElements).max().orElseThrow(() -> new IllegalStateException("no upper bound found"))
        );
        Arrays.stream(intervalElements).forEach(this.orderedElementsSet::add);
        super.sequenceOfElement = orderedElementsSet.stream().toList();
    }

    public void addElements(double ... elements){
        Arrays.stream(elements).forEach(this.orderedElementsSet::add);
        super.sequenceOfElement = orderedElementsSet.stream().toList();
        super.lowerBound = sequenceOfElement.get(0);
        super.upperBound = sequenceOfElement.get(sequenceOfElement.size()-1);
    }


    /**
     * binary search in O( log n )
     * @param value
     * @return
     */
    @Override
    public double getIntervalValueClosestTo(double value) {
        if(value < this.lowerBound)
            return this.lowerBound;
        if(value > this.upperBound)
            return this.upperBound;

        int lo = 0;
        int hi = sequenceOfElement.size() - 1;

        while (lo <= hi) {
            int mid = (hi + lo) / 2;
            double midValue = sequenceOfElement.get(mid);
            if (value < midValue) {
                hi = mid - 1;
            } else if (value > midValue) {
                lo = mid + 1;
            } else {
                return midValue;
            }
        }
        // lo == hi + 1

        return (sequenceOfElement.get(lo) - value) < (value - sequenceOfElement.get(hi)) ? sequenceOfElement.get(lo) : sequenceOfElement.get(hi);

    }

    @Override
    public Interval getDeepCopy() {
        return new DiscreteSetInterval(super.getId(),super.sequenceOfElement.stream().mapToDouble(d -> d).toArray());
    }

}
