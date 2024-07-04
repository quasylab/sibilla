/*
 *  Sibilla:  a Java framework designed to support analysis of Collective
 *  Adaptive Systems.
 *
 *              Copyright (C) ${YEAR}.
 *
 *  See the NOTICE file distributed with this work for additional information
 *  regarding copyright ownership.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *    or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package it.unicam.quasylab.sibilla.tools.stl;

import it.unicam.quasylab.sibilla.core.simulator.Trajectory;
import it.unicam.quasylab.sibilla.core.util.BooleanSignal;
import it.unicam.quasylab.sibilla.core.util.Interval;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * This interface implements a monitor that, given a trajectory returns the {@link BooleanSignal}
 * representing the time intervals when the evaluation of the monitor is true.
 *
 * @param <S> type of states in the trajectory.
 */
public interface QualitativeMonitor<S> {

    /**
     * Monitor the given trajectory and return a BooleanSignal representing the time intervals when
     * the evaluation of the qualitative property is true.
     *
     * @param trj The trajectory to monitor, containing information about the system's behavior over time.
     * @return A BooleanSignal indicating the time intervals when the qualitative property is true.
     */
    BooleanSignal monitor(Trajectory<S> trj);

    /**
     * Return the time horizon of interest, which indicates the maximum time duration over which
     * the monitor is concerned.
     *
     * @return The time horizon of interest.
     */
    double getTimeHorizon();

    /**
     * Calculate the probability of a given qualitative monitor being satisfied over time using
     * sampled trajectories.
     *
     * @param monitor The qualitative monitor to be evaluated.
     * @param trajectoryProvider A supplier for generating trajectories.
     * @param runs The number of simulation runs.
     * @param samplings The number of time steps to consider.
     * @param dt The time step duration.
     * @return An array of probabilities for the monitor satisfaction at different time steps.
     */
    static <S> double[] computeProbability(QualitativeMonitor<S> monitor, Supplier<Trajectory<S>> trajectoryProvider, int runs, int samplings, double dt) {
        return computeProbability(monitor, trajectoryProvider, runs, IntStream.range(0, samplings).mapToDouble(i -> i*dt).toArray());
    }

    /**
     * Calculate the probability of a given qualitative monitor being satisfied over time using
     * sampled trajectories
     *
     * @param monitor The qualitative monitor to be evaluated.
     * @param trajectoryProvider A supplier for generating trajectories.
     * @param runs The number of simulation runs.
     * @param timeStep An array of time steps at which the probabilities are calculated.
     * @return An array of probabilities for the monitor satisfaction at different time steps.
     */
    static <S> double[] computeProbability(QualitativeMonitor<S> monitor, Supplier<Trajectory<S>> trajectoryProvider, int runs, double[] timeStep) {
        int[] counter = new int[timeStep.length];
        for(int i=0; i<runs; i++) {
            boolean[] evaluations = monitor.monitor(trajectoryProvider.get()).getValuesAt(timeStep);
            IntStream.range(0, counter.length).filter(j -> evaluations[j]).forEach(j -> counter[j]++);
        }
        return Arrays.stream(counter).mapToDouble(j -> j / ((double) runs)).toArray();
    }


    /**
     * Calculate the probability of a given qualitative monitor being satisfied over time using
     * sampled simulations with a specified error probability and confidence interval.
     *
     * @param monitor The qualitative monitor to be evaluated.
     * @param trajectoryProvider A supplier for generating trajectories.
     * @param errorProbability The desired error probability for the estimation.
     * @param delta The confidence interval for the estimation.
     * @param timeStep An array of time steps at which the probabilities are calculated.
     * @return An array of probabilities for the monitor satisfaction at different time steps.
     */
    static <S> double[] computeProbability(QualitativeMonitor<S> monitor, Supplier<Trajectory<S>> trajectoryProvider, double errorProbability, double delta, double[] timeStep) {
        return computeProbability(monitor, trajectoryProvider, (int) ((1/Math.pow(errorProbability,2))*Math.log(2/delta))+1,timeStep);
    }
    /**
     * Calculate the probability of a given qualitative monitor being satisfied over time using
     * sampled simulation with a specified error probability and confidence interval.
     *
     * @param monitor The qualitative monitor to be evaluated.
     * @param trajectoryProvider A supplier for generating trajectories.
     * @param errorProbability The desired error probability.
     * @param delta The confidence interval.
     * @param samplings The number of time steps to consider.
     * @param dt The time step duration.
     * @return An array of probabilities for the monitor satisfaction at different time steps.
     */
    static <S> double[] computeProbability(QualitativeMonitor<S> monitor, Supplier<Trajectory<S>> trajectoryProvider, double errorProbability, double delta, int samplings, double dt) {
        return computeProbability(monitor, trajectoryProvider, (int) ((1/Math.pow(errorProbability,2))*Math.log(2/delta))+1,samplings, dt);
    }


