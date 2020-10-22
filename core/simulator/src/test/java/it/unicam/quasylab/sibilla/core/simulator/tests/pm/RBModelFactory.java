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

package it.unicam.quasylab.sibilla.core.simulator.tests.pm;

import it.unicam.quasylab.sibilla.core.models.pm.BroadcastRule;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationRule;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.models.pm.UnicastRule;
import it.unicam.quasylab.sibilla.core.models.pm.UnicastRule.UnicastReceiver;

import java.util.LinkedList;

/**
 * @author loreti
 *
 */
public class RBModelFactory {
	
	public static final int SIZE = 100;

	public static final int R_INDEX = 0;
	public static final int B_INDEX = 1;
	public static final int RT_INDEX = 2;
	public static final int BT_INDEX = 3;

	public static final double SPREAD_RATE = 0.1;
	public static final double CHANGE_RATE = 1.0;

	public static final double K = 10;
	
	private int[] initial;

	private double stay_prob;

	private double change_prob;

	private double lambda_s;

	private double lambda_c;

	private double k;	
	
	private LinkedList<PopulationRule> buildRules() {
		LinkedList<PopulationRule> rules = new LinkedList<>();
		
		
		//R START
		rules.add( 
				new BroadcastRule(
						"red*",
						s -> lambda_s,
						R_INDEX, 
						rg -> R_INDEX, 
						new BroadcastRule.BroadcastReceiver(
								R_INDEX,
								s -> broadcastRed(R_INDEX, s), 
								rg -> rg.nextDouble()<change_prob?RT_INDEX:R_INDEX							
								),
						new BroadcastRule.BroadcastReceiver(
								BT_INDEX,
								s -> broadcastRed(BT_INDEX, s), 
								rg -> rg.nextDouble()<stay_prob?B_INDEX:BT_INDEX							
								)																	
						)				
		);		
		//R END
		
		//B START
		rules.add( 
				new BroadcastRule(
						"blue*",
						s -> lambda_s,
						B_INDEX, 
						rg -> B_INDEX, 
						new BroadcastRule.BroadcastReceiver(
								B_INDEX,
								s -> broadcastBlue(B_INDEX, s), 
								rg -> rg.nextDouble()<change_prob?BT_INDEX:B_INDEX							
								),
						new BroadcastRule.BroadcastReceiver(
								RT_INDEX,
								s -> broadcastBlue(RT_INDEX, s), 
								rg -> rg.nextDouble()<stay_prob?R_INDEX:RT_INDEX							
								)																	
						)				
		);		
		//B END
		
		//CR START
		rules.add(
			new UnicastRule(
					"changeB", 
					s -> lambda_c, 
					RT_INDEX, 
					rg  -> R_INDEX, 
					new UnicastReceiver(
							R_INDEX,
							s -> 1.0,
							rg -> B_INDEX
							),
					new UnicastReceiver(
							RT_INDEX,
							s -> 1.0,
							rg -> B_INDEX
							)
					)								
		);
				
		rules.add( 
				new BroadcastRule(
						"red*",
						s -> lambda_s,
						RT_INDEX, 
						rg -> RT_INDEX, 
						new BroadcastRule.BroadcastReceiver(
								R_INDEX,
								s -> broadcastRed(R_INDEX, s), 
								rg -> rg.nextDouble()<change_prob?RT_INDEX:R_INDEX							
								),
						new BroadcastRule.BroadcastReceiver(
								BT_INDEX,
								s -> broadcastRed(BT_INDEX, s), 
								rg -> rg.nextDouble()<stay_prob?B_INDEX:BT_INDEX							
								)																	
						)				
		);		
		//CR END
		
		//CB START
		rules.add(
			new UnicastRule(
					"changeR", 
					s -> lambda_c, 
					BT_INDEX, 
					rg  -> B_INDEX, 
					new UnicastReceiver(
							B_INDEX,
							s -> 1.0,
							rg -> R_INDEX
							),
					new UnicastReceiver(
							BT_INDEX,
							s -> 1.0,
							rg -> R_INDEX
							)
					)								
		);
				
		rules.add( 
				new BroadcastRule(
						"blue*",
						s -> lambda_s,
						BT_INDEX, 
						rg -> BT_INDEX, 
						new BroadcastRule.BroadcastReceiver(
								B_INDEX,
								s -> broadcastBlue(B_INDEX, s), 
								rg -> rg.nextDouble()<change_prob?BT_INDEX:B_INDEX							
								),
						new BroadcastRule.BroadcastReceiver(
								RT_INDEX,
								s -> broadcastBlue(RT_INDEX, s), 
								rg -> rg.nextDouble()<stay_prob?R_INDEX:RT_INDEX							
								)																	
						)				
		);		
		//CB END		
		
		return rules;
	}
	
	private double broadcastRed( int idx , PopulationState s ) {
		double pop = s.getOccupancy(idx);
		if (pop == 0.0) {
			pop = 1.0;
		}
		double tot = s.getOccupancy(R_INDEX)+s.getOccupancy(BT_INDEX);
		//return pop*k/(tot*tot);
		//return Math.min(1.0, (k*pop)/(tot*tot));
		return Math.min(1.0, k/tot);
	}

	private double broadcastBlue( int idx , PopulationState s ) {
		double pop = s.getOccupancy(idx);
		if (pop == 0.0) {
			pop = 1.0;
		}
		double tot = s.getOccupancy(B_INDEX)+s.getOccupancy(RT_INDEX);
		//return pop*k/(tot*tot);
		//return Math.min(1.0, (k*pop)/(tot*tot));
		return Math.min(1.0, k/tot);
	}

}
