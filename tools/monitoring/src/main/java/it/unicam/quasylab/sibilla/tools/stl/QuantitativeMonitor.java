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
import it.unicam.quasylab.sibilla.core.util.Interval;
import it.unicam.quasylab.sibilla.core.util.Signal;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;

/**
 * This interface implements a monitor that, given a trajectory returns the {@link Signal}
 * representing the robustness for each time intervals.
 *
 * @param <S> type of states in the trajectory
 */
public interface QuantitativeMonitor<S> {


    /**
     * Monitor a trajectory and return a quantitative signal representing the robustness.
     *
     * @param trajectory The trajectory to monitor.
     * @return A quantitative signal representing the robustness over time.
     */
    Signal monitor(Trajectory<S> trajectory);


    /**
     * Calculate the probability of a given quantitative monitor being satisfied over time using
     * sampled trajectories
     *
     * @param monitor The quantitative monitor to be evaluated.
     * @param trajectoryProvider A supplier for generating trajectories.
     * @param runs The number of simulation runs.
     * @param timeStep An array of time steps at which the probabilities are calculated.
     * @return An array of probabilities for the monitor satisfaction at different time steps.
     */
    static <S> double[] computeProbability(QuantitativeMonitor<S> monitor, Supplier<Trajectory<S>> trajectoryProvider, int runs, double[] timeStep) {
        int[] counter = new int[timeStep.length];
        for(int i=0; i<runs; i++) {
            double[] evaluations = monitor.monitor(trajectoryProvider.get()).valuesAt(timeStep);
            IntStream.range(0, counter.length).filter(j -> evaluations[j]>0).forEach(j -> counter[j]++);
        }
        return Arrays.stream(counter).mapToDouble(j -> j / ((double) runs)).toArray();
    }




    /**
     * Calculate the probability of a given quantitative monitor being satisfied over time using
     * pre-generated trajectories.
     *
     * @param monitor The quantitative monitor to be evaluated.
     * @param trajectories A list of pre-generated trajectories for evaluation.
     * @param timeStep An array of time steps at which the probabilities are calculated.
     * @return An array of probabilities for the monitor satisfaction at different time steps.
     */
    static <S> double[] computeProbability(QuantitativeMonitor<S> monitor, List<Trajectory<S>> trajectories, double[] timeStep) {
        int[] counter = new int[timeStep.length];
        for (Trajectory<S> trajectory : trajectories) {
            double[] evaluations = monitor.monitor(trajectory).valuesAt(timeStep);
            IntStream.range(0, counter.length).filter(j -> evaluations[j] > 0).forEach(j -> counter[j]++);
        }
        return Arrays.stream(counter).mapToDouble(j -> j / ((double) trajectories.size())).toArray();
    }


    /**
     * Computes the mean robustness of a monitor over multiple runs.
     *
     * @param <S>                the type of states in the trajectory
     * @param monitor            the monitor to compute the mean robustness for
     * @param trajectoryProvider a supplier for generating trajectories
     * @param runs               the number of simulation runs
     * @param timeStep           an array of time steps at which the mean robustness is calculated
     * @return an array representing the mean robustness at different time steps
     */
    static <S> double[] computeMeanRobustness(QuantitativeMonitor<S> monitor, Supplier<Trajectory<S>> trajectoryProvider, int runs, double[] timeStep){
        double[] meanEvaluations = new double[timeStep.length];
        for(int run = 0; run < runs; run++) {
            double[] currentRobustnessEvaluation = monitor.monitor(trajectoryProvider.get()).valuesAt(timeStep);
            for(int i = 0; i < timeStep.length; i++) {
                meanEvaluations[i] += currentRobustnessEvaluation[i];
            }
        }
        for(int i = 0; i < timeStep.length; i++) {
            meanEvaluations[i] /= runs;
        }
        return meanEvaluations;
    }


