package it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import static it.unicam.quasylab.sibilla.tools.synthesis.Commons.DEFAULT_INTERVAL_ID;
import static it.unicam.quasylab.sibilla.tools.synthesis.Commons.EXCEPT_LOWER_BIGGER_THAN_UPPER;

public abstract class AbstractInterval implements Interval{
    protected final String id;
    protected double lowerBound;
    protected double upperBound;
    protected Random rand;
    private static final AtomicLong idCounter = new AtomicLong();

    public AbstractInterval(String id, double lowerBound, double upperBound) {
        if (lowerBound >= upperBound)
            throw new IllegalArgumentException(EXCEPT_LOWER_BIGGER_THAN_UPPER);
        this.id = id;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.rand = new Random();
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
    public void setSeed(long seed) {
        this.rand.setSeed(seed);
    }

    public static void resetCounter() {
        idCounter.set(0L);
    }

    @Override
    public String toString() {
        return "interval [" +
                "id='" + id + '\'' +
                ", lowerBound=" + lowerBound +
                ", upperBound=" + upperBound +
                ']';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractInterval that = (AbstractInterval) o;
        return Double.compare(that.lowerBound, lowerBound) == 0 && Double.compare(that.upperBound, upperBound) == 0 && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, lowerBound, upperBound);
    }
}
