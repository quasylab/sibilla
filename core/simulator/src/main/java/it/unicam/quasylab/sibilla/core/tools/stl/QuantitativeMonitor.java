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
import it.unicam.quasylab.sibilla.core.util.BooleanSignal;
import it.unicam.quasylab.sibilla.core.util.Signal;

import java.util.function.ToDoubleFunction;
/**
 * This interface implements a monitor that, given a trajectory returns the {@link Signal}
 * representing the robustness for each time intervals.
 *
 * @param <S> type of states in the trajectory
 */
public interface QuantitativeMonitor<S> {

    Signal monitor(Trajectory<S> trajectory);

    /**
     * Return the time horizon of interest
     *
     * @return the time horizon
     */
    double getTimeHorizon();

    /**
     * A monitor used to evaluate an atomic formula
     * @param <S> type of states in the trajectory
     */
    class AtomicMonitor<S> implements QuantitativeMonitor<S> {
        private final ToDoubleFunction<S> atomicFunction;
        public AtomicMonitor(ToDoubleFunction<S> atomicFunction) {
            this.atomicFunction = atomicFunction;
        }
        @Override
        public Signal monitor(Trajectory<S> trajectory) {return trajectory.apply(atomicFunction);}
        @Override
        public double getTimeHorizon() {
            return 0.0;
        }
    }

    /**
     * A monitor used to evaluate the conjunction of two formulae
     *
     * @param m1 left argument of the conjunction
     * @param m2 right argument of the conjunction
     * @return the conjunction monitor
     * @param <S>  type of states in the trajectory
     */
    static <S> QuantitativeMonitor<S> conjunction(QuantitativeMonitor<S> m1, QuantitativeMonitor<S> m2) {
        return new QuantitativeMonitor<>() {
            @Override
            public Signal monitor(Trajectory<S> trajectory) {
                return Signal.apply(
                        m1.monitor(trajectory),
                        Math::min, m2.monitor(trajectory)
                );
            }
            @Override
            public double getTimeHorizon() {
                return Math.max(m1.getTimeHorizon(),m2.getTimeHorizon());
            }
        };
    }

    /**
     * A monitor used to evaluate the disjunction of two formulae
     *
     * @param m1 left argument of the disjunction
     * @param m2 right argument of the disjunction
     * @return the disjunction monitor
     * @param <S>  type of states in the trajectory
     */
    static <S> QuantitativeMonitor<S> disjunction(QuantitativeMonitor<S> m1, QuantitativeMonitor<S> m2) {
        return new QuantitativeMonitor<>() {
            @Override
            public Signal monitor(Trajectory<S> trajectory) {
                return Signal.apply(
                        m1.monitor(trajectory),
                        Math::max, m2.monitor(trajectory)
                );
            }

            @Override
            public double getTimeHorizon() {
                return Math.max(m1.getTimeHorizon(),m2.getTimeHorizon());
            }
        };
    }


    /**
     *
     * A monitor used to evaluate the negation of a formula
     *
     * @param m monitor to monitorNegation
     * @return the negation monitor
     * @param <S> type of states in the trajectory
     */
    static <S> QuantitativeMonitor<S> negation(QuantitativeMonitor<S> m) {
        return new QuantitativeMonitor<>() {
            @Override
            public Signal monitor(Trajectory<S> trajectory) {
                return Signal.apply(m.monitor(trajectory),d->-d);
            }
            @Override
            public double getTimeHorizon() {
                return m.getTimeHorizon();
            }
        };
    }

    /**
     * A monitor used to evaluate an unbounded until monitor
     * @param m1 left argument
     * @param m2 right argument
     * @return unbounded until monitor
     * @param <S> type of states in the trajectory
     */

    static <S> QuantitativeMonitor<S> until(QuantitativeMonitor<S> m1, QuantitativeMonitor<S> m2) {
        return new QuantitativeMonitor<>() {
            @Override
            public Signal monitor(Trajectory<S> trajectory) {
                Signal s1 = m1.monitor(trajectory);
                Signal s2 = m2.monitor(trajectory);
                double[] times = Signal.getTimeSteps(s1, s2);
                double[] data = until(s1.valuesAt(times), s2.valuesAt(times));
                return new Signal(times, data);
            }

            @Override
            public double getTimeHorizon() {
                return Double.POSITIVE_INFINITY;
            }
        };
    }


    /**
     *
     * A monitor used to evaluate an atomic the "eventually" monitor
     * @param m the monitor
     * @param from start of the interval
     * @param to end of the interval
     * @return the "eventually" monitor
     * @param <S>  a state in the trajectory
     */
    static <S> QuantitativeMonitor<S> eventually(QuantitativeMonitor<S> m, double from, double to) {
        if (from>=to) throw new IllegalArgumentException();
        return new QuantitativeMonitor<>() {
            @Override
            public Signal monitor(Trajectory<S> trajectory) {
                SlidingWindow sw = new SlidingWindow(from,to);
                return sw.apply(m.monitor(trajectory));
            }

            @Override
            public double getTimeHorizon() {
                return to + m.getTimeHorizon();
            }
        };
    }

    /**
     * A monitor used to evaluate an atomic the "globally" monitor
     * @param m the monitor
     * @param from start of the interval
     * @param to end of the interval
     * @return the "globally" monitor
     * @param <S>  a state in the trajectory
     */
    static <S> QuantitativeMonitor<S> globally(QuantitativeMonitor<S> m, double from, double to) {
        return new QuantitativeMonitor<>() {
            @Override
            public Signal monitor(Trajectory<S> trajectory) {
                return negation(eventually(negation(m), from, to)).monitor(trajectory);
            }

            @Override
            public double getTimeHorizon() {
                return to + m.getTimeHorizon();
            }
        };
    }

    /**
     * A monitor used to evaluate an atomic the until monitor
     * @param m1 left argument
     * @param m2 right argument
     * @param from start of the interval
     * @param to end of the interval
     * @return the until monitor
     * @param <S>  a state in the trajectory
     */
    static <S> QuantitativeMonitor<S> until(QuantitativeMonitor<S> m1, double from, double to, QuantitativeMonitor<S> m2) {
        return new QuantitativeMonitor<>() {
            @Override
            public Signal monitor(Trajectory<S> trajectory) {
                return conjunction(until(m1, m2), eventually(m2, from, to)).monitor(trajectory);
            }

            @Override
            public double getTimeHorizon() {
                return to + Math.max(m1.getTimeHorizon(),m2.getTimeHorizon());
            }
        };
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
