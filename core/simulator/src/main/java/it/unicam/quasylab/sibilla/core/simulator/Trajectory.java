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

import it.unicam.quasylab.sibilla.core.simulator.sampling.Sample;
import it.unicam.quasylab.sibilla.core.simulator.sampling.SamplingHandler;
import it.unicam.quasylab.sibilla.core.util.BooleanSignal;
import it.unicam.quasylab.sibilla.core.util.Signal;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author loreti
 */
public class Trajectory<S> implements Externalizable, Iterable<Sample<S>> {

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

    public void sample(SamplingHandler<? super S> f) {
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
        ArrayList<S> values = data.stream().map(Sample::getValue).collect(Collectors.toCollection(ArrayList::new));
        return values::get;
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


    public BooleanSignal test(Predicate<S> predicate) {
        BooleanSignal result = new BooleanSignal();
        boolean flag = false;
        double lastPositive = 0.0;
        for (Sample<S> interval: data) {
            boolean newFlag = predicate.test(interval.getValue());
            if (!flag&&newFlag) {
                lastPositive = interval.getTime();
            }
            if (flag&&!newFlag) {
                result.add(lastPositive, interval.getTime());
            }
            flag = newFlag;
        }
        if (flag && lastPositive < this.end) {
            result.add(lastPositive, this.end);
        }
        return result;
    }

    public Signal apply(ToDoubleFunction<S> function) {
        Signal result = new Signal();
        data.forEach(s -> {
            if(s.getTime() <= this.end)
                result.add(s.getTime(), function.applyAsDouble(s.getValue()));
        });
        if(!Double.isNaN(this.end)) result.setEnd(this.end);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder result= new StringBuilder();
        result.append("start : ").append(this.start).append(" ends : ").append(this.end).append("\n");
        result.append("Trajectory samples : ").append(data.size()).append("\n");
        if(this.data.size() <= 20){
            for (int i = 0; i < data.size()-1; i++) {
                result.append(data.get(i).toString()).append(" --> ");
            }
            result.append(data.get(data.size() - 1));
        }else{
            for (int i = 0; i < 3; i++) {
                result.append(data.get(i).toString()).append(" --> ");
            }
            result.append("... -->").append(data.get(data.size() - 2)).append(data.get(data.size() - 1));
        }

        return result.toString();
    }

    public void removeSampleOverTheEnd(){
        if(Double.isNaN(end) || this.data.get(data.size()-1).getTime() <= this.end)
            return;
        else
           while(this.data.get(data.size()-1).getTime() > this.end){
               this.data.remove(data.size()-1);
           }
    }

    @Override
    public Iterator<Sample<S>> iterator() {
        return data.iterator();
    }

    public Stream<Sample<S>> stream() {
        return this.data.stream();
    }
}
