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

import it.unicam.quasylab.sibilla.core.simulator.sampling.Sample;

import java.util.*;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * A signal represents a (piecewise constant) function associating double values to time.
 * Each signal is identified by a starting time and an ending time.
 */
public final class Signal implements Iterable<Sample<Double>> {

    private double start;
    private double end;

    private Sample<Double> last;

    private final LinkedList<Sample<Double>> values;

    /**
     * Creates and empty signal.
     */
    public Signal() {
        this(Double.NaN, Double.NaN, new LinkedList<>());
    }

    /**
     * Creates a new signal starting from <code>start</code>, ending at <code>end</code> containing the given values.
     *
     * @param start the starting time of the created signal
     * @param end the ending time of the created signal
     * @param values the values in the signal.
     */
    private Signal(double start, double end, LinkedList<Sample<Double>> values) {
        this.start = start;
        this.end = end;
        this.values = values;
    }

    public Signal(double[] times, double[] data) {
        this();
        if (times.length != data.length) {
            throw new IllegalArgumentException();
        }
        IntStream.range(0, times.length).forEach(i -> add(times[i], data[i]));
    }

    /**
     * Adds a new value in the signal at the given time.
     *
     * @param time the time at wich the signal value is added
     * @param value the value added in the signal
     * @throws IllegalArgumentException if time is not a finite value a
     */
    public void add(double time, double value) {
        if (Double.isFinite(time)&&(time >= 0)&&isAfter(time)) {
            if (last == null || !Objects.equals(last.getValue(), value)) {
                this.last = new Sample<>(time,value);
                if (Double.isNaN(this.start)) {
                    this.start = time;
                }
                this.values.add(last);
            }
            this.end = time;
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Returns true if the given time is after the end of this signal.
     *
     * @param time a time value.
     * @return true if the given time is after the end of this signal.
     */
    private boolean isAfter(double time) {
        return (time>=0)&&((Double.isNaN(end)||end<time));
    }

    /**
     * Returns true if the given time is before the beginning of this signal.
     *
     * @param time a time value.
     * @return true if the given time is before the beginning of this signal.
     */
    private boolean isBefore(double time) {
        return (time>=0)&&Double.isFinite(start)&&(time<start);
    }

    /**
     * Returns the array of time points occurring in the signal
     *
     * @return the array of time points occurring in the signal;
     */
    private double[] timePoints() {
        if (Double.isNaN(start)) {
            return new double[0];
        }
        return this.values.stream().mapToDouble(Sample::getTime).toArray();
    }

    private double[] timePoints(double start, double end) {
        return this.values.stream().mapToDouble(Sample::getTime).filter(t -> (t>=start)&&(t<=end)).toArray();
    }


    /**
     * Returns the value of the signal at the given time.
     *
     * @param time a non negative time value
     * @return the value of the signal at the given time.
     */
    public double valueAt(double time) {
        if (contains(time)) {
            Sample<Double> previous = null;
            for (Sample<Double> value : values) {
                if ((previous != null)&&((previous.getTime()<=time)&&(time<value.getTime()))) {
                        return previous.getValue();
                }
                previous = value;
            }
            if (previous != null) return previous.getValue();
        }
        return Double.NaN;
    }

    /**
     * Returns an array containing the values in the signal at the given time steps.
     * @param times a (sorted) array of time steps
     * @return an array containing the values in the signal at the given time steps.
     */
    public double[] valuesAt(double[] times) {
        double[] data = new double[times.length];
        Arrays.fill(data, Double.NaN);
        OptionalInt optionalInt = IntStream.range(0, times.length).filter(j -> contains(times[j])).findFirst();
        if (optionalInt.isPresent()) {
            Sample<Double> previous = null;
            int i = optionalInt.getAsInt();
            for (Sample<Double> value : values) {
                while (previous != null && i < times.length) {
                    if (previous.getTime() <= times[i] && times[i] < value.getTime()) {
                        data[i++] = previous.getValue();
                    } else {
                        break;
                    }
                }
                previous = value;
            }
            for (; previous != null && i < times.length && !isAfter(times[i]); i++) {
                data[i] = previous.getValue();
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
        return Double.isFinite(start)&&Double.isFinite(time)&&(time>=start)&&(time<=end);
    }

    public static Signal apply(Signal s1, DoubleBinaryOperator op, Signal s2) {
        double[] times = getCommonTimeSteps(s1, s2);
        double[] values1 = s1.valuesAt(times);
        double[] values2 = s2.valuesAt(times);
        Signal result = new Signal();
        IntStream.range(0, times.length).forEach(i -> result.add(times[i], op.applyAsDouble(values1[i], values2[i])));
        result.setEnd(Math.min(s1.getEnd(), s2.getEnd()));
        return result;
    }

    public static double[] getCommonTimeSteps(Signal s1, Signal s2) {
        double start = Math.max(s1.start, s2.start);
        double end = Math.min(s1.end, s2.end);
        if (Double.isFinite(start)&&Double.isFinite(end)&&(start<=end)) {
            return DoubleStream.concat(
                            DoubleStream.of(s1.timePoints(start, end)),
                            DoubleStream.of(s2.timePoints(start, end))
                    )
                    .boxed()
                    .collect(Collectors.toCollection(LinkedHashSet::new))
                    .stream()
                    .mapToDouble(Double::doubleValue)
                    .sorted()
                    .toArray();
        } else {
            return new double[0];
        }
    }

    public static Signal apply(Signal s, DoubleUnaryOperator op) {
        double start = s.start;
        double end = s.end;
        Signal result = new Signal();
        if (Double.isFinite(start)&&Double.isFinite(end)&&(start<=end)){
            double[] times = s.timePoints();
            double[] values = s.values();
            IntStream.range(0, times.length).forEach(i -> result.add(times[i], op.applyAsDouble(values[i])));
        }
        result.setEnd(s.getEnd());
        return result;
    }

    private double[] values() {
        return this.values.stream().mapToDouble(Sample::getValue).toArray();
    }


    @Override
    public Iterator<Sample<Double>> iterator() {
        return values.iterator();
    }

    public Sample<Double> last() {
        return last;
    }

    public double getEnd() {
        return end;
    }

    public double getStart() {
        return start;
    }

    @Override
    public String toString() {
        StringBuilder valuesString = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if(values.size() != (i+1))
                valuesString.append("[")
                        .append(values.get(i).getTime()).append("->")
                        .append(values.get(i + 1).getTime()).append(") : ")
                        .append(values.get(i).getValue()).append(" -- ");
            else
                valuesString.append("[")
                        .append(values.get(i).getTime()).append("->").append(this.end)
                        .append(") : ")
                        .append(values.get(i).getValue());

        }

        return "start=" + start + ", end=" + end + "\n" + valuesString;
    }

    public void setStart(double start) {
        this.start = start;
    }

    public void setEnd(double end) {
        this.end = end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Signal samples = (Signal) o;
        return Objects.equals(values, samples.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }

//    public Signal extract(double from) {
//        Signal result = new Signal();
//        Sample<Double> previous = null;
//        boolean flag = true;
//        for (Sample<Double> sample : this) {
//            if (sample.getTime()>=from) {
//                if (flag&&(previous != null)&&(sample.getTime()>from)) {
//                    result.add(from, previous.getValue());
//                    flag = false;
//                }
//                result.add(sample.getTime(), sample.getValue());
//            }
//            previous = flag ? sample : null;
//        }
//        return result;
//    }

    public Signal extract(double from) {
        Signal result = new Signal();
        double valueAtFrom = this.valueAt(from);
        boolean flag = true;
        for (Sample<Double> sample : this) {
            if(sample.getTime()>=from){
                if(flag){
                    if(sample.getValue()==valueAtFrom)
                        result.add(from + (sample.getTime()-from), valueAtFrom);
                    else{
                        result.add(from,valueAtFrom);
                        result.add(sample.getTime(),sample.getValue());
                    }
                    flag = false;
                }else{
                    result.add(sample.getTime(), sample.getValue());
                }
            }
        }
        return result;
    }
}
