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

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class FirstPassageTimeResults {


    private final int tests;
    private final DescriptiveStatistics statistics;

    public FirstPassageTimeResults(int tests, DescriptiveStatistics statistics) {
        this.tests = tests;
        this.statistics = statistics;
    }

    public int getTests() {
        return tests;
    }

    public double getMin() {
        return statistics.getMin();
    }

    public double getMax() {
        return statistics.getMax();
    }

    public double getQ1() {
        return statistics.getPercentile(25);
    }

    public double getQ2() {
        return statistics.getPercentile(50);
    }

    public double getQ3() {
        return statistics.getPercentile(75);
    }

    public double getMean() {
        return statistics.getMean();
    }

    public double getStandardDeviation() {
        return statistics.getStandardDeviation();
    }

    public long getHits() {
        return statistics.getN();
    }
}
