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

    private final double from;

    private final double to;

    private class Window {

        private final LinkedList<Sample<Double>> content = new LinkedList<>();

        private double windowEnd = Double.NaN;

        private Sample<Double> lastRemoved;

        public void add(Signal result, Sample<Double> sample) {
            add(result, sample.getTime(), sample.getValue());
        }

        public void add(Signal result, double nextTime, double nextValue) {
            while (!isEmpty()&&(getFirstTimeInWindow()+getSize()<nextTime)) {
                Sample<Double> firstSample = removeFirst();
                result.add(firstSample.getTime()-from, firstSample.getValue());
                lastRemoved = firstSample;
                reinsertIfNeeded(nextTime);
            }
            doAdd(nextTime, nextValue);
        }

        private void reinsertIfNeeded(double nextTime) {
            if (content.isEmpty()) {
                content.add(new Sample<>(nextTime-getSize(), lastRemoved.getValue()));
            } else {
                double delta1 = getFirstTimeInWindow()-lastRemoved.getTime();
                double delta2 = nextTime - windowEnd;
                if (delta1 > delta2) {
                    content.add(new Sample<>(lastRemoved.getTime()+delta2, lastRemoved.getValue()));
                }
            }
        }

        private double getFirstTimeInWindow() {
            return content.getFirst().getTime();
        }

        private void doAdd(double nextTime, double nextValue) {
            double timeOfInsertedElement = nextTime;
            while (!content.isEmpty()&&(getLastValue()<=nextValue)) {
                Sample<Double> lastSampling = content.removeLast();
                timeOfInsertedElement = lastSampling.getTime();
            }
            content.add(new Sample<>(timeOfInsertedElement, nextValue));
            windowEnd = nextTime;
        }

        private boolean isEmpty() {
            return content.isEmpty();
        }

        private Sample<Double> removeFirst() {
            return content.removeFirst();
        }

        public void end(Signal result, double end) {
            if (end > windowEnd) {
                add(result, end, getLastValue());
                if (getFirstTimeInWindow()+getSize()>windowEnd) {
                    Sample<Double> firstSample = content.getFirst();
                    result.add(firstSample.getTime()-from, firstSample.getValue());
                }
            }
        }

        private double getLastValue() {
            if (!content.isEmpty()) return content.getLast().getValue();
            if (lastRemoved != null) return lastRemoved.getValue();
            return Double.NaN;
        }
    }

    public SlidingWindow(double from, double to) {
        this.from = from;
        this.to = to;
    }

    public Signal apply(Signal s) {
        Signal result = new Signal();
        Window w = new Window();
        for (Sample<Double> sample : s.extract(from)) {
            w.add(result, sample);
        }
        w.end(result, s.getEnd());
        return result;
    }


    private double getSize() {
        return to-from;
    }

}
