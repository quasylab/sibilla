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
package it.unicam.quasylab.sibilla.core.past;

import it.unicam.quasylab.sibilla.core.simulator.DefaultRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.function.Function;

/**
 * @author loreti
 *
 */
public class RandomGeneratorRegistry {
	
	private static RandomGeneratorRegistry instance;
	
	private RandomGenerator rg;
	
	private HashMap<Thread, RandomGenerator> registry;
	
	private RandomGeneratorRegistry() {
		this.registry = new HashMap<>();
		this.rg = new DefaultRandomGenerator();
	}
	
	public synchronized static RandomGeneratorRegistry getInstance() {
		if (instance == null) {
			instance = new RandomGeneratorRegistry();
		}
		return instance;
	}
	
	public synchronized void register( RandomGenerator rg ) {
		registry.put(Thread.currentThread(), rg);
	}
	
	public synchronized void unregister() {
		registry.remove(Thread.currentThread());
	}
	
	public synchronized RandomGenerator get( ) {
		return retrieve( Thread.currentThread() );
	}

	private RandomGenerator retrieve(Thread currentThread) {
		RandomGenerator rg = registry.get(currentThread);
		if (rg == null) {
			rg = this.rg;			
		}
		return rg;
	}

	@SafeVarargs 
	public static <T> T uniform( T ... data ) {
		RandomGenerator rg = getInstance().get();
		return data[rg.nextInt(data.length)];
	}
	
	public static <T> T uniformSelect( Collection<T> collection ) {
		if (collection.size()==0) {
			System.out.println("IS EMPTY!!!");
			return null;
		}
		int idx = 0;
		if (collection.size()>1) {
			RandomGenerator rg = getInstance().get();
			idx = rg.nextInt(collection.size());
		}
		int counter = 0;
		T last = null;
		for (T t : collection) {
			last = t;
			if (counter == idx) {
				return t;
			} else {
				counter++;
			}
		}
		return last;
	}

	public static <T> T select( Collection<T> collection , Function<T,Double> weight ) {
		if (collection.size()==0) {
			System.out.println("IS EMPTY!!!");
			return null;
		}
		double[] weightArray = new double[collection.size()];
		double total = 0.0;
		ArrayList<T> elements = new ArrayList<>();
		int counter = 0;
		for (T e : collection) {
			Double w = weight.apply(e);
			if (w == null) {
				w = 0.0;
			}
			total += w;
			weightArray[counter] = total;
			elements.add(e);
			counter++;

		}		
		return select( elements , weightArray , total );
	}
	
	public static <T> T weightedSelect(T[] data, double[] weights) {
		//double total = DoubleStream.of(weights).sum();
		//Arrays.parallelPrefix(weights, Double::sum);
		//return select(new ArrayList<T>(Arrays.asList(data)), weights, total);
		double total = 0;
		double[] weightsArray = new double[weights.length];
		for (int i = 0; i < weights.length; i++) {
			total += weights[i];
			weightsArray[i] = total;
		}
		return select(new ArrayList<T>(Arrays.asList(data)), weightsArray, total);
	}

	private static <T> T select(ArrayList<T> elements, double[] weightArray, double total) {
		if (total == 0) {
			return null;
		}
		double val = total*rnd();
		for (int i=0 ; i<weightArray.length; i++ ) {
			if (val<weightArray[i]) {
				return elements.get(i);
			}
		}
		return null;
	}

	public static double rnd() {
		RandomGenerator rg = getInstance().get();
		return rg.nextDouble();
	}
	
	public static double normal(double mean, double sd) {
		RandomGenerator rg = getInstance().get();
		return rg.nextGaussian()*sd+mean;
	}
}
