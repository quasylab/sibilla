package it.unicam.quasylab.sibilla.core.optimization.sampling;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import static it.unicam.quasylab.sibilla.core.optimization.Constants.DEFAULT_INTERVAL_ID;
import static it.unicam.quasylab.sibilla.core.optimization.Constants.EXCEPT_LOWER_BIGGER_THAN_UPPER;

public abstract class AbstractInterval implements Interval{

    protected final String id;
    protected double lowerBound;
    protected double upperBound;
    protected Random rand = new Random();
    private static final AtomicLong idCounter = new AtomicLong();
    public AbstractInterval(String id, double lowerBound, double upperBound) {
        if (lowerBound >= upperBound)
            throw new IllegalArgumentException(EXCEPT_LOWER_BIGGER_THAN_UPPER);
        this.id = id;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public AbstractInterval(double lowerBound, double upperBound) {
        this(DEFAULT_INTERVAL_ID + idCounter.getAndIncrement(), lowerBound, upperBound);
    }

    public String getId() {
        return this.id;
    }

    public double getLowerBound() {
        return this.lowerBound;
    }

    public double getUpperBound() {
        return this.upperBound;
    }

    public boolean isContinuous() {
        return this instanceof ContinuousInterval;
    }

    public boolean isDiscrete() {
        return this instanceof AbstractDiscreteInterval;
    }

    public double length(){
        return this.upperBound-this.lowerBound;
    }

    public double center(){
        return this.upperBound - (this.length()/2);
    }

    @Override
    public String toString() {
        return "Interval [" +
                "id='" + id + '\'' +
                ", lowerBound=" + lowerBound +
                ", upperBound=" + upperBound +
                ']';
    }

}
