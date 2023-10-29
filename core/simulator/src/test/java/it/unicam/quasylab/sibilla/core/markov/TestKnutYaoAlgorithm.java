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

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestKnutYaoAlgorithm {
	
	public enum STATES {
		S1,
		S2,
		S3,
		S4,
		S5,
		S6,
		S7,
		D1,
		D2,
		D3,
		D4,
		D5,
		D6		
	}

	private Map<STATES,Double> getForwardStep0() {
		HashMap<STATES,Double> toReturn = new HashMap<>();
		toReturn.put(STATES.S1, 1.0);
		return toReturn;
	}
	
	private Map<STATES,Double> getForwardStep1() {
		HashMap<STATES,Double> toReturn = new HashMap<>();
		toReturn.put(STATES.S2, 0.5);
		toReturn.put(STATES.S3, 0.5);
		return toReturn;
	}

	private Map<STATES,Double> getForwardStep2() {
		HashMap<STATES,Double> toReturn = new HashMap<>();
		toReturn.put(STATES.S4, 0.25);
		toReturn.put(STATES.S5, 0.25);
		toReturn.put(STATES.S6, 0.25);
		toReturn.put(STATES.S7, 0.25);
		return toReturn;
	}

	private Map<STATES,Double> getForwardStep3() {
		HashMap<STATES,Double> toReturn = new HashMap<>();
		toReturn.put(STATES.S2, 0.125);
		toReturn.put(STATES.D1, 0.125);
		toReturn.put(STATES.D2, 0.125);
		toReturn.put(STATES.D3, 0.125);
		toReturn.put(STATES.D4, 0.125);
		toReturn.put(STATES.D5, 0.125);
		toReturn.put(STATES.D6, 0.125);
		toReturn.put(STATES.S3, 0.125);
		return toReturn;
	}

	private Map<STATES,Double> getForwardStep4() {
		HashMap<STATES,Double> toReturn = new HashMap<>();
		toReturn.put(STATES.S4, 0.125*0.5);
		toReturn.put(STATES.S5, 0.125*0.5);
		toReturn.put(STATES.D1, 0.125);
		toReturn.put(STATES.D2, 0.125);
		toReturn.put(STATES.D3, 0.125);
		toReturn.put(STATES.D4, 0.125);
		toReturn.put(STATES.D5, 0.125);
		toReturn.put(STATES.D6, 0.125);
		toReturn.put(STATES.S6, 0.125*0.5);
		toReturn.put(STATES.S7, 0.125*0.5);
		return toReturn;
	}

	private Map<STATES,Double> getForwardStep5() {
		HashMap<STATES,Double> toReturn = new HashMap<>();
		toReturn.put(STATES.S2, 0.125*0.5*0.5);
		toReturn.put(STATES.D1, 0.125+0.125*0.5*0.5);
		toReturn.put(STATES.D2, 0.125+0.125*0.5*0.5);
		toReturn.put(STATES.D3, 0.125+0.125*0.5*0.5);
		toReturn.put(STATES.D4, 0.125+0.125*0.5*0.5);
		toReturn.put(STATES.D5, 0.125+0.125*0.5*0.5);
		toReturn.put(STATES.D6, 0.125+0.125*0.5*0.5);
		toReturn.put(STATES.S3, 0.125*0.5*0.5);
		return toReturn;
	}

	private Map<STATES,Double> getBackwardStep0() {
		HashMap<STATES,Double> toReturn = new HashMap<>();
		toReturn.put(STATES.D6, 1.0);
		return toReturn;
	}
	
	private Map<STATES,Double> getBackwardStep1() {
		HashMap<STATES,Double> toReturn = new HashMap<>();
		toReturn.put(STATES.D6, 1.0);
		toReturn.put(STATES.S7, 0.5);
		return toReturn;
	}

	private Map<STATES,Double> getBackwardStep2() {
		HashMap<STATES,Double> toReturn = new HashMap<>();
		toReturn.put(STATES.D6, 1.0);
		toReturn.put(STATES.S3, 0.25);
		toReturn.put(STATES.S7, 0.5);
		return toReturn;
	}

	private Map<STATES,Double> getBackwardStep3() {
		HashMap<STATES,Double> toReturn = new HashMap<>();
		toReturn.put(STATES.D6, 1.0);
		toReturn.put(STATES.S1, 0.125);
		toReturn.put(STATES.S3, 0.25);
		toReturn.put(STATES.S7, 0.5+0.125);
		return toReturn;
	}

	private Map<STATES,Double> getBackwardStep4() {
		HashMap<STATES,Double> toReturn = new HashMap<>();
		toReturn.put(STATES.D6, 1.0);
		toReturn.put(STATES.S1, 0.125);
		toReturn.put(STATES.S3, (0.5+0.125)*0.5);
		toReturn.put(STATES.S7, 0.5+0.125);
		return toReturn;
	}

	private Map<STATES,Double> getBackwardStep5() {
		HashMap<STATES,Double> toReturn = new HashMap<>();
		toReturn.put(STATES.D6, 1.0);
		toReturn.put(STATES.S1, 0.5*((0.5+0.125)*0.5));
		toReturn.put(STATES.S3, (0.5+0.125)*0.5);
		toReturn.put(STATES.S7, 0.5+((0.5+0.125)*0.5)*0.5);
		return toReturn;
	}
	

	public Map<STATES,Double> step( STATES s ) {
		HashMap<STATES,Double> toReturn = new HashMap<>();
		switch (s) {
		case S1:
			toReturn.put(STATES.S2, 0.5);
			toReturn.put(STATES.S3, 0.5);
			break ;
		case S2:
			toReturn.put(STATES.S4, 0.5);
			toReturn.put(STATES.S5, 0.5);
			break ;
		case S3:
			toReturn.put(STATES.S6, 0.5);
			toReturn.put(STATES.S7, 0.5);
			break ;
		case S4:
			toReturn.put(STATES.D1, 0.5);
			toReturn.put(STATES.S2, 0.5);
			break ;
		case S5:
			toReturn.put(STATES.D2, 0.5);
			toReturn.put(STATES.D3, 0.5);
			break ;
		case S6:
			toReturn.put(STATES.D4, 0.5);
			toReturn.put(STATES.D5, 0.5);
			break ;
		case S7:
			toReturn.put(STATES.S3, 0.5);
			toReturn.put(STATES.D6, 0.5);
			break ;
		case D1:
			toReturn.put(STATES.D1, 1.0);
			break ;
		case D2:
			toReturn.put(STATES.D2, 1.0);
			break ;
		case D3:
			toReturn.put(STATES.D3, 1.0);
			break ;
		case D4:
			toReturn.put(STATES.D4, 1.0);
			break ;
		case D5:
			toReturn.put(STATES.D5, 1.0);
			break ;
		case D6:		
			toReturn.put(STATES.D6, 1.0);
			break ;
		}
		return toReturn;
	}
	
	private DiscreteTimeMarkovChain<STATES> generateDTMC() {
		return MarkovChain.generateMarkovChain(DiscreteTimeMarkovChain::new, STATES.S1, this::step );
	}
	
	@Test
	void testEquals() {
        assertEquals(STATES.D1, STATES.D1);
	}

	@Test
	void testGenerate() {
		DiscreteTimeMarkovChain<STATES> dtmc = generateDTMC();
		assertEquals(13,dtmc.numberOfStates());
	}

	@Test
	void testReach() {
		STATES[] values = new STATES[]{ STATES.D1, STATES.D2, STATES.D3, STATES.D4, STATES.D5, STATES.D6 };
		DiscreteTimeMarkovChain<STATES> dtmc = generateDTMC();
		for (STATES d : values) {
			checkReachProbability("Reach "+d, dtmc, STATES.S1, d, 1.0/6.0 );
		}
	}

	@Test
	void testBoundedReach() {
		DiscreteTimeMarkovChain<STATES> dtmc = generateDTMC();
		BoundedReachabilityDiscreteSolver<STATES> solver = new BoundedReachabilityDiscreteSolver<>(dtmc, s -> s==STATES.D6);
		Map<STATES, Double> prob;
		double expectedS1 = 0.0;
		double expectedS3 = 0.0;
		double expectedS7 = 0.0;
		double expectedD6 = 1.0;
		double stepS1 = 0.0;
		double stepS3 = 0.0;
		double stepS7 = 0.0;
		double stepD6 = 1.0;
		for( int k=0 ; k<100 ; k++ ) {
			prob = solver.compute(k);
			assertEquals(expectedS1,prob.getOrDefault(STATES.S1,0.0),0.000001,"P(S1) in "+k+"steps.");
			assertEquals(expectedS3,prob.getOrDefault(STATES.S3,0.0),0.000001,"P(S3) in "+k+"steps.");
			assertEquals(expectedS7,prob.getOrDefault(STATES.S7,0.0),0.000001,"P(S7) in "+k+"steps.");
			assertEquals(expectedD6,prob.getOrDefault(STATES.D6,0.0),0.000001,"P(D6) in "+k+"steps.");
			double oldStepS3 = stepS3;
			double oldStepS7 = stepS7;
			stepS7 = (k==0?0.5:0.5*oldStepS3);
			stepS3 = 0.5*oldStepS7;
			stepS1 = 0.5*oldStepS3;
			expectedS1 = expectedS1+stepS1;
			expectedS3 = expectedS3+stepS3;
			expectedS7 = expectedS7+stepS7;
		}
		
	}

	private void checkReachProbability(String label, DiscreteTimeMarkovChain<STATES> dtmc, STATES start, STATES goal, double expected) {
		UnboundedReachabilitySolver<STATES> solver = new UnboundedReachabilitySolver<>(dtmc, s -> s==goal);
		Map<STATES, Double> prob = solver.compute();
		assertEquals(expected, prob.get(start),0.000001,label);
	}

	@Test
	void testForward() {
		DiscreteTimeMarkovChain<STATES> dtmc = generateDTMC();
		Map<STATES,Double> step1 = dtmc.forward(getForwardStep0());
		assertEquals(getForwardStep1(),step1);
		Map<STATES,Double> step2 = dtmc.forward(step1);
		assertEquals(getForwardStep2(),step2);
		Map<STATES,Double> step3 = dtmc.forward(step2);
		assertEquals(getForwardStep3(),step3);
		Map<STATES,Double> step4 = dtmc.forward(step3);
		assertEquals(getForwardStep4(),step4);
		Map<STATES,Double> step5 = dtmc.forward(step4);
		assertEquals(getForwardStep5(),step5);
	}

	@Test
	void testBackward() {
		DiscreteTimeMarkovChain<STATES> dtmc = generateDTMC();
		Map<STATES,Double> step1 = dtmc.backward(getBackwardStep0());
		assertEquals(getBackwardStep1(),step1);
		Map<STATES,Double> step2 = dtmc.backward(step1);
		assertEquals(getBackwardStep2(),step2);
		Map<STATES,Double> step3 = dtmc.backward(step2);
		assertEquals(getBackwardStep3(),step3);
		Map<STATES,Double> step4 = dtmc.backward(step3);
		assertEquals(getBackwardStep4(),step4);
		Map<STATES,Double> step5 = dtmc.backward(step4);
		assertEquals(getBackwardStep5(),step5);
	}
	
	@Test
	void checkBounded() {
		
	}
	
//	public MarkovProcess<Integer> queue( int size, double service, double arrival) {
//		MarkovProcess<Integer> queue = i -> {
//			HashMap<Integer,Double> next = new HashMap<>();
//			if (i>0) {
//				next.put(i-1,service);
//			} 
//			if (i<size) {
//				next.put(i+1, arrival);
//			}
//			return next;
//		};
//		return queue;
//	}
//	
}
