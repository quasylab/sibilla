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
/**
 * 
 */
package quasylab.sibilla.core.simulator.pm;

import java.io.Serializable;
import java.util.function.Function;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * @author loreti
 *
 */
public class ReactionRule implements PopulationRule, Serializable {
	
	private static final long serialVersionUID = 6508399289508390200L;

	private final Specie[] reactants;
	
	private final Specie[] products;

	private final Function<PopulationState,Double> rateFunction;
	
	private final String name;

	private Update update;
	
	/**
	 * @param reactants
	 * @param products
	 * @param rate
	 */
	public ReactionRule(String name, Specie[] reactants, Specie[] products, Function<PopulationState, Double> rateFunction) {
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
			this.update.consume(reactants[i].index, reactants[i].size);
		}
		for( int i=0 ; i<products.length ; i++ ) {
			this.update.produce(products[i].index, products[i].size);
		}
	}

	@Override
	public PopulationTransition apply(RandomGenerator r, PopulationState state) {
		if (isEnabled(state)) {
			double rate = rateFunction.apply(state);
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
			if (state.getOccupancy(reactants[i].index)<reactants[i].size) {
				return false;
			}
		}
		return true;
	}

	public static class Specie implements Serializable{
		
		private static final long serialVersionUID = 5501961970972786801L;

		private int index;

		private int size;
		
		/**
		 * @param index
		 * @param size
		 */
		public Specie(int index, int size) {
			super();
			this.index = index;
			this.size = size;
		}

		public Specie(int s) {
			this(s,1);
		}

	}
	
}
