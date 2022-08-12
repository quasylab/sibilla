package it.unicam.quasylab.sibilla.core.optimization.sampling;


import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

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
    private final double lowerBound;
    private final double upperBound;
    private final boolean isContinuous;
    private final double length;
    private static final AtomicLong idCounter = new AtomicLong();

    public Interval(String id, double lowerBound, double upperBound, boolean isContinuous )
    {
        if (lowerBound >= upperBound)
            throw new IllegalArgumentException("the lower bound ( "+lowerBound+ " ) " +
                    "must be smaller than the upper bound ( "+ upperBound+ " )");
        this.id = id;
        this.isContinuous = isContinuous;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.length = upperBound-lowerBound;
    }

    public Interval(String id, double lowerBound, double upperBound){
        this(id,lowerBound,upperBound,true);
    }

    public Interval( double lowerBound, double upperBound ){
        this("V"+String.valueOf(idCounter.getAndIncrement()),lowerBound,upperBound);
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
        return Objects.equals(id, interval.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

