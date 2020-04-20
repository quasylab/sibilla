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

import quasylab.sibilla.core.models.MarkovProcess;
import quasylab.sibilla.core.past.State;
import quasylab.sibilla.core.simulator.sampling.SamplePredicate;

import java.io.Serializable;
import java.util.function.Predicate;

/**
 * @author loreti
 *
 */
public class SimulationUnit<S extends State> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2809995821274223033L;

	private MarkovProcess<S> model;
	
	private S state;
	
	private SamplePredicate<? super S> stoppingPredicate;
	
	private Predicate<? super S> reachPredicate;
	
	public SimulationUnit(MarkovProcess<S> model, S state, SamplePredicate<? super S> stoppingPredicate, Predicate<? super S> reachPredicate) {
		this.model = model;
		this.state = state;
		this.stoppingPredicate = stoppingPredicate;
		this.reachPredicate = reachPredicate;
	}

	public MarkovProcess<S> getModel() {
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
	public Predicate<? super S> getReachPredicate() {
		return reachPredicate;
	}
	
	
	
}