    /**
     * Calculate the probability of a given qualitative monitor being satisfied over time using
     * sampled trajectories and return results in a double[][] array.
     *
     * @param monitor The qualitative monitor to be evaluated.
     * @param trajectoryProvider A supplier for generating trajectories.
     * @param runs The number of simulation runs.
     * @param timeStep An array of time steps at which the probabilities are calculated.
     * @return A double[][] array where:
     *         - results[i][0] is the time step
     *         - results[i][1] is the probability of the monitor satisfaction at the time step
     */
    static <S> double[][] computeTimeSeriesProbabilities(
            QualitativeMonitor<S> monitor,
            Supplier<Trajectory<S>> trajectoryProvider,
            int runs,
            double[] timeStep) {

        int[] counter = new int[timeStep.length];
        for (int i = 0; i < runs; i++) {
            boolean[] evaluations = monitor.monitor(trajectoryProvider.get()).getValuesAt(timeStep);
            IntStream.range(0, counter.length).filter(j -> evaluations[j]).forEach(j -> counter[j]++);
        }
        double[] probabilities = Arrays.stream(counter).mapToDouble(j -> j / ((double) runs)).toArray();

        double[][] results = new double[timeStep.length][2];
        for (int i = 0; i < timeStep.length; i++) {
            results[i][0] = timeStep[i];
            results[i][1] = probabilities[i];
        }
        return results;
    }

    /**
     * Calculate the probability of a given qualitative monitor being satisfied over time using
     * sampled trajectories and return results in a double[][] array.
     *
     * @param monitor The qualitative monitor to be evaluated.
     * @param trajectoryProvider A supplier for generating trajectories.
     * @param runs The number of simulation runs.
     * @param samplings The number of time steps to consider.
     * @param dt The time step duration.
     * @return A double[][] array where:
     *         - results[i][0] is the time step
     *         - results[i][1] is the probability of the monitor satisfaction at the time step
     */
    static <S> double[][] computeTimeSeriesProbabilities(
            QualitativeMonitor<S> monitor,
            Supplier<Trajectory<S>> trajectoryProvider,
            int runs,
            int samplings,
            double dt) {

        double[] timeStep = IntStream.range(0, samplings).mapToDouble(i -> i * dt).toArray();
        return computeTimeSeriesProbabilities(monitor, trajectoryProvider, runs, timeStep);
    }

    /**
     * Calculate the probability of a given qualitative monitor being satisfied over time using
     * sampled trajectories with a specified error probability and confidence interval, and return results in a double[][] array.
     *
     * @param monitor The qualitative monitor to be evaluated.
     * @param trajectoryProvider A supplier for generating trajectories.
     * @param errorProbability The desired error probability for the estimation.
     * @param delta The confidence interval for the estimation.
     * @param timeStep An array of time steps at which the probabilities are calculated.
     * @return A double[][] array where:
     *         - results[i][0] is the time step
     *         - results[i][1] is the probability of the monitor satisfaction at the time step
     */
    static <S> double[][] computeTimeSeriesProbabilities(
            QualitativeMonitor<S> monitor,
            Supplier<Trajectory<S>> trajectoryProvider,
            double errorProbability,
            double delta,
            double[] timeStep) {

        int runs = (int) ((1 / Math.pow(errorProbability, 2)) * Math.log(2 / delta)) + 1;
        return computeTimeSeriesProbabilities(monitor, trajectoryProvider, runs, timeStep);
    }

    /**
     * Calculate the probability of a given qualitative monitor being satisfied over time using
     * sampled simulation with a specified error probability and confidence interval, and return results in a double[][] array.
     *
     * @param monitor The qualitative monitor to be evaluated.
     * @param trajectoryProvider A supplier for generating trajectories.
     * @param errorProbability The desired error probability.
     * @param delta The confidence interval.
     * @param samplings The number of time steps to consider.
     * @param dt The time step duration.
     * @return A double[][] array where:
     *         - results[i][0] is the time step
     *         - results[i][1] is the probability of the monitor satisfaction at the time step
     */
    static <S> double[][] computeTimeSeriesProbabilities(
            QualitativeMonitor<S> monitor,
            Supplier<Trajectory<S>> trajectoryProvider,
            double errorProbability,
            double delta,
            int samplings,
            double dt) {

        int runs = (int) ((1 / Math.pow(errorProbability, 2)) * Math.log(2 / delta)) + 1;
        return computeTimeSeriesProbabilities(monitor, trajectoryProvider, runs, samplings, dt);
    }

