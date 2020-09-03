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
package quasylab.sibilla.core.simulator.tests.pm;

import quasylab.sibilla.core.models.pm.*;
import quasylab.sibilla.core.simulator.SimulationEnvironment;
import quasylab.sibilla.core.simulator.sampling.Measure;
import quasylab.sibilla.core.simulator.sampling.SamplingCollection;
import quasylab.sibilla.core.simulator.sampling.SamplingFunction;
import quasylab.sibilla.core.simulator.sampling.StatisticSampling;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.function.Function;

/**
 * @author loreti
 *
 */
public class GossipUnicast {
	
	private static final int SIM_TIME = 20;
	
	private static final int SAMPLINGS = 100;

	private static final double DIFF_RATE = 5.0;
	private static final double PASS_RATE = 1.0;
	private static final double PROB = 1.0;
	private static final double LOST_PROB = 0.75;
	private static final double CHANGE_PROB = 1.1;
	private static final double K = 5.0;
	private static final double DEADLINE = 100;
	private static final int ITERATIONS = 1;

	private static final int PS_INIT_SIZE = 0;
	private static final int PI_INIT_SIZE = 20;
	private static final int AS_INIT_SIZE = 0;
	private static final int AI_INIT_SIZE = 0;
	private static final int PU_INIT_SIZE = 80;
	private static final int AU_INIT_SIZE = 0;
	
	public static final int SIZE = 100;
	
	public static final int PI_INDEX = 0;
	public static final int PS_INDEX = 1;
	public static final int AI_INDEX = 2;
	public static final int AS_INDEX = 3;
	public static final int AU_INDEX = 4;
	public static final int PU_INDEX = 5;
	
	public static final double P_RATE = 0.1;
	public static final double C_RATE = 1.0;
	
//	public static final double REC_PROB = 0.2;
	public static final double REC_PROB = 0.2;	

	

	public static void main(String[] argv) throws InterruptedException, FileNotFoundException {
		GossipUnicast model = new GossipUnicast(DIFF_RATE,PASS_RATE);
		model.run(1, 1, DEADLINE, SAMPLINGS, "/Users/loreti/tmp/testdata/");
		model.run(10, 1, DEADLINE, SAMPLINGS, "/Users/loreti/tmp/testdata/");
		model.run(100, 1, DEADLINE, SAMPLINGS, "/Users/loreti/tmp/testdata/");
		model.run(1000, 1, DEADLINE, SAMPLINGS, "/Users/loreti/tmp/testdata/");
		System.exit(1);
	}

	private double p_d;

	private double lambda_s;

	private double lambda_a;

	private double p_channge;
	
	public GossipUnicast(
			double lambda_s,
			double lambda_a
			) {
		this.lambda_s = lambda_s;
		this.lambda_a = lambda_a;
	}
	
