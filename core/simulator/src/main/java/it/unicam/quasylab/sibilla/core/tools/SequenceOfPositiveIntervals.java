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

package it.unicam.quasylab.sibilla.core.tools;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Represents a sequence of (disjoint) unitary intervals. Each sequence has a start starts
 */
public class SequenceOfPositiveIntervals {

    private final double start;
    private final double end;
    private final LinkedList<UnitaryInterval> intervals = new LinkedList<>();


    /**
     * Creates an empty sequence of positive intervals interval starting from <code>0.0</code> and ending in plus infinite.
     */
    public SequenceOfPositiveIntervals() {
        this(0.0);
    }

    /**
     * Create an empty sequence from start to positive infinite.
     *
     * @param start a non negative value indicating the beginning of the trajectory.
     */
    public SequenceOfPositiveIntervals(double start) {
        this(Math.max(0, start), Double.POSITIVE_INFINITY);
    }

    /**
     * Create an empty sequence from start to end.
     *
     * @param start beginning of the sequence.
     * @param end end of the sequence.
     */
    public SequenceOfPositiveIntervals(double start, double end) {
        this.start = start;
        this.end = end;
    }

    /**
     * Return the beginning of the signal.
     *
     * @return the beginning of the signal.
     */
    public double getStart() {
        return start;
    }

    /**
     * Return the end of the signal.
     *
     * @return the end of the signal.
     */
    public double getEnd() {
        return end;
    }

    /**
     * Return the first positive time point or Double.NaN if the signal is empty.
     *
     * @return the first positive time point or Double.NaN if the signal is empty.
     */
    public double firstPositive() {
        if (intervals.isEmpty()) {
            return Double.NaN;
        }
        return intervals.getFirst().lowerBound;
    }

    /**
     * Return the last positive time point or Double.NaN if the signal is empty.
     *
     * @return the last positive time point or Double.NaN if the signal is empty.
     */
    public double lastPositive() {
        if (intervals.isEmpty()) {
            return Double.NaN;
        }
        return intervals.getLast().upperBound;
    }

    /**
     * Add a positive interval starting from the beginning of the signal.
     *
     * @param from start of the new positive interval.
     * @param to end of the new positive interval.
     * @return true if the interval has been added.
     */
    public synchronized boolean add(double from, double to) {
        UnitaryInterval interval = getPositiveInterval(from, to);
        if (interval == null) {
            return false;
        }
        return doAdd(interval);
    }

    /**
     * Append a positive interval at the end of the signal.
     *
     * @param from start of the new positive interval.
     * @param to end of the new positive interval.
     * @return true if the interval has been added.
     */
    public synchronized boolean append(double from, double to) {
        UnitaryInterval interval = getPositiveInterval(from, to);
        if (interval == null) {
            return false;
        }
        return doAppend(interval);
    }

    private boolean doAdd(UnitaryInterval interval) {
        LinkedList<UnitaryInterval> queue = new LinkedList<>();
        UnitaryInterval current = interval;
        while (!intervals.isEmpty()&&!interval.isBefore(intervals.peek())) {
            UnitaryInterval first = intervals.removeFirst();
            if (interval.isAfter(current)) {
                queue.add(first);
            } else {
                current = current.merge(first);
            }
        }
        intervals.addFirst(current);
        while (!queue.isEmpty()) {
            intervals.add(queue.removeLast());
        }
        return true;
    }

    private boolean doAppend(UnitaryInterval interval) {
        LinkedList<UnitaryInterval> queue = new LinkedList<>();
        UnitaryInterval current = interval;
        while (!intervals.isEmpty()&&!interval.isAfter(intervals.peekLast())) {
            UnitaryInterval last = intervals.removeLast();
            if (interval.isBefore(current)) {
                queue.add(last);
            } else {
                current = current.merge(last);
            }
        }
        intervals.addFirst(current);
        intervals.addAll(queue);
        return true;
    }

    private UnitaryInterval getPositiveInterval(double from, double to) {
        double lowerBound = getInSignalValue(from);
        double upperBound = getInSignalValue(to);
        if (lowerBound>=upperBound) {
            return null;
        }
        return new UnitaryInterval(lowerBound, upperBound);
    }

    private double getInSignalValue(double v) {
        return Math.max(start, Math.min(v, end));
    }

    /**
     * Return the trajectory obtained by applying boolean conjunction of this trajectory with the one passed as
     * parameter.
     *
     * @param other another trajectory.
     * @return the trajectory obtained by applying boolean conjunction of this trajectory with the one passed as
     * parameter.
     */
    public SequenceOfPositiveIntervals computeConjuction(SequenceOfPositiveIntervals other) {
        if (other == null) { return this; }
        SequenceOfPositiveIntervals result = new SequenceOfPositiveIntervals(Math.min(this.start, other.end), Math.max(this.end, other.end));
        Iterator<UnitaryInterval> thisIterator = this.intervals.iterator();
        Iterator<UnitaryInterval> otherIterator = other.intervals.iterator();
        UnitaryInterval thisCurrent = (thisIterator.hasNext()?thisIterator.next():null);
        UnitaryInterval otherCurrent = (otherIterator.hasNext()?otherIterator.next():null);
        while ((thisCurrent!=null)&&(otherCurrent!=null)) {
            if (thisCurrent.isBefore(otherCurrent)) {
                thisCurrent = (thisIterator.hasNext()?thisIterator.next():null);
                continue;
            }
            if (otherCurrent.isBefore(thisCurrent)) {
                otherCurrent = (otherIterator.hasNext()?otherIterator.next():null);
                continue;
            }

            result.doAppend(new UnitaryInterval(Math.min(thisCurrent.lowerBound, otherCurrent.lowerBound), Math.max(thisCurrent.lowerBound, otherCurrent.upperBound)));
            thisCurrent = (thisIterator.hasNext()?thisIterator.next():null);
            otherCurrent = (otherIterator.hasNext()?otherIterator.next():null);
        }
        return result;
    }