    /**
     * Calculate the probability of a given qualitative monitor being satisfied over time using
     * sampled trajectories, and return results in a double[][] array.
     *
     * @param monitor The qualitative monitor to be evaluated.
     * @param trajectoryProvider A supplier for generating trajectories.
     * @param runs The number of simulation runs.
     * @param dt The time step duration.
     * @param deadline The total time horizon.
     * @return A double[][] array where:
     *         - results[i][0] is the time step
     *         - results[i][1] is the probability of the monitor satisfaction at the time step
     */
    static <S> double[][] computeTimeSeriesProbabilities(
            QualitativeMonitor<S> monitor,
            Supplier<Trajectory<S>> trajectoryProvider,
            int runs,
            double dt,
            double deadline) {

        double[] timeStep = generateTimeSteps(dt, deadline-monitor.getTimeHorizon());
        return computeTimeSeriesProbabilities(monitor, trajectoryProvider, runs, timeStep);
    }

    /**
     * Calculate the probability of a given qualitative monitor being satisfied over time using
     * sampled trajectories, and return results in a double[][] array.
     *
     * @param monitor The qualitative monitor to be evaluated.
     * @param trajectoryProvider A supplier for generating trajectories.
     * @param runs The number of simulation runs.
     * @param dt The time step duration.
     * @return A double[][] array where:
     *         - results[i][0] is the time step
     *         - results[i][1] is the probability of the monitor satisfaction at the time step
     */
    static <S> double[][] computeTimeSeriesProbabilities(
            QualitativeMonitor<S> monitor,
            Supplier<Trajectory<S>> trajectoryProvider,
            int runs,
            double dt) {

        return computeTimeSeriesProbabilities(monitor, trajectoryProvider, runs, dt, monitor.getTimeHorizon());
    }



    private static double[] generateTimeSteps(double dt, double deadline) {
        int stepsCount = (int) Math.ceil(deadline / dt);
        double[] timeSteps = new double[stepsCount];
        for (int i = 0; i < stepsCount; i++) {
            timeSteps[i] = i * dt;
        }
        return timeSteps;
    }






    /**
     * A monitor used to evaluate an atomic proposition of the form p(m(s)).
     *
     * @param <S>  type of states in the trajectory
     */
    static <S> QualitativeMonitor<S> atomicFormula(Predicate<S> predicate) {
        //return new QualitativeMonitor.AtomicMonitor<>(predicate);
        return new QualitativeMonitor<>() {
            @Override
            public BooleanSignal monitor(Trajectory<S> trj) {
                return trj.test(predicate);
            }

            @Override
            public double getTimeHorizon() {
                return 0;
            }
        };
    }

    /**
     * Create a monitor that always evaluates to true.
     *
     * @param <S> The type of states in the trajectory.
     * @return A monitor that always evaluates to true.
     */
    static <S> QualitativeMonitor<S> trueFormula() {
        return atomicFormula(s -> true);
    }

    /**
     * Create a monitor that always evaluates to false.
     *
     * @param <S> The type of states in the trajectory.
     * @return A monitor that always evaluates to false.
     */
    static <S> QualitativeMonitor<S> falseFormula() {
        return atomicFormula(s -> false);
    }

    /**
     * Create a negation monitor that negates the evaluation of the given monitor.
     *
     * @param <S> The type of states in the trajectory.
     * @param m The monitor to be negated.
     * @return A negation monitor.
     */
    static <S> QualitativeMonitor<S> negation(QualitativeMonitor<S> m) {
        return new QualitativeMonitor<>() {
            @Override
            public BooleanSignal monitor(Trajectory<S> trj) {
                return m.monitor(trj).negate();
            }

            @Override
            public double getTimeHorizon() {
                return m.getTimeHorizon();
            }
        };
    }

    /**
     * Create a conjunction monitor that evaluates the logical conjunction of two given monitors.
     *
     * @param <S> The type of states in the trajectory.
     * @param m1 The first monitor.
     * @param m2 The second monitor.
     * @return A conjunction monitor.
     */
    static <S> QualitativeMonitor<S> conjunction(QualitativeMonitor<S> m1, QualitativeMonitor<S> m2) {
        return new QualitativeMonitor<>() {
            @Override
            public BooleanSignal monitor(Trajectory<S> trj) {
                return m1.monitor(trj).computeConjunction(m2.monitor(trj));
            }

            @Override
            public double getTimeHorizon() {
                return Math.max(m1.getTimeHorizon(),m2.getTimeHorizon());
            }
        };
    }

