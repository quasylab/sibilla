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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TaxiScenarioMC {
	
	public static int T = 0;
	public static int U = 1;
	public static int TS = 2;
	public static int TL = 3;
	
	public static int MAX_USERS = 6;
	public static int NUMBER_OF_TAXIS = 10;
	public static double LAMBDA = 1.0/5.0;
	public static double BETA = 1.0;
	public static double MU_SHORT = 1.0/10.0;
	public static double MU_LONG = 1.0/30.0;
	public static double P_SHORT = 0.5;
	
	
	
	public static HashMap<State,Double> next(State s) {
		HashMap<State,Double> toReturn = new HashMap<State, Double>();
		int[] values = s.getState();
		
		//A new user arrives at the station
		if(values[U] < MAX_USERS) {
			int[] newState = Arrays.copyOf(values, values.length);
			newState[U] = newState[U] + 1;
			toReturn.put(new State(newState), LAMBDA);
		}
		
		//A user enters in a taxi...
		if((values[U] > 0) && (values[T] > 0)) {
			//...and selects the short trip
			int[] newState = Arrays.copyOf(values, values.length);
			newState[U] = newState[U] - 1;
			newState[T] = newState[T] - 1;
			newState[TS] = newState[TS] + 1;
			toReturn.put(new State(newState), P_SHORT*BETA);
			
			//...and selects the long trip
			newState = Arrays.copyOf(values, values.length);
			newState[U] = newState[U] - 1;
			newState[T] = newState[T] - 1;
			newState[TL] = newState[TL] + 1;
			toReturn.put(new State(newState), (1-P_SHORT)*BETA);
		}
		
		//A taxi returns from a short trip
		if(values[TS] > 0) {
			int[] newState = Arrays.copyOf(values, values.length);
			newState[T] = newState[T] + 1;
			newState[TS] = newState[TS] - 1;
			toReturn.put(new State(newState), MU_SHORT);
		}
		
		//A taxi returns from a long trip
		if(values[TL] > 0) {
			int[] newState = Arrays.copyOf(values, values.length);
			newState[T] = newState[T] + 1;
			newState[TL] = newState[TL] - 1;
			toReturn.put(new State(newState), MU_LONG);
		}

		return toReturn;
	}
	
	
	public static ContinuousTimeMarkovChain<State> generateCTMC() {
		return MarkovChain.generateMarkovChain(ContinuousTimeMarkovChain<State>::new, 
				new State(NUMBER_OF_TAXIS,0,0,0), TaxiScenarioMC::next);
	}
	
	public static void main(String[] args) {
		State init = new State(NUMBER_OF_TAXIS, 0, 0, 0);
		ContinuousTimeMarkovChain<State> ctmc = generateCTMC();
		System.out.println(ctmc.getStates().size());
		TransientProbabilityContinuousSolver<State> solver = new TransientProbabilityContinuousSolver<State>(ctmc, 1.0E-6, init);
		
//		for (int i=0 ; i<720 ; i++ ) {
//			Map<State, Double> prob = solver.compute(i);
//			System.out.println(prob.get(init));
//		}

		BoundedReachabilityContinuousSolver<State> solver2 = new BoundedReachabilityContinuousSolver<State>(ctmc, 1.0E-6, s -> s.retrieve(U)>5);
		for (int i=0 ; i<120 ; i++ ) {
			Map<State, Double> prob = solver2.compute(i);
			System.out.println(prob.get(init));
		}

		
//		SteadyStateSolver<State> solver = new SteadyStateSolver<State>(ctmc, init);
//		solver.computeBSCC();
		
//		Map<State,Integer> index = ctmc.getStates().stream().collect(Collectors.toMap(
//				x -> x, x -> ctmc.numberOfStates()));
//		RealMatrix rm = MarkovChain.generateMatrix(i -> MatrixUtils.createRealMatrix(i, i),
//				s -> ctmc.rateMatrixRow(s) , index);
		
		
	}
	
	
	
	
}