    /**
     * Compute the mean robustness of a monitor over multiple runs.
     *
     * @param monitor The quantitative monitor to compute the mean robustness for.
     * @param trajectories A list of pre-generated trajectories for evaluation.
     * @param timeStep An array of time steps at which the mean robustness is calculated.
     * @return An array representing the mean robustness at different time steps.
     */
    static <S> double[] computeMeanRobustness(QuantitativeMonitor<S> monitor, List<Trajectory<S>> trajectories, double[] timeStep){
        double[] meanEvaluations = new double[timeStep.length];
        int runs = trajectories.size();
        for (Trajectory<S> trajectory : trajectories) {
            double[] currentRobustnessEvaluation = monitor.monitor(trajectory).valuesAt(timeStep);
            for (int i = 0; i < timeStep.length; i++) {
                meanEvaluations[i] += currentRobustnessEvaluation[i];
            }
        }
        for(int i = 0; i < timeStep.length; i++) {
            meanEvaluations[i] /= runs;
        }
        return meanEvaluations;
    }



    /**
     * Computes the robustness of a quantitative monitor using normal distribution.
     * Note : the standard deviation is evaluated using Sample Standard Deviation
     *
     * @param monitor           the quantitative monitor to evaluate
     * @param trajectoryProvider the supplier of trajectories
     * @param runs              the number of runs to perform
     * @param timeStep          the time steps at which to evaluate the monitor
     * @param <S>               the type of the monitor state
     * @return an array of NormalDistribution objects representing the robustness of the monitor at each time step
     */
    static <S> NormalDistribution[] computeNormalDistributionRobustness(
            QuantitativeMonitor<S> monitor,
            Supplier<Trajectory<S>> trajectoryProvider,
            int runs,
            double[] timeStep) {

        DescriptiveStatistics[] stats = Arrays.stream(timeStep).mapToObj(i -> new DescriptiveStatistics())
                .toArray(DescriptiveStatistics[]::new);

        for (int run = 0; run < runs; run++) {
            double[] currentRobustnessEvaluation = monitor.monitor(trajectoryProvider.get()).valuesAt(timeStep);
            for (int i = 0; i < timeStep.length; i++) {
                stats[i].addValue(currentRobustnessEvaluation[i]);
            }
        }

        return Arrays.stream(stats)
                .map(stat -> new NormalDistribution(stat.getMean(), stat.getStandardDeviation() == 0 ? Double.MIN_VALUE : stat.getStandardDeviation()))
                .toArray(NormalDistribution[]::new);
    }



    /**
     * Calculates the normal distribution of robustness values for a given monitor, set of trajectories, and time steps.
     * Note : the standard deviation is evaluated using Sample Standard Deviation
     *
     * @param monitor       The quantitative monitor for evaluating trajectories.
     * @param trajectories  The list of trajectories to evaluate.
     * @param timeStep      The array of time steps at which to evaluate the robustness.
     * @param <S>           The type of states in the trajectories.
     * @return An array of NormalDistribution objects representing the computed normal distributions.
     */
    static <S> NormalDistribution[] computeNormalDistributionRobustness(
            QuantitativeMonitor<S> monitor,
            List<Trajectory<S>> trajectories,
            double[] timeStep) {

        DescriptiveStatistics[] stats = Arrays.stream(timeStep).mapToObj(i -> new DescriptiveStatistics())
                .toArray(DescriptiveStatistics[]::new);

        for (Trajectory<S> trajectory : trajectories) {
            double[] currentRobustnessEvaluation = monitor.monitor(trajectory).valuesAt(timeStep);
            for (int i = 0; i < timeStep.length; i++) {
                stats[i].addValue(currentRobustnessEvaluation[i]);
            }
        }

        return Arrays.stream(stats)
                .map(stat -> new NormalDistribution(stat.getMean(), stat.getStandardDeviation() == 0 ? Double.MIN_VALUE : stat.getStandardDeviation()))
                .toArray(NormalDistribution[]::new);
    }

