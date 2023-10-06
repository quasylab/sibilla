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

import it.unicam.quasylab.sibilla.core.simulator.Trajectory;
import it.unicam.quasylab.sibilla.core.util.Signal;

import java.util.function.ToDoubleFunction;

public interface QuantitativeMonitor<S> {

    Signal monitor(Trajectory<S> trajectory);

    class AtomicMonitor<S> implements QuantitativeMonitor<S> {

        private final ToDoubleFunction<S> atomicFunction;

        public AtomicMonitor(ToDoubleFunction<S> atomicFunction) {
            this.atomicFunction = atomicFunction;
        }

        @Override
        public Signal monitor(Trajectory<S> trajectory) {
            return null;
        }
    }

    static <S> QuantitativeMonitor<S> conjunction(QuantitativeMonitor<S> m1, QuantitativeMonitor<S> m2) {
        return trj -> Signal.apply(m1.monitor(trj), Math::max, m2.monitor(trj));
    }

    static <S> QuantitativeMonitor<S> disjunction(QuantitativeMonitor<S> m1, QuantitativeMonitor<S> m2) {
        return trj -> Signal.apply(m1.monitor(trj), Math::min, m2.monitor(trj));
    }

    static <S> QuantitativeMonitor<S> negation(QuantitativeMonitor<S> m) {
        return trj -> Signal.apply(m.monitor(trj), d -> -d);
    }

    static <S> QuantitativeMonitor<S> until(QuantitativeMonitor<S> m1, QuantitativeMonitor<S> m2) {
        return trj -> {
            Signal s1 = m1.monitor(trj);
            Signal s2 = m2.monitor(trj);
            double[] times = Signal.getCommonTimeSteps(s1, s2);
            double[] data = until(s1.valuesAt(times), s2.valuesAt(times));
            return new Signal(times, data);
        };
    }


    static <S> QuantitativeMonitor<S> eventually(QuantitativeMonitor<S> m, double from, double to) {
        if (from>=to) {
            throw new IllegalArgumentException();
        }
        SlidingWindow sw = new SlidingWindow(to-from);
        return trj -> sw.apply(m.monitor(trj));
    }

    static <S> QuantitativeMonitor<S> globally(QuantitativeMonitor<S> m, double from, double to) {
        return negation(eventually(negation(m), from, to));
    }

    static <S> QuantitativeMonitor<S> until(QuantitativeMonitor<S> m1, double from, double to, QuantitativeMonitor<S> m2) {
        return conjunction(until(m1, m2), eventually(m2, from, to));
    }


    private static double[] until(double[] s1, double[] s2) {
        double[] result = new double[s1.length];
        double next = Double.NEGATIVE_INFINITY;
        for(int i = s1.length-1;i>=0;i--) {
            result[i] = Math.max(s2[i], Math.min(s1[i],next));
            next = result[i];
        }
        return result;
    }

}
