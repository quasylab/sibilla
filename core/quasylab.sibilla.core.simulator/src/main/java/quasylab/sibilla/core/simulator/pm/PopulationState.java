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
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * The instances of this class represent a generic population state having species of type <code>S</code>. 
 * 
 * 
 * @author loreti
 *
 */
public class PopulationState implements Serializable {

	private static final long serialVersionUID = -4973919753621170006L;
	/**
	 * Internal representation of the state as continuing vector.
	 */
	private final int[] populationVector;
	private final double population;
	
	public PopulationState( int size ) {
		this(new int[size]);
	}
	
	public PopulationState(int[] state) {
		this.populationVector = state;
		this.population = IntStream.range(0, state.length).map(i -> state[i]).sum();
	}

	public double poluation( ) {
		return population;
	}
	
	public double getOccupancy( int i ) {
		try {
			return populationVector[i];
		} catch (ArrayIndexOutOfBoundsException e) {
			return 0;
		}
	}
	
	public double getOccupancy( int ... idx ) {
		return IntStream.of(idx).boxed().map(i -> (double) populationVector[i]).reduce(0.0, (x,y) -> x+y);
	}
	
	// applies one update function
	public PopulationState apply( Update update ) {
		int[] newState = Arrays.copyOf(populationVector, populationVector.length);
		for (Entry<Integer, Integer> u : update.getUpdate()) {
			int idx = u.getKey();
			int newValue = newState[idx]+u.getValue(); 
			if (newValue>=0) {
				newState[idx] = newValue;
			} else {
				throw new IllegalArgumentException("Population Vector: "+this+" newState: "+Arrays.toString(newState)+" Update: "+update+" idx: "+idx+" newValue: "+newValue+" u: "+u);
			}
		}		
		return new PopulationState(newState);
	}
	
	public double min( Function<Integer, Double> f ) {
		return min( i -> true , f );
	}
	
	public double min( Predicate<Integer> p , Function<Integer, Double> f) {
		double min = Double.MAX_VALUE;
		for( int i=0 ; i<populationVector.length ; i++ ) {
			if ((p.test(i))&&(this.populationVector[i]>0)) {
				min = Math.min(min,f.apply(i));
			}
		}
		return min;
	}
	
	public double max( Function<Integer, Double> f ) {
		return max( i -> true , f );
	}
	
	public double max( Predicate<Integer> p , Function<Integer, Double> f) {
		double max = Double.MIN_VALUE;
		for( int i=0 ; i<populationVector.length ; i++ ) {
			if ((p.test(i))&&(this.populationVector[i]>0)) {
				max = Math.max(max,f.apply(i));
			}
		}
		return max;
	}
	
	public double average( Predicate<Integer> p , Function<Integer, Double> f ) {
		double total = 0.0;
		int counter = 0;
		for( int i=0 ; i<populationVector.length ; i++ ) {
			if (p.test(i)&&(populationVector[i]>0)) {
				counter += populationVector[i];
				total += populationVector[i]*f.apply(i);
			}
		}
		return total/counter;
	}
	
	public double average( Function<Integer, Double> f ) {
		return average( s -> true , f );
	}
	
	public int count( Set<Integer> species ) {
		int result = 0;
		for (Integer i : species) {
			result += this.populationVector[i];
		}
		return result;
	}
	
	public int count( Predicate<Integer> p ) {
		int result = 0;
		for( int i=0 ; i< this.populationVector.length ; i++ ) {
			result += this.populationVector[i];
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Arrays.toString(populationVector);
	}

	public int size() {
		return populationVector.length;
	}

	public double fraction( int i ) {
		return getOccupancy(i)/population;
	}
	

}
