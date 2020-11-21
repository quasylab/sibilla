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
 * Implements the lazy evaluation of the transition step of a stochastic model.
 */
@FunctionalInterface
public interface StepFunction<S> {

	/**
	 * Computes the next state associated to a transition.
	 *
	 * @param r random
	 * @param now
	 * @param dt
	 * @return
	 */
	S step(RandomGenerator r , double now , double dt);

}
