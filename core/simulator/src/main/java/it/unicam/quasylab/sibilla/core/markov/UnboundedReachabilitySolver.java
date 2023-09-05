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

import it.unicam.quasylab.sibilla.core.util.datastructures.Pair;
import org.apache.commons.math3.linear.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author loreti
 * @param <S>
 *
 */
public class UnboundedReachabilitySolver<S> {

	private Predicate<S> condition;
	private Predicate<S> goal;
	private MarkovChain<S> chain;
	private Set<S> yesNodes;
	private Set<S> computingNodes;
	private Map<S,Integer> index;
	
	public UnboundedReachabilitySolver( 
			MarkovChain<S> chain,
			Predicate<S> condition,
			Predicate<S> goal) {
		this.chain = chain;
		this.condition = condition;
		this.goal = goal;
	}
	
	public UnboundedReachabilitySolver( 
			MarkovChain<S> chain,
			Predicate<S> goal) {
		this(chain,s -> true,goal);
	}
	

	private RealVector buildRealVector() {
		return MarkovChain.generateVector(ArrayRealVector::new, index, s -> 
		chain.probabilityMatrixRow(s).entrySet().stream()
		.filter(e2 -> yesNodes.contains(e2.getKey()))
		.collect(Collectors.summingDouble(e -> e.getValue())));
	}

	private RealMatrix buidProbabilityMatrix(  ) {
		return MarkovChain.generateMatrix(i -> MatrixUtils.createRealIdentityMatrix(i), s -> 
			chain.probabilityMatrixRow(s).entrySet().stream()
				.filter(p -> computingNodes.contains(p.getKey()))
				, index);
	}
	
	public Map<S,Double> compute() {
		computeReachabilitySets();
		computeStateIndex();
		RealMatrix m = buidProbabilityMatrix( );
		DecompositionSolver solver = new LUDecomposition( m ).getSolver();
		RealVector p0 = buildRealVector( );
		RealVector result = solver.solve(p0);			
		Map<S,Double> map = index.entrySet().stream()
				.map(e -> Pair.apply(e, result::getEntry))
				.collect(Collectors.toConcurrentMap(e -> e.getKey(), e -> e.getValue()))
				;
		for (S s : yesNodes) {
			map.put(s, 1.0);
		}
		return map;
	}


	private void computeStateIndex() {
		AtomicInteger counter = new AtomicInteger(0);
		index = computingNodes.stream().collect(Collectors.toConcurrentMap(s -> s,s -> counter.getAndIncrement()));
	}


	private void computeReachabilitySets() {
		yesNodes = chain.select(goal);
		computingNodes = chain.reachSet( condition, yesNodes );
		Set<S> noNodes = chain.select( s -> !computingNodes.contains(s)&&!yesNodes.contains(s) );
		Set<S> reachNo = chain.reachSet( condition, noNodes );
		Set<S> reachOnlyYes = computingNodes.parallelStream().filter(i -> !reachNo.contains(i)).collect(Collectors.toCollection(HashSet::new));
		yesNodes.addAll(reachOnlyYes);
		computingNodes.removeAll(reachOnlyYes);
	}
}
