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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a sequence of (disjoint) unitary intervals.
 */
public class SequenceOfPositiveIntervals {

    private final LinkedList<Interval> intervals = new LinkedList<>();


    /**
     * Create an empty sequence.
     *
     */
    public SequenceOfPositiveIntervals() {}

    public static SequenceOfPositiveIntervals of(List<Interval> intervals) {
        SequenceOfPositiveIntervals sequence = new SequenceOfPositiveIntervals();
        intervals.forEach(sequence::add);
        return sequence;
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
        return intervals.getFirst().start();
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
        return intervals.getLast().end();
    }

    /**
     * Adds a positive interval at the beginning of this sequence.
     *
     * @param from start of the new positive interval.
     * @param to end of the new positive interval.
     */
    public synchronized void add(double from, double to) {
        add(new Interval(from, to));
    }

    /**
     * Adds a positive interval at the beginning of this sequence.
     *
     * @param interval an interval.
     */
    public void add(Interval interval) {
        if (intervals.isEmpty()) {
            intervals.add(interval);
        } else {
            Interval currentLast = intervals.getLast();
            if (interval.start()>=currentLast.start()) {
                Optional<Interval> optionalInterval = currentLast.join(interval);
                if (optionalInterval.isPresent()) {
                    intervals.removeLast();
                    intervals.add(optionalInterval.get());
                } else {
                    intervals.addLast(interval);
                }
            } else {
                throw new IllegalArgumentException("Added interval must either meet of be after the last one in the sequence!");
            }
        }
    }

    public SequenceOfPositiveIntervals negate() {
        SequenceOfPositiveIntervals result = new SequenceOfPositiveIntervals();
        double last = 0.0;
        for (Interval current: this.intervals) {
            if (!current.startsAt(last)) {
                result.add(last, current.start());
            }
            last = current.end();
        }
        if (last != Double.POSITIVE_INFINITY) {
            result.add(last, Double.POSITIVE_INFINITY);
        }
        return result;
    }

    /**
     * Returns the sequence obtained from this one by shifting each positive interval by the given interval.
     * @param interval a positive interval.
     * @return the sequence obtained from this one by shifting each positive interval by the given interval.
     */
    public SequenceOfPositiveIntervals shift(Interval interval) {
        SequenceOfPositiveIntervals result = new SequenceOfPositiveIntervals();
        for(Interval current: this.intervals) {
            current.shiftBack(interval).ifPresent(result::add);
        }
        return result;
    }


    public boolean[] getValuesAt(double[] steps) {
        boolean[] result = new boolean[steps.length];
        Iterator<Interval> iterator = iterator();
        Interval current = (iterator.hasNext()?iterator.next():null);
        for(int i=0; i<steps.length; i++) {
            while ((current != null)&&current.isBefore(steps[i])&& iterator().hasNext()) {
                current = (iterator.hasNext()?iterator.next():null);
            }
            result[i] = (current != null) && current.contains(steps[i]);
        }
        return result;
    }


    /**
     * Returns the trajectory obtained by applying boolean conjunction of this trajectory with the one passed as
     * parameter.
     *
     * @param other another trajectory.
     * @return the trajectory obtained by applying boolean conjunction of this trajectory with the one passed as
     * parameter.
     */
    public SequenceOfPositiveIntervals computeConjunction(SequenceOfPositiveIntervals other) {
        if (this.isEmpty()) {
            return other;
        }
        if (other.isEmpty()) {
            return this;
        }
        return new ConjunctionMerger(this, other).getResult();
    }

    /**
     * Returns true if this sequence is empty.
     *
     * @return true if this sequence is empty.
     */
    private boolean isEmpty() {
        return intervals.isEmpty();
    }

    /**
     * Returns an iterator over the intervals in this sequence.
     *
     * @return an iterator over the intervals in this sequence.
     */
    private Iterator<Interval> iterator() {
        return intervals.iterator();
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
        if (this.isEmpty()) { return this; }
        if (other.isEmpty()) { return other; }
        return new DisjunctionMerger(this, other).getResult();
    }

    /**
     * Returns the number of intervals in this sequence.
     *
     * @return the number of intervals in this sequence.
     */
    public int size() {
        return intervals.size();
    }

    /**
     * Returns the interval at the specified position in this sequence.
     *
     * @param i index of interval to return.
     * @return the interval at the specified position in this sequence.
     * @throws IndexOutOfBoundsException – if i is out of range <code>(index < 0 || index >= size())</code>
     */
    public Interval get(int i) {
        return intervals.get(i);
    }

    /**
     * Returns the time points where the
     *
     * @return
     */
    public Set<Double> timePoints() {
        if (this.intervals.isEmpty()) {
            return Set.of();
        }
        return Stream.concat(intervals.stream().map(Interval::start), intervals.stream().map(Interval::end)).collect(Collectors.toSet());
    }

