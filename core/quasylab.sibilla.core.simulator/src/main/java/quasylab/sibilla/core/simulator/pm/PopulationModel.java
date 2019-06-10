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
package quasylab.sibilla.core.simulator.pm;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.IntStream;

import org.apache.commons.math3.random.RandomGenerator;

import quasylab.sibilla.core.simulator.Model;
import quasylab.sibilla.core.simulator.StepFunction;
import quasylab.sibilla.core.simulator.util.WeightedElement;
import quasylab.sibilla.core.simulator.util.WeightedLinkedList;
import quasylab.sibilla.core.simulator.util.WeightedStructure;

/**
 * 
 * This class implements a population model. This class is parametrised with
 * respect to types <code>S</code> and and <code>T</code>. The former is the
 * data type used to identify population species in the population vector.
 * Parameter <code>T</code> identifies environment
 * 
 * @author loreti
 *
 */
public class PopulationModel implements Model<PopulationState> {

	private PopulationRule[] rules;

	private double time;
	
	private PopulationState initialState;

	
	public PopulationModel( PopulationState currentState, PopulationRule ... rules ) {
		this.rules = rules;
		this.initialState = currentState;
	}
	
	public PopulationModel(PopulationState currentState, LinkedList<PopulationRule> buildRules) {
		this(currentState,buildRules.toArray(new PopulationRule[buildRules.size()]));
	}

	public PopulationState getCurrentState() {
		return initialState;
	}
	
	public double getOccupancy( int idx ) {
		return initialState.getOccupancy(idx);
	}

	protected void setState(PopulationState newState) {
		this.initialState = newState;
	}

	@Override
	public WeightedStructure<StepFunction<PopulationState>> getActivities(RandomGenerator r , PopulationState state ) {
		WeightedLinkedList<StepFunction<PopulationState>> activities = new WeightedLinkedList<>();
		for (PopulationRule rule : rules) {
			PopulationTransition tra = rule.apply(r, state);
			if (tra != null) {
				activities.add(
					new WeightedElement<StepFunction<PopulationState>>(
						tra.getRate(), 
						(rnd,now,dt) -> state.apply(tra.apply(rnd))
					)						
				);
			}
		}
		return activities;
	}

	protected void apply(Update drift) {
		setState(getCurrentState().apply(drift));
	}

	public double getTime() {
		return time;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return initialState.toString();
	}

	@Override
	public PopulationState initialState() {
		return this.initialState;
	}
	
	public static Map<String,Integer> createPopulation( String ... species ) {
		HashMap<String, Integer> map = new HashMap<>();
		IntStream.range(0, species.length).forEach(i -> map.put(species[i],i));
		return map;
	}

	public static PopulationState vectorOf( int ... species ) {
		return new PopulationState(species);
	}
}
