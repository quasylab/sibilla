package it.unicam.quasylab.sibilla.core.util;

import java.util.Objects;
import java.util.Optional;

public class BooleanSignalSegment {

    private final double from;

    private double to;

    private boolean rightClosed;

    /**
     * Creates a new segment starting at <code>from</code> and ending at <code>to</code> that assumes the
     * value is true
     *
     * @param from the beginning of the time interval of the signal
     * @param to the end of the time interval of the signal
     * @throws IllegalArgumentException when <code>from > to</code>.
     */
    public BooleanSignalSegment(double from, double to, boolean rightClosed) {
        if (from > to) {
            throw new IllegalArgumentException();//TODO: Add message
        }
        this.from = from;
        this.to = to;
        this.rightClosed = rightClosed;
    }

    public BooleanSignalSegment(double from,double to){
        this(from,to,false);
    }

    /**
     * Creates a segment consisting of a single time point.
     *
     * @param time the time point of the segment
     */
    public BooleanSignalSegment(double time) {
        this(time, time, true);
    }


    /**
     * Returns true if this interval <i>contains</i> the given one.
     *
     * @param other an interval.
     * @return true if this interval <i>contains</i> the given one.
     */
    public boolean contains(BooleanSignalSegment other) {
        return (this.from < other.from) && (other.to < this.to);
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
     * Returns true if this interval <i>is before</i> the given one.
     *
     * @param other an interval.
     * @return true if this interval <i>is before</i> the given one.
     */
    public boolean isBefore(BooleanSignalSegment other) {
        return this.to<other.from;
    }


    public boolean isAfter(BooleanSignalSegment other) {
        return this.to<other.from;
    }

    public boolean isBefore(double time) {
        return time < from;
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


    /**
     * Returns the interval obtained by joining this interval with the given one.
     *
     * @param other an interval.
     * @return the interval obtained by joining this interval with the given one.
     */
    public Optional<BooleanSignalSegment> join(BooleanSignalSegment other) {
        if (this.equals(other)||this.contains(other)) {
            return Optional.of(this);
        }
        if (other.contains(this)) {
            return Optional.of(other);
        }
        if (this.isBefore(other)||this.isAfter(other)) {
            return Optional.empty();
        }
        return Optional.of(new BooleanSignalSegment(
                Math.min(this.from, other.from),
                Math.max(this.to, other.to),
                (this.to > other.to ? this.rightClosed : other.rightClosed )
                )
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BooleanSignalSegment that = (BooleanSignalSegment) o;
        return Double.compare(from, that.from) == 0 && Double.compare(to, that.to) == 0 && rightClosed == that.rightClosed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, rightClosed);
    }
}
