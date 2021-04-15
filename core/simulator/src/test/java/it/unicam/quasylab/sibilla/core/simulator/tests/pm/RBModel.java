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

/**
 * 
 */
package it.unicam.quasylab.sibilla.core.simulator.tests.pm;

import it.unicam.quasylab.sibilla.core.models.pm.*;
import it.unicam.quasylab.sibilla.core.models.pm.UnicastRule.UnicastReceiver;
import it.unicam.quasylab.sibilla.core.models.pm.util.PopulationRegistry;
import it.unicam.quasylab.sibilla.core.simulator.SimulationEnvironment;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import it.unicam.quasylab.sibilla.core.simulator.sampling.SamplingCollection;
import it.unicam.quasylab.sibilla.core.simulator.sampling.SamplingFunction;
import it.unicam.quasylab.sibilla.core.simulator.sampling.StatisticSampling;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.function.Function;

/**
 * @author loreti
 *
 */
public class RBModel {

	public static final int SIZE = 100;

	public static final int R_INDEX = 0;
	public static final int B_INDEX = 1;
	public static final int RT_INDEX = 2;
	public static final int BT_INDEX = 3;

	public static final double SPREAD_RATE = 0.1;
	public static final double CHANGE_RATE = 1.0;

	private static final int R_INIT_SIZE = 97;
	private static final int B_INIT_SIZE = 1;
	private static final int CR_INIT_SIZE = 1;
	private static final int CB_INIT_SIZE = 1;

	private static final int SAMPLINGS = 100;

	private static final double DEADLINE = 10.0;
	private static final int ITERATIONS = 1;

	public static final double K = 10;

	private final double stay_prob;

	private final double change_prob;

	private final double lambda_s;

	private final double lambda_c;

	private final double k;

	public static void main(String[] argv) throws InterruptedException, FileNotFoundException {
		RBModel model = new RBModel(10, 1, 1, 0.5, 0.5);
		model.run(1, 1, DEADLINE, SAMPLINGS, "/Users/loreti/tmp/testdata/");
		model.run(10, 1, DEADLINE, SAMPLINGS, "/Users/loreti/tmp/testdata/");
		model.run(100, 1, DEADLINE, SAMPLINGS, "/Users/loreti/tmp/testdata/");
		model.run(1000, 1, DEADLINE, SAMPLINGS, "/Users/loreti/tmp/testdata/");
	}

	public RBModel(int k, double lambda_s, double lambda_c, double change_prob, double stay_prob) {
		this.k = k;
		this.lambda_s = lambda_s;
		this.lambda_c = lambda_c;
		this.change_prob = change_prob;
		this.stay_prob = stay_prob;
	}

	private LinkedList<PopulationRule> buildRules() {
		LinkedList<PopulationRule> rules = new LinkedList<>();

		// R START
		rules.add(new BroadcastRule("red*", s -> lambda_s, R_INDEX, rg -> R_INDEX,
				new BroadcastRule.BroadcastReceiver(R_INDEX, s -> broadcastRed(R_INDEX, s),
						rg -> rg.nextDouble() < change_prob ? RT_INDEX : R_INDEX),
				new BroadcastRule.BroadcastReceiver(BT_INDEX, s -> broadcastRed(BT_INDEX, s),
						rg -> rg.nextDouble() < stay_prob ? B_INDEX : BT_INDEX)));
		// R END

		// B START
		rules.add(new BroadcastRule("blue*", s -> lambda_s, B_INDEX, rg -> B_INDEX,
				new BroadcastRule.BroadcastReceiver(B_INDEX, s -> broadcastBlue(B_INDEX, s),
						rg -> rg.nextDouble() < change_prob ? BT_INDEX : B_INDEX),
				new BroadcastRule.BroadcastReceiver(RT_INDEX, s -> broadcastBlue(RT_INDEX, s),
						rg -> rg.nextDouble() < stay_prob ? R_INDEX : RT_INDEX)));
		// B END

		// CR START
		rules.add(new UnicastRule("changeB", s -> lambda_c, RT_INDEX, rg -> R_INDEX,
				new UnicastReceiver(R_INDEX, s -> 1.0, rg -> B_INDEX),
				new UnicastReceiver(RT_INDEX, s -> 1.0, rg -> B_INDEX)));

		rules.add(new BroadcastRule("red*", s -> lambda_s, RT_INDEX, rg -> RT_INDEX,
				new BroadcastRule.BroadcastReceiver(R_INDEX, s -> broadcastRed(R_INDEX, s),
						rg -> rg.nextDouble() < change_prob ? RT_INDEX : R_INDEX),
				new BroadcastRule.BroadcastReceiver(BT_INDEX, s -> broadcastRed(BT_INDEX, s),
						rg -> rg.nextDouble() < stay_prob ? B_INDEX : BT_INDEX)));
		// CR END

		// CB START
		rules.add(new UnicastRule("changeR", s -> lambda_c, BT_INDEX, rg -> B_INDEX,
				new UnicastReceiver(B_INDEX, s -> 1.0, rg -> R_INDEX),
				new UnicastReceiver(BT_INDEX, s -> 1.0, rg -> R_INDEX)));

		rules.add(new BroadcastRule("blue*", s -> lambda_s, BT_INDEX, rg -> BT_INDEX,
				new BroadcastRule.BroadcastReceiver(B_INDEX, s -> broadcastBlue(B_INDEX, s),
						rg -> rg.nextDouble() < change_prob ? BT_INDEX : B_INDEX),
				new BroadcastRule.BroadcastReceiver(RT_INDEX, s -> broadcastBlue(RT_INDEX, s),
						rg -> rg.nextDouble() < stay_prob ? R_INDEX : RT_INDEX)));
		// CB END

		return rules;
	}

