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

package quasylab.sibilla.core.simulator.sampling;

import quasylab.sibilla.core.past.State;

import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.function.Function;

/**
 * @author loreti
 *
 */
public class SamplingLog<S extends State> implements SamplingFunction<S> {

	private double dt;
	private double last_time = 0.0;

	public SamplingLog(double dt) {
		this.dt = dt;
	}

	@Override
	public void sample(double time, S context) {
		while (time >= last_time) {
			System.out.println(last_time + ": " + context.toString());
			this.last_time += dt;
		}
	}

	@Override
	public void end(double time) {
		System.out.println(time + ": END");
	}

	@Override
	public void start() {
		this.last_time = 0.0;
	}

	@Override
	public void printTimeSeries(Function<String, String> nameFunction, char separator, double significance) {
	}

	@Override
	public LinkedList<SimulationTimeSeries> getSimulationTimeSeries( int replications) {
		return new LinkedList<>();
	}

}
