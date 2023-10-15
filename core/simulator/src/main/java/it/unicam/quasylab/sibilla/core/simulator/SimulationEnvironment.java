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

import it.unicam.quasylab.sibilla.core.models.*;
import it.unicam.quasylab.sibilla.core.simulator.sampling.SamplePredicate;
import it.unicam.quasylab.sibilla.core.simulator.sampling.SamplingFunction;
import it.unicam.quasylab.sibilla.core.simulator.sampling.SamplingHandler;
import it.unicam.quasylab.sibilla.core.simulator.sampling.TrajectoryCollector;
import it.unicam.quasylab.sibilla.core.tools.stl.QualitativeMonitor;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * An object responsible for managing simulations. When a new request is
 * received, it is delegated to a {@link SimulationManager} that is built via
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
	 * @param simulationManagerFactory factory function used to create the simulation manager.
	 */
	public SimulationEnvironment(SimulationManagerFactory simulationManagerFactory) {
		this.simulationManagerFactory = simulationManagerFactory;
		LOGGER.info("Simulation environment created");
	}

	/**
	 * Performs a given number of simulations of a given {@link Model} a new set of
	 * simulations. Data are collected via a {@link SamplingFunction}. A monitor is
	 * passed to control simulation. The starting state of the simulation is obtained by
	 * using the <code>initialStateSupplier</code> passed as argument.
	 *
	 * @param model             model to simulate.
	 * @param initialStateSupplier      initial state supplier.
	 * @param handlerSupplier 	supplier used to build the function used to collect data from the sampled trajectories.
	 * @param iterations        number of iterations.
	 * @param deadline          simulation deadline.
	 *
	 * @throws InterruptedException is thrown when simulation is interrupted.
	 */
	public <S extends State> void simulate(
			Model<S> model,
			Function<RandomGenerator,S> initialStateSupplier,
			Supplier<SamplingHandler<S>> handlerSupplier,
			int iterations,
			double deadline) throws InterruptedException {
		simulate(null, new DefaultRandomGenerator(), model, initialStateSupplier, handlerSupplier, iterations, deadline);
	}

	/**
	 * Performs a given number of simulations of a given {@link Model} a new set of
	 * simulations. Data are collected via a {@link SamplingFunction}. A monitor is
	 * passed to control simulation.The starting state of the simulation is obtained by
	 * using the <code>initialStateSupplier</code> passed as argument.
	 *
	 * @param random            random generator used in the simulation.
	 * @param model             model to simulate.
	 * @param initialStateSupplier      initial state supplier.
	 * @param handlerSupplier 	supplier used to build the function used to collect data from the sampled trajectories.
	 * @param iterations        number of iterations.
	 * @param deadline          simulation deadline.
	 *
	 * @throws InterruptedException is thrown when simulation is interrupted.
	 */
	public <S extends State> void simulate(
			RandomGenerator random,
			Model<S> model,
			Function<RandomGenerator,S> initialStateSupplier,
			Supplier<SamplingHandler<S>> handlerSupplier,
			long iterations,
			double deadline) throws InterruptedException {
		simulate(null, random, model, initialStateSupplier, handlerSupplier, iterations, deadline);
	}

	/**
	 * Performs a given number of simulations of a given {@link Model} a new set of
	 * simulations. Data are collected via a {@link SamplingFunction}. A monitor is
	 * passed to control simulation.
	 *
	 * @param monitor           monitor used to control simulation.
	 * @param random            random generator used in the simulation.
	 * @param model             model to simulate.
	 * @param initialStateSupplier      initial state supplier.
	 * @param handlerSupplier 	supplier used to create the function used to collect data from the sampled trajectories.
	 * @param iterations        number of iterations.
	 * @param deadline          simulation deadline.
	 *
	 * @throws InterruptedException is thrown when simulation is interrupted.
	 */
	public <S extends State> void simulate(
			SimulationMonitor monitor,
			RandomGenerator random,
			Model<S> model,
			Function<RandomGenerator,S> initialStateSupplier,
			Supplier<SamplingHandler<S>> handlerSupplier,
			long iterations,
			double deadline)
			throws InterruptedException {
		simulate(monitor, random, model::createSimulationCursor, initialStateSupplier, handlerSupplier, iterations, deadline);
	}

	/**
	 * Performs a given number of simulations by using the given cursor supplier. Data are collected via
	 * a {@link SamplingFunction}. A monitor is passed to control simulation.
	 *
	 * @param monitor           monitor used to control simulation.
	 * @param random            random generator used in the simulation.
	 * @param cursorSupplier    function used to build the simulator cursor.
	 * @param initialStateSupplier      initial state supplier.
	 * @param handlerSupplier 	supplier used to create the function used to collect data from the sampled trajectories.
	 * @param iterations        number of iterations.
	 * @param deadline          simulation deadline.
	 *
	 * @throws InterruptedException is thrown when simulation is interrupted.
	 */
	public <S extends State> void simulate(
			SimulationMonitor monitor,
			RandomGenerator random,
			BiFunction<RandomGenerator, Function<RandomGenerator, S>, SimulatorCursor<S>> cursorSupplier,
			Function<RandomGenerator,S> initialStateSupplier,
			Supplier<SamplingHandler<S>> handlerSupplier,
			long iterations,
			double deadline)
			throws InterruptedException {
		SimulationManager<S> simulationManager = simulationManagerFactory.getSimulationManager(random, monitor);
		SimulationUnit<S> unit = new SimulationUnit<>(cursorSupplier, initialStateSupplier, handlerSupplier,
				SamplePredicate.timeDeadlinePredicate(deadline));
		for (long i = 0; (((monitor == null) || (!monitor.isCancelled())) && (i < iterations)); i++) {
			simulationManager.simulate(unit);
		}
		//TODO: check if we have to add this code --> simulationManager.join();
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
			Function<RandomGenerator,S> state, StatePredicate<? super S> goal) throws InterruptedException {
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
			Function<RandomGenerator,S> state, StatePredicate<? super S> condition, StatePredicate<? super S> goal) throws InterruptedException {
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
			double deadline, Model<S> model, Function<RandomGenerator,S> state, StatePredicate<? super S> condition,
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
			double errorProbability, double delta, double deadline, Model<S> model, Function<RandomGenerator,S> state,
			StatePredicate<? super S> condition, StatePredicate<? super S> goal) throws InterruptedException {
			return reachability(monitor, random, errorProbability, delta, deadline, model::createSimulationCursor, state, condition, goal);
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
	 * @param cursorSupplier   cursor used to generate the simulated trajectory.
	 * @param state            initial state
	 * @param condition        condition predicate.
	 * @param goal             goal predicate.
	 * @return the probability to reach a state satisfying the given condition
	 *         within the given deadline.
	 * @throws InterruptedException is thrown when simulation is interrupted.
	 */
	public <S extends State> double reachability(
			SimulationMonitor monitor,
			RandomGenerator random,
			double errorProbability,
			double delta,
			double deadline,
			BiFunction<RandomGenerator, Function<RandomGenerator, S>, SimulatorCursor<S>> cursorSupplier,
			Function<RandomGenerator,S> state,
			StatePredicate<? super S> condition,
			StatePredicate<? super S> goal) throws InterruptedException {
		ReachabilityChecker<S> reachabilityChecker = new ReachabilityChecker<S>(condition, goal);
		double n = Math.ceil(Math.log(2 / delta) / (2 * Math.pow(errorProbability,2)));
		LOGGER.info("Computing reachability with "+(int) n+" iterations.");
		SimulationUnit<S> unit = new SimulationUnit<>(cursorSupplier, state, reachabilityChecker,
				(t, s) -> (t > deadline) || goal.check(s) || !condition.check(s), goal);
		SimulationManager<S> simulationManager = simulationManagerFactory.getSimulationManager(random, monitor);

		for (int i = 0; i < n; i++) {
			simulationManager.simulate(unit);
		}
		simulationManager.shutdown();
		return reachabilityChecker.numberOfSuccessful() / n;
	}

	public <S extends State> double robustnessAtZero(
			SimulationMonitor monitor,
			RandomGenerator randomGenerator,
			QualitativeMonitor<? extends S> qualitativeMonitor
	){
		return 0.0;
	}

	private static class ReachabilityChecker<S extends State> implements Supplier<SamplingHandler<S>> {

		private int counter = 0;
		private final StatePredicate<? super S> goal;
		private final StatePredicate<? super S> condition;

		public ReachabilityChecker(StatePredicate<? super S> condition, StatePredicate<? super S> goal) {
			this.condition = condition;
			this.goal = goal;
		}

		private synchronized void record(boolean doReach) {
			if (doReach) {
				counter++;
			}
		}

		public synchronized double numberOfSuccessful() {
			return counter;
		}

		@Override
		public SamplingHandler<S> get() {
			return new SamplingHandler<S>() {

				private boolean reached = false;
				private boolean failed = false;

				@Override
				public void start() {

				}

				@Override
				public void sample(double time, S state) {
					failed = failed || (!condition.check(state)&&!goal.check(state));
					reached = reached || goal.check(state);
				}

				@Override
				public void end(double time) {
					record(!failed&&reached);
				}
			};
		}
	}

	public <S extends ImmutableState> Trajectory<S> sampleTrajectory(RandomGenerator random, Model<S> model, S state,
			double deadline) {
		return sampleTrajectory(random, model::createSimulationCursor, state, deadline);
	}

	public <S extends ImmutableState> Trajectory<S> sampleTrajectory(
			RandomGenerator random,
			BiFunction<RandomGenerator, Function<RandomGenerator, S>, SimulatorCursor<S>> cursorSupplier,
			S state,
			double deadline) {
		TrajectoryCollector<S> collector = new TrajectoryCollector<>();
		SimulationUnit<S> unit = new SimulationUnit<>(cursorSupplier, state, collector, SamplePredicate.timeDeadlinePredicate(deadline),
				s -> true);
		SimulationTask<S> simulationRun = new SimulationTask<>(0, random, unit);
			try {
			simulationRun.get();
			return collector.getTrajectory();
		} catch (Exception e) {
			LOGGER.severe(e.getMessage());
			return null;
		}
	}

	public <S extends ImmutableState> Trajectory<S> sampleTrajectory(
			RandomGenerator random,
			SimulationStepFunction<S> stepFunction,
			S state,
			double deadline) {
		Trajectory<S> trajectory = new Trajectory<>();
		double time = 0.0;
		S current = state;
		try {
			while (time<deadline) {
				trajectory.add(time, current);
				Optional<TimeStep<S>> optionalStep = stepFunction.next(random, time, current);
				if (optionalStep.isPresent()) {
					TimeStep<S> step = optionalStep.get();
					time += step.getTime();
					current = step.getValue();
				} else {
					time = deadline;
				}
			}
			trajectory.setEnd(deadline);
			return trajectory;
		} catch (Exception e) {
			LOGGER.severe(e.getMessage());
			return null;
		}
	}


	public <S extends ImmutableState> Trajectory<S> sampleTrajectory(
			RandomGenerator random,
			ContinuousTimeMarkovProcess<S> model,
			S state,
			double deadline, StatePredicate<? super S> reachPredicate) {
		TrajectoryCollector<S> collector = new TrajectoryCollector<>();
		SimulationUnit<S> unit = new SimulationUnit<S>(model::createSimulationCursor, state, collector,
				SamplePredicate.samplePredicate(deadline, reachPredicate), reachPredicate);
		SimulationTask<S> simulationRun = new SimulationTask<>(random, unit);
		try {
			simulationRun.get();
			return collector.getTrajectory();
		} catch (Exception e) {
			LOGGER.severe(e.getMessage());
			return null;
		}
	}

}
