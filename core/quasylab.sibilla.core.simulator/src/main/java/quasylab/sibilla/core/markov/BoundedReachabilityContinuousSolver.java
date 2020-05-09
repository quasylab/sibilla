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

package quasylab.sibilla.core.markov;

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
import java.util.stream.IntStream;

/**
 * @author loreti
 * @param <S>
 *
 */
public class BoundedReachabilityContinuousSolver<S> {

	private Predicate<S> condition;
	private Predicate<S> goal;
	private ContinuousTimeMarkovChain<S> chain;
	private RealMatrix matrix;
	private RealVector p0;
	private LinkedList<RealVector> vectorResults;
	private Set<S> yesNodes;
	private Set<S> computingNodes;
	private Map<S, Integer> index;
	private double epsilon;
	
	public BoundedReachabilityContinuousSolver( 
			ContinuousTimeMarkovChain<S> chain,
			double epsilon,
			Predicate<S> condition,
			Predicate<S> goal) {
		this.chain = chain;
		this.condition = condition;
		this.goal = goal;
		this.epsilon = epsilon;
	}

	public BoundedReachabilityContinuousSolver( 
			ContinuousTimeMarkovChain<S> chain,
			double epsilon,
			Predicate<S> goal) {
		this(chain, epsilon,s->true, goal);
	}


	private RealVector buildRealVector() {
		return MarkovChain.generateVector(ArrayRealVector::new, index, s -> (yesNodes.contains(s)?1.0:0.0));
	}

	private RealMatrix buidProbabilityMatrix(  ) {
		return MarkovChain.generateMatrix(i -> MatrixUtils.createRealMatrix(i, i), 
				s -> {			
					if (yesNodes.contains(s)) {
						HashMap<S,Double> unitRow = new HashMap<>();
						unitRow.put(s, 1.0);
						return unitRow.entrySet().stream();
					} else {
						return chain.uniformisedMatrixRow(s)
								.entrySet().stream() 
								.filter(p -> computingNodes.contains(p.getKey())||yesNodes.contains(p.getKey()));
					}
		
				},
				index);
	}
	
	public Map<S,Double> compute(double t) {
		if (vectorResults == null) {
			computeReachabilitySets();
			computeStateIndex();
			matrix = buidProbabilityMatrix( ).transpose();
			p0 = buildRealVector( );
			vectorResults = new LinkedList<>();
			vectorResults.add(p0);
		}
		FoxGlynn fg = (t>0?FoxGlynn.compute(chain.getMaxRate()*t, epsilon):null);
		if (fg != null) {
			generateSteps(fg.rightPoint());			
		}
		return generateMap( fg );
	}


	private Map<S, Double> generateMap( FoxGlynn fg ) {
		HashMap<S,Double> toReturn = new HashMap<>();
		yesNodes.forEach(s -> toReturn.put(s, 1.0));
		if (fg!=null) {
			RealVector v = sum(fg);
			index.forEach((s,i) -> toReturn.put(s, v.getEntry(i)));
		}
		return toReturn;
	}


	private RealVector sum(FoxGlynn fg ) {
		return IntStream.range(fg.leftPoint(), fg.rightPoint()).boxed()
				.map(i -> vectorResults.get(i).mapMultiply(fg.weight(i)/fg.totalWeight()))
				.reduce(new ArrayRealVector(index.size()), (v1,v2) -> v1.add(v2));
	}


	private void generateSteps(int k) {
		while (vectorResults.size()<k) {
			vectorResults.add(matrix.preMultiply(vectorResults.getLast()));
		}
	}


	private void computeStateIndex() {
		AtomicInteger counter = new AtomicInteger(0);
		index = computingNodes.stream().collect(Collectors.toConcurrentMap(s -> s,s -> counter.getAndIncrement()));
		yesNodes.stream().forEach(s -> index.put(s, counter.getAndIncrement()));;
	}


	private void computeReachabilitySets() {
		yesNodes = chain.select(goal);
		computingNodes = chain.reachSet( condition, yesNodes );
	}
}
