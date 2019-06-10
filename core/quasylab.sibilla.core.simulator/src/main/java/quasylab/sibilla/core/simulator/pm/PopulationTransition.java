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

import java.util.function.Function;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * @author loreti
 *
 */
public class PopulationTransition {

	private final Function<RandomGenerator,Update> transitionDriftFunction;
	private final double rate;
	private final String name;

	public PopulationTransition(
			String name,
			double rate,
			Function<RandomGenerator,Update> transitionDriftFunction
			) {
		this.name = name;
		this.transitionDriftFunction = transitionDriftFunction;
		this.rate = rate;
	}

	public double getRate() {
		return rate;
	}

	public Update apply( RandomGenerator r ) {
		return transitionDriftFunction.apply(r);
	}

	public String getName() {
		return name;
	}

}
