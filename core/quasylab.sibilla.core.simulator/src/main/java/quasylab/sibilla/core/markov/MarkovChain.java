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

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author loreti
 *
 */
public  abstract class MarkovChain<S> {
	
	private final HashSet<S> states = new HashSet<>();
	private final HashMap<S,Map<S,Double>> matrixR = new HashMap<>();
	private final HashMap<S,Map<S,Double>> preR = new HashMap<>();
	private final HashMap<S,Double> exitRates = new HashMap<>();
	
	public MarkovChain() {
	}
	
	public boolean contains( S s ) {
		return states.contains(s);
	}

	public int numberOfStates() {
		return states.size();
	}

	
	public abstract void add(S s, Map<S, Double> map);
	
	protected double addToRow(S s, Map<S, Double> row) {
		createIfNotExists(s);
		Map<S,Double> current = matrixR.get(s);
		current.putAll(row);
		row.forEach((s2,v) -> addIncomingEdge(s,v,s2));
		double sum = current.values().stream().collect(Collectors.summingDouble(Double::doubleValue));
		exitRates.put(s, sum);
		return sum;
	}
	
	public double sumOfRow( S s ) {
		return exitRates.getOrDefault(s, 0.0);
	}
	
	private void addIncomingEdge(S s, Double v, S s2) {
		createIfNotExists(s2);
		Map<S, Double> pre = preR.get(s2);
		pre.put(s, pre.getOrDefault(s, 0.0)+v);
	}

	protected Map<S,Double> getRow( S s ) {
		return matrixR.get(s);
	}
	
	protected void createIfNotExists(S s) {
		if (!states.contains(s)) {
			states.add(s);
			matrixR.put(s, new HashMap<>());
			preR.put(s, new HashMap<>());
		}
	}

	public double rate(S s1, S s2) {
		return matrixR.getOrDefault(s1,new HashMap<>()).getOrDefault(s2, 0.0);
	}
	
	public static <S, M extends MarkovChain<S>> M generateMarkovChain(  Supplier<M> builder, S init , Function<S,Map<S,Double>> stepFunction ) {
		HashSet<S> visited = new HashSet<>();
		LinkedList<S> queue = new LinkedList<>();
		queue.add(init);
		visited.add(init);
		M markovChain = builder.get();
		while (!queue.isEmpty()) {
			S s = queue.poll();
			Map<S, Double> next = stepFunction.apply(s);
			markovChain.add(s, next);
			next.entrySet().forEach(e -> {
				if (!visited.contains(e.getKey())) {
					queue.add(e.getKey());
					visited.add(e.getKey());
				}
			});
		}		
		return markovChain;
	}

	
	public Set<S> reachSet(Predicate<S> condition, Set<S> nodes) {
		HashSet<S> toReturn = new HashSet<>();
		LinkedList<S> queue = new LinkedList<>(nodes);
		while (!queue.isEmpty()) {
			S s1 = queue.poll();
			preR.get(s1).keySet().stream().filter(condition).forEach(s2 -> {
				if ((!toReturn.contains(s2))&(!nodes.contains(s2))) {
					toReturn.add(s2);
					queue.add(s2);
				}
			});
		}
		return toReturn;
	}

	public Set<S> select(Predicate<S> filter) {
		HashSet<S> s = states.parallelStream().filter(filter).collect(Collectors.toCollection(HashSet::new));
		return s;
	}

	@SuppressWarnings("unchecked")
	public Set<S> getStates() {
		return (Set<S>) states.clone();
	}

	public abstract Map<S,Double> probabilityMatrixRow( S s ); 
	
	public Map<S,Double> forward(Map<S,Double> v) {
		return move(this.matrixR,v);
	}

	public Map<S,Double> backward(Map<S,Double> v) {
		return move(this.preR,v);
	}
	
	public List<Map<S,Double>> forward( Map<S,Double> v , int steps )  {
		return move( this::forward , v , steps );
	}

	public List<Map<S, Double>> move(Function<Map<S,Double>,Map<S,Double>> transition, Map<S,Double> v , int steps) {
		ArrayList<Map<S,Double>> toReturn = new ArrayList<>();
		Map<S, Double> step = v;
		for(  int i=0 ; i<steps; i++) {
			toReturn.add(step);
			step = transition.apply(step);
		}
		toReturn.add(step);
		return toReturn;
	}

	public List<Map<S,Double>> backward( Map<S,Double> v , int steps )  {
		return move( this::backward, v , steps );
	}


	public static <S> Map<S,Double> move(Map<S,Map<S,Double>> transition, Map<S,Double> v) {
		Map<S,Double> toReturn = Collections.synchronizedMap(new HashMap<>());
		v.entrySet().stream().forEach( eV ->
			transition.get(eV.getKey()).entrySet().forEach( eM ->
				addTo(toReturn,eM.getKey(),eM.getValue()*eV.getValue())
			)
		);
		return toReturn;
	}

	public static <S> void addTo( Map<S,Double> m , S s , double v ) {
		m.merge(s, v, (x,y) -> (x+y));
	}
	
	public static <S> Map<S,Double> sum(Map<S,Double> m1, Map<S,Double> m2) {
		Map<S,Double> toReturn = Collections.synchronizedMap(new HashMap<>(m1));
		m2.entrySet().stream().forEach(e -> addTo(toReturn,e.getKey(),e.getValue()));
		return toReturn;
	}
	
	public static <S> RealMatrix generateMatrix( IntFunction<RealMatrix> matrixBuilder, Function<S,Stream<Map.Entry<S,Double>>> rowFunction, Map<S,Integer> index) {
		RealMatrix rm = matrixBuilder.apply(index.size());
		index.forEach((s1,i1) -> {
			rowFunction.apply(s1).forEach(p -> {
				rm.addToEntry(i1, index.get(p.getKey()), p.getValue());
			});;
		});
		return rm;
	}
	
	public static <S> RealVector generateVector( IntFunction<RealVector> vectorBuilder, Map<S,Integer> index, Function<S,Double> init) {
		RealVector rv = vectorBuilder.apply(index.size());
		index.forEach((s,i) -> rv.setEntry(i, init.apply(s)));
		return rv;
	}
	
	public Set<S> next(S s) {
		return matrixR.get(s).keySet();
	}
}
