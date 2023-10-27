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
import it.unicam.quasylab.sibilla.core.util.Interval;
import it.unicam.quasylab.sibilla.core.util.BooleanSignal;

import java.util.Arrays;
import java.util.function.DoublePredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;

/**
 * This interface implements a monitor that, given a trajectory returns the {@link BooleanSignal}
 * representing the time intervals when the evalutaion of the monitor is true.
 *
 * @param <S> type of states in the trajectory.
 */
//@FunctionalInterface
public interface QualitativeMonitor<S> {

    static <S> QualitativeMonitor<S> monitorGlobally(Interval interval, QualitativeMonitor<S> m) {
        return new QualitativeMonitor.GloballyMonitor<>(m, interval);
    }

    static <S> QualitativeMonitor<S> monitorFinally(Interval interval, QualitativeMonitor<S> m) {
        return new QualitativeMonitor.FinallyMonitor<>(m, interval);
    }

    static <S> QualitativeMonitor<S> monitorNegation(QualitativeMonitor<S> m) {
        return new QualitativeMonitor.NegationMonitor<>(m);
    }

    static <S> QualitativeMonitor<S> monitorConjunction(QualitativeMonitor<S> m1, QualitativeMonitor<S> m2) {
        return new QualitativeMonitor.ConjunctionMonitor<>(m1, m2);
    }

    static <S> QualitativeMonitor<S> monitorDisjuction(QualitativeMonitor<S> m1, QualitativeMonitor<S> m2) {
        return new QualitativeMonitor.DisjunctionMonitor<>(m1, m2);
    }

    static <S> QualitativeMonitor<S> monitorImplication(QualitativeMonitor<S> m1, QualitativeMonitor<S> m2) {
        return new QualitativeMonitor.DisjunctionMonitor<>(monitorNegation(m1), m2);
    }

    static <S> QualitativeMonitor<S> monitorIfAndOnlyIf(QualitativeMonitor<S> m1, QualitativeMonitor<S> m2) {
        return monitorConjunction(monitorImplication(m1, m2), monitorImplication(m2, m1));
    }


    static <S> QualitativeMonitor<S> monitorUntil(Interval interval, QualitativeMonitor<S> m1, QualitativeMonitor<S> m2) {
        return new QualitativeMonitor.UntilMonitor<>(m1, interval, m2);
    }

    static <S> QualitativeMonitor<S> monitorTrue() {
        return atomicMonitor(s -> true);
    }

    static <S> QualitativeMonitor<S> atomicMonitor(Predicate<S> predicate) {
        return new QualitativeMonitor.AtomicMonitor<>(predicate);
    }

    static <S> QualitativeMonitor<S> monitorFalse() {
        return atomicMonitor(s -> false);
    }

    BooleanSignal monitor(Trajectory<S> trj);

    static <S> double[] computeProbability(QualitativeMonitor<S> monitor, Supplier<Trajectory<S>> trajectoryProvider, int runs, int samplings, double dt) {
        return computeProbability(monitor, trajectoryProvider, runs, IntStream.range(0, samplings).mapToDouble(i -> i*dt).toArray());
    }

    static <S> double[] computeProbability(QualitativeMonitor<S> monitor, Supplier<Trajectory<S>> trajectoryProvider, int runs, double[] timeStep) {
        int[] counter = new int[timeStep.length];
        for(int i=0; i<runs; i++) {
            boolean[] evaluations = monitor.monitor(trajectoryProvider.get()).getValuesAt(timeStep);
            IntStream.range(0, counter.length).filter(j -> evaluations[j]).forEach(j -> counter[j]++);
        }
        return Arrays.stream(counter).mapToDouble(j -> j / ((double) runs)).toArray();
    }

    static <S> double[] computeProbability(QualitativeMonitor<S> monitor, Supplier<Trajectory<S>> trajectoryProvider, double errorProbability, double delta, double[] timeStep) {
        return computeProbability(monitor, trajectoryProvider, (int) ((1/Math.pow(errorProbability,2))*Math.log(2/delta))+1,timeStep);
    }

    static <S> double[] computeProbability(QualitativeMonitor<S> monitor, Supplier<Trajectory<S>> trajectoryProvider, double errorProbability, double delta, int samplings, double dt) {
        return computeProbability(monitor, trajectoryProvider, (int) ((1/Math.pow(errorProbability,2))*Math.log(2/delta))+1,samplings, dt);
    }

    /**
     * Return the time horizon of interest
     *
     * @return the time horizon
     */
    double getTimeHorizon();

    /**
     * A monitor used to evaluate an atomic proposition of the form p(m(s)).
     *
     * @param <S>
     */
    class AtomicMonitor<S> implements QualitativeMonitor<S> {

        private final Predicate<S> predicate;


        public AtomicMonitor(Predicate<S> predicate) {
            this.predicate = predicate;
        }

