package it.unicam.quasylab.sibilla.core.optimization.sampling;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class DiscreteStepInterval extends AbstractDiscreteInterval{

    private double step;

    public DiscreteStepInterval(String id, double lowerBound, double upperBound, double step) {
        super(id, lowerBound, upperBound);
        this.step = step;
        generateSequenceOfElement();
    }

    public DiscreteStepInterval(double lowerBound, double upperBound, double step) {
        super(lowerBound, upperBound);
        this.step = step;
        generateSequenceOfElement();
    }

    /**
     * The use of BigDecimals ensures the accuracy of the values contained in the Interval
     */
    private void generateSequenceOfElement(){
        super.sequenceOfElement.clear();
        super.sequenceOfElement = Stream.iterate(
                new BigDecimal(super.lowerBound +""),
                        d -> d.compareTo(new BigDecimal(super.upperBound+"")) <= 0,
                        d -> d.add(new BigDecimal(this.step+"")))
                .mapToDouble(BigDecimal::doubleValue)
                .boxed()
                .collect(toList());
    }

    /**
     * Return value from the discrete interval that is closest
     * to the one passed as a parameter in constant
     * time complexity O(1)
     * @param value searched value
     * @return value from the discrete interval that is the closest one to parameter
     */
    public double getIntervalValueClosestTo(double value){
        if(value<this.lowerBound)
            return super.sequenceOfElement.get(0);
        if(value>this.upperBound)
            return  super.sequenceOfElement.get(super.sequenceOfElement.size()-1);
        double distance = Math.abs(value-this.lowerBound);
        return super.sequenceOfElement.get((int) Math.round((distance / step)));
    }

    public void changeStepValue(double newStepValue){
        this.step = newStepValue;
        generateSequenceOfElement();
    }

    public void scaleStep(double scaleRatio){
        this.step = scaleRatio * this.step;
        generateSequenceOfElement();
    }

    public void changeStep(double newStep){
        this.step = newStep;
        generateSequenceOfElement();
    }

    @Override
    public Interval getDeepCopy() {
        return new DiscreteStepInterval(this.getId(),this.lowerBound,this.upperBound,this.step);
    }
}
