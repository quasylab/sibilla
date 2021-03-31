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
import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Function;

/**
 * @author loreti
 *
 */
public class SamplingCollection<S extends State> implements SamplingFunction<S> {

	private final LinkedList<SamplingFunction<S>> functions;

	public SamplingCollection() {
		this.functions = new LinkedList<SamplingFunction<S>>();
	}

	@SafeVarargs
	public SamplingCollection(SamplingFunction<S>... functions) {
		this();
		for (SamplingFunction<S> f : functions) {
			this.functions.add(f);
		}
	}


	public SamplingCollection(Collection<? extends SamplingFunction<S>> functions) {
		this();
		this.functions.addAll(functions);
	}

	@Override
	public void sample(double time, S context) {
		for (SamplingFunction<S> f : functions) {
			f.sample(time, context);
		}
	}

	@Override
	public void end(double time) {
		for (SamplingFunction<S> f : functions) {
			f.end(time);
		}
	}

	@Override
	public void start() {
		for (SamplingFunction<S> f : functions) {
			f.start();
		}
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
	public LinkedList<SimulationTimeSeries> getSimulationTimeSeries( int replications) {
		LinkedList<SimulationTimeSeries> toReturn = new LinkedList<>();
		for (SamplingFunction<S> f : functions) {
			toReturn.addAll(f.getSimulationTimeSeries( replications ));
		}
		return toReturn;
	}

	public void add(StatisticSampling<S> f) {
		functions.add(f);
	}
}
