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

import it.unicam.quasylab.sibilla.core.simulator.util.WeightedElement;
import it.unicam.quasylab.sibilla.core.simulator.util.WeightedStructure;
import org.apache.commons.math3.random.RandomGenerator;
import it.unicam.quasylab.sibilla.core.util.SibillaMessages;

import java.util.LinkedList;
import java.util.List;

/**
 * This is a model implementing a Markov process. 
 * 
 */
public interface MarkovProcess<S extends State> extends Model<S> {

	/**
	 * Returns the transitions enabled in a given state at a given time. Each transition
	 * is represented via a <code>StepFunction</code>, and all the enabled transitions are stored
	 * in a <code>WeightedStructure</code> that associates each function with its rate.
	 *
	 *
	 * @param r random generator used to sample needed random varibales.
	 * @param time current time.
	 * @param s current state.
	 * @return the weighted structure with all the enabled transitions.
	 */
	WeightedStructure<StepFunction<S>> getTransitions(RandomGenerator r, double time, S s);

	@Override
	default TimeStep<S> next(RandomGenerator r, double time, S state) {
		WeightedStructure<StepFunction<S>> activities = getTransitions(r, time, state);
		double totalRate = activities.getTotalWeight();
		if (totalRate == 0.0) {
			return null;
		}
		double dt = sampleExponentialDistribution(totalRate,r);
		double select = r.nextDouble() * totalRate;
		WeightedElement<StepFunction<S>> wa = activities.select(select);
		return new TimeStep<>(dt,wa.getElement().step(r,time,dt));
	}

	@Override
	default List<Action<S>> actions(RandomGenerator r, double time, S state) {
		WeightedStructure<StepFunction<S>> activities = getTransitions(r, time, state);
		List<Action<S>> list = new LinkedList<>();
		for (WeightedElement<StepFunction<S>> w: activities.getAll()) {
			list.add(Action.actionOfMarkovStepFunction(time,activities.getTotalWeight(),w.getWeight(),state,w.getElement()));
		}
		return list;
	}

	/**
	 * Sample a random value of a random variable exponentially distributed with
	 * parameter <code>rate</code>.
	 *
	 * @param rate a positive value representing the parameter of an exponentially
	 *             distributed random variable.
	 * @param r a random generator.
	 * @return a random value sampled from an exponentially distributed random variable
	 * with parameter <code>rate</code>.
	 */
	static double sampleExponentialDistribution(double rate, RandomGenerator r) {
		if (rate <= 0) {
			throw new IllegalArgumentException(SibillaMessages.aPositiveValueIsExpected(rate));
		}
		return (1.0 / rate) * Math.log(1 / (r.nextDouble()));
	}

}