    /**
     * Create a disjunction monitor that evaluates the logical disjunction of two given monitors.
     *
     * @param <S> The type of states in the trajectory.
     * @param m1 The first monitor.
     * @param m2 The second monitor.
     * @return A disjunction monitor.
     */
    static <S> QualitativeMonitor<S> disjunction(QualitativeMonitor<S> m1, QualitativeMonitor<S> m2) {
        return new QualitativeMonitor<>() {
            @Override
            public BooleanSignal monitor(Trajectory<S> trj) {
                return m1.monitor(trj).computeDisjunction(m2.monitor(trj));
            }

            @Override
            public double getTimeHorizon() {
                return Math.max(m1.getTimeHorizon(),m2.getTimeHorizon());
            }
        };
    }

    /**
     * Create an implication monitor that evaluates the logical implication of two given monitors.
     *
     * @param <S> The type of states in the trajectory.
     * @param m1 The first monitor.
     * @param m2 The second monitor.
     * @return An implication monitor.
     */
    static <S> QualitativeMonitor<S> implication(QualitativeMonitor<S> m1, QualitativeMonitor<S> m2) {
        return new QualitativeMonitor<>() {
            @Override
            public BooleanSignal monitor(Trajectory<S> trj) {
                return disjunction(negation(m1),m2).monitor(trj);
            }

            @Override
            public double getTimeHorizon() {
                return Math.max(m1.getTimeHorizon(),m2.getTimeHorizon());
            }
        };
    }

    /**
     * Create an "if and only if" monitor that evaluates the logical equivalence of two given monitors.
     *
     * @param <S> The type of states in the trajectory.
     * @param m1 The first monitor.
     * @param m2 The second monitor.
     * @return An "if and only if" monitor.
     */
    static <S> QualitativeMonitor<S> ifAndOnlyIf(QualitativeMonitor<S> m1, QualitativeMonitor<S> m2) {
        return new QualitativeMonitor<>() {
            @Override
            public BooleanSignal monitor(Trajectory<S> trj) {
                return conjunction(implication(m1,m2), implication(m2,m1)).monitor(trj);
            }

            @Override
            public double getTimeHorizon() {
                return Math.max(m1.getTimeHorizon(),m2.getTimeHorizon());
            }
        };
    }




    /**
     * Create an "until" monitor that evaluates the logical "until" operator.
     *
     * @param <S> The type of states in the trajectory.
     * @param m1 The first monitor.
     * @param interval The time interval for the "until" operator.
     * @param m2 The second monitor.
     * @return An "until" monitor.
     */
    static <S> QualitativeMonitor<S> until(QualitativeMonitor<S> m1, Interval interval, QualitativeMonitor<S> m2) {
        //return new QualitativeMonitor.UntilMonitor<>(m1, interval, m2);
        return new QualitativeMonitor<>() {
            @Override
            public BooleanSignal monitor(Trajectory<S> trj) {
                return m1.monitor(trj).computeConjunction(m2.monitor(trj).shift(interval));
            }

            @Override
            public double getTimeHorizon() {
                return interval.end() + Math.max(m1.getTimeHorizon(),m2.getTimeHorizon());
            }
        };
    }


    /**
     * Create an "eventually" monitor that evaluates the given monitor over a specified time interval.
     *
     * @param <S> The type of states in the trajectory.
     * @param interval The time interval over which to evaluate the monitor.
     * @param m The monitor to be evaluated.
     * @return An "eventually" monitor.
     */
    static <S> QualitativeMonitor<S> eventually(Interval interval, QualitativeMonitor<S> m) {
        return new QualitativeMonitor<>() {
            @Override
            public BooleanSignal monitor(Trajectory<S> trj) {
                return m.monitor(trj).shift(interval);
            }

            @Override
            public double getTimeHorizon() {
                return interval.end() + m.getTimeHorizon();
            }
        };
    }


    /**
     * Create a global monitor that evaluates the given monitor over a specified time interval.
     *
     * @param <S> The type of states in the trajectory.
     * @param interval The time interval over which to evaluate the monitor.
     * @param m The monitor to be evaluated.
     * @return A global monitor.
     */
    static <S> QualitativeMonitor<S> globally(Interval interval, QualitativeMonitor<S> m) {
        return new QualitativeMonitor<>() {
            @Override
            public BooleanSignal monitor(Trajectory<S> trj) {
                return m.monitor(trj).negate().shift(interval).negate();
            }

            @Override
            public double getTimeHorizon() {
                return 0;
            }
        };
    }


}
