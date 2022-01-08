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

package it.unicam.quasylab.sibilla.core.simulator.sampling;

import it.unicam.quasylab.sibilla.core.models.State;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author loreti
 *
 */
public class SamplingCollection<S extends State> implements SamplingFunction<S> {

	private final LinkedList<SamplingFunction<S>> functions;

	@SafeVarargs
	public SamplingCollection(SamplingFunction<S>... functions) {
		this(Arrays.stream(functions).collect(Collectors.toList()));
	}


	public SamplingCollection(Collection<? extends SamplingFunction<S>> functions) {
		this.functions = new LinkedList<>(functions);
	}


	@Override
	public SamplingHandler<S> getSamplingHandler() {
		return new CompositeSamplingHandler<>(this.functions.stream().map(SamplingFunction::getSamplingHandler).collect(Collectors.toList()));
	}

	@Override
	public void printTimeSeries(Function<String, String> nameFunction, char separator, double significance) throws FileNotFoundException {
		for (SamplingFunction<S> sf: functions) {
			sf.printTimeSeries(nameFunction,separator,significance);
		}
	}

	public int size(){
		return this.functions.size();
	}
	
	public SamplingFunction<S> get(int i){
		return this.functions.get(i);
	}

	@Override
	public Map<String, double[][]> getSimulationTimeSeries() {
		TreeMap<String,double[][]> toReturn = new TreeMap<>();
		for (SamplingFunction<S> f : functions) {
			toReturn.putAll(f.getSimulationTimeSeries());
		}
		return toReturn;
	}

	public void add(SamplingFunction<S> f) {
		functions.add(f);
	}
}
