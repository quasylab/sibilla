/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 * Copyright (C) 2020.
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package quasylab.sibilla.core.simulator;

import org.apache.commons.math3.random.RandomGenerator;
import quasylab.sibilla.core.models.*;
import quasylab.sibilla.core.past.State;
import quasylab.sibilla.core.simulator.sampling.SamplePredicate;
import quasylab.sibilla.core.simulator.sampling.SamplingFunction;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * An object responsible for managing simulations. When a new request is
 * received, it is delegated to a {@ling SimulationMangaer} that is built via
 * {@link SimulationManagerFactory}.
 */
public class SimulationEnvironment implements Serializable {

	/**
	 * Default simulation manager factory.
	 */
	public final static SimulationManagerFactory DEFAULT_FACTORY = SequentialSimulationManager::new;
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(SimulationEnvironment.class.getName());
	public static boolean silent = true;
	private final SimulationManagerFactory simulationManagerFactory;

	/**
	 * Creates a new simulation environment with default simulation factory. The
	 * latter is a multithreaded based simulation factory.
	 */
	public SimulationEnvironment() {
		this(DEFAULT_FACTORY);
	}

	/**
	 * Creates a new simulation environment given a
	 * {@link SimulationManagerFactory}. The latter is used to instantiate the
	 * {@link QueuedSimulationManager} used to handle the specific simulation.
	 *
	 * @param simulationManagerFactory
	 */
	public SimulationEnvironment(SimulationManagerFactory simulationManagerFactory) {
		this.simulationManagerFactory = simulationManagerFactory;
		LOGGER.info("Simulation environment created");
	}

	/**
	 * Performs a given number of simulations of a given {@link Model} a new set of
	 * simulations. Data are collected via a {@link SamplingFunction}. A monitor is
	 * passed to control simulation.
	 *
	 * @param model             model to simulate.
	 * @param initialState      initial state.
	 * @param sampling_function sampling functions to use.
	 * @param iterations        number of interations.
	 * @param deadline          simulation deadline.
	 *
	 * @throws InterruptedException is thrown when simulation is interrupted.
	 */
	public <S extends State> void simulate(Model<S> model, S initialState, SamplingFunction<S> sampling_function,
			int iterations, double deadline) throws InterruptedException {
		simulate(null, new DefaultRandomGenerator(), model, initialState, sampling_function, iterations, deadline);
	}

	/**
	 * Performs a given number of simulations of a given {@link Model} a new set of
	 * simulations. Data are collected via a {@link SamplingFunction}. A monitor is
	 * passed to control simulation.
	 *
	 * @param random            random generator used in the simulation.
	 * @param model             model to simulate.
	 * @param initialState      initial state.
	 * @param sampling_function sampling functions to use.
	 * @param iterations        number of interations.
	 * @param deadline          simulation deadline.
	 *
	 * @throws InterruptedException is thrown when simulation is interrupted.
	 */
	public <S extends State> void simulate(RandomGenerator random, Model<S> model, S initialState,
			SamplingFunction<S> sampling_function, int iterations, double deadline) throws InterruptedException {
		simulate(null, random, model, initialState, sampling_function, iterations, deadline);
	}

	/**
	 * Performs a given number of simulations of a given {@link Model} a new set of
	 * simulations. Data are collected via a {@link SamplingFunction}. A monitor is
	 * passed to control simulation.
	 *
	 * @param monitor           monitor used to control simulation.
	 * @param random            random generator used in the simulation.
	 * @param model             model to simulate.
	 * @param initialState      initial state.
	 * @param sampling_function sampling functions to use.
	 * @param iterations        number of interations.
	 * @param deadline          simulation deadline.
	 *
	 * @throws InterruptedException is thrown when simulation is interrupted.
	 */
	public <S extends State> void simulate(SimulationMonitor monitor, RandomGenerator random, Model<S> model,
			S initialState, SamplingFunction<S> sampling_function, int iterations, double deadline)
			throws InterruptedException {
		SimulationManager<S> simulationManager = simulationManagerFactory.getSimulationManager(random, monitor,
				model.getModelDefinition(), trc -> trc.sample(sampling_function));
		SimulationUnit<S> unit = new SimulationUnit<S>(model, initialState,
				SamplePredicate.timeDeadlinePredicate(deadline));
		for (int i = 0; (((monitor == null) || (!monitor.isCancelled())) && (i < iterations)); i++) {
			simulationManager.simulate(unit);
		}
		simulationManager.shutdown();
		LOGGER.info("The simulation has concluded with success");
		if (monitor != null) {
			monitor.endSimulation();
		}
	}

	/**
	 * Estimates the probability to reach a state satisfying the given goal
	 * predicate within the given deadline while traversing only states satisfying a
	 * given condition. The estimated probability differs from the exact one by
	 * <code>delta</code> with a probability less or equal than
	 * <code>errorProbability</code>.
	 *
	 * @param errorProbability error probability.
	 * @param delta            error gap.
	 * @param deadline         reachability deadline.
	 * @param model            model to simulate.
	 * @param state            initial state.
	 * @param goal             goal predicate.
	 * @return the probability to reach a state satisfying the given condition
	 *         within the given deadline.
	 * @throws InterruptedException is thrown when simulation is interrupted.
	 */
	public <S extends State> double reachability(double errorProbability, double delta, double deadline, Model<S> model,
			S state, StatePredicate<? super S> goal) throws InterruptedException {
		return reachability(new DefaultRandomGenerator(), errorProbability, delta, deadline, model, state, s -> true,
				goal);
	}

