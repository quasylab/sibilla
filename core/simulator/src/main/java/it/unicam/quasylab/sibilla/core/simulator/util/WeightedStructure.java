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

import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collector;

/**
 * @author loreti
 *
 */
public interface WeightedStructure<S> {

	double getTotalWeight();

	WeightedElement<S> select(double w);

	WeightedStructure<S> add(double w, S s);

	WeightedStructure<S> add(WeightedStructure<S> s);

	List<WeightedElement<S>> getAll();

	static <S> Collector<WeightedElement<S>, ?, WeightedStructure<S>> collector() {
		return Collector.of(WeightedLinkedList::new, (we, wlist) -> {wlist.add(we);}, WeightedStructure::add);
	}

}
