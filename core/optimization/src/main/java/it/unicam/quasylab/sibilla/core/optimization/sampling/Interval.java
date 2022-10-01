package it.unicam.quasylab.sibilla.core.optimization.sampling;


import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import static it.unicam.quasylab.sibilla.core.optimization.Constants.*;

/**
 * A simple class that represent an interval between two numbers
 * An Interval have :
 * <ul>
 * <li>An identifier
 * <li>A lower bound
 * <li>an upper bound
 * </ul>
 *
 * you can also specify whether the interval is continuous or discrete
 *
 * @author      Lorenzo Matteucci (lorenzo.matteucci@unicam.it)
 */
public class Interval {
    private final String id;
    private double lowerBound;
    private double upperBound;
    private final boolean isContinuous;
    private double length;
    private double center;
    private static final AtomicLong idCounter = new AtomicLong();

    public Interval(String id, double lowerBound, double upperBound, boolean isContinuous )
    {
        if (lowerBound >= upperBound)
            throw new IllegalArgumentException(EXCEPT_LOWER_BIGGER_THAN_UPPER);
        this.id = id;
        this.isContinuous = isContinuous;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.length = upperBound-lowerBound;
        this.center = upperBound - length/2;
    }

    public Interval(String id, double lowerBound, double upperBound){
        this(id,lowerBound,upperBound,true);
    }

    public Interval( double lowerBound, double upperBound ){
        this(DEFAULT_INTERVAL_ID+ idCounter.getAndIncrement(),lowerBound,upperBound);
    }
    public String getId() {
        return id;
    }
    public double getLowerBound() {
        return lowerBound;
    }
    public double getUpperBound() {
        return upperBound;
    }
    public boolean isContinuous() {
        return isContinuous;
    }
    public double length() {
        return length;
    }
    public void scale(double scaleFactor){
        this.length = this.length * scaleFactor;
        this.upperBound = this.center + this.length/2;
        this.lowerBound = this.center - this.length/2;
    }
    public double getRandomValue(){
        Random random = new Random();
        return random.nextDouble() * (upperBound - lowerBound) + lowerBound;
    }
    public void changeCenter(double newCenter){
        this.center = newCenter;
        this.lowerBound = newCenter-this.length/2;
        this.upperBound = newCenter+this.length/2;
    }
    public boolean contains(double value){
        return value >= this.lowerBound && value <= this.upperBound;
    }
    @Override
    public String toString() {
        String valueType = isContinuous ? "Continuous" : "Discrete";
        return "Interval [" +
                "id='" + id + '\'' +
                ", lowerBound=" + lowerBound +
                ", upperBound=" + upperBound +
                ", " + valueType +
                ']';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Interval interval = (Interval) o;
        return Double.compare(interval.lowerBound, lowerBound) == 0 &&
                Double.compare(interval.upperBound, upperBound) == 0 &&
                isContinuous == interval.isContinuous &&
                Double.compare(interval.length, length) == 0 &&
                Double.compare(interval.center, center) == 0 &&
                id.equals(interval.id);
    }
    @Override
    public int hashCode() {
        return Objects.hash(id, lowerBound, upperBound, isContinuous, length, center);
    }
}

