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

public class FirstPassageTime<S extends State> implements Consumer<Trajectory<S>> {

    private final String name;
    private final Predicate<S> condition;
    private final DescriptiveStatistics values;
    private int tests = 0;

    public FirstPassageTime(String name, Predicate<S> condition) {
        this.name = name;
        this.condition = condition;
        this.values = new DescriptiveStatistics();
    }

    @Override
    public synchronized void accept(Trajectory<S> trajectory) {
        tests++;
        double time = trajectory.firstPassageTime(condition);
        if (!Double.isNaN(time)) {
            values.addValue(time);
        }
    }

    public FirstPassageTimeResults getResults() {
        return new FirstPassageTimeResults(tests, values);
    }

}
