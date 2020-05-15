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
/**
 * 
 */
package quasylab.sibilla.core.models.pm;

import org.apache.commons.math3.random.RandomGenerator;

import java.io.Serializable;

/**
 * @author loreti
 *
 */
public class ReactionRule implements PopulationRule, Serializable {
	
	private static final long serialVersionUID = 6508399289508390200L;

	private final Population[] reactants;
	
	private final Population[] products;

	private final RatePopulationFunction rateFunction;
	
	private final String name;

	private Update update;
	
	/**
	 * @param reactants
	 * @param products
	 * @param rateFunction
	 */
	public ReactionRule(String name, Population[] reactants, Population[] products, RatePopulationFunction rateFunction) {
		super();
		this.reactants = reactants;
		this.products = products;
		this.rateFunction = rateFunction;
		this.name = name;
		this.update = new Update(name);
		initDrift();
	}

	private void initDrift() {
		for( int i=0 ; i<reactants.length ; i++ ) {
			this.update.consume(reactants[i].getIndex(), reactants[i].getSize());
		}
		for( int i=0 ; i<products.length ; i++ ) {
			this.update.produce(products[i].getIndex(), products[i].getSize());
		}
	}

	@Override
	public PopulationTransition apply(RandomGenerator r, double now, PopulationState state) {
		if (isEnabled(state)) {
			double rate = rateFunction.apply(now,state);
			if (rate>0) {
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
		for( int i=0 ; i<reactants.length ; i++ ) {
			if (state.getOccupancy(reactants[i].getIndex())<reactants[i].getSize()) {
				return false;
			}
		}
		return true;
	}

}
