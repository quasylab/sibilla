package it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval;

import java.util.List;

public abstract class AbstractDiscreteInterval extends AbstractInterval {

    protected List<Double> sequenceOfElement;

    public AbstractDiscreteInterval(String id, double lowerBound, double upperBound) {
        super(id, lowerBound, upperBound);
    }

    public AbstractDiscreteInterval(double lowerBound, double upperBound) {
        super(lowerBound, upperBound);
    }

    /**
     * Returns the number of elements in this discrete interval
     * @return elements in the discrete interval
     */
    public abstract int size();
    //    public int size(){
    //        return sequenceOfElement.size();
    //    }

//    @Override
//    public void scale(double scaleRatio){
//        sequenceOfElement = sequenceOfElement.stream().map(element -> element*scaleRatio).toList();
//        relocatesBounds();
//    }

//    @Override
//    public double getRandomValue(){
//        return  sequenceOfElement.get(rand.nextInt(this.sequenceOfElement.size()));
//    }

//    @Override
//    public void changeCenter(double newCenter){
//        sequenceOfElement = sequenceOfElement.stream().map(element -> element+newCenter).toList();
//        relocatesBounds();
//    }

//    protected void relocatesBounds(){
//        super.lowerBound = sequenceOfElement.get(0);
//        super.upperBound = sequenceOfElement.get(sequenceOfElement.size()-1);
//    }

//    @Override
//    public boolean contains(double value) {
//        return this.sequenceOfElement.contains(value);
//    }

    public abstract List<Double> getIntervalElements();

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        if (!super.equals(o)) return false;
//        AbstractDiscreteInterval that = (AbstractDiscreteInterval) o;
//        return Objects.equals(sequenceOfElement, that.sequenceOfElement);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(super.hashCode(), sequenceOfElement);
//    }
}
