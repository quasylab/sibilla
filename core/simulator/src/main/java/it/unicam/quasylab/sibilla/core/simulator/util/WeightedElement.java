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
package it.unicam.quasylab.sibilla.core.simulator.util;

import java.util.LinkedList;
import java.util.List;

/**
 * @author loreti
 *
 */
public class WeightedElement<S> implements WeightedStructure<S> {

	private double w;
	private S s;

	public WeightedElement(double w, S s) {
		this.w = w;
		this.s = s;
	}

	public double getWeight() {
		return w;
	}

	public S getElement() {
		return s;
	}

	public WeightedElement<S> residual(double w) {
		return new WeightedElement<S>(this.w - w, s);
	}

	@Override
	public double getTotalWeight() {
		return w;
	}

	@Override
	public WeightedElement<S> select(double w) {
		if (w <= this.w) {
			return this;
		}
		return null;
	}

	@Override
	public WeightedStructure<S> add(double w, S s) {
		if (w == 0.0) {
			return this;
		} 
		return new ComposedWeightedStructure<>(this,new WeightedElement<S>(w, s));
	}

	@Override
	public WeightedStructure<S> add(WeightedStructure<S> s) {
		return new ComposedWeightedStructure<S>(this, s);
	}

	@Override
	public String toString() {
		return s+":"+w;
	}

	@Override
	public List<WeightedElement<S>> getAll() {
		LinkedList<WeightedElement<S>> list = new LinkedList<>();
		list.add(this);
		return list;
	}


}
