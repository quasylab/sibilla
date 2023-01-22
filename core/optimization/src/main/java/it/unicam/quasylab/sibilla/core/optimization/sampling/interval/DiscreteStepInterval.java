package it.unicam.quasylab.sibilla.core.optimization.sampling.interval;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static it.unicam.quasylab.sibilla.core.optimization.Constants.EXCEPT_ILLEGAL_STEP;
import static java.util.stream.Collectors.toList;


/**
 * A class that describe a discrete interval composed by a sequence of numbers, starting from a lower bound value,
 * and increments by a certain step, and stops before an upper bound including it.
 */
public class DiscreteStepInterval extends AbstractDiscreteInterval{

    private boolean startFromTheLowerBound;
    private BigDecimal stepBD;
    private BigDecimal lowerBoundBD;
    private BigDecimal upperBoundBD;
    //private double step;

    /**
     * Constructs a discrete interval composed by a sequence of numbers, starting from a lower bound value,
     * and increments by a certain step, and stops before an upper bound including it.
     *
     * @param id the interval id
     * @param lowerBound the lower limit of the range
     * @param upperBound the upper limit of the range
     * @param step the value by which the sequence of numbers is constructed starting
     *             from the lowerBound and increasing from time to time by the step value
     * @param startFromTheLowerBound if it is true, steps start from the lower bound, vice-versa steps start
     *                               from upper bound.
     *
     */
    public DiscreteStepInterval(String id, double lowerBound, double upperBound, double step, boolean startFromTheLowerBound) {
        super(id, lowerBound, upperBound);
        this.setStep(step);
        this.setLowerBound(lowerBound);
        this.setUpperBound(upperBound);
        this.startFromTheLowerBound = startFromTheLowerBound;
    }

    /**
     * Constructs a discrete interval composed by a sequence of numbers, starting from a lower bound value,
     * and increments by a certain step, and stops before an upper bound including it.
     * By default, steps start from the lower bound.
     *
     * @param id the interval id
     * @param lowerBound the lower limit of the range
     * @param upperBound the upper limit of the range
     * @param step the value by which the sequence of numbers is constructed starting
     *            from the lowerBound and increasing from time to time by the step value
     */
    public DiscreteStepInterval(String id, double lowerBound, double upperBound, double step) {
        this(id,lowerBound,upperBound,step,true);
    }

    /**
     * Constructs a discrete interval composed by a sequence of numbers, starting from a lower bound value,
     * and increments by a certain step, and stops before an upper bound including it.
     *
     * @param lowerBound the lower limit of the range
     * @param upperBound the upper limit of the range
     * @param step the value by which the sequence of numbers is constructed starting
     *            from the lowerBound and increasing from time to time by the step value
     */
    public DiscreteStepInterval(double lowerBound, double upperBound, double step, boolean startFromTheLowerBound) {
        super(lowerBound, upperBound);
        this.startFromTheLowerBound = startFromTheLowerBound;
        this.setStep(step);
        this.setLowerBound(lowerBound);
        this.setUpperBound(upperBound);
    }


    /**
     * Constructs a discrete interval composed by a sequence of numbers, starting from a lower bound value,
     * and increments by a certain step, and stops before an upper bound including it.
     *
     * @param lowerBound the lower limit of the range
     * @param upperBound the upper limit of the range
     * @param step the value by which the sequence of numbers is constructed starting
     *            from the lowerBound and increasing from time to time by the step value
     */
    public DiscreteStepInterval(double lowerBound, double upperBound, double step ) {
        super(lowerBound, upperBound);
        this.setStep(step);
        this.setLowerBound(lowerBound);
        this.setUpperBound(upperBound);
    }

    /**
     * The use of BigDecimals ensures the accuracy of the values contained in the Interval
     */
    private void generateSequenceOfElement(){
        if(super.sequenceOfElement != null)
            super.sequenceOfElement.clear();
        if(startFromTheLowerBound)
            super.sequenceOfElement = getIntervalElementsFromUpperBound();
        else
            super.sequenceOfElement = getIntervalElementsFromLowerBound();
    }

    private List<Double> getIntervalElementsFromUpperBound(){
        return Stream.iterate(
                        this.lowerBoundBD,
                        d -> d.compareTo(this.upperBoundBD) <= 0,
                        d -> d.add(this.stepBD))
                .mapToDouble(BigDecimal::doubleValue)
                .boxed()
                .sorted()
                .collect(toList());
    }

