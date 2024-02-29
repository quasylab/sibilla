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

package it.unicam.quasylab.sibilla.core.simulator;

import org.apache.commons.math3.random.AbstractRandomGenerator;

import java.io.Serializable;
import java.util.Random;
import java.util.SplittableRandom;

/**
 * Default random generator.
 */
public class DefaultRandomGenerator extends AbstractRandomGenerator implements Serializable{

	private static final long serialVersionUID = -8354414629214279876L;
	private SplittableRandom random = new SplittableRandom();

	public DefaultRandomGenerator(long seed) {
		super();
		random = new SplittableRandom(seed);
	}

	public DefaultRandomGenerator() {
		super();
	}

	@Override
	public void setSeed(long seed) {
		clear();
		random = new SplittableRandom(seed);
	}

	@Override
	public double nextDouble() {
		return random.nextDouble();
	}


}
