/*
 *  Sibilla:  a Java framework designed to support analysis of Collective
 *  Adaptive Systems.
 *
 *              Copyright (C) ${YEAR}.
 *
 *  See the NOTICE file distributed with this work for additional information
 *  regarding copyright ownership.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *    or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package it.unicam.quasylab.sibilla.tools.stl;

import it.unicam.quasylab.sibilla.core.simulator.sampling.Sample;
import it.unicam.quasylab.sibilla.core.util.Signal;
import it.unicam.quasylab.sibilla.core.util.SignalSegment;
import java.util.Deque;
import java.util.LinkedList;

/**
 * The SlidingWindow class implements a sliding window algorithm for signal processing.
 * It maintains a window of samples and updates the signal based on the samples within the window.
 */
public class SlidingWindow {
    private final double from;
    private final double to;
    private final Deque<Sample<Double>> content = new LinkedList<>();
    private double windowEnd = Double.NaN;
    private Sample<Double> lastRemoved;

    /**
     * Constructs a SlidingWindow with the specified start and end times.
     *
     * @param from The start time of the sliding window.
     * @param to The end time of the sliding window.
     * @throws IllegalArgumentException if 'from' is greater than or equal to 'to'.
     */
    public SlidingWindow(double from, double to) {
        if (from >= to) {
            throw new IllegalArgumentException("'from' must be less than 'to'");
        }
        this.from = from;
        this.to = to;
    }

    /**
     * Applies the sliding window algorithm to the given signal.
     *
     * @param s The input signal to process.
     * @return A new Signal object representing the processed signal.
     */
    public Signal apply(Signal s) {
        Signal result = new Signal();
        Signal extractedSignal = s.extract(from);
        for (SignalSegment sample : extractedSignal) {
            add(result, sample);
        }
        end(result, s.getEnd());
        return result;
    }

    /**
     * Calculates the size of the sliding window.
     *
     * @return The size of the sliding window.
     */
    private double getSize() {
        return to - from;
    }

    /**
     * Adds a new sample from a SignalSegment to the sliding window.
     *
     * @param result The result signal being built.
     * @param sample The SignalSegment containing the new sample.
     */
    private void add(Signal result, SignalSegment sample) {
        add(result, sample.getFrom(), sample.getValue());
    }

    /**
     * Adds a new sample to the sliding window.
     *
     * @param result The result signal being built.
     * @param nextTime The time of the new sample.
     * @param nextValue The value of the new sample.
     */
    private void add(Signal result, double nextTime, double nextValue) {
        removeOutdatedSamples(result, nextTime);
        addNewSample(nextTime, nextValue);
    }

    /**
     * Removes samples that are no longer within the sliding window.
     *
     * @param result The result signal being built.
     * @param nextTime The time of the next sample to be added.
     */
    private void removeOutdatedSamples(Signal result, double nextTime) {
        //while (!content.isEmpty() && (Math.log(getFirstTimeInWindow() + getSize()) < Math.log(nextTime))) {
        while (!content.isEmpty() && (getFirstTimeInWindow() + getSize() < nextTime)) {
            Sample<Double> firstSample = content.removeFirst();
            result.add(firstSample.getTime() - from, firstSample.getValue());
            lastRemoved = firstSample;
            reinsertIfNeeded(nextTime);
        }
    }



    /**
     * Reinserts the last removed sample if necessary to maintain window consistency.
     *
     * @param nextTime The time of the next sample to be added.
     */
    private void reinsertIfNeeded(double nextTime) {
        if (content.isEmpty()) {
            content.add(new Sample<>(nextTime - getSize(), lastRemoved.getValue()));
        } else {
            double delta1 = getFirstTimeInWindow() - lastRemoved.getTime();
            double delta2 = nextTime - windowEnd;
            if (delta1 > delta2) {
                content.addFirst(new Sample<>(lastRemoved.getTime() + delta2, lastRemoved.getValue()));
            }
        }
    }

    /**
     * Adds a new sample to the sliding window, removing any samples with lower or equal values.
     *
     * @param nextTime The time of the new sample.
     * @param nextValue The value of the new sample.
     */
    private void addNewSample(double nextTime, double nextValue) {
        double timeOfInsertedElement = nextTime;
        while (!content.isEmpty() && (getLastValue() <= nextValue)) {
            Sample<Double> lastSampling = content.removeLast();
            timeOfInsertedElement = lastSampling.getTime();
        }
        content.add(new Sample<>(timeOfInsertedElement, nextValue));
        windowEnd = nextTime;
    }

    /**
     * Finalizes the sliding window processing and adds any remaining samples to the result signal.
     *
     * @param result The result signal being built.
     * @param end The end time of the signal.
     */
    private void end(Signal result, double end) {
        if (end > windowEnd) {
            add(result, end, getLastValue());
            if (getFirstTimeInWindow() + getSize() >= windowEnd) {
                Sample<Double> firstSample = content.getFirst();
                result.add(firstSample.getTime() - from, firstSample.getValue());
            }
        }
    }

    /**
     * Gets the time of the first sample in the window.
     *
     * @return The time of the first sample, or Double.NaN if the window is empty.
     */
    private double getFirstTimeInWindow() {
        return content.isEmpty() ? Double.NaN : content.getFirst().getTime();
    }

    /**
     * Gets the value of the last sample in the window.
     *
     * @return The value of the last sample, the value of the last removed sample if the window is empty,
     *         or Double.NaN if there are no samples.
     */
    private double getLastValue() {
        if (!content.isEmpty()) return content.getLast().getValue();
        if (lastRemoved != null) return lastRemoved.getValue();
        return Double.NaN;
    }
}