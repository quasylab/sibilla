package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.mesh;

import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.AbstractDiscreteInterval;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.Interval;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static it.unicam.quasylab.sibilla.core.optimization.Constants.EXCEPT_ILLEGAL_STEP;

public class MeshInterval extends AbstractDiscreteInterval {

    private double point;
    private double step;
    public MeshInterval(String id, double lowerBound, double upperBound) {
        super(id, lowerBound, upperBound);
    }

    public MeshInterval(String id, double lowerBound, double upperBound, double step, double point) {
        this(id,lowerBound, upperBound);
    }

    @Override
    public int size() {
        double size1 = Math.abs((point - lowerBound) / step);
        double size2 = Math.abs((upperBound - point) / step);
        return (int) (size1 + size2 + 1);
    }


    private double getLeftmostElement(){
        return lowerBound + Math.abs((point - lowerBound) % step);
    }

    private double getRightmostElement(){
        return upperBound - Math.abs((upperBound - point) % step);
    }

    @Override
    public List<Double> getIntervalElements() {
        ArrayList<Double> listOfElements = Stream.iterate(
                        this.point,
                        d -> d <= this.lowerBound,
                        d -> d - this.step)
                .sorted().collect(Collectors.toCollection(ArrayList::new));

        listOfElements.addAll(Stream.iterate(
                        this.point + this.step,
                        d -> d >= this.lowerBound,
                        d -> d - this.step)
                .sorted().toList());

        listOfElements.add(this.point);

        listOfElements.addAll(Stream.iterate(
                        this.point + this.step,
                        d -> d <= this.upperBound,
                        d -> d + this.step)
                .sorted().toList());

        return listOfElements;
    }

    private void setLowerBound(double value){
        this.lowerBound = value;
    }

    private void setUpperBound(double value){
        this.upperBound = value;
    }

    public void setStep(double newStepValue){
        if(newStepValue <= 0 )
            throw new IllegalArgumentException(EXCEPT_ILLEGAL_STEP);
        this.step = newStepValue;
    }

    public void setPoint(double point){
        if(point < lowerBound || point >upperBound )
            throw new IllegalArgumentException(EXCEPT_ILLEGAL_STEP);
        this.point = point;
    }

    @Override
    public void scale(double scaleFactor) {
        double newStep = step * scaleFactor;
        double newLowerBound = point - Math.abs(point - lowerBound) * scaleFactor;
        double newUpperBound = point + Math.abs(upperBound - point) * scaleFactor;
        if (newStep <= 0) {
            throw new IllegalArgumentException(EXCEPT_ILLEGAL_STEP);
        }
        setStep(newStep);
        setLowerBound(newLowerBound);
        setUpperBound(newUpperBound);
    }

    @Override
    public double getRandomValue() {
        return this.getLeftmostElement() + step * randomIntBetween(0, this.size()-1);
    }
    private int randomIntBetween(int min,int max){
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }

    @Override
    public void changeCenter(double newCenter) {
        double offset = newCenter - point;
        double newLowerBound = lowerBound + offset;
        double newUpperBound = upperBound + offset;
        setLowerBound(newLowerBound);
        setUpperBound(newUpperBound);
        setPoint(newCenter);
    }

    @Override
    public boolean contains(double value) {
        return false;
    }

    @Override
    public double getClosestValueTo(double value) {
        if (value < lowerBound) {
            return getLeftmostElement();
        } else if (value > upperBound) {
            return getRightmostElement();
        } else {
            double distance = Math.abs(value - getLeftmostElement());
            int numSteps = (int) Math.round(distance / step);
            return getLeftmostElement() + numSteps * step;
        }
    }
    @Override
    public Interval getDeepCopy() {
        return new MeshInterval(this.getId(), this.getLowerBound(), this.getUpperBound(), this.step, this.point);
    }

}
