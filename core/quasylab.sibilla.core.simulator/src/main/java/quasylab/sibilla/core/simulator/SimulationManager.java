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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * A <code>SimulationManager</code> has the responsibility to coordinate 
 * simulation activities. These are arranged in <it>sessions</it> ({@link SimulationSessionI}).
 * 
 * @author Matteo Belenchia, Michele Loreti
 *
 */
public abstract class SimulationManager {
	
	/**
	 * A map contining a reference to all active sessions.
	 */
	private final Map<Integer,SimulationSessionI> sessions;
	
	/**
	 * Session counter.
	 */
	private int sessionCounter = 0;
	
	/**
	 * Creates a new simulation manager.
	 */
	public SimulationManager() {
		this.sessions = new HashMap<>();
	}
	
	/**
	 * 
	 * Initializes a new simulation session. Each session is associated with a 
	 * type <code>S</code> indicating states of simulated model. 
	 * 
	 * @param <S> type of simulated model states
	 * @param consumer trace consumer associated with the session
	 * @return a new session
	 * 
	 */
    public synchronized <S> SimulationSession<S> newSession(RandomGenerator random, Consumer<Trajectory<S>> consumer) {
    	SimulationSession<S> session = new SimulationSession<S>( sessionCounter++, random, consumer );
    	this.sessions.put(session.sessionId, session);
    	return session;
    }
    
    /**
     * Executes a <code>SimulationUnit</code> by using the specific random generator.
     * The trajectory resulting from the simulation is passed to the argument <code>conumer</code>.
     * 
     * @param <S> type of simulated states
     * @param random random generator used in the simulation
     * @param consumer consumer used to handle the trajectory resulting from the simulation
     * @param simulation simulation unit
     */
    protected abstract <S> void runSimulation(RandomGenerator random, Consumer<Trajectory<S>> consumer, SimulationUnit<S> simulation);
    
    /**
     * 
     * @author loreti
     *
     * @param <S>
     */
    public class SimulationSession<S> implements SimulationSessionI {
    	
    	private int sessionId;
        private int runningTasks = 0;
        private Consumer<Trajectory<S>> trajectoryConsumer;
        private RandomGenerator random;
        private boolean running;
        private LinkedList<Long> executionTime;
        
        private SimulationSession(int sessionId, RandomGenerator random, Consumer<Trajectory<S>> trajectoryConsumer){
            this.sessionId = sessionId;
            this.trajectoryConsumer = trajectoryConsumer;
            this.random = random;
            this.running = true;
        }

    	@Override
    	public int getSessionId() {
    		return sessionId;
    	}

    	@Override
    	public synchronized boolean isRunning() {
    		return running;
    	}
    	
    	public synchronized void setRunning(boolean flag) {
    		this.running = false;
    	}

    	@Override
    	public void shutdown() {
    		if (isRunning()) {
    			
    		}
    		// TODO Auto-generated method stub
    		
    	}
    	
    	public synchronized void simulate( SimulationUnit<S> unit ) {
    		runningTasks++;
    		runSimulation( random, this::handleTrajectory, unit );
    	}
    	
    	private synchronized void handleTrajectory( Trajectory<S> trj ) {
    		this.executionTime.add(trj.getGenerationTime());
    		trajectoryConsumer.accept(trj);
    		runningTasks--;
    		notifyAll();
    	}

		@Override
		public synchronized void join() throws InterruptedException {
			while (this.runningTasks>0) {
				wait();
			}
		}

		@Override
		public int computedTrajectories() {
			return executionTime.size();
		}

		@Override
		public double averageExecutionTime() {
			return executionTime.stream().collect(Collectors.averagingDouble(l -> l.doubleValue()));
		}

    }

}
