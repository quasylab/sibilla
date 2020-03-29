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

import java.util.*;

/**
 * @author loreti
 * @param <S>
 *
 */
public class SteadyStateSolver<S> {

	private ContinuousTimeMarkovChain<S> chain;
	private LinkedList<Set<S>> bscc;
	private S init;
	private Map<S,Integer> bsccIndex = new HashMap<S,Integer>();
	private Map<S,Integer> lowIndex = new HashMap<S,Integer>();
	private List<S> bsccQueue = new LinkedList<>();
	private Set<S> inQueue = new HashSet<S>();
	private int indexCounter = 0;
	
	public SteadyStateSolver(ContinuousTimeMarkovChain<S> chain, S init) {
		this.chain = chain;
		this.init = init;
	}
	
	public void computeBSCC( ) {
		for (S s : chain.getStates()) {
			strongconnected(s);
		}
	}

	private void strongconnected(S s) {
		bsccIndex.put(s, indexCounter);
		lowIndex.put(s, indexCounter++);
		bsccQueue.add(s);
		inQueue.add(s);
		for (S w : chain.next(s)) {
			Integer wIndex = bsccIndex.get(s);
			if (wIndex != null) {
				strongconnected(w);
				lowIndex.merge(s, lowIndex.get(w), Math::min);
			} else {
				if (inQueue.contains(w)) {
					lowIndex.merge(s, bsccIndex.get(w), Math::min);
				}
			}
		}
	}

}
