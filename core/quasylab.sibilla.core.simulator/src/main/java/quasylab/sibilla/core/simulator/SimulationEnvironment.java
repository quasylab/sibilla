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
import java.util.function.Predicate;

import org.apache.commons.math3.random.RandomGenerator;


import quasylab.sibilla.core.simulator.sampling.SamplingFunction;
import quasylab.sibilla.core.simulator.sampling.SimulationTimeSeries;
import quasylab.sibilla.core.simulator.util.WeightedElement;
import quasylab.sibilla.core.simulator.util.WeightedStructure;

/**
 * @author loreti
 *
 */
public class SimulationEnvironment<M extends Model<S>, S> {

	protected RandomGenerator random;
	private M model;
	private S state;
	private SamplingFunction<S> sampling_function;
	private int iterations = 0;
	private SimulationManager<S> simManager;

	public SimulationEnvironment(M model) {
		this(model, new ThreadSimulationManager<S>(1));
	}

	public SimulationEnvironment(M model, SimulationManager<S> simManager) {
		this(model, new DefaultRandomGenerator(), simManager);
	}

	public SimulationEnvironment(M model, RandomGenerator randomGenerator, SimulationManager<S> simManager) {
		this.model = model;
		this.random = randomGenerator;
		this.simManager = simManager;
	}

	public void setModel(M model) {
		this.model = model;
	}

	public void seed(long seed) {
		random.setSeed(seed);
	}

	public void setSampling(SamplingFunction<S> sampling_function) {
		this.sampling_function = sampling_function;
	}

	public synchronized void simulate(SimulationMonitor monitor, int iterations, double deadline) throws InterruptedException {
		RandomGeneratorRegistry rgi = RandomGeneratorRegistry.getInstance();
		SimulationSession<S> session1 = simManager.newSession(iterations, sampling_function, true);
		//SimulationSession<S> session2 = simManager.newSession(iterations, sampling_function);
		rgi.register(random);
		for (int i = 0; (((monitor == null) || (!monitor.isCancelled())) && (i < iterations)); i++) {
			if (monitor != null) {
				monitor.startIteration(i);
			}
			System.out.print('<');
			if ((i + 1) % 50 == 0) {
				System.out.print(i + 1);
			}
			System.out.flush();
			SimulationTask<S> task = new SimulationTask<>(random, model, deadline);
			simManager.run(session1, task);
			//task = new SimulationTask<>(random, model, deadline);
			//simManager.run(session2, task);
			if (monitor != null) {
				monitor.endSimulation(i);
			}
			System.out.print('>');
			if ((i + 1) % 50 == 0) {
				System.out.print("\n");
			}
			System.out.flush();
			this.iterations++;
		}
		rgi.unregister();
		simManager.waitTermination(session1);
		//simManager.waitTermination(session2);
	}

	public synchronized void simulate(int iterations, double deadline) throws InterruptedException {
		simulate(null, iterations, deadline);
	}

	public void simulate(S model, SamplingFunction<S> measure, double deadline) {
		this.sampling_function = measure;
		doSimulate(model, null, deadline);
	}

	public synchronized double simulate(double deadline) {
		RandomGeneratorRegistry rgi = RandomGeneratorRegistry.getInstance();
		rgi.register(random);
		double result = doSimulate(deadline);
		rgi.unregister();
		return result;
	}

	public double reachability(double error, double delta, double deadline, Predicate<? super S> phi,
			Predicate<? super S> psi) throws InterruptedException {
		double n = Math.ceil(Math.log(2 / delta) / (2 * error));
		double count = 0;
		SimulationSession<S> session = simManager.newSession((int) n, null, true);
		for (int i = 0; i < n; i++) {
			SimulationTask<S> simulationRun = new SimulationTask<>(random, model, deadline, phi, psi);
			simManager.run(session, simulationRun);																					// iteration
		}
		simManager.waitTermination(session);
		count = simManager.reach(session);
		return count / n;
	}

	private Boolean sample(double deadline, Predicate<? super S> phi, Predicate<? super S> psi) {
		SimulationTask<S> simulationRun = new SimulationTask<>(random, model, deadline, phi, psi);
		try {
			simulationRun.get();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return simulationRun.reach(); // returns a true for each task that reached predicates
	}

	private double doSimulate(S s, SimulationMonitor monitor, double deadline) { // s and monitor is not used?
		// TODO: Change SimulationMonitor to take into account the new usage protocol.
		/*for (int i = 0; i < tasks; i++) {
			SimulationTask<S> task = new SimulationTask<>(random, model, deadline);
			simManager.addTask(task);
		}
		simManager.runTasks(this.sampling_function);*/
		return 0.0; // return value?
	}

	// private double doSimulate(S s, SimulationMonitor monitor , double deadline) {
	// //TODO: Change SimulationMonitor to take into account the new usage protocol.
	// this.state = model.initialState();
	// double time = 0.0;
	// if (sampling_function != null) {
	// sampling_function.start();
	// sampling_function.sample(time, state);
	// }
	// while (((monitor == null)||(!monitor.isCancelled()))&&(time < deadline)) {
	// double dt = doAStep(time);
	// if (dt <= 0) {
	// if (sampling_function != null) {
	// sampling_function.end(time);
	// }
	// return time;
	// }
	// time += dt;
	//// this.model.timeStep(dt);
	// if (monitor != null && !monitor.isCancelled()) {
	// monitor.update(time);
	// }
	// if (sampling_function != null) {
	// sampling_function.sample(time, state);
	// }
	// }
	//
	// if (sampling_function != null) {
	// sampling_function.end(time);
	// }
	// return time;
	// }
	//

  private double doSimulate(double deadline) {
		return doSimulate(model.initialState(), null, deadline);
	}

	private double doAStep(double now) {
		WeightedStructure<StepFunction<S>> agents = this.model.getActivities(random, state);
		double totalRate = agents.getTotalWeight();
		if (totalRate == 0.0) {
			return 0.0;
		}
		double dt = (1.0 / totalRate) * Math.log(1 / (random.nextDouble()));
		double select = random.nextDouble() * totalRate;
		WeightedElement<StepFunction<S>> wa = agents.select(select);
		if (wa == null) {
			return 0.0;
		}
		this.state = wa.getElement().step(random, now, dt);
		return dt;
	}

	public double nextDouble() {
		return random.nextDouble();
	}

	public int nextInt(int zones) {
		return random.nextInt(zones);
	}

	public LinkedList<SimulationTimeSeries> getTimeSeries() {
		if (sampling_function == null) {
			return null;
		}
		return sampling_function.getSimulationTimeSeries(iterations);
	}

	public Trajectory<S> sampleTrajectory(double deadline) {
		SimulationTask<S> simulationRun = new SimulationTask<>(random, model, deadline);
		try {
			return simulationRun.get();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public Trajectory<S> sampleTrajectory(double deadline, Predicate<? super S> reachPredicate) {
		SimulationTask<S> simulationRun = new SimulationTask<>(random, model, deadline, reachPredicate);
		try {
			return simulationRun.get();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public Trajectory<S> sampleTrajectory(double deadline, Predicate<? super S> transientPredicate,
			Predicate<? super S> reachPredicate) {
		SimulationTask<S> simulationRun = new SimulationTask<>(random, model, deadline, transientPredicate,
				reachPredicate);
		try {
			return simulationRun.get();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
}
