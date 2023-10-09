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

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.util.HashMap;
import java.util.LinkedList;
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
public class BoundedReachabilityDiscreteSolver<S> {

	private final Predicate<S> condition;
	private final Predicate<S> goal;
	private final DiscreteTimeMarkovChain<S> chain;
	private RealMatrix matrix;
	private RealVector p0;
	private LinkedList<RealVector> vectorResults;
	private Set<S> yesNodes;
	private Set<S> computingNodes;
	private Map<S, Integer> index;
	
	public BoundedReachabilityDiscreteSolver( 
			DiscreteTimeMarkovChain<S> chain,
			Predicate<S> condition,
			Predicate<S> goal) {
		this.chain = chain;
		this.condition = condition;
		this.goal = goal;
	}

	public BoundedReachabilityDiscreteSolver( 
			DiscreteTimeMarkovChain<S> chain,
			Predicate<S> goal) {
		this(chain, s->true, goal);
	}


	private RealVector buildRealVector() {
		return MarkovChain.generateVector(ArrayRealVector::new, index, s -> 
		chain.probabilityMatrixRow(s)
		.entrySet().stream()
		.filter(e2 -> yesNodes.contains(e2.getKey()))
		.collect(Collectors.summingDouble(Map.Entry::getValue)));
	}

	private RealMatrix buidProbabilityMatrix(  ) {
		return MarkovChain.generateMatrix(i -> MatrixUtils.createRealMatrix(i, i), s -> 
			chain.probabilityMatrixRow(s).entrySet().stream()
				.filter(p -> computingNodes.contains(p.getKey()))
				, index);
	}
	
	public Map<S,Double> compute(int k) {
		if (vectorResults == null) {
			computeReachabilitySets();
			computeStateIndex();
			matrix = buidProbabilityMatrix( ).transpose();
			p0 = buildRealVector( );
			vectorResults = new LinkedList<>();
			vectorResults.add(p0);
		}
		generateSteps(k);
		return generateMap( k );
	}


	private Map<S, Double> generateMap( int k ) {
		HashMap<S,Double> toReturn = new HashMap<>();
		yesNodes.forEach(s -> toReturn.put(s, 1.0));
		if (k>0) {
			RealVector v = sum(k);
			index.forEach((s,i) -> toReturn.put(s, v.getEntry(i)));
		}
		return toReturn;
	}


	private RealVector sum(int k) {
		return vectorResults.stream().limit(k).reduce(new ArrayRealVector(index.size()), (v1,v2) -> v1.add(v2));
	}


	private void generateSteps(int k) {
		while (vectorResults.size()<k) {
			vectorResults.add(matrix.preMultiply(vectorResults.getLast()));
		}
	}


	private void computeStateIndex() {
		AtomicInteger counter = new AtomicInteger(0);
		index = computingNodes.stream().collect(Collectors.toConcurrentMap(s -> s,s -> counter.getAndIncrement()));
	}


	private void computeReachabilitySets() {
		yesNodes = chain.select(goal);
		computingNodes = chain.reachSet( condition, yesNodes );
	}
}