    private List<Double> getIntervalElementsFromLowerBound(){
        return Stream.iterate(
                        this.upperBoundBD,
                        d -> d.compareTo(this.lowerBoundBD) >= 0,
                        d -> d.subtract(this.stepBD))
                .mapToDouble(BigDecimal::doubleValue)
                .boxed()
                .sorted()
                .collect(toList());
    }

    @Override
    public void scale(double scaleFactor) {
        double newLength = this.length() * scaleFactor;
        this.lowerBoundBD = new BigDecimal(this.center()-newLength/2);
        this.upperBoundBD = new BigDecimal(this.center()+newLength/2);
        this.stepBD = this.stepBD.multiply(new BigDecimal(scaleFactor));
    }

    @Override
    public double getRandomValue() {
        if(this.startFromTheLowerBound)
            return this.lowerBoundBD.add(stepBD.multiply(new BigDecimal(randomIntBetween(0, this.size()-1)))).doubleValue();
        else
            return this.upperBoundBD.add(stepBD.multiply(new BigDecimal(randomIntBetween(0, this.size()-1))).negate()).doubleValue();
    }

    @SuppressWarnings("all")
    private int randomIntBetween(int min,int max){
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }

    @Override
    public void changeCenter(double newCenter) {
        this.setLowerBound(newCenter - this.length()/2);
        this.setUpperBound(newCenter + this.length()/2);
    }

    @Override
    public double length() {
        return (upperBoundBD.subtract(lowerBoundBD).doubleValue());
    }

    @Override
    public boolean contains(double value) {
       return BigDecimal.valueOf(this.length()).divideAndRemainder(BigDecimal.valueOf(value))[1].equals(BigDecimal.valueOf(0.0));
    }

    /**
     * Return value from the discrete interval that is closest
     * to the one passed as a parameter in constant
     * time complexity O(1)
     * @param value searched value
     * @return value from the discrete interval that is the closest one to parameter
     */
    public double getClosestValueTo(double value){
        if(value<this.lowerBound)
            return super.lowerBound;
        if(value>this.upperBound)
            return  super.upperBound;
        double distance = Math.abs(value-this.lowerBound);
        if(startFromTheLowerBound){
            return this.lowerBoundBD.add(stepBD.multiply(new BigDecimal((int) Math.round((distance / this.stepBD.doubleValue()))))).doubleValue();
        }else{
            distance = this.length() - distance;
            return this.upperBoundBD.subtract(stepBD.multiply(new BigDecimal((int) Math.round((distance / this.stepBD.doubleValue()))))).doubleValue();
        }
    }


    public void scaleStep(double scaleRatio){
        this.setStep(new BigDecimal(scaleRatio+"").multiply(this.stepBD));
    }

    @Override
    public Interval getDeepCopy() {
        return new DiscreteStepInterval(this.getId(),this.lowerBound,this.upperBound,this.stepBD.doubleValue(),this.startFromTheLowerBound);
    }

    @Override
    public String toString() {
        return "Discrete "+super.toString()+"\n -> Step size of : "+this.stepBD.doubleValue();
    }

    @Override
    public int size() {
       return (upperBoundBD.subtract(lowerBoundBD).abs()).divideAndRemainder(stepBD)[0].intValue()+1;
    }

    private void setLowerBound(double value){
        this.lowerBound = value;
        this.lowerBoundBD = BigDecimal.valueOf(value);
    }

    private void setUpperBound(double value){
        this.upperBound = value;
        this.upperBoundBD = BigDecimal.valueOf(value);
    }


    public void setStep(double newStepValue){
        if(newStepValue <= 0 )
            throw new IllegalArgumentException(EXCEPT_ILLEGAL_STEP);
        this.stepBD = BigDecimal.valueOf(newStepValue);
    }

    public void setStep(BigDecimal newStepValue){
        if(newStepValue.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException(EXCEPT_ILLEGAL_STEP);
        this.stepBD = newStepValue;
    }

    @Override
    public List<Double> getIntervalElements() {
        generateSequenceOfElement();
        return this.sequenceOfElement;
    }
}
