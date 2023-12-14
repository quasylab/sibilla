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

package it.unicam.quasylab.sibilla.core.util;

import it.unicam.quasylab.sibilla.core.simulator.Trajectory;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Sample;

import java.util.Map;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Instances of this class are used to store data sets. This consists of a sequence of values observed at different
 * time steps.
 */
public final class SimulationData {

    private final String name;

    private final String[] labels;

    private final double[] timeSteps;

    private final double[][] data;


    /**
     * Creates a new SimulationData with the given name, the given labels and the given data set.
     *
     * @param name name of the created data set
     * @param labels an array containing the labels of data values in this data set
     * @param timeSteps time steps at which values are observed
     * @param data a matrix of doubles containing the values in this data set
     * @throws IllegalArgumentException: when <code>data.length != timeSteps.length</code>
     */
    private SimulationData(String name, String[] labels, double[] timeSteps, double[][] data) {
        if (data.length != timeSteps.length) throw new IllegalArgumentException("Inconsistent number of time steps and data elements");
        this.name = name;
        this.labels = labels;
        this.timeSteps = timeSteps;
        this.data = data;
    }

    /**
     * Returns the name of this data set.
     *
     * @return the name of this data set.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the number of rows in this data set.
     *
     * @return the number of rows in this data set.
     */
    public int size() {
        return timeSteps.length;
    }

    /**
     * Return the number of entries in each row of the data set.
     *
     * @return the number of entries in each row of the data set.
     */
    public int getNumberOfEntries() {
        return labels.length;
    }


    /**
     * Returns the value of the entry <code>j</code> at step <code>i</code>.
     *
     * @param i time step index
     * @param j feature index
     * @return the value of the entry <code>j</code> at step <code>i</code>.
     */
    public double get(int i, int j) {
        return data[i][j];
    }

    /**
     * Returns the time value at step <code>i</code>.
     *
     * @param i time step index
     * @return the time value at step <code>i</code>.
     */
    public double time(int i) {
        return timeSteps[i];
    }

    public static <S> SimulationData getDataSet(String name, Trajectory<S> trajectory, Map<String, ToDoubleFunction<S>> functions) {
        String[] labels = functions.keySet().toArray(new String[0]);
        double[] timeSteps = trajectory.stream().mapToDouble(Sample::getTime).toArray();
        double[][] data = trajectory.stream().map(Sample::getValue).map(s -> apply(labels, functions, s)).toArray(double[][]::new);
        return new SimulationData(name, labels, timeSteps, data);
    }

    private static <S> double[] apply(String[] labels, Map<String, ToDoubleFunction<S>> functions, S s) {
        return Stream.of(labels).mapToDouble(l -> functions.get(l).applyAsDouble(s)).toArray();
    }

    public double[] getRow(int i) {
        return DoubleStream.concat(DoubleStream.of(time(i)), DoubleStream.of(data[i])).toArray();
    }

    public String getLabel(int i) {
        return labels[i];
    }
}
