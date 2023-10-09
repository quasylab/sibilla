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

class TransientCTMC {
	
	public double[] resultsReachability = new double[] { 
			0.0,
			0.1812692444810056,
			0.32967994070200607,
			0.4511883447424949,
			0.550671018664013,
			0.6321205465608238,
			0.6988057804303445,
			0.7534029956652349,
			0.7981034613203072,
			0.8347011014829853,
			0.8646646794988324,
			0.8891968242887861,
			0.9092819937634702,
			0.9257263979243864,
			0.9391899266401373,
			0.9502129029712245,
			0.9592377832959382,
			0.9666266987435475,
			0.9726762637309468,
			0.9776291962751261,
			0.9816843470479861 };
	
	public double[] time = new double[] { 
			0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9,
			1.0, 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 2.0
	};
	
	public double[][] resultsTransient = new double[][] {
		new double[] {1.0, 0.0},
		new double[] {0.84261224158021, 0.157387720261577},
		new double[] {0.7471517313945306, 0.2528481922890446},
		new double[] {0.6892519960258308, 0.3107478894995343},
		new double[] {0.6541340222416013, 0.34586582512555497},
		new double[] {0.6328338853742251, 0.36716592383472396},
		new double[] {0.619914690268214, 0.38008508078252934},
		new double[] {0.6120787933101007, 0.3879209395824383},
		new double[] {0.6073260725386473, 0.39267362219568885},
		new double[] {0.6044433926582476, 0.39555626391788706},
		new double[] {0.6026949499159475, 0.3973046685019873},
		new double[] {0.601634456774963, 0.3983651234847735},
		new double[] {0.6009912261605287, 0.39900831594101077},
		new double[] {0.6006010780622121, 0.3993984258811318},
		new double[] {0.600364432269732, 0.399635033515418},
		new double[] {0.6002208903323049, 0.39977853729465257},
		new double[] {0.6001338187376425, 0.3998655707311236},
		new double[] {0.6000809981373153, 0.39991835317326113},
		new double[] {0.6000489518152287, 0.39995036133715944},
		new double[] {0.6000295057298595, 0.39996976926434186},
		new double[] {0.6000177020743979, 0.399981534761618}	
	};
	
	@Test
	void testUniformisazion() {
		ContinuousTimeMarkovChain<Integer> ctmc = generateCTMC();
		Map<Integer, Double> row0 = ctmc.uniformisedMatrixRow(0);
		assertEquals(1.0/3.0, row0.get(0),0.000001);
		assertEquals(2.0/3.0, row0.get(1),0.000001);
		Map<Integer, Double> row1 = ctmc.uniformisedMatrixRow(1);
		assertEquals(1.0, row1.get(0),0.000001);
		assertEquals(null, row1.get(1));
	}
	
	@Test
	void testBoundedReach() {
		ContinuousTimeMarkovChain<Integer> ctmc = generateCTMC();
		BoundedReachabilityContinuousSolver<Integer> solver = new BoundedReachabilityContinuousSolver<Integer>(ctmc, 1.0E-6, s -> s.intValue()==1);
		Map<Integer,Double> prob;
		for( int i=0 ; i<resultsReachability.length; i++ ) {
			prob = solver.compute(time[i]);
			assertEquals(resultsReachability[i],prob.getOrDefault(0, 0.0),0.000001);
			assertEquals(1.0,prob.get(1),0.000001);
		}
	}

	@Test
	void testTransient() {
		ContinuousTimeMarkovChain<Integer> ctmc = generateCTMC();
		TransientProbabilityContinuousSolver<Integer> solver = new TransientProbabilityContinuousSolver<Integer>(ctmc, 1.0E-6, 0);
		Map<Integer,Double> prob;
		for( int i=0 ; i<resultsReachability.length; i++ ) {
			prob = solver.compute(time[i]);
			assertEquals(resultsTransient[i][0],prob.getOrDefault(0, 0.0),0.000001);
			assertEquals(resultsTransient[i][1],prob.get(1),0.000001);
		}
	}

	private ContinuousTimeMarkovChain<Integer> generateCTMC() {
		ContinuousTimeMarkovChain<Integer> ctmc = new ContinuousTimeMarkovChain<Integer>();
		Map<Integer,Double> next0 = new HashMap<>();
		next0.put(1, 2.0);
		Map<Integer,Double> next1 = new HashMap<>();
		next1.put(0, 3.0);
		ctmc.add(0, next0);
		ctmc.add(1, next1);
		return ctmc;
	}


}
