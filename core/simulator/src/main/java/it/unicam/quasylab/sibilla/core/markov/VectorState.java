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

package it.unicam.quasylab.sibilla.core.markov;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

/**
 * @author loreti
 *
 */
public class VectorState<S> {
	
	private final S[] state;
	private final IntFunction<S[]> generator;
	
	public VectorState( IntFunction<S[]> generator , int size , IntFunction<S> init ) {
		this( generator, IntStream.range(0, size).mapToObj(init).toArray(generator) );
	}

	private VectorState( IntFunction<S[]> generator, S[] state ) {
		this.state = state;
		this.generator = generator;
	}
	
	public int size( ) {
		return state.length;
	}
	
	public VectorState<S> apply( BiFunction<Integer, S, S> update ) {
		S[] newState = generator.apply(state.length);
		IntStream.range(0, state.length).forEach(i -> newState[i] = update.apply(i, state[i]));
		return new VectorState<>(generator, newState);
	}
	
	public S get( int i ) {
		return state[i];
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.deepHashCode(state);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VectorState<?> other = (VectorState<?>) obj;
		return Arrays.deepEquals(state, other.state);
	}

	@Override
	public String toString() {
		return Arrays.deepToString(state);
	}
	
	
	
}