    /**
     * Computes the mean and standard deviation of robustness values at specified time steps.
     *
     * @param monitor the quantitative monitor to evaluate
     * @param trajectoryProvider the supplier of trajectories
     * @param runs the number of runs to perform
     * @param timeStep the time steps at which to evaluate the monitor
     * @param <S> the type of the monitor state
     * @return a double[][] array where:
     *         - results[i][0] is the time step
     *         - results[i][1] is the mean robustness at the time step
     *         - results[i][2] is the standard deviation of robustness at the time step
     */
    static <S> double[][] meanAndStandardDeviationRobustness(
            QuantitativeMonitor<S> monitor,
            Supplier<Trajectory<S>> trajectoryProvider,
            int runs,
            double[] timeStep) {

        double[][] robustnessEvaluations = new double[timeStep.length][runs];

        for (int run = 0; run < runs; run++) {
            Trajectory<S> trajectory = trajectoryProvider.get();
            Signal s = monitor.monitor(trajectory);
            double[] currentRobustnessEvaluation = monitor.monitor(trajectoryProvider.get()).valuesAt(timeStep);
            for (int i = 0; i < timeStep.length; i++) {
                robustnessEvaluations[i][run] = currentRobustnessEvaluation[i];
            }
        }

        return calculateMeanAndSd(robustnessEvaluations, timeStep);
    }


    /**
     * Computes the mean and standard deviation of robustness values at specified time steps.
     *
     * @param monitor the quantitative monitor to evaluate
     * @param trajectories the list of trajectories to evaluate
     * @param timeStep the time steps at which to evaluate the monitor
     * @param <S> the type of the monitor state
     * @return a double[][] array where:
     *         - results[i][0] is the time step
     *         - results[i][1] is the mean robustness at the time step
     *         - results[i][2] is the standard deviation of robustness at the time step
     */
    static <S> double[][] meanAndStandardDeviationRobustness(
            QuantitativeMonitor<S> monitor,
            List<Trajectory<S>> trajectories,
            double[] timeStep) {

        double[][] robustnessEvaluations = new double[timeStep.length][trajectories.size()];

        for (int run = 0; run < trajectories.size(); run++) {
            double[] currentRobustnessEvaluation = monitor.monitor(trajectories.get(run)).valuesAt(timeStep);
            for (int i = 0; i < timeStep.length; i++) {
                robustnessEvaluations[i][run] = currentRobustnessEvaluation[i];
            }
        }
        return calculateMeanAndSd(robustnessEvaluations, timeStep);

    }


    private static double[] generateTimeSteps(double deadline, double dt) {
        int stepsCount = (int) Math.ceil(deadline / dt)+1;
        double[] timeSteps = new double[stepsCount];
        for (int i = 0; i < stepsCount; i++) {
            timeSteps[i] = i * dt;
        }
        return timeSteps;
    }
    /**
     * Computes the mean and standard deviation of robustness values at time steps generated from 0 to the deadline with increments of dt.
     *
     * @param monitor the quantitative monitor to evaluate
     * @param trajectoryProvider the supplier of trajectories
     * @param runs the number of runs to perform
     * @param dt the time step increment
     * @param deadline the total time horizon
     * @param <S> the type of the monitor state
     * @return a double[][] array where:
     *         - results[i][0] is the time step
     *         - results[i][1] is the mean robustness at the time step
     *         - results[i][2] is the standard deviation of robustness at the time step
     */
    static <S> double[][] meanAndStandardDeviationRobustness(
            QuantitativeMonitor<S> monitor,
            Supplier<Trajectory<S>> trajectoryProvider,
            int runs,
            double dt,
            double deadline) {
        return meanAndStandardDeviationRobustness(monitor, trajectoryProvider, runs, generateTimeSteps(deadline-monitor.getTimeHorizon(), dt));
    }
    /**
     * Computes the mean and standard deviation of robustness values at time steps generated from 0 to the monitor's time horizon with increments of dt.
     *
     * @param monitor the quantitative monitor to evaluate
     * @param trajectoryProvider the supplier of trajectories
     * @param runs the number of runs to perform
     * @param dt the time step increment
     * @param <S> the type of the monitor state
     * @return a double[][] array where:
     *         - results[i][0] is the time step
     *         - results[i][1] is the mean robustness at the time step
     *         - results[i][2] is the standard deviation of robustness at the time step
     */
    static <S> double[][] meanAndStandardDeviationRobustness(
            QuantitativeMonitor<S> monitor,
            Supplier<Trajectory<S>> trajectoryProvider,
            int runs,
            double dt) {
        return meanAndStandardDeviationRobustness(monitor, trajectoryProvider, runs, dt, monitor.getTimeHorizon());
    }

//    static <S> double[][] meanAndStandardDeviationRobustness(
//            QuantitativeMonitor<S> monitor,
//            List<Trajectory<S>> trajectories,
//            double dt,
//            double deadline){
//        return meanAndStandardDeviationRobustness(monitor, trajectories, generateTimeSteps(deadline, dt));
//    }



//    static <S> double[][] meanAndStandardDeviationRobustness(
//            QuantitativeMonitor<S> monitor,
//            List<Trajectory<S>> trajectories,
//            double dt){
//        return meanAndStandardDeviationRobustness(monitor, trajectories,dt, monitor.getTimeHorizon());
//    }