	/**
	 * Estimates the probability to reach a state satisfying the given goal
	 * predicate within the given deadline while traversing only states satisfying a
	 * given condition. The estimated probability differs from the exact one by
	 * <code>delta</code> with a probability less or equal than
	 * <code>errorProbability</code>.
	 *
	 * @param errorProbability error probability.
	 * @param delta            error gap.
	 * @param deadline         reachability deadline.
	 * @param model            model to simulate.
	 * @param state            initial state
	 * @param condition        condition predicate.
	 * @param goal             goal predicate.
	 * @return the probability to reach a state satisfying the given condition
	 *         within the given deadline.
	 * @throws InterruptedException is thrown when simulation is interrupted.
	 */
	public <S extends State> double reachability(double errorProbability, double delta, double deadline, Model<S> model,
			S state, StatePredicate<? super S> condition, StatePredicate<? super S> goal) throws InterruptedException {
		return reachability(new DefaultRandomGenerator(), errorProbability, delta, deadline, model, state, condition,
				goal);
	}

	/**
	 * Estimates the probability to reach a state satisfying the given goal
	 * predicate within the given deadline while traversing only states satisfying a
	 * given condition. The estimated probability differs from the exact one by
	 * <code>delta</code> with a probability less or equal than
	 * <code>errorProbability</code>.
	 *
	 * @param random           random generator used in the simulation.
	 * @param errorProbability error probability.
	 * @param delta            error gap.
	 * @param deadline         reachability deadline.
	 * @param model            model to simulate.
	 * @param state            initial state
	 * @param condition        condition predicate.
	 * @param goal             goal predicate.
	 * @return the probability to reach a state satisfying the given condition
	 *         within the given deadline.
	 * @throws InterruptedException is thrown when simulation is interrupted.
	 */
	public <S extends State> double reachability(RandomGenerator random, double errorProbability, double delta,
			double deadline, Model<S> model, S state, StatePredicate<? super S> condition,
			StatePredicate<? super S> goal) throws InterruptedException {
		return reachability(null, random, errorProbability, delta, deadline, model, state, condition, goal);
	}

	/**
	 * Estimates the probability to reach a state satisfying the given goal
	 * predicate within the given deadline while traversing only states satisfying a
	 * given condition. The estimated probability differs from the exact one by
	 * <code>delta</code> with a probability less or equal than
	 * <code>errorProbability</code>.
	 *
	 * @param monitor          monitor used to control simulation.
	 * @param random           random generator used in the simulation.
	 * @param errorProbability error probability.
	 * @param delta            error gap.
	 * @param deadline         reachability deadline.
	 * @param model            model to simulate.
	 * @param state            initial state
	 * @param condition        condition predicate.
	 * @param goal             goal predicate.
	 * @return the probability to reach a state satisfying the given condition
	 *         within the given deadline.
	 * @throws InterruptedException is thrown when simulation is interrupted.
	 */
	public <S extends State> double reachability(SimulationMonitor monitor, RandomGenerator random,
			double errorProbability, double delta, double deadline, Model<S> model, S state,
			StatePredicate<? super S> condition, StatePredicate<? super S> goal) throws InterruptedException {
		ReachabilityTraceConsumer<S> traceConsumer = new ReachabilityTraceConsumer<>();
		double n = Math.ceil(Math.log(2 / delta) / (2 * errorProbability));
		SimulationUnit<S> unit = new SimulationUnit<>(model, state,
				(t, s) -> (t >= deadline) || goal.check(s) || !condition.check(s), goal);
		SimulationManager<S> simulationManager = simulationManagerFactory.getSimulationManager(random, monitor,
				model.getModelDefinition(), traceConsumer);

		for (int i = 0; i < n; i++) {
			simulationManager.simulate(unit);
		}
		simulationManager.shutdown();
		return traceConsumer.counter / n;
	}

	private static class ReachabilityTraceConsumer<S extends State> implements Consumer<Trajectory<S>> {

		private int counter = 0;

		@Override
		public void accept(Trajectory<S> t) {
			if (t.isSuccesfull()) {
				counter++;
			}
			if (!silent) {
				System.out.print(t.isSuccesfull() ? '+' : '-');
				System.out.flush();
			}
		}

	}

	public <S extends State> Trajectory<S> sampleTrajectory(RandomGenerator random, MarkovProcess<S> model, S state,
			double deadline) {
		SimulationUnit<S> unit = new SimulationUnit<>(model, state, SamplePredicate.timeDeadlinePredicate(deadline),
				s -> true);
		SimulationTask<S> simulationRun = new SimulationTask<>(0, random, unit);
		try {
			return simulationRun.get();
		} catch (Exception e) {
			LOGGER.severe(e.getMessage());
			return null;
		}
	}

	public <S extends State> Trajectory<S> sampleTrajectory(RandomGenerator random, MarkovProcess<S> model, S state,
			double deadline, StatePredicate<? super S> reachPredicate) {
		SimulationUnit<S> unit = new SimulationUnit<S>(model, state,
				SamplePredicate.samplePredicate(deadline, reachPredicate), reachPredicate);
		SimulationTask<S> simulationRun = new SimulationTask<>(random, unit);
		try {
			return simulationRun.get();
		} catch (Exception e) {
			LOGGER.severe(e.getMessage());
			return null;
		}
	}

}
