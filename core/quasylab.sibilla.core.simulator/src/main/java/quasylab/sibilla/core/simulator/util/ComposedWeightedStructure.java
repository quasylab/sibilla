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
/**
 * 
 */
package quasylab.sibilla.core.simulator.util;

import java.util.LinkedList;
import java.util.List;

/**
 * @author loreti
 *
 */
public class ComposedWeightedStructure<S> implements WeightedStructure<S> {

	@Override
	public String toString() {
		if (total_weight == 0) {
			return "0";
		}
		return left+" - "+right;
	}

	private double total_weight;
	private WeightedStructure<S> left;
	private WeightedStructure<S> right;

	public ComposedWeightedStructure() {
		this(null, null);
	}

	public ComposedWeightedStructure(WeightedStructure<S> left,
			WeightedStructure<S> right) {
		total_weight = 0.0;
		this.left = left;
		this.right = right;
		if (this.left != null) {
			total_weight += this.left.getTotalWeight();
		}
		if (this.right != null) {
			total_weight += this.right.getTotalWeight();
		}
	}

	@Override
	public double getTotalWeight() {
		return total_weight;
	}

	@Override
	public WeightedElement<S> select(double w) {
		if (total_weight == 0.0) {
			return null;
		}
		if ((left != null) && (w < left.getTotalWeight())) {
			return left.select(w);
		}
		if (right != null) {
			return right.select(w - left.getTotalWeight());
		}
		return null;
	}

	@Override
	public WeightedStructure<S> add(double w, S s) {
		return add(new WeightedElement<S>(w, s));
	}

	@Override
	public WeightedStructure<S> add(WeightedStructure<S> s) {
		if (s == null) {
			return this;
		}
		double increment = s.getTotalWeight();
		if (left == null) {
			// right is null
			// this.left = s;
			// this.total_weight += increment;
			// return this;
			return s;
		}
		if (right == null) {
			this.right = s;
			this.total_weight += increment;
			return this;
		}
		if ((increment >= left.getTotalWeight())
				&& (increment >= right.getTotalWeight())) {
			return new ComposedWeightedStructure<S>(this, s);
		}
		if (this.left.getTotalWeight() < this.right.getTotalWeight()) {
			this.left = this.left.add(s);
		} else {
			this.right = this.right.add(s);
		}
		this.total_weight += increment;
		return this;
	}

	@Override
	public List<WeightedElement<S>> getAll() {
		LinkedList<WeightedElement<S>> toReturn = new LinkedList<>();
		if (left != null) {
			toReturn.addAll(left.getAll());
		}
		if (right != null) {
			toReturn.addAll(right.getAll());
		}
		return toReturn;
	}

}
