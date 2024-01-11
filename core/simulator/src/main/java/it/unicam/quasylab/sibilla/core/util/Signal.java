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

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * A signal represents a (piecewise constant) function associating double values to time.
 * Each signal is identified by a starting time and an ending time.
 */
public final class Signal implements Iterable<SignalSegment> {


    private final LinkedList<SignalSegment> values;

    /**
     * Creates and empty signal.
     */
    public Signal() {
        this(new LinkedList<>());
    }

    /**
     * Creates a new signal starting from <code>start</code>, ending at <code>end</code> containing the given values.
     *
     * @param values the values in the signal.
     */
    private Signal(LinkedList<SignalSegment> values) {
        this.values = values;
    }

    public Signal(double[] times, double[] data) {
        this();
        if (times.length != data.length) {
            throw new IllegalArgumentException();
        }
        IntStream.range(0, times.length).forEach(i -> add(times[i], data[i]));
    }

    public static double[] getTimeSteps(Signal s1, Signal s2) {
        double start = Math.max(s1.getStart(), s2.getStart());
        double end = Math.min(s1.getEnd(), s2.getEnd());
        return DoubleStream.concat(Arrays.stream(s1.getTimeSteps(start, end)), Arrays.stream(s2.getTimeSteps(start, end))).distinct().sorted().toArray();
    }

    /**
     * Adds a new value in the signal at the given time.
     *
     * @param time the time at wich the signal value is added
     * @param value the value added in the signal
     * @throws IllegalArgumentException if time is not a finite value a
     */
    public void add(double time, double value) {
        if (Double.isFinite(time)&& isAfter(time)) {
            if (!this.values.isEmpty()) {
                SignalSegment last = this.values.getLast();
                last.extendsInterval(time);
                if (last.getValue() == value) {
                    last.closeOnRight();
                    return ;
                }
            }
            this.values.add(new SignalSegment(time, value));
        } else {
            throw new IllegalArgumentException();
        }
    }

    private void add(SignalSegment segment) {
        if (!this.values.isEmpty()) {
            SignalSegment last = this.values.getLast();
            if (last.isClosedOnRight()&&(last.getTo() != segment.getFrom())) throw new IllegalArgumentException();
            if (last.getValue() == segment.getValue()) {
                last.extendsInterval(segment.getTo());
                if (segment.isClosedOnRight()) {
                    last.closeOnRight();
                }
            }
        }
        this.values.add(segment);
    }

    /**
     * Returns true if the given time is after the end of this signal.
     *
     * @param time a time value.
     * @return true if the given time is after the end of this signal.
     */
    private boolean isAfter(double time) {
        if (time < 0) return false;
        if (!this.values.isEmpty()) {
            return this.values.getLast().isAfter(time);
        }
        return true;
    }


    /**
     * Returns the array of time points occurring in the signal
     *
     * @return the array of time points occurring in the signal;
     */
    public double[] getTimeSteps() {
        return this.values.stream().map(SignalSegment::getTimeSteps).flatMapToDouble(DoubleStream::of).distinct().sorted().toArray();
    }


    private double[] getTimeSteps(double from, double to) {
        return this.values.stream().filter(s -> s.overlaps(from, to)).map(s -> s.getTimeSteps(from, to)).flatMapToDouble(DoubleStream::of).distinct().sorted().toArray();
    }



    /**
     * Returns the value of the signal at the given time.
     *
     * @param time a non negative time value
     * @return the value of the signal at the given time.
     */
    public double valueAt(double time) {
        return this.values.stream().filter(s -> s.contains(time)).findFirst().map(SignalSegment::getValue).orElse(Double.NaN);
    }

    /**
     * Returns an array containing the values in the signal at the given time steps.
     * @param times a (sorted) array of time steps
     * @return an array containing the values in the signal at the given time steps.
     */
    public double[] valuesAt(double[] times) {
        double[] data = new double[times.length];
        Arrays.fill(data, Double.NaN);
        int idx = 0;
        for (SignalSegment segment : values) {
            while ((idx<times.length)&&(segment.contains(times[idx]))) {
                data[idx++] = segment.getValue();
            }
        }
        return data;
    }

    /**
     * Returns true if the given time is inside the domain of this signal.
     *
     * @param time a time value
     * @return true if the given time is inside the domain of this signal.
     */
    public boolean contains(double time) {
        return !this.values.isEmpty()&&((this.values.getFirst().getFrom()>=time)||(this.values.getLast().getTo()>=time));
    }

    public static Signal apply(Signal s1, DoubleBinaryOperator op, Signal s2) {
        double start = Math.max(s1.getStart(), s2.getStart());
        double end = Math.min(s1.getEnd(), s2.getEnd());
        double[] timeSteps = DoubleStream.concat(Arrays.stream(s1.getTimeSteps(start, end)), Arrays.stream(s2.getTimeSteps(start, end))).distinct().sorted().toArray();
        double[] valuesOfSignal1 = s1.valuesAt(timeSteps);
        double[] valuesOfSignal2 = s2.valuesAt(timeSteps);
        Signal result = new Signal();
        for (int i = 0; i < timeSteps.length; i++) {
            result.add(timeSteps[i], op.applyAsDouble(valuesOfSignal1[i], valuesOfSignal2[i]));
        }
        result.setEnd(end);
        return result;
    }


    public static Signal apply(Signal s, DoubleUnaryOperator op) {
        Signal result = new Signal();
        for (SignalSegment signalSegment : s) {
            result.add(signalSegment.apply(op));
        }
        return result;
    }

    public double[] values() {
        return this.values.stream().mapToDouble(SignalSegment::getValue).toArray();
    }


    @Override
    public Iterator<SignalSegment> iterator() {
        return values.iterator();
    }

    public double getEnd() {
        if (this.values.isEmpty()) {
            return Double.NaN;
        }
        return this.values.getLast().getTo();
    }

    public double getStart() {
        if (this.values.isEmpty()) {
            return Double.NaN;
        }
        return this.values.getFirst().getFrom();
    }

    @Override
    public String toString() {
        return this.values.stream().map(Object::toString).collect(Collectors.joining(" -- "));
    }


    public void setEnd(double end) {
        if (this.values.isEmpty()) throw new IllegalArgumentException();
        SignalSegment last = this.values.getLast();
        last.extendsInterval(end);
        last.closeOnRight();
    }


    public Signal extract(double from) {
        Signal result = new Signal();
        for (SignalSegment signalSegment : this) {
            if (signalSegment.getFrom()>=from) {
                result.add(signalSegment);
            } else {
                if (signalSegment.contains(from)) {
                    result.add(signalSegment.subSegment(from));
                }
            }
        }
        return result;
    }

    public Signal truncate(double cutoffTime){
        Signal result = new Signal();
        for(SignalSegment ss : this){
            if(ss.getTo() > cutoffTime){
                if(ss.getFrom() < cutoffTime)
                    result.add(new SignalSegment(ss.getFrom(),cutoffTime,true,ss.getValue()));
            } else
                result.add(new SignalSegment(ss.getFrom(), ss.getTo(),ss.isClosedOnRight(),ss.getValue()));

        }
        return result;
    }



}