        @Override
        public BooleanSignal monitor(Trajectory<S> trj) {
            return trj.test(predicate);
        }

        @Override
        public double getTimeHorizon() {
            return 0.0;
        }
    }

    class NegationMonitor<S> implements QualitativeMonitor<S> {

        private final QualitativeMonitor<S> argument;

        public NegationMonitor(QualitativeMonitor<S> argument) {
            this.argument = argument;
        }

        @Override
        public BooleanSignal monitor(Trajectory<S> trj) {
            return argument.monitor(trj).negate();
        }
        @Override
        public double getTimeHorizon() {
            return argument.getTimeHorizon();
        }

    }

    class ConjunctionMonitor<S> implements QualitativeMonitor<S> {

        private final QualitativeMonitor<S> firstArgument;

        private final QualitativeMonitor<S> secondArgument;

        public ConjunctionMonitor(QualitativeMonitor<S> firstArgument, QualitativeMonitor<S> secondArgument) {
            this.firstArgument = firstArgument;
            this.secondArgument = secondArgument;
        }

        @Override
        public BooleanSignal monitor(Trajectory<S> trj) {
            return this.firstArgument.monitor(trj).computeConjunction(this.secondArgument.monitor(trj));
        }

        @Override
        public double getTimeHorizon() {
            return Math.max(firstArgument.getTimeHorizon(),secondArgument.getTimeHorizon());
        }

    }

    class DisjunctionMonitor<S> implements QualitativeMonitor<S> {

        private final QualitativeMonitor<S> firstArgument;

        private final QualitativeMonitor<S> secondArgument;

        public DisjunctionMonitor(QualitativeMonitor<S> firstArgument, QualitativeMonitor<S> secondArgument) {
            this.firstArgument = firstArgument;
            this.secondArgument = secondArgument;
        }

        @Override
        public BooleanSignal monitor(Trajectory<S> trj) {
            return this.firstArgument.monitor(trj).computeDisjunction(this.secondArgument.monitor(trj));
        }

        @Override
        public double getTimeHorizon() {
            return Math.max(firstArgument.getTimeHorizon(),secondArgument.getTimeHorizon());
        }

    }

    class ImplicationMonitor<S> implements QualitativeMonitor<S> {

        private final QualitativeMonitor<S> firstArgument;

        private final QualitativeMonitor<S> secondArgument;

        public ImplicationMonitor(QualitativeMonitor<S> firstArgument, QualitativeMonitor<S> secondArgument) {
            this.firstArgument = firstArgument;
            this.secondArgument = secondArgument;
        }

        @Override
        public BooleanSignal monitor(Trajectory<S> trj) {
            return this.firstArgument.monitor(trj).negate().computeDisjunction(this.secondArgument.monitor(trj));
        }

        @Override
        public double getTimeHorizon() {
            return Math.max(firstArgument.getTimeHorizon(),secondArgument.getTimeHorizon());
        }
    }

    class UntilMonitor<S> implements QualitativeMonitor<S> {

        private final QualitativeMonitor<S> firstArgument;

        private final Interval interval;

        private final QualitativeMonitor<S> secondArgument;


        public UntilMonitor(QualitativeMonitor<S> firstArgument, Interval interval, QualitativeMonitor<S> secondArgument) {
            this.firstArgument = firstArgument;
            this.interval = interval;
            this.secondArgument = secondArgument;
        }


        @Override
        public BooleanSignal monitor(Trajectory<S> trj) {
            return firstArgument.monitor(trj).computeConjunction(secondArgument.monitor(trj).shift(interval));
        }

        @Override
        public double getTimeHorizon() {
            return interval.end() + Math.max(firstArgument.getTimeHorizon(),secondArgument.getTimeHorizon());
        }
    }

    class FinallyMonitor<S> implements QualitativeMonitor<S> {

        private final QualitativeMonitor<S> argument;

        private final Interval interval;



        public FinallyMonitor(QualitativeMonitor<S> argument, Interval interval) {
            this.argument = argument;
            this.interval = interval;
        }


        @Override
        public BooleanSignal monitor(Trajectory<S> trj) {
            return argument.monitor(trj).shift(interval);
        }
        @Override
        public double getTimeHorizon() {
            return interval.end() + argument.getTimeHorizon();
        }
    }

    class GloballyMonitor<S> implements QualitativeMonitor<S> {

        private final QualitativeMonitor<S> argument;

        private final Interval interval;

        public GloballyMonitor(QualitativeMonitor<S> argument, Interval interval) {
            this.argument = argument;
            this.interval = interval;
        }


        @Override
        public BooleanSignal monitor(Trajectory<S> trj) {
            return argument.monitor(trj).negate().shift(interval).negate();
        }
        @Override
        public double getTimeHorizon() {
            return interval.end() + argument.getTimeHorizon();
        }
    }
}
