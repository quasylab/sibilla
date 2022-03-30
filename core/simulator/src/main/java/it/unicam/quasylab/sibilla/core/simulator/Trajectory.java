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

package it.unicam.quasylab.sibilla.core.simulator;

import it.unicam.quasylab.sibilla.core.models.State;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Sample;
import it.unicam.quasylab.sibilla.core.simulator.sampling.SamplingFunction;
import it.unicam.quasylab.sibilla.core.simulator.sampling.SamplingHandler;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author loreti
 */
public class Trajectory<S> implements Externalizable {

    private static final long serialVersionUID = -9039722623650234376L;
    private List<Sample<S>> data;
    private double start = Double.NaN;
    private double end = Double.NaN;
    private boolean successful;
    private long generationTime = -1;

    public Trajectory() {
        this.data = new LinkedList<>();
    }

    public void add(double time, S value) {
        if (!Double.isFinite(start)) {
            this.start = time;
        }
        if (!Double.isFinite(this.end) && (this.end >= time)) {
            throw new IllegalArgumentException();//TODO: Add message!
        }
        this.data.add(new Sample<S>(time, value));
    }

    public void sample(SamplingHandler<S> f) {
        if (!Double.isFinite(start)) {
            throw new IllegalArgumentException();
        }
        f.start();
        for (Sample<S> s : this.data) {
            f.sample(s.getTime(), s.getValue());
        }
        //this.data.stream().sequential().forEach(s -> f.sample(s.getTime(), s.getValue()));
        f.end(end);
    }

    public void addSample(Sample<S> sample) {
        this.data.add(sample);
    }

    public List<Sample<S>> getData() {
        return data;
    }

    public double getStart() {
        return start;
    }

    public double getEnd() {
        return end;
    }

    public int size() {
        return data.size();
    }

    public IntFunction<S> getSteps() {
        ArrayList<S> values = new ArrayList<>(data.stream().map(s -> s.getValue()).collect(Collectors.toList()));
        return i -> values.get(i);
    }

    /**
     * @return the succesful
     */
    public boolean isSuccessful() {
        return successful;
    }

    /**
     * @param successful the succesful to set
     */
    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    /**
     * @return the generationTime
     */
    public long getGenerationTime() {
        return generationTime;
    }

    /**
     * @param generationTime the generationTime to set
     */
    public void setGenerationTime(long generationTime) {
        this.generationTime = generationTime;
    }


    public void setStart(double start) {
        this.start = start;
    }

    public void setEnd(double end) {
        this.end = end;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(data.size());
        out.writeDouble(start);
        out.writeDouble(end);
        out.writeBoolean(successful);
        out.writeLong(generationTime);

        for (Sample sampleToWrite : data) {
            out.writeObject(sampleToWrite);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int numberOfSamples = in.readInt();
        this.start = in.readDouble();
        this.end = in.readDouble();
        this.successful = in.readBoolean();
        this.generationTime = in.readLong();

        List<Sample<S>> samples = new LinkedList<Sample<S>>();
        for (int i = 0; i < numberOfSamples; i++) {
            samples.add((Sample) in.readObject());
        }
        this.data = samples;
    }

    public double firstPassageTime(Predicate<S> condition) {
        for (Sample<S> sample : data) {
            if (condition.test(sample.getValue())) {
                return sample.getTime();
            }
        }
        return Double.NaN;
    }
}
