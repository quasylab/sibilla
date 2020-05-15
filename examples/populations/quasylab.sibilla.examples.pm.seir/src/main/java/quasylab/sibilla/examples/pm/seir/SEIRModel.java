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

/**
 * 
 */
package quasylab.sibilla.examples.pm.seir;

import quasylab.sibilla.core.models.ModelDefinition;
import quasylab.sibilla.core.models.pm.PopulationModel;
import quasylab.sibilla.core.models.pm.PopulationState;
import quasylab.sibilla.core.simulator.SimulationEnvironment;
import quasylab.sibilla.core.simulator.sampling.SamplingCollection;
import quasylab.sibilla.core.simulator.sampling.StatisticSampling;

import java.io.FileNotFoundException;
import java.net.UnknownHostException;

/**
 * @author loreti
 *
 */
public class SEIRModel {

	public final static int SAMPLINGS = 120;
	public final static double DEADLINE = 120;
	private static final int REPLICA = 10;

	public static void main(String[] argv) throws FileNotFoundException, InterruptedException, UnknownHostException {
		SEIRModelDefinition def = new SEIRModelDefinition();
		SimulationEnvironment simulator = new SimulationEnvironment();
		SamplingCollection<PopulationState> collection = new SamplingCollection<>();
		collection.add(StatisticSampling.measure("S",SAMPLINGS,DEADLINE,SEIRModelDefinition::fractionOfS));
		collection.add(StatisticSampling.measure("E",SAMPLINGS,DEADLINE,SEIRModelDefinition::fractionOfE));
		collection.add(StatisticSampling.measure("I",SAMPLINGS,DEADLINE,SEIRModelDefinition::fractionOfI));
		collection.add(StatisticSampling.measure("R",SAMPLINGS,DEADLINE,SEIRModelDefinition::fractionOfR));
		simulator.simulate(def.createModel(),def.state(),collection,REPLICA,DEADLINE);
		collection.printTimeSeries("data","seir_",".data");
	}


//	PopulationRule rule_S_I = new ReactionRule(
//			"S->I",
//			new Population[] { new Population(S), new Population(I)} ,
//			new Population[] { new Population(I), new Population(I)},
//			s -> s.getOccupancy(S)* PROB_TRANSMISSION*LAMBDA_MEET *(s.getOccupancy(I)/N));
//
//	PopulationRule rule_I_R = new ReactionRule(
//			"I->R",
//			new Population[] { new Population(I) },
//			new Population[] { new Population(R) },
//			s -> s.getOccupancy(I)*LAMBDA_R
//	);
//
//
//	PopulationModel f = new PopulationModel();
//		f.addState("initial", initialState());
//		f.addRule(rule_S_I);
//		f.addRule(rule_I_R);
//
//	StatisticSampling<PopulationState> fsSamp =
//			StatisticSampling.measure("Fraction Infected",
//					SAMPLINGS, DEADLINE,
//					s -> s.getOccupancy(S)/N) ;
//	StatisticSampling<PopulationState> fiSamp =
//			StatisticSampling.measure("Fraction Infected",
//					SAMPLINGS, DEADLINE,
//					s -> s.getOccupancy(I)/N) ;
//	StatisticSampling<PopulationState> frSamp =
//			StatisticSampling.measure("Fraction Recovered",
//					SAMPLINGS, DEADLINE,
//					s -> s.getOccupancy(R)/N) ;
//
////		StatisticSampling<PopulationModel> eSamp = StatisticSampling.measure("#E", SAMPLINGS, DEADLINE, s -> s.getCurrentState().getOccupancy(E)) ;
////		StatisticSampling<PopulationModel> iSamp = StatisticSampling.measure("#I", SAMPLINGS, DEADLINE, s -> s.getCurrentState().getOccupancy(I)) ;
////		StatisticSampling<PopulationModel> rSamp = StatisticSampling.measure("#R", SAMPLINGS, DEADLINE, s -> s.getCurrentState().getOccupancy(R)) ;
//
//	// SimulationEnvironment<PopulationModel,PopulationState> sim = new SimulationEnvironment<>( f );
//	SimulationEnvironment sim = new SimulationEnvironment( ThreadSimulationManager.getFixedThreadSimulationManagerFactory(TASKS) );
//
//	SamplingFunction<PopulationState> sf = new SamplingCollection<>(fsSamp,fiSamp,frSamp);
//
//		sim.simulate(new DefaultRandomGenerator(),f,initialState(),sf,REPLICA,DEADLINE, true);
//
//		fiSamp.printTimeSeries(new PrintStream("data/sir_I_.data"),';');
//		frSamp.printTimeSeries(new PrintStream("data/sir_R_.data"),';');
//		fsSamp.printTimeSeries(new PrintStream("data/sir_S_.data"),';');
//		System.exit(0);

}
