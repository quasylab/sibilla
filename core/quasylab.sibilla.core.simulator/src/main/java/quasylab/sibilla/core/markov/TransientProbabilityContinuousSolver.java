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

package quasylab.sibilla.core.markov;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author loreti
 * @param <S>
 *
 */
public class TransientProbabilityContinuousSolver<S> {

	private ContinuousTimeMarkovChain<S> chain;
	private RealMatrix matrix;
	private S init;
	private LinkedList<RealVector> vectorResults;
	private double epsilon;
	private Map<S, Integer> index;
	
	public TransientProbabilityContinuousSolver( 
			ContinuousTimeMarkovChain<S> chain,
			double epsilon,
			S init) {
		this.chain = chain;
		this.init = init;
		this.epsilon = epsilon;
	}

	private RealVector buildRealVector() {
		RealVector rv = new ArrayRealVector(index.size());
		rv.setEntry(index.get(init), 1.0);
		return rv;
	}

	private RealMatrix buidProbabilityMatrix(  ) {
		return MarkovChain.generateMatrix(i -> MatrixUtils.createRealMatrix(i, i), 
				s -> chain.uniformisedMatrixRow(s).entrySet().stream(),
				index);
	}
	
	public Map<S,Double> compute(double t) {
		if (vectorResults == null) {
			computeStateIndex();
			matrix = buidProbabilityMatrix( );
			vectorResults = new LinkedList<>();
			vectorResults.add(buildRealVector( ));
		}
		FoxGlynn fg = (t>0?FoxGlynn.compute(chain.getMaxRate()*t, epsilon):null);
		if (fg != null) {
			generateSteps(fg.rightPoint());			
		}
		return generateMap( fg );
	}


	private Map<S, Double> generateMap( FoxGlynn fg ) {
		HashMap<S,Double> toReturn = new HashMap<>();
		RealVector v;
		if (fg!=null) {
			v = sum(fg);
		} else {
			v = this.vectorResults.getFirst();
		}
		index.forEach((s,i) -> toReturn.put(s, v.getEntry(i)));
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
		index = chain.getStates().stream().collect(Collectors.toConcurrentMap(s -> s,s -> counter.getAndIncrement()));
	}


}
