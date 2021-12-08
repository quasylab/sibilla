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

package it.unicam.quasylab.sibilla.core.simulator.sampling;

import it.unicam.quasylab.sibilla.core.models.State;

import java.io.FileNotFoundException;
import java.util.Map;
import java.util.function.Function;

public abstract class StatisticSampling<S extends State> implements SamplingFunction<S> {
    protected final Measure<S> measure;
    protected final double dt;
    private double last_measure;
    private double next_time;
    private int current_index;
    private double new_measure;

    public StatisticSampling(Measure<S> measure, double dt) {
        this.measure = measure;
        this.dt = dt;
    }

    protected abstract void init();

    @Override
    public synchronized void sample(double time, S context) {
        this.new_measure = measure.measure(context);
        if ((time >= this.next_time) && (this.current_index < getSize())) {
            recordMeasure(time);
        } else {
            this.last_measure = this.new_measure;
        }
    }

    private void recordMeasure(double time) {
        while ((this.next_time < time) && (this.current_index < getSize())) {
            this.recordSample();
        }
        this.last_measure = this.new_measure;
        if (this.next_time == time) {
            this.recordSample();
        }
    }

    private void recordSample() {
        recordSample(this.current_index++,this.last_measure);
        this.next_time += this.dt;
    }

    protected abstract void recordSample(int i, double last_measure);

    @Override
    public synchronized void end(double time) {
        while (this.current_index < getSize()) {
            recordSample(this.current_index++, this.last_measure);
            this.next_time += this.dt;
        }
    }

    @Override
    public void start() {
        this.current_index = 0;
        this.next_time = 0;
    }

    public String getName() {
        return measure.getName();
    }

    @Override
    public Map<String,double[][]> getSimulationTimeSeries() {
        return Map.of(measure.getName(), getData());
    }

    public abstract int getSize();

    @Override
    public void printTimeSeries(Function<String, String> nameFunction) throws FileNotFoundException {
        printTimeSeries(nameFunction,';');
    }

    @Override
    public void printTimeSeries(Function<String, String> nameFunction, char separator) throws FileNotFoundException {
        printTimeSeries(nameFunction,separator,0.05);
    }

    public double getTimeOfIndex(int i) {
        return i*dt;
    }

    public double[][] getData() {
        double[][] data = new double[getSize()][];
        for(int i=0; i<getSize(); i++) {
            data[i] = getDataRow(i);
        }
        return data;
    }

    protected abstract double[] getDataRow(int i);

}
