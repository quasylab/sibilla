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

import java.util.LongSummaryStatistics;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;
import java.util.logging.Logger;

import org.apache.commons.math3.random.RandomGenerator;
import quasylab.sibilla.core.simulator.pm.State;

/**
 * @author belenchia
 *
 */
public class ThreadSimulationManager<S extends State> extends SimulationManager<S> {

	private static final Logger LOGGER = Logger.getLogger(ThreadSimulationManager.class.getName());
	private ExecutorService executor;

	// private final int concurrentTasks;
	// private int runningTasks = 0;
	// private int sessionCounter = 0;
	// private LinkedList<SimulationTask<S>> waitingTasks = new LinkedList<>();

	public ThreadSimulationManager(RandomGenerator random, Consumer<Trajectory<S>> consumer) {
		this(Executors.newCachedThreadPool(), random, consumer);
	}

	public ThreadSimulationManager(int concurrentTasks, RandomGenerator random, Consumer<Trajectory<S>> consumer) {
		this(Executors.newFixedThreadPool(concurrentTasks), random, consumer);
	}

	public static final SimulationManagerFactory getFixedThreadSimulationManagerFactory(int n) {
		return new SimulationManagerFactory() {

			@Override
			public <S extends State> SimulationManager<S> getSimulationManager(RandomGenerator random,
					Consumer<Trajectory<S>> consumer) {
				return new ThreadSimulationManager<>(n, random, consumer);
			}
		};

	}

	public static final SimulationManagerFactory getCachedThreadSimulationManagerFactory() {
		return new SimulationManagerFactory() {

			@Override
			public <S extends State> SimulationManager<S> getSimulationManager(RandomGenerator random,
					Consumer<Trajectory<S>> consumer) {
				return new ThreadSimulationManager<>(random, consumer);
			}
		};

	}

	public static final SimulationManagerFactory getThreadsimulationManagerFactory(ExecutorService executor) {
		return new SimulationManagerFactory() {

			@Override
			public <S extends State> SimulationManager<S> getSimulationManager(RandomGenerator random,
					Consumer<Trajectory<S>> consumer) {
				return new ThreadSimulationManager<>(executor, random, consumer);
			}
		};

	}

	// private void doSample(SamplingFunction<S> sampling_function, Trajectory<S>
	// trajectory) {
	// if (sampling_function != null) {
	// trajectory.sample(sampling_function);
	// }
	// }

	// waits for all tasks to end, then prints timing information to file
	// private void terminate() {
	// try {
	// printTimingInformation(System.out);
	// printTimingInformation(new PrintStream(new
	// FileOutputStream("thread_data.data", true)));
	// } catch (FileNotFoundException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// }

	public ThreadSimulationManager(ExecutorService executor, RandomGenerator random, Consumer<Trajectory<S>> consumer) {
		super(random, consumer);
		this.executor = executor;
		this.start();
	}

	// // samples the trajectory, updates counters, then runs next task.
	// // if no new tasks to run, shutdowns the executor
	// private synchronized <S> void manageTask(SimulationSession<S> session,
	// Trajectory<S> trajectory) {
	// session.getSamplingFunction().accept(trajectory);
	//// doSample(session.getSamplingFunction(), trajectory);
	// runningTasks--;
	// session.taskCompleted();
	// SimulationTask<S> nextTask = waitingTasks.poll();
	// if (nextTask != null) {
	// run(session, nextTask);
	// } else if (isCompleted(session)){
	// this.notify();
	// }
	// }

	// private synchronized boolean isCompleted(SimulationSession<S> session) {
	// return (runningTasks+session.getExpectedTasks()==0);
	// }

	@Override
	protected void start() {

		Thread t = new Thread(this::handleTasks);
		t.start();

	}

	private void handleTasks() {
		try {
			while (isRunning() || hasTasks()) {
				SimulationTask<S> nextTask = nextTask(true);
				if (nextTask != null) {
					CompletableFuture.supplyAsync(nextTask, executor).thenAccept(this::handleTrajectory);
				}
			}
		} catch (InterruptedException e) {
			LOGGER.severe(e.getMessage());
		}

	}

	@Override
	public synchronized void join() throws InterruptedException {
		while (getRunningTasks() > 0 || hasTasks()) {
			wait();
		}
		LongSummaryStatistics statistics = getExecutionTimes().stream().mapToLong(Long::valueOf).summaryStatistics();
		String data = ((ThreadPoolExecutor) executor).getMaximumPoolSize() + ";"
				+ ((ThreadPoolExecutor) executor).getPoolSize() + ";" + statistics.getAverage() + ";"
				+ statistics.getMax() + ";" + statistics.getMin();
		propertyChange("end", data);

		/*
		 * time testing stuff executor.shutdown(); try { PrintStream out = new
		 * PrintStream(new FileOutputStream("thread_data.data", true));
		 * out.println(data); out.close(); } catch (FileNotFoundException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 *//////////////////////////
	}

	// //waiting until executor is shutdown
	// @Override
	// public synchronized void waitTermination(SimulationSession<S> session) throws
	// InterruptedException {
	// while (!isCompleted(session)) {
	// this.wait();
	// }
	// terminate();
	// //executor.shutdown(); // only when recording time
	// }
	//
	// private void printTimingInformation(PrintStream out){
	// LongSummaryStatistics statistics = tasks.stream().map(x ->
	// x.getElapsedTime()).mapToLong(Long::valueOf).summaryStatistics();
	// out.println(concurrentTasks +";"+((ThreadPoolExecutor)
	// executor).getPoolSize()+";" + statistics.getAverage() + ";" +
	// statistics.getMax() +";" + statistics.getMin());
	// }
	//
	// @Override
	// public long reach() {
	// return tasks.stream().filter(task -> task.reach() == true).count();
	// }
	//
	// @Override
	// public <S> void waitTermination(SimulationSession<S> session) throws
	// InterruptedException {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public <S> void waitTermination(SimulationSession<S> session) throws
	// InterruptedException {
	// // TODO Auto-generated method stub
	//
	// }

}