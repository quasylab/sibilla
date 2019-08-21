/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package quasylab.sibilla.core.simulator;

import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.commons.math3.random.RandomGenerator;

import quasylab.sibilla.core.simulator.SimulationManager.SimulationSession;
import quasylab.sibilla.core.simulator.sampling.SamplingFunction;

/**
 * @author loreti
 *
 */
public class SimulationEnvironment {

	private SimulationManager simManager;

	public SimulationEnvironment() {
		this(new ThreadSimulationManager(1));
	}

	public SimulationEnvironment(SimulationManager simManager) {
		this.simManager = simManager;
	}

	public synchronized <S> void simulate(RandomGenerator random, Model<S> model, S initialState, SamplingFunction<S> sampling_function, int iterations, double deadline) throws InterruptedException {
		simulate(null, random, model, initialState, sampling_function, iterations, deadline);
	}

	public synchronized <S> void simulate(SimulationMonitor monitor, RandomGenerator random, Model<S> model, S initialState, SamplingFunction<S> sampling_function, int iterations, double deadline) throws InterruptedException {
		RandomGeneratorRegistry rgi = RandomGeneratorRegistry.getInstance();
		SimulationSession<S> session = simManager.newSession(random,trc -> trc.sample(sampling_function));
		SimulationUnit<S> unit = new SimulationUnit<S>(model, initialState,SamplePredicate.timeDeadlinePredicate(deadline),s -> true);
		rgi.register(random);//FIXME: Remove!
		for (int i = 0; (((monitor == null) || (!monitor.isCancelled())) && (i < iterations)); i++) {
			if (monitor != null) {
				monitor.startIteration(i);
			}
//			System.out.print('<');
//			if ((i + 1) % 50 == 0) {
//				System.out.print(i + 1);
//			}
			session.simulate(unit);
			if (monitor != null) {
				monitor.endSimulation(i);
			}
//			System.out.print('>');
//			if ((i + 1) % 50 == 0) {
//				System.out.print("\n");
//			}
//			System.out.flush();
		}
		session.join();
		rgi.unregister();
		System.out.println("DONE!");
	}

	public <S> double reachability(SimulationMonitor monitor, RandomGenerator random, Model<S> model, S state, double error, double delta, double deadline, Predicate<? super S> phi,
			Predicate<? super S> psi) throws InterruptedException {
		double n = Math.ceil(Math.log(2 / delta) / (2 * error));
		ReachabilityTraceConsumer<S> traceConsumer = new ReachabilityTraceConsumer<>();
		SimulationUnit<S> unit = new SimulationUnit<S>(model, state, (t,s) -> (t>=deadline)||psi.test(s)||!phi.test(s), psi);
		SimulationSession<S> session = simManager.newSession(random, traceConsumer);
		for (int i = 0; i < n; i++) {
			session.simulate(unit);
		}
		session.join();
		return traceConsumer.counter / n;
	}

	public <S> Trajectory<S> sampleTrajectory(RandomGenerator random, Model<S> model, S state, double deadline) {
		SimulationUnit<S> unit = new SimulationUnit<S>(model, state,SamplePredicate.timeDeadlinePredicate(deadline),s -> true);
		SimulationTask<S> simulationRun = new SimulationTask<>(random, unit);
		try {
			return simulationRun.get();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public <S> Trajectory<S> sampleTrajectory(RandomGenerator random, Model<S> model, S state, double deadline, Predicate<? super S> reachPredicate) {
		SimulationUnit<S> unit = new SimulationUnit<S>(model, state, (t,s) -> (t>=deadline)||reachPredicate.test(s), reachPredicate);
		SimulationTask<S> simulationRun = new SimulationTask<>(random, unit);
		try {
			return simulationRun.get();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public <S> Trajectory<S> sampleTrajectory(RandomGenerator random, Model<S> model, S state, double deadline, Predicate<? super S> transientPredicate,
			Predicate<? super S> reachPredicate) {
		SimulationUnit<S> unit = new SimulationUnit<S>(model, state, (t,s) -> (t>=deadline)||reachPredicate.test(s)||!transientPredicate.test(s), reachPredicate);
		SimulationTask<S> simulationRun = new SimulationTask<>(random, unit);
		try {
			return simulationRun.get();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	private static class ReachabilityTraceConsumer<S> implements Consumer<Trajectory<S>> {

		private int counter = 0;
		
		@Override
		public void accept(Trajectory<S> t) {
			if (t.isSuccesfull()) {
				counter++;
			}
		}
		
	}
	
}