    private static double[][] calculateMeanAndSd(double[][] robustnessEvaluations, double[] timeStep) {
        Function<double[], double[]> calculateMeanAndStandardDeviation = getMeanAndSdFunction();
        double[][] results = new double[timeStep.length][3];

        for (int i = 0; i < timeStep.length; i++) {
            double[] meanAndSd = calculateMeanAndStandardDeviation.apply(robustnessEvaluations[i]);
            results[i][0] = timeStep[i];   // Time step
            results[i][1] = meanAndSd[0];  // Mean
            results[i][2] = meanAndSd[1];  // Standard deviation
        }

        return results;
    }

    private static Function<double[], double[]> getMeanAndSdFunction(){
        return values -> {
            double sum = 0.0;
            double sumOfSquares = 0.0;
            int length = values.length;

            for (double value : values) {
                sum += value;
                sumOfSquares += value * value;
            }

            double mean = sum / length;
            double variance = (sumOfSquares - (sum * sum) / length) / (length - 1);
            double standardDeviation = Math.sqrt(variance);

            return new double[]{mean, standardDeviation};
        };
    }


    /**
     * Return the time horizon of interest, which indicates the maximum time duration over which
     * the monitor is concerned.
     *
     * @return The time horizon of interest.
     */
    double getTimeHorizon();



    /**
     * A monitor used to evaluate an atomic formula
     * @param <S> type of states in the trajectory
     */
    static <S> QuantitativeMonitor<S> atomicFormula(ToDoubleFunction<S> function){
        return new QuantitativeMonitor<>() {
            @Override
            public Signal monitor(Trajectory<S> trajectory) {
                return trajectory.apply(function);
            }

            @Override
            public double getTimeHorizon() {
                return 0;
            }
        };
    }

    /**
     * Create a quantitative monitor for the true formula.
     *
     * @param <S> type of states in the trajectory
     * @return A quantitative monitor for the true formula.
     */

    static <S> QuantitativeMonitor<S> trueFormula(){
        return atomicFormula(s -> Double.POSITIVE_INFINITY);
    }

