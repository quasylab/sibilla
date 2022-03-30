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
import it.unicam.quasylab.sibilla.core.simulator.Trajectory;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class FirstPassageTime<S extends State> implements Supplier<SamplingHandler<S>> {

    private final String name;
    private final Predicate<? super S> condition;
    private final DescriptiveStatistics values;
    private int tests = 0;

    public FirstPassageTime(String name, Predicate<? super S> condition) {
        this.name = name;
        this.condition = condition;
        this.values = new DescriptiveStatistics();
    }

    public synchronized FirstPassageTimeResults getResults() {
        return new FirstPassageTimeResults(tests, values);
    }

    private synchronized void testStart() {
        this.tests++;
    }

    private synchronized void addValue(double time) {
        this.values.addValue(time);
    }

    @Override
    public SamplingHandler<S> get() {
        return new SamplingHandler<>() {

            private boolean flag = false;

            @Override
            public void start() {
                testStart();
            }

            @Override
            public void sample(double time, S state) {
                if (condition.test(state)&&!flag) {
                    addValue(time);
                    flag = true;
                }
            }

            @Override
            public void end(double time) {}
        };
    }
}
