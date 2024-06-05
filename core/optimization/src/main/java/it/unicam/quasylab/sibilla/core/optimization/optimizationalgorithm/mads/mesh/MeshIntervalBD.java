package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.mesh;

import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.AbstractDiscreteInterval;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.Interval;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static it.unicam.quasylab.sibilla.core.optimization.Constants.EXCEPT_ILLEGAL_STEP;
public class MeshIntervalBD extends AbstractDiscreteInterval {

    private BigDecimal pointBD;
    private BigDecimal stepBD;
    private BigDecimal lowerBoundBD;
    private BigDecimal upperBoundBD;

    /**
     * Creates a mesh interval with the specified ID, lower and upper bounds.
     * The step size and central point are not yet initialized.
     *
     * @param id         The identifier of the interval.
     * @param lowerBound The lower bound of the interval.
     * @param upperBound The upper bound of the interval.
     */
    public MeshIntervalBD(String id, double lowerBound, double upperBound) {
        super(id, lowerBound, upperBound);
    }


    /**
     * Creates a mesh interval with the specified ID, lower and upper bounds, step size, and central point.
     *
     * @param id         The identifier of the interval.
     * @param lowerBound The lower bound of the interval.
     * @param upperBound The upper bound of the interval.
     * @param step       The step size between points in the interval.
     * @param point      The central point of the interval.
     * @throws IllegalArgumentException If the step is not positive or the point is not within the bounds.
     */
    public MeshIntervalBD(String id, double lowerBound, double upperBound, double step, double point) {
        this(id,lowerBound, upperBound);
        this.setStep(step);
        this.setPoint(point);
        this.setLowerBound(lowerBound);
        this.setUpperBound(upperBound);
    }



    @Override
    public int size() {
        return (pointBD.subtract(lowerBoundBD).abs()).divideAndRemainder(stepBD)[0].intValue()
                +(upperBoundBD.subtract(pointBD).abs()).divideAndRemainder(stepBD)[0].intValue()
                +1;
    }

    private BigDecimal getLeftmostElement(){
        return lowerBoundBD.add((pointBD.subtract(lowerBoundBD)).abs().remainder(stepBD));
    }

    private BigDecimal getRightmostElement(){
        return upperBoundBD.subtract((upperBoundBD.subtract(pointBD)).abs().remainder(stepBD));
    }

    @Override
    public List<Double> getIntervalElements() {
        ArrayList<Double> listOfElements = new ArrayList<>(Stream.iterate(
                        this.pointBD,
                        d -> d.compareTo(this.lowerBoundBD) <= 0,
                        d -> d.subtract(this.stepBD))
                .mapToDouble(BigDecimal::doubleValue)
                .boxed()
                .sorted().toList());
        listOfElements.addAll(Stream.iterate(
                        this.pointBD,
                        d -> d.compareTo(this.lowerBoundBD) >= 0,
                        d -> d.subtract(this.stepBD)).skip(1)
                .mapToDouble(BigDecimal::doubleValue)
                .boxed()
                .sorted().toList());
        listOfElements.add(pointBD.doubleValue());
        listOfElements.addAll(Stream.iterate(
                        this.pointBD,
                        d -> d.compareTo(this.upperBoundBD) <= 0,
                        d -> d.add(this.stepBD)).skip(1)
                .mapToDouble(BigDecimal::doubleValue)
                .boxed()
                .sorted().toList());

        return listOfElements;
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

    public void setPoint(double newPoint){
        if(newPoint < lowerBound || newPoint >upperBound )
            throw new IllegalArgumentException(EXCEPT_ILLEGAL_STEP);
        this.pointBD = BigDecimal.valueOf(newPoint);
    }

    @Override
    public void scale(double scaleFactor) {
        BigDecimal scaledStepBD = this.stepBD.multiply(BigDecimal.valueOf(scaleFactor));
        BigDecimal scaledDistanceToLeftmostBD = this.pointBD.subtract(this.lowerBoundBD).abs().remainder(scaledStepBD);
        BigDecimal scaledNewLeftmostElementBD = this.lowerBoundBD.add(scaledDistanceToLeftmostBD);
        BigDecimal scaledDistanceToRightmostBD = this.upperBoundBD.subtract(this.pointBD).abs().remainder(scaledStepBD);
        BigDecimal scaledNewRightmostElementBD = this.upperBoundBD.subtract(scaledDistanceToRightmostBD);
        this.setStep(scaledStepBD.doubleValue());
        this.setPoint(this.getClosestValueTo(this.pointBD.doubleValue() * scaleFactor));
        this.setLowerBound(scaledNewLeftmostElementBD.doubleValue());
        this.setUpperBound(scaledNewRightmostElementBD.doubleValue());
    }

    @Override
    public double getRandomValue() {
        return this.getLeftmostElement().add(stepBD.multiply(new BigDecimal(randomIntBetween(0, this.size()-1)))).doubleValue();
    }
    @SuppressWarnings("SameParameterValue")
    private int randomIntBetween(int min,int max){
        return super.rand.nextInt((max - min) + 1) + min;
    }

    @Override
    public void changeCenter(double newCenter) {
        BigDecimal newCenterBD = BigDecimal.valueOf(newCenter);
        BigDecimal shiftBD = newCenterBD.subtract(this.pointBD);
        BigDecimal newLowerBoundBD = this.lowerBoundBD.add(shiftBD);
        BigDecimal newUpperBoundBD = this.upperBoundBD.add(shiftBD);
        this.setLowerBound(newLowerBoundBD.doubleValue());
        this.setUpperBound(newUpperBoundBD.doubleValue());
        this.setPoint(newCenter);
    }

    @Override
    public boolean contains(double value) {
        BigDecimal valueBD = BigDecimal.valueOf(value); // Convert to BigDecimal

        if (valueBD.compareTo(lowerBoundBD) < 0 || valueBD.compareTo(upperBoundBD) > 0) {
            return false;
        }

        BigDecimal distanceFromLeftmost = valueBD.subtract(getLeftmostElement()).abs();

        return distanceFromLeftmost.remainder(stepBD).compareTo(BigDecimal.ZERO) == 0;
    }

    @Override
    public double getClosestValueTo(double value) {
        if(value<this.lowerBound)
            return getLeftmostElement().doubleValue();
        if(value>this.upperBound)
            return getRightmostElement().doubleValue();
        double distance = Math.abs(value-this.getLeftmostElement().doubleValue());
        return this.getLeftmostElement().add(stepBD.multiply(new BigDecimal((int) Math.round((distance / this.stepBD.doubleValue()))))).doubleValue();
    }

    @Override
    public Interval getDeepCopy() {
        MeshIntervalBD newCopy = new MeshIntervalBD(this.getId(), this.getLowerBound(), this.getUpperBound());
        newCopy.setPoint(this.pointBD.doubleValue());
        newCopy.setStep(this.stepBD.doubleValue());
        return newCopy;
    }
}
