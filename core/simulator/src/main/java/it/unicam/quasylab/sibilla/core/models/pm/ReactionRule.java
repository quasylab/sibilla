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
/**
 * 
 */
package it.unicam.quasylab.sibilla.core.models.pm;

import org.apache.commons.math3.random.RandomGenerator;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @author loreti
 *
 */
public class ReactionRule implements PopulationRule, Serializable {
	
	private static final long serialVersionUID = 6508399289508390200L;

	private final Predicate<PopulationState> guard;

	private final Map<Integer, Integer> reactants;
	
	private final RatePopulationFunction rateFunction;
	
	private final String name;

	private final Update update;
	
	/**
	 * @param reactants
	 * @param products
	 * @param rateFunction
	 */
	public ReactionRule(String name, Population[] reactants, Population[] products, RatePopulationFunction rateFunction) {
		this(name,null,reactants,products,rateFunction);
	}

	public ReactionRule(String name, Predicate<PopulationState> guard, Population[] reactants, Population[] products, RatePopulationFunction rateFunction) {
		super();
		this.guard = guard;
		this.reactants = new HashMap<>();
		this.rateFunction = rateFunction;
		this.name = name;
		this.update = new Update(name);
		initReactants(reactants);
		initDrift(reactants, products);
	}

	private void initReactants(Population[] reactants) {
		for (Population p: reactants) {
			int value = this.reactants.getOrDefault(p.getIndex(), 0);
			this.reactants.put(p.getIndex(), value+p.getSize());
		}
	}


	private void initDrift(Population[] reactants, Population[] products) {
		for (Population reactant : reactants) {
			this.update.consume(reactant.getIndex(), reactant.getSize());
		}
		for (Population product : products) {
			this.update.produce(product.getIndex(), product.getSize());
		}
	}

	@Override
	public PopulationTransition apply(RandomGenerator r, double now, PopulationState state) {
		if (isEnabled(state)) {
			double rate = rateFunction.apply(now,state);
			if (rate>0&&Double.isFinite(rate)) {
				return new PopulationTransition(
						name, 
						rate, 
						(rg -> update)
				);
			}
		}
		return null;
	}
	
	private boolean isEnabled(PopulationState state) {
		if ((guard != null)&&(!guard.test(state))) {
			return false;
		}
		for (Map.Entry<Integer, Integer> e: this.reactants.entrySet()) {
			if (state.getOccupancy(e.getKey())<e.getValue()) {
				return false;
			}
		}
		return true;
	}

}
