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

package quasylab.sibilla.core.simulator.sampling;

import quasylab.sibilla.core.past.State;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * @author loreti
 *
 */
public interface SamplingFunction<S extends State> extends Serializable {

	void sample(double time, S context);

	void end(double time);

	void start();

	default void printTimeSeries(Function<String, String> nameFunction) throws FileNotFoundException {
		printTimeSeries(nameFunction,';');
	}

	default void printTimeSeries(Function<String, String> nameFunction, char separator) throws FileNotFoundException {
		printTimeSeries(nameFunction,separator,0.05);
	}

	void printTimeSeries(Function<String, String> nameFunction, char separator, double significance) throws FileNotFoundException;

	default void printTimeSeries(String dir, String prefix, String postfix, char separator, double significance) throws FileNotFoundException {
		printTimeSeries(n->dir+"/"+prefix+n+postfix,separator,significance);
	}

	default void printTimeSeries(String dir, String prefix, String postfix, char separator) throws FileNotFoundException {
		printTimeSeries(dir,prefix,postfix,separator,0.05);
	}

	default void printTimeSeries(String dir, String prefix, String postfix) throws FileNotFoundException {
		printTimeSeries(dir,prefix,postfix,';',0.05);
	}

	List<SimulationTimeSeries> getSimulationTimeSeries(int replications);

}