	private double broadcastRed(int idx, PopulationState s) {
		double pop = s.getOccupancy(idx);
		if (pop == 0.0) {
			pop = 1.0;
		}
		double tot = s.getOccupancy(R_INDEX) + s.getOccupancy(BT_INDEX);
		// return pop*k/(tot*tot);
		// return Math.min(1.0, (k*pop)/(tot*tot));
		return Math.min(1.0, k / tot);
	}

	private double broadcastBlue(int idx, PopulationState s) {
		double pop = s.getOccupancy(idx);
		if (pop == 0.0) {
			pop = 1.0;
		}
		double tot = s.getOccupancy(B_INDEX) + s.getOccupancy(RT_INDEX);
		// return pop*k/(tot*tot);
		// return Math.min(1.0, (k*pop)/(tot*tot));
		return Math.min(1.0, k / tot);
	}

	public void run(int scale, int iterations, double deadline, int samplings, String outputDir)
			throws FileNotFoundException, InterruptedException {
		String label = outputDir + "rb_" + scale + "_";
		SimulationEnvironment sim = new SimulationEnvironment();
		StatisticSampling<PopulationState> rSamp = getMeasure(samplings, deadline, "R", s -> s.getOccupancy(R_INDEX));
		StatisticSampling<PopulationState> bSamp = getMeasure(samplings, deadline, "B", s -> s.getOccupancy(B_INDEX));
		StatisticSampling<PopulationState> btSamp = getMeasure(samplings, deadline, "BT",
				s -> s.getOccupancy(BT_INDEX));
		StatisticSampling<PopulationState> rtSamp = getMeasure(samplings, deadline, "RT",
				s -> s.getOccupancy(RT_INDEX));
		StatisticSampling<PopulationState> red = getMeasure(samplings, deadline, "RED",
				s -> s.getOccupancy(R_INDEX) + s.getOccupancy(RT_INDEX));
		StatisticSampling<PopulationState> blue = getMeasure(samplings, deadline, "BLUE",
				s -> s.getOccupancy(B_INDEX) + s.getOccupancy(BT_INDEX));

		long start = System.currentTimeMillis();
		SamplingFunction<PopulationState> sf = new SamplingCollection<>(rSamp, bSamp, btSamp, rtSamp, red, blue);
		// sim.simulate(new
		// DefaultRandomGenerator(),buildPopulationModel(scale),getInitState(scale),sf,iterations,deadline);

		System.out.println("Time: " + (System.currentTimeMillis() - start));
		sf.printTimeSeries(outputDir, "rb_" + scale + "_", "_.data");
	}

	private static StatisticSampling<PopulationState> getMeasure(int samplings, double deadline, String name,
			Function<PopulationState, Double> m) {
		return new StatisticSampling<PopulationState>(samplings, deadline / samplings, new Measure<PopulationState>() {

			@Override
			public double measure(PopulationState t) {
				// TODO Auto-generated method stub
				return m.apply(t);
			}

			@Override
			public String getName() {
				return name;
			}

		});
	}

	private PopulationModel buildPopulationModel(int scale) {
//		PopulationModel m = new PopulationModel(4);
//		m.addRules(buildRules());
		return new PopulationModel(PopulationRegistry.createRegistry(4),buildRules(),new HashMap<>());
	}

	private PopulationState getInitState(int scale) {
		int[] result = new int[4];
		result[R_INDEX] = scale * R_INIT_SIZE;
		result[B_INDEX] = scale * B_INIT_SIZE;
		result[BT_INDEX] = scale * CR_INIT_SIZE;
		result[RT_INDEX] = scale * CB_INIT_SIZE;
		return new PopulationState(result);
	}

}
