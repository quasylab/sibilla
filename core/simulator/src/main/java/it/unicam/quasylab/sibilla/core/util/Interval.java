/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 *             Copyright (C) 2020.
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package it.unicam.quasylab.sibilla.core.util;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents an interval.
 */
public class Interval {

    private final double start;

    private final double end;


    /**
     * Creates an interval starting from <code>start</code> and ending at <code>end</code>.
     *
     * @param start starting point of the interval.
     * @param end ending point of the interval.
     */
    public Interval(double start, double end) {
        if (start>=end) {
            throw new IllegalArgumentException("Illegal parameters for positive interval.");
        }
        this.start = start;
        this.end = end;
    }


    /**
     * Creates an interval starting from <code>start</code> and ending at {@link Double#POSITIVE_INFINITY}.
     *
     * @param start starting point of the interval.
     */
    public Interval(double start) {
        this(start, Double.POSITIVE_INFINITY);
    }

    /**
     * Creates an interval starting from <code>0</code> and ending at {@link Double#POSITIVE_INFINITY}.
     */
    public Interval() {
        this(0);
    }

    /**
     * Returns the shift of this interval by the given one, if it exists. Given two intervals <code>I=[a,b)</code> and
     * <code>J=[c,d)</code> such that <code>c<b</code>, the shift of <code>I</code> by <code>J</code> is the interval starting at
     * <code>Math.max(0.0, a-d)</code> and ending at <code>Math.max(0,b-c)</code>.
     *
     * @param other the interval used to compute the shift.
     * @return the shift of this interval by the given parameter if it exists.
     */
    public Optional<Interval> shiftBack(Interval other) {
        if (other.start>=this.end()) {
            return Optional.empty();
        } else {
            return Optional.of(new Interval(Math.max(0.0, this.start-other.end), Math.max(0.0, this.end-other.start)));
        }
    }

    /**
     * Returns true if this interval starts at the given value.
     *
     * @param x a double value.
     * @return true if this interval starts at the given value.
     */
    boolean startsAt(double x) {
        return this.start == x;
    }

    /**
     * Returns true if this interval ends at the given value.
     *
     * @param x a double value.
     * @return true if this interval ends at the given value.
     */
    boolean endsAt(double x) {
        return this.end == x;
    }

    /**
     * Returns true if the given value is inside the interval.
     *
     * @param x a double value.
     * @return true if the given value is inside the interval.
     */
    public boolean contains(double x) {
        return (this.start<=x)&&(x<this.end);
    }

    /**
     * Returns true if this interval occurs before the given value, namely that every value in the interval is less
     * it.
     *
     * @param x a double value.
     * @return true if this interval occurs before the given value.
     */
    public boolean isBefore(double x) {
        return x>this.end;
    }

    /**
     * Returns true if this interval occurs after the given value, namely that every value in the interval is greater
     * than it.
     *
     * @param x a double value.
     * @return true if this interval occurs after the given value.
     */
    public boolean isAfter(double x) {
        return x>this.start;
    }

    /**
     * Returns, if it exists, the portion of this interval starting from the given value.
     *
     * @param d a double value.
     * @return the portion of this interval ending at the given value.
     */
    public Optional<Interval> splitAfter(double d) {
        if (d>=this.end) {
            return Optional.empty();
        } else {
            return Optional.of(new Interval(d, this.end));
        }
    }

    /**
     * Returns, if it exists, the portion of this interval ending at the given value.
     *
     * @param d a double value.
     * @return the portion of this interval ending at the given value.
     */
    public Optional<Interval> splitBefore(double d) {
        if (d<=this.start) {
            return Optional.empty();
        } else {
            return Optional.of(new Interval(this.start, d));
        }
    }

    /**
     * Returns true if this interval <i>is before</i> the given one.
     *
     * @param other an interval.
     * @return true if this interval <i>is before</i> the given one.
     */
    public boolean isBefore(Interval other) {
        return this.end<=other.start;
    }

    /**
     * Returns true if this interval <i>is after</i> the given one.
     *
     * @param other an interval.
     * @return true if this interval <i>is after</i> the given one.
     */
    public boolean isAfter(Interval other) {
        return this.start>other.end;
    }

    /**
     * Returns true if this interval <i>meets</i> the given one, namely whenever
     * the end of this interval is equal to the start of the other.
     *
     * @param other an interval.
     * @return true if this interval <i>meets</i> the given one.
     */
    public boolean meets(Interval other) {
        return Double.compare(this.end, other.start)==0;
    }

    /**
     * Returns true if this interval <i>overlaps</i> the given one.
     *
     * @param other an interval.
     * @return true if this interval <i>overlaps</i> the given one.
     */
    public boolean overlaps(Interval other) {
        return (this.start<other.start) && (this.end<other.end);
    }

    /**
     * Returns true if this interval <i>contains</i> the given one.
     *
     * @param other an interval.
     * @return true if this interval <i>contains</i> the given one.
     */
    public boolean contains(Interval other) {
        return (this.start<other.start) && (other.end<this.end);
    }

    /**
     * Returns true if this interval <i>starts</i> the given one.
     *
     * @param other an interval.
     * @return true if this interval <i>starts</i> the given one.
     */
    public boolean starts(Interval other) {
        return (this.start==other.start) && (this.end<other.end);
    }

    /**
     * Returns true if this interval <i>finishes</i> the given one.
     *
     * @param other an interval.
     * @return true if this interval <i>finishes</i> the given one.
     */
    public boolean finishes(Interval other) {
        return (this.start<other.start) && (this.end==other.end);
    }


    /**
     * Returns the interval obtained by joining this interval with the given one.
     *
     * @param other an interval.
     * @return the interval obtained by joining this interval with the given one.
     */
    public Optional<Interval> join(Interval other) {
        if (this.equals(other)||this.contains(other)) {
            return Optional.of(this);
        }
        if (other.contains(this)) {
            return Optional.of(other);
        }
        if ((this.isBefore(other) && (this.end !=other.start ))||this.isAfter(other)) {
            return Optional.empty();
        }
        return Optional.of(new Interval(Math.min(this.start, other.start), Math.max(this.end, other.end)));
    }

    /**
     * Returns the interval obtained by the intersection of this interval with the given one.
     *
     * @param other an interval.
     * @return the intersection of this interval with the given one.
     */
    public Optional<Interval> intersect(Interval other) {
        if (this.equals(other)||other.contains(this)) {
            return Optional.of(this);
        }
        if (this.contains(this)) {
            return Optional.of(other);
        }
        if (this.meets(other)||other.meets(this)||this.isBefore(other)||this.isAfter(other)) {
            return Optional.empty();
        }
        return Optional.of(new Interval(Math.max(this.start, other.start), Math.min(this.end, other.end)));
    }

    /**
     * Returns the start point of this interval.
     *
     * @return  the start point of this interval.
     */
    public double start() {
        return start;
    }

    /**
     * Returns the end point of this interval.
     *
     * @return the end point of this interval.
     */
    public double end() {
        return end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Interval interval = (Interval) o;
        return Double.compare(interval.start, start) == 0 && Double.compare(interval.end, end) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }

    @Override
    public String toString() {
        return " [ " + start + " , " + end +" ) ";
    }
}
