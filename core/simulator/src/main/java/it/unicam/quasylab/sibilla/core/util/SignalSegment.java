package it.unicam.quasylab.sibilla.core.util;

import java.util.Objects;
import java.util.function.DoubleUnaryOperator;

/**
 * Represents a time interval where a signal assumes a constant value. The time iterval associated to a
 * segment can be either an interval, that is closed on the left
 * and opened on the right, or a signel time point.
 *
 */
public class SignalSegment {


    private final double from;

    private double to;

    private final double value;

    private boolean rightClosed;

    /**
     * Creates a new segment starting at <code>from</code> and ending at <code>to</code> that assumes the constant
     * value <code>value</code>.
     *
     * @param from the beginning of the time interval of the signal
     * @param to the end of the time interval of the signal
     * @param value the value of the segment
     * @throws IllegalArgumentException when <code>from > to</code>.
     */
    public SignalSegment(double from, double to, boolean rightClosed, double value) {
        if (from > to) {
            throw new IllegalArgumentException();//TODO: Add message
        }
        this.from = from;
        this.to = to;
        this.value = value;
        this.rightClosed = rightClosed;
    }

    /**
     * Creates a segment consisting of a single time point.
     *
     * @param time the time point of the segment
     * @param value the value of the segment
     */
    public SignalSegment(double time, double value) {
        this(time, time, true, value);
    }

    /**
     * Returns true if the given time point is inside this segment
     *
     * @param t a time point
     * @return true if the given time point is inside this segment
     */
    public boolean contains(double t) {
        return (from <= t)&&(rightClosed?t<=to:t<to);
    }


    /**
     * Returns the first time point of the segment.
     *
     * @return the first time point of the segment.
     */
    public double getFrom() {
        return from;
    }

    /**
     * Returns the end of the signal. {@link Double#NaN} is returned if this interval consists of
     * a single time-point.
     *
     * @return the end of the signal.
     */
    public double getTo() {
        return to;
    }

    /**
     * Extend this segment to the given time value time interval.
     *
     * @param to the new end of the segment
     * @throws IllegalArgumentException when <code>(this.to > to)</code>
     */
    public void extendsInterval(double to) {
        if (this.to > to) {
            throw new IllegalArgumentException(); //TODO: Add message
        }
        if (this.to != to) {
            this.to = to;
            this.rightClosed = false;
        }
    }

    /**
     * Returns the value of the signal in this segment.
     *
     * @return the value of the signal in this segment.
     */
    public double getValue() {
        return this.value;
    }

    public boolean isAfter(double time) {
        return (rightClosed&&(to==time))||(to<time);
    }

    public void closeOnRight() {
        this.rightClosed = true;
    }

    public boolean isClosedOnRight() {
        return rightClosed;
    }

    public boolean isAPoint() {
        return this.from == this.to;
    }

    public Interval getInterval() {
        return new Interval(from, to);
    }

    public boolean overlaps(double start, double end) {
        return ((start<=this.from)&&(this.from<=end))
                ||((start<=this.to)&&(this.to<=end));
    }

    public SignalSegment apply(DoubleUnaryOperator op) {
        return new SignalSegment(from, to, rightClosed, op.applyAsDouble(value));
    }

    public SignalSegment subSegment(double from) {
        if (this.from == from) return this;
        if (from<this.to||(rightClosed&&(this.to==from))) {
            return new SignalSegment(from, this.to, rightClosed, value);
        }
        throw new IllegalArgumentException();
    }

    public double[] getTimeSteps(double from, double to) {
        if ((this.to<from)||(this.from>to)) {
            return new double[0];
        }
        if (rightClosed) {
            return new double[] {Math.max(this.from, from), Math.min(this.to, to)};
        }
        if (this.to>to) {
            return new double[] {Math.max(this.from, from), to};
        }
        return new double[] {Math.max(this.from, from)};
    }

    public double[] getTimeSteps() {
        if (rightClosed) return new double[] {from, to};
        return new double[] {from};
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SignalSegment that = (SignalSegment) o;
        return Double.compare(from, that.from) == 0 && Double.compare(to, that.to) == 0 && Double.compare(value, that.value) == 0 && rightClosed == that.rightClosed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, value, rightClosed);
    }

    @Override
    public String toString() {
        return "[ "+this.from +" , "+this.to+(rightClosed?" ]":" )")+" -> "+value;
    }
}
