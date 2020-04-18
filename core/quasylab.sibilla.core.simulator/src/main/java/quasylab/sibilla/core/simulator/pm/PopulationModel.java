/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 *  Copyright (C) 2020.
 *
 *  See the NOTICE file distributed with this work for additional information
 *  regarding copyright ownership.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package quasylab.sibilla.core.simulator.pm;

import org.apache.commons.math3.random.RandomGenerator;
import quasylab.sibilla.core.simulator.Model;
import quasylab.sibilla.core.simulator.StepFunction;
import quasylab.sibilla.core.simulator.util.WeightedElement;
import quasylab.sibilla.core.simulator.util.WeightedLinkedList;
import quasylab.sibilla.core.simulator.util.WeightedStructure;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;

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
public class PopulationModel implements Model<PopulationState>, Serializable {

	private static final long serialVersionUID = 6871037109869821108L;

	private LinkedList<PopulationRule> rules;

	private double time;
	
	private HashMap<String,PopulationState> states;
	
	private HashMap<String,Function<? super PopulationState,Double>> measures;
	
	public PopulationModel( ) {
		this.rules = new LinkedList<PopulationRule>();
		this.states = new HashMap<>();
		this.measures = new HashMap<>();
	}
	
	@Override
	public WeightedStructure<StepFunction<PopulationState>> getActivities(RandomGenerator r , PopulationState state ) {
		WeightedLinkedList<StepFunction<PopulationState>> activities = 
				new WeightedLinkedList<>();
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

	public double getTime() {
		return time;
	}

	public static Map<String,Integer> createPopulation( String ... species ) {
		HashMap<String, Integer> map = new HashMap<>();
		IntStream.range(0, species.length).forEach(i -> map.put(species[i],i));
		return map;
	}

	public static PopulationState vectorOf( int ... species ) {
		return new PopulationState(species);
	}

//	@Override
//	public PopulationState getState(String label) {
//		return states.get(label);
//	}
//
//	@Override
//	public Set<String> getStateLabels() {
//		return states.keySet();
//	}
//
//	@Override
//	public PopulationState copy(PopulationState state) {
//		return state.copy();
//	}
//	
	public void addRule( PopulationRule rule ) {
		this.rules.add(rule);
	}

//	@Override
//	public Function<? super PopulationState, Double> getMeasure(String label) {
//		return null;
//	}
//
//	@Override
//	public Set<String> getMeasureLabels() {
//		return measures.keySet();
//	}

	public void addState(String label, PopulationState state) {
		this.states.put(label, state);
	}

	public void addRules(Collection<PopulationRule> rules) {
		this.rules.addAll(rules);
	}
	
}
