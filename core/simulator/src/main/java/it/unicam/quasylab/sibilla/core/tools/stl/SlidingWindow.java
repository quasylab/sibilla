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

package it.unicam.quasylab.sibilla.core.tools.stl;

import it.unicam.quasylab.sibilla.core.simulator.sampling.Sample;
import it.unicam.quasylab.sibilla.core.util.Signal;

import java.util.LinkedList;

public class SlidingWindow {

    private final double size;


    public SlidingWindow(double size) {
        this.size = size;
    }

    public Signal apply(Signal s) {
        Signal result = new Signal();
        LinkedList<Sample<Double>> w = new LinkedList<>();
        for (Sample<Double> sample : s) {
            add(result, w, sample);
        }
        if (isFull(w)) {
            Sample<Double> doubleSample = w.removeFirst();
            result.add(doubleSample.getTime(), doubleSample.getValue());
        }
        complete(result, w, s.getEnd());
        result.setEnd(s.getEnd());
        return result;
    }

    private void complete(Signal result, LinkedList<Sample<Double>> w, double end) {
        while (!w.isEmpty()&&((end-w.getFirst().getTime())>=size)) {
            Sample<Double> doubleSample = w.removeFirst();
            result.add(doubleSample.getTime(), doubleSample.getValue());
        }
    }

    private boolean isFull(LinkedList<Sample<Double>> w) {
        if (!w.isEmpty()) {
            return (w.getLast().getTime()-w.getFirst().getTime()) == size;
        } else {
            return false;
        }
    }

    private void add(Signal result, LinkedList<Sample<Double>> w, Sample<Double> sample) {
        while (!w.isEmpty()&&((sample.getTime()-w.getFirst().getTime())>size)) {
            Sample<Double> doubleSample = w.removeFirst();
            result.add(doubleSample.getTime(), doubleSample.getValue());
        }
        append(w, sample);
    }

    private void append(LinkedList<Sample<Double>> w, Sample<Double> sample) {
        double time = sample.getTime();
        double value = sample.getValue();
        while (!w.isEmpty()&&w.getLast().getValue()<=value) {
            time = w.removeLast().getTime();
        }
        w.add(new Sample<>(time, value));
    }


}
