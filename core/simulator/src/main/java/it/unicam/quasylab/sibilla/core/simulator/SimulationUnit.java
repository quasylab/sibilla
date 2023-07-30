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

package it.unicam.quasylab.sibilla.core.simulator;

import it.unicam.quasylab.sibilla.core.models.State;
import it.unicam.quasylab.sibilla.core.models.StatePredicate;
import it.unicam.quasylab.sibilla.core.simulator.sampling.SamplePredicate;
import it.unicam.quasylab.sibilla.core.simulator.sampling.SamplingHandler;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.Serializable;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author loreti
 *
 */
public class SimulationUnit<S extends State> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2809995821274223033L;

	private final BiFunction<RandomGenerator, Function<RandomGenerator,S>, SimulatorCursor<S>> simulationCursorSupplier;
	
	private final Function<RandomGenerator,S> stateSupplier;
	
	private final SamplePredicate<? super S> stoppingPredicate;
	
	private final StatePredicate<? super S> reachPredicate;

	private final Supplier<SamplingHandler<S>> handlerSupplier;

	public SimulationUnit(BiFunction<RandomGenerator, Function<RandomGenerator,S>, SimulatorCursor<S>> simulationCursorSupplier, Function<RandomGenerator,S> stateSupplier, Supplier<SamplingHandler<S>> handlerSupplier, SamplePredicate<? super S> stoppingPredicate) {
		this(simulationCursorSupplier, stateSupplier,handlerSupplier,stoppingPredicate,StatePredicate.TRUE);
	}

	public SimulationUnit(BiFunction<RandomGenerator, Function<RandomGenerator,S>, SimulatorCursor<S>> simulationCursorSupplier, S stateSupplier, Supplier<SamplingHandler<S>> handlerSupplier, SamplePredicate<? super S> stoppingPredicate) {
		this(simulationCursorSupplier, stateSupplier,handlerSupplier,stoppingPredicate,StatePredicate.TRUE);
	}

	public SimulationUnit(BiFunction<RandomGenerator, Function<RandomGenerator,S>, SimulatorCursor<S>> simulationCursorSupplier, S stateSupplier, Supplier<SamplingHandler<S>> handlerSupplier, SamplePredicate<? super S> stoppingPredicate, StatePredicate<? super S> reachPredicate) {
		this(simulationCursorSupplier, rg -> stateSupplier, handlerSupplier, stoppingPredicate, reachPredicate);
	}

	public SimulationUnit(BiFunction<RandomGenerator, Function<RandomGenerator,S>, SimulatorCursor<S>> simulationCursorSupplier, Function<RandomGenerator,S> stateSupplier, Supplier<SamplingHandler<S>> handlerSupplier, SamplePredicate<? super S> stoppingPredicate, StatePredicate<? super S> reachPredicate) {
		this.simulationCursorSupplier = simulationCursorSupplier;
		this.stateSupplier = stateSupplier;
		this.handlerSupplier = handlerSupplier;
		this.stoppingPredicate = stoppingPredicate;
		this.reachPredicate = reachPredicate;
	}

	public Function<RandomGenerator, S> getStateSupplier() {
		return stateSupplier;
	}

	public SamplingHandler<S> getSamplingHandler() {
		return handlerSupplier.get();
	}

	/**
	 * @return the stoppingPredicate
	 */
	public SamplePredicate<? super S> getStoppingPredicate() {
		return stoppingPredicate;
	}

	/**
	 * 
	 * @return the reachPredicate
	 */
	public StatePredicate<? super S> getReachPredicate() {
		return reachPredicate;
	}


    public SimulatorCursor<S> getSimulationCursor(RandomGenerator random) {
		return this.simulationCursorSupplier.apply(random, this.stateSupplier);
    }
}
