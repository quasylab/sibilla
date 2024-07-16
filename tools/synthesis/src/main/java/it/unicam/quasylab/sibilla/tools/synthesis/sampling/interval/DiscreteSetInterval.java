package it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval;

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

public class DiscreteSetInterval extends AbstractDiscreteInterval{


    private final TreeSet<Double> orderedElementsSet= new TreeSet<>();


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

    /**
     *
     * Add elements in the set that compose the interval extending it
     *
     * @param elements to add in the interval
     */

    public void addElements(double ... elements){
        Arrays.stream(elements).forEach(this.orderedElementsSet::add);
        super.sequenceOfElement = orderedElementsSet.stream().toList();
        super.lowerBound = sequenceOfElement.get(0);
        super.upperBound = sequenceOfElement.get(sequenceOfElement.size()-1);
    }


    @Override
    public void scale(double scaleFactor) {
        sequenceOfElement = sequenceOfElement.stream().map(element -> element*scaleFactor).toList();
        relocatesBounds();
    }

    @Override
    public double getRandomValue() {
        return  sequenceOfElement.get(rand.nextInt(this.sequenceOfElement.size()));
    }

    @Override
    public void changeCenter(double newCenter) {
        sequenceOfElement = sequenceOfElement.stream().map(element -> element+newCenter).toList();
        relocatesBounds();
    }

    @Override
    public boolean contains(double value) {
        return this.sequenceOfElement.contains(value);
    }

    @Override
    public int size() {
        return sequenceOfElement.size();
    }

    @Override
    public List<Double> getIntervalElements() {
        return sequenceOfElement;
    }

    private void relocatesBounds(){
        super.lowerBound = sequenceOfElement.get(0);
        super.upperBound = sequenceOfElement.get(sequenceOfElement.size()-1);
    }

    /**
     * Return value from the discrete interval that is closest
     * to the one passed as a parameter using binary search in O( log n )
     * @param value searched value
     * @return value from the discrete interval that is the closest one to parameter
     */
    @Override
    public double getClosestValueTo(double value) {
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

    @Override
    public String toString() {
        StringBuilder rtn = new StringBuilder("Discrete interval id=" + super.id + "\n [");
        if(sequenceOfElement.size()>0){
            if(sequenceOfElement.size()<=10){
                for (double d: sequenceOfElement) {
                    rtn.append("    ").append(d);
                }
            }else{
                rtn.append("    ").append(sequenceOfElement.get(0)).append("    ").append(sequenceOfElement.get(1)).append("  ...  ").append("    ").append(sequenceOfElement.get(sequenceOfElement.size() - 2)).append("    ").append(sequenceOfElement.get(sequenceOfElement.size() - 1));
            }
        }
        rtn.append("    ]");
        return rtn.toString();
    }

}
