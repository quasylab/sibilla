package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.search;

import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.AbstractDiscreteInterval;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.Interval;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static it.unicam.quasylab.sibilla.core.optimization.Constants.EXCEPT_ILLEGAL_STEP;
public class MeshInterval extends AbstractDiscreteInterval {

    private BigDecimal pointBD;
    private BigDecimal stepBD;
    private BigDecimal lowerBoundBD;
    private BigDecimal upperBoundBD;


    public MeshInterval(String id, double lowerBound, double upperBound) {
        super(id, lowerBound, upperBound);
    }

    public MeshInterval(String id, double lowerBound, double upperBound, double step, double point) {
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

    }

    @Override
    public double getRandomValue() {
        return this.getLeftmostElement().add(stepBD.multiply(new BigDecimal(randomIntBetween(0, this.size()-1)))).doubleValue();
    }
    private int randomIntBetween(int min,int max){
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }

    @Override
    public void changeCenter(double newCenter) {

    }

    @Override
    public boolean contains(double value) {
        return false;
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
        return null;
    }
}