    /**
     * Return the trajectory obtained by applying boolean disjunction of this trajectory with the one passed as
     * parameter.
     *
     * @param other another trajectory.
     * @return the trajectory obtained by applying boolean conjunction of this trajectory with the one passed as
     * parameter.
     */
    public SequenceOfPositiveIntervals computeDisjunction(SequenceOfPositiveIntervals other) {
        if (other == null) { return this; }
        SequenceOfPositiveIntervals result = new SequenceOfPositiveIntervals(Math.min(this.start, other.end), Math.max(this.end, other.end));
        Iterator<UnitaryInterval> thisIterator = this.intervals.iterator();
        Iterator<UnitaryInterval> otherIterator = other.intervals.iterator();
        UnitaryInterval thisCurrent = (thisIterator.hasNext()?thisIterator.next():null);
        UnitaryInterval otherCurrent = (otherIterator.hasNext()?otherIterator.next():null);
        while ((thisCurrent!=null)||(otherCurrent!=null)) {
            if ((thisCurrent!=null)&&(thisCurrent.isBefore(otherCurrent))) {
                result.doAppend(thisCurrent);
                thisCurrent = (thisIterator.hasNext()?thisIterator.next():null);
                continue;
            }
            if ((otherCurrent!=null)&&(otherCurrent.isBefore(thisCurrent))) {
                result.doAppend(otherCurrent);
                otherCurrent = (otherIterator.hasNext()?otherIterator.next():null);
                continue;
            }
            result.doAppend(thisCurrent.merge(otherCurrent));
            thisCurrent = (thisIterator.hasNext()?thisIterator.next():null);
            otherCurrent = (otherIterator.hasNext()?otherIterator.next():null);
        }
        return result;
    }


    /**
     * Represent an interval where a given property is true. All the intervals are closed on left, and
     * opened on right.
     */
    public static class UnitaryInterval {

        private final double lowerBound;
        private final double upperBound;

        /**
         * Create a new interval from lowerBound to upperBound.
         *
         * @param lowerBound lowerBound of the interval.
         * @param upperBound upperBound of the interval.
         */
        public UnitaryInterval(double lowerBound, double upperBound) {
            if (lowerBound >= upperBound) {
                throw new IllegalArgumentException("Lower bound must be less than upper bound.");
            }
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }

        /**
         * Return true if this interval is before the one received as an input.
         *
         * @param other an interval.
         * @return  true if this interval is before the one received as an input.
         */
        public boolean isBefore(UnitaryInterval other) {
            return (other == null)||(this.upperBound <= other.lowerBound);
        }

        /**
         * Return true if this interval is after the one received as input.
         *
         * @param other an interval.
         * @return  true if this interval is after the one received as an input.
         */
        public boolean isAfter(UnitaryInterval other) {
            return (other == null)||(this.lowerBound >= other.upperBound);
        }

        /**
         * Return true if this interval intersects the one received as input.
         *
         * @param other an interval.
         * @return  true if this interval intersects the one received as an input.
         */
        public boolean intersect(UnitaryInterval other) {
            if (other == null) { return false; }
            double min = Math.max(this.lowerBound,other.lowerBound);
            double max = Math.max(this.upperBound, other.upperBound);
            return min < max;
        }

        /**
         * Compute the intersection of this interval with the one received as input. Null is returned if
         * this intersection is empty.
         *
         * @param other an interval.
         * @return the intersection of this interval with the one received as input.
         */
        public UnitaryInterval intersection(UnitaryInterval other) {
            if (other == null) { return null; }
            double min = Math.max(this.lowerBound,other.lowerBound);
            double max = Math.min(this.upperBound, other.upperBound);
            if (min < max) {
                return new UnitaryInterval(min, max);
            } else {
                return null;
            }
        }

        /**
         * Compute the intersection of this interval with the one received as input. Null is returned if
         * this intersection is empty.
         *
         * @param other an interval.
         * @return the intersection of this interval with the one received as input.
         */
        public UnitaryInterval merge(UnitaryInterval other) {
            if (other == null) { return this; }
            if (!this.intersect(other)) { return null; }
            double min = Math.min(this.lowerBound,other.lowerBound);
            double max = Math.max(this.upperBound, other.upperBound);
            if (min < max) {
                return new UnitaryInterval(min, max);
            } else {
                return null;
            }
        }
    }

}