	private LinkedList<PopulationRule> buildRules() {
		LinkedList<PopulationRule> rules = new LinkedList<>();
		
		rules.add(
				new UnicastRule(//AI: shuffle_00^ii interaction with PI
					"s_ii" ,	
					(PopulationState s) ->
						this.lambda_s
						*(s.getOccupancy(PI_INDEX)/((s.getOccupancy(PI_INDEX)+s.getOccupancy(PS_INDEX)+s.getOccupancy(PU_INDEX)))),
					AI_INDEX ,
					rgi -> PI_INDEX , 
					new UnicastRule.UnicastReceiver(PI_INDEX, s -> 1.0, rgi -> PI_INDEX)
				)							
			);

			rules.add(
					new UnicastRule(//AI: 0-0 interaction with PI
						"s_iu" ,	
						(PopulationState s) -> 
							this.lambda_s*
								((s.getOccupancy(PS_INDEX)+s.getOccupancy(PU_INDEX))/((s.getOccupancy(PI_INDEX)+s.getOccupancy(PS_INDEX)+s.getOccupancy(PU_INDEX)))),
						AI_INDEX ,
						rgi -> PS_INDEX , 
						new UnicastRule.UnicastReceiver(PU_INDEX, s -> 1.0, rgi -> PI_INDEX),
						new UnicastRule.UnicastReceiver(PS_INDEX, s -> 1.0, rgi -> PI_INDEX)
					)							
				);



			rules.add(
					new UnicastRule(//AI: 0-0 interaction with PI
						"s_ui" ,	
						(PopulationState s) -> 
							this.lambda_s*
								(s.getOccupancy(PI_INDEX)/((s.getOccupancy(PI_INDEX)+s.getOccupancy(PS_INDEX)+s.getOccupancy(PU_INDEX)))),
						AU_INDEX ,
						rgi -> PI_INDEX , 
						new UnicastRule.UnicastReceiver(PI_INDEX, s -> 1.0, rgi -> PS_INDEX)
					)							
				);

			rules.add(
					new UnicastRule(//AI: 0-0 interaction with PI
						"s_uu" ,	
						(PopulationState s) -> 
							this.lambda_s*
								((s.getOccupancy(PS_INDEX)+s.getOccupancy(PU_INDEX))/((s.getOccupancy(PI_INDEX)+s.getOccupancy(PS_INDEX)+s.getOccupancy(PU_INDEX)))),
						AU_INDEX ,
						rgi -> PS_INDEX , 
						new UnicastRule.UnicastReceiver(PS_INDEX, s -> 1.0, rgi -> PS_INDEX),
						new UnicastRule.UnicastReceiver(PU_INDEX, s -> 1.0, rgi -> PU_INDEX)
					)							
				);
		
			rules.add(
					new UnicastRule(//AI: 0-0 interaction with PI
						"s_ui" ,	
						(PopulationState s) -> 
							this.lambda_s*
								(s.getOccupancy(PI_INDEX)/((s.getOccupancy(PI_INDEX)+s.getOccupancy(PS_INDEX)+s.getOccupancy(PU_INDEX)))),
						AS_INDEX ,
						rgi -> PS_INDEX , 
						new UnicastRule.UnicastReceiver(PI_INDEX, s -> 1.0, rgi -> PS_INDEX)
					)							
				);

			rules.add(
					new UnicastRule(//AI: 0-0 interaction with PI
						"s_uu" ,	
						(PopulationState s) -> 
							this.lambda_s*
								((s.getOccupancy(PS_INDEX)+s.getOccupancy(PU_INDEX))/((s.getOccupancy(PI_INDEX)+s.getOccupancy(PS_INDEX)+s.getOccupancy(PU_INDEX)))),
						AU_INDEX ,
						rgi -> PU_INDEX , 
						new UnicastRule.UnicastReceiver(PU_INDEX, s -> 1.0, rgi -> PU_INDEX),
						new UnicastRule.UnicastReceiver(PS_INDEX, s -> 1.0, rgi -> PS_INDEX)
					)							
				);


			
			// END AX_RULES

			rules.add( 
					new BroadcastRule(
						"PU_beact*", 
						s -> this.lambda_a, 
						PS_INDEX, 
						rg -> AS_INDEX
					)
				);

			rules.add( 
					new BroadcastRule(
						"PI_beact*", 
						s -> this.lambda_a, 
						PI_INDEX, 
						rg -> AI_INDEX
					)
				);

			rules.add( 
					new BroadcastRule(
						"PX_beact*", 
						s -> this.lambda_a, 
						PU_INDEX, 
						rg -> AU_INDEX
					)
				);

		return rules;
	}	
	

	
	public void run(
			int scale ,
			int iterations, 
			double deadline,
			int samplings,
			String outputDir
	) throws FileNotFoundException, InterruptedException {
		String label = outputDir+"u_"+scale+"_";
		SimulationEnvironment sim = new SimulationEnvironment();
		StatisticSampling<PopulationState> aiSamp = getMeasure(samplings,deadline,"AI",s -> s.getOccupancy(AI_INDEX));
		StatisticSampling<PopulationState> piSamp = getMeasure(samplings,deadline,"PI",s -> s.getOccupancy(PI_INDEX));
		StatisticSampling<PopulationState> auSamp = getMeasure(samplings,deadline,"AU",s -> s.getOccupancy(AU_INDEX));
		StatisticSampling<PopulationState> puSamp = getMeasure(samplings,deadline,"PU",s -> s.getOccupancy(PU_INDEX));
		StatisticSampling<PopulationState> asSamp = getMeasure(samplings,deadline,"AS",s -> s.getOccupancy(AS_INDEX));
		StatisticSampling<PopulationState> psSamp = getMeasure(samplings,deadline,"PS",s -> s.getOccupancy(PS_INDEX));

		long start = System.currentTimeMillis();
		SamplingFunction<PopulationState> sf = new SamplingCollection<>(aiSamp, piSamp, auSamp, puSamp,asSamp,psSamp);
	//	sim.simulate(new DefaultRandomGenerator(),buildPopulationModel(scale),getInitState(scale),sf,iterations,deadline);
		
		System.out.println("Time: "+(System.currentTimeMillis()-start));
//		aiSamp.printTimeSeries(new PrintStream(label+"_ai_.data"));
//		piSamp.printTimeSeries(new PrintStream(label+"_pi_.data"));
//		auSamp.printTimeSeries(new PrintStream(label+"_au_.data"));
//		puSamp.printTimeSeries(new PrintStream(label+"_pu_.data"));
//		asSamp.printTimeSeries(new PrintStream(label+"_as_.data"));
//		psSamp.printTimeSeries(new PrintStream(label+"_ps_.data"));
		sf.printTimeSeries(outputDir,"u_"+scale+"_","_.data");

	}	
	
	private static StatisticSampling<PopulationState> getMeasure(int samplings, double deadline, String name, Function<PopulationState,Double> m) {
		return new StatisticSampling<PopulationState>(samplings, deadline/samplings, 
				new Measure<PopulationState>() {

			@Override
			public double measure(PopulationState t) {
				// TODO Auto-generated method stub
				return m.apply( t );
			}

			@Override
			public String getName() {
				return name;
			}

		});
	}
	
	private PopulationModel buildPopulationModel(int scale) {
		PopulationModel m = new PopulationModel(6);
		m.addRules( buildRules() );
		return m;
	}
	
	
	private PopulationState getInitState(int scale) {
		return new PopulationState( new int[] { PI_INIT_SIZE*scale, PS_INIT_SIZE*scale, AI_INIT_SIZE*scale, AS_INIT_SIZE*scale, AU_INIT_SIZE*scale, PU_INIT_SIZE*scale });
	}
	
}
