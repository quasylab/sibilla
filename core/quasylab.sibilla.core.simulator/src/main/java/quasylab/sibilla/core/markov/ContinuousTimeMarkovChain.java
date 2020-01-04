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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import quasylab.sibilla.core.util.Pair;

/**
 * @author loreti
 *
 */
public class ContinuousTimeMarkovChain<S> extends MarkovChain<S> {
	
	private double maxExitRate;

	public void add( S s , Map<S,Double> row) {
		if (!row.entrySet().stream().noneMatch(e -> (e.getValue().doubleValue()<=0.0))) {
			throw new IllegalArgumentException("All entries must be greater than 0.0!");
		}
		double sum = addToRow(s, row);
		if (sum>maxExitRate) {
			maxExitRate = sum;
		}
	}

	@Override
	public Map<S,Double> probabilityMatrixRow(S s) {
		double totalRate = sumOfRow(s);		
		return getRow(s).entrySet().stream().collect(Collectors.toConcurrentMap(e -> e.getKey(),e -> e.getValue()/totalRate));
	}

	public Map<S, Double> uniformisedMatrixRow(S s) {
		Map<S,Double> row = getRow(s);
		double exitRate = sumOfRow(s);
		Map<S,Double> uniformisedRow = getRow(s).entrySet().stream().collect(Collectors.toConcurrentMap(e -> e.getKey(), e -> e.getValue()/maxExitRate));	
		if (exitRate<maxExitRate) {
			uniformisedRow.put(s, (maxExitRate-exitRate)/maxExitRate);
		}
		return uniformisedRow;
	}

	public Stream<Pair<S, Double>> rateMatrixRow(S s) {
		return getRow(s).entrySet().stream().map(Pair::new);
	}

	public double getMaxRate() {
		return maxExitRate;
	}


}