    private static class ConjunctionMerger {

        private final Iterator<Interval> leftIterator;

        private final Iterator<Interval> rightIterator;

        private Interval currentLeft;
        private Interval currentRight;
        private Interval currentResult;

        private SequenceOfPositiveIntervals result;

        public ConjunctionMerger(SequenceOfPositiveIntervals left, SequenceOfPositiveIntervals right) {
            leftIterator = left.iterator();
            rightIterator = right.iterator();
        }

        public SequenceOfPositiveIntervals getResult() {
            if (result == null) {
                compute();
            }
            return result;
        }


        private void compute() {
            result = new SequenceOfPositiveIntervals();
            while (((currentLeft != null)||leftIterator.hasNext())&&
                    ((currentRight != null)||rightIterator.hasNext())) {
                advanceLeft();
                advanceRight();
                computeConjunction();
                saveAndUpdate();
            }
        }

        private void saveAndUpdate() {
            if ((currentResult != null)&&(currentLeft != null)) {
                currentLeft.splitAfter(currentResult.end()).ifPresentOrElse(i -> currentLeft = i, () -> currentLeft = null);
            }
            if ((currentResult != null)&&(currentRight != null)) {
                currentRight.splitAfter(currentResult.end()).ifPresentOrElse(i -> currentRight = i, () -> currentRight = null);
            }
            if (currentResult != null) {
                result.add(currentResult);
                currentResult = null;
            }
        }

        private void computeConjunction() {
            if (currentLeft.isBefore(currentRight)) {
                currentLeft = null;
                return ;
            }
            if (currentRight.isBefore(currentLeft)) {
                currentRight = null;
                return ;
            }
            currentLeft.intersect(currentRight).ifPresent(interval -> currentResult = interval);
        }

        private void advanceLeft() {
            if ((currentLeft == null)&&(leftIterator.hasNext())) {
                currentLeft = leftIterator.next();
            }
        }

        private void advanceRight() {
            if ((currentRight == null)&&rightIterator.hasNext()) {
                currentRight = rightIterator.next();
            }
        }

    }


    private static class DisjunctionMerger {

        private final Iterator<Interval> leftIterator;

        private final Iterator<Interval> rightIterator;

        private Interval currentLeft;
        private Interval currentRight;
        private Interval currentResult;

        private SequenceOfPositiveIntervals result;

        public DisjunctionMerger(SequenceOfPositiveIntervals left, SequenceOfPositiveIntervals right) {
            leftIterator = left.iterator();
            rightIterator = right.iterator();
        }

        public SequenceOfPositiveIntervals getResult() {
            if (result == null) {
                compute();
            }
            return result;
        }


        private void compute() {
            result = new SequenceOfPositiveIntervals();
            advanceLeft();
            advanceRight();
            while ((currentLeft!=null)||(currentRight!=null)) {
                if (currentResult == null) {
                    computeDisjunction();
                } else {
                    mergeIntervals();
                }
                saveResult();
            }
        }

        private void saveResult() {
            if (((currentLeft == null)||(currentResult.isBefore(currentLeft)))
                    ||((currentRight == null)||(currentResult.isBefore(currentRight)))) {
                result.add(currentResult);
                currentResult = null;
            }
        }

        private void mergeIntervals() {
            if (currentLeft != null) {
                Optional<Interval> optionalInterval = currentResult.join(currentLeft);
                if (optionalInterval.isPresent()) {
                    currentResult = optionalInterval.get();
                    advanceLeft();
                }
            }
            if (currentRight != null) {
                Optional<Interval> optionalInterval = currentResult.join(currentRight);
                if (optionalInterval.isPresent()) {
                    currentResult = optionalInterval.get();
                    advanceRight();
                }
            }
        }

        private void computeDisjunction() {
            if ((currentLeft != null)&&((currentRight == null)||currentLeft.isBefore(currentRight))) {
                currentResult = currentLeft;
                advanceLeft();
                return ;
            }
            if ((currentRight != null)&&((currentLeft == null)||currentRight.isBefore(currentLeft))) {
                currentResult = currentRight;
                advanceRight();
                return ;
            }
            currentResult = currentLeft.join(currentRight).get();
            advanceLeft();
            advanceRight();
        }

        private void advanceLeft() {
            if (leftIterator.hasNext()) {
                currentLeft = leftIterator.next();
            } else {
                currentLeft = null;
            }
        }

        private void advanceRight() {
            if (rightIterator.hasNext()) {
                currentRight = rightIterator.next();
            } else {
                currentRight = null;
            }
        }

    }


}
