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
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package quasylab.sibilla.core.simulator;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.models.StatePredicate;
import quasylab.sibilla.core.models.State;
import quasylab.sibilla.core.simulator.sampling.SamplePredicate;

import java.io.Serializable;

/**
 * @author loreti
 *
 */
public class SimulationUnit<S extends State> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2809995821274223033L;

	private Model<S> model;
	
	private S state;
	
	private SamplePredicate<? super S> stoppingPredicate;
	
	private StatePredicate<? super S> reachPredicate;

	public SimulationUnit(Model<S> model, S state, SamplePredicate<? super S> stoppingPredicate) {
		this(model,state,stoppingPredicate,StatePredicate.TRUE);
	}

	public SimulationUnit(Model<S> model, S state, SamplePredicate<? super S> stoppingPredicate, StatePredicate<? super S> reachPredicate) {
		this.model = model;
		this.state = state;
		this.stoppingPredicate = stoppingPredicate;
		this.reachPredicate = reachPredicate;
	}

	public Model<S> getModel() {
		return model;
	}

	public S getState() {
		return state;
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
	
	
	
}
