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
public class WeightedLinkedList<S> implements WeightedStructure<S> {

	private double totalWeight;

	private final LinkedList<WeightedElement<S>> list;

	public WeightedLinkedList() {
		this.totalWeight = 0.0;
		this.list = new LinkedList<WeightedElement<S>>();
	}

	@Override
	public double getTotalWeight() {
		return this.totalWeight;
	}

	@Override
	public WeightedElement<S> select(double w) {
		double total = 0.0;
		for (WeightedElement<S> weightedElement : list) {
			total += weightedElement.getWeight();
			if (w <= total) {
				return weightedElement.residual(w);
			}
		}
		return null;
	}

	@Override
	public WeightedStructure<S> add(double w, S s) {
		totalWeight += w;
		list.add(new WeightedElement<S>(w, s));
		return this;
	}

	public WeightedStructure<S> add(WeightedElement<S> we) {
		totalWeight += we.getWeight();
		list.add(we);
		return this;
	}

	@Override
	public WeightedStructure<S> add(WeightedStructure<S> s) {
		if (s.getTotalWeight() == 0.0) {
			return this;
		}
		return new ComposedWeightedStructure<S>(this, s);
	}

	@Override
	public List<WeightedElement<S>> getAll() {
		return list;
	}

}
