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

package it.unicam.quasylab.sibilla.core.models;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * Represents an action that may change current state of a model. When action is executed,
 * it duration time is sampled together with the next state. An action is selected with
 * a given probability. Moreover an action can be reverted to obtain the state originating
 * the action.
 *
 * @param <S> type of model state.
 */
public interface Action<S> {

    /**
     * Returns the probability that this action is selected as next step.
     *
     * @return the probability that this action is selected as next step.
     */
    double probability();

    /**
     * Returns the model state after action execution. The <code>RandomGenerator</code> is
     * the one used by the action to sample possible random values.
     *
     * @param r random generator used to sample needed random varibales.
     * @return result of action execution.
     */
    TimeStep<S> execute(RandomGenerator r);

    /**
     * Returns the model state before action execution.
     *
     * @return model state before action execution.
     */
    S revert();

    /**
     * Utility method that is used to create the action associated to a transition in a markov process.
     *
     * @param now time when the transition is performed.
     * @param totalRate total exit rate of current state.
     * @param stepRate rate of the selected step.
     * @param state current state.
     * @param f lazy function used to compute next state.
     * @param <S> type of states of Markov process.
     * @return the action associated to a transition in a markov process.
     */
    static <S> Action<S> actionOfMarkovStepFunction(double now, double totalRate, double stepRate, S state, StepFunction<S> f) {
        return new Action<>() {

            @Override
            public double probability() {
                return stepRate/totalRate;
            }

            @Override
            public TimeStep<S> execute(RandomGenerator r) {
                double dt = MarkovProcess.sampleExponentialDistribution(totalRate,r);
                return new TimeStep<>(dt,f.step(r,now,dt));
            }

            @Override
            public S revert() {
                return state;
            }
        };
    }
}