    /**
     * Create a quantitative monitor for the false formula.
     *
     * @param <S> type of states in the trajectory
     * @return A quantitative monitor for the false formula.
     */
    static <S> QuantitativeMonitor<S> falseFormula(){
        return atomicFormula(s -> Double.NEGATIVE_INFINITY);
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
     * A monitor used to evaluate the implication of two formulae.
     *
     * @param m1 Left argument of the implication.
     * @param m2 Right argument of the implication.
     * @param <S> type of states in the trajectory
     * @return A quantitative monitor representing the implication of the two input monitors.
     */
    static <S> QuantitativeMonitor<S> implication(QuantitativeMonitor<S> m1, QuantitativeMonitor<S> m2){
        return new QuantitativeMonitor<>() {
            @Override
            public Signal monitor(Trajectory<S> trajectory) {
                return disjunction(negation(m1),m2).monitor(trajectory);
            }

            @Override
            public double getTimeHorizon() {
                return Math.max(m1.getTimeHorizon(),m2.getTimeHorizon());
            }
        };
    }


    /**
     * A monitor used to evaluate the "if and only if" of two formulae.
     *
     * @param m1 Left argument of the "if and only if".
     * @param m2 Right argument of the "if and only if".
     * @param <S> type of states in the trajectory
     * @return A quantitative monitor representing the "if and only if" of the two input monitors.
     */
    static <S> QuantitativeMonitor<S> ifAndOnlyIf(QuantitativeMonitor<S> m1, QuantitativeMonitor<S> m2){
        return new QuantitativeMonitor<>() {
            @Override
            public Signal monitor(Trajectory<S> trajectory) {
                return conjunction(implication(m1,m2),implication(m2,m1)).monitor(trajectory);
            }

            @Override
            public double getTimeHorizon() {
                return Math.max(m1.getTimeHorizon(),m2.getTimeHorizon());
            }
        };
    }

    /**
     * A monitor used to evaluate an unbounded "until" monitor.
     *
     * @param m1 Left argument of the "until".
     * @param m2 Right argument of the "until".
     * @param <S> type of states in the trajectory
     * @return A quantitative monitor representing the unbounded "until" of the two input monitors.
     */
    private static <S> QuantitativeMonitor<S> until(QuantitativeMonitor<S> m1, QuantitativeMonitor<S> m2) {
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
     * A monitor used to evaluate the "until" monitor.
     *
     * @param m1     Left argument of the "until".
     * @param interval An interval during which the "until" holds.
     * @param m2    Right argument of the "until".
     * @param <S>   a state in the trajectory
     * @return A quantitative monitor representing the "until" of the two input monitors within a specified interval.
     */
    static <S> QuantitativeMonitor<S> until( QuantitativeMonitor<S> m1,Interval interval, QuantitativeMonitor<S> m2) {
        return new QuantitativeMonitor<>() {
            @Override
            public Signal monitor(Trajectory<S> trajectory) {
                return conjunction(until(m1, m2), eventually(interval,m2)).monitor(trajectory);
            }

            @Override
            public double getTimeHorizon() {
                return interval.end() + Math.max(m1.getTimeHorizon(),m2.getTimeHorizon());
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


    /**
     * a monitor used to evaluate the "eventually" monitor.
     *
     * @param interval The time interval over which the "eventually" monitor is applicable.
     * @param m       The quantitative monitor to apply.
     * @return A "quantitative eventually" monitor with the specified interval.
     * @param <S> Type of states in the trajectory.
     * @throws IllegalArgumentException If the start of the interval is greater than or equal to its end.
     */
    static <S> QuantitativeMonitor<S> eventually(Interval interval, QuantitativeMonitor<S> m) {
        if (interval.start()>=interval.end()) throw new IllegalArgumentException();
        return new QuantitativeMonitor<>() {
            @Override
            public Signal monitor(Trajectory<S> trajectory) {
                SlidingWindow sw = new SlidingWindow(interval.start(), interval.end());
                return sw.apply(m.monitor(trajectory));
            }

            @Override
            public double getTimeHorizon() {
                return interval.end() + m.getTimeHorizon();
            }
        };
    }


    /**
     * Create a "quantitative globally" monitor with a specified interval.
     *
     * @param interval The time interval over which the "globally" monitor is applicable.
     * @param m       The quantitative monitor to apply.
     * @return A "quantitative globally" monitor with the specified interval.
     * @param <S> Type of states in the trajectory.
     */
    static <S> QuantitativeMonitor<S> globally(Interval interval,QuantitativeMonitor<S> m) {
        return new QuantitativeMonitor<>() {
            @Override
            public Signal monitor(Trajectory<S> trajectory) {
                return negation(eventually(interval, negation(m))).monitor(trajectory);
            }

            @Override
            public double getTimeHorizon() {
                return interval.end() + m.getTimeHorizon();
            }
        };
    }

}
