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
package it.unicam.quasylab.sibilla.examples.lio.rb;

import it.unicam.quasylab.sibilla.core.models.lio.*;
import it.unicam.quasylab.sibilla.core.tools.ProbabilityVector;
import it.unicam.quasylab.sibilla.core.tools.glotl.ChachedFunction;
import it.unicam.quasylab.sibilla.core.tools.glotl.GLoTLDiscreteTimeModelChecker;
import it.unicam.quasylab.sibilla.core.tools.glotl.GLoTLStatisticalModelChecker;
import it.unicam.quasylab.sibilla.core.tools.glotl.global.*;
import it.unicam.quasylab.sibilla.core.tools.glotl.local.LocalAlwaysFormula;
import it.unicam.quasylab.sibilla.core.tools.glotl.local.LocalAtomicFormula;
import it.unicam.quasylab.sibilla.core.tools.glotl.local.LocalFormula;
import it.unicam.quasylab.sibilla.core.tools.glotl.local.LocalNextFormula;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.DoublePredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * @author loreti
 *
 */
public class MainQest {

	public static final String R = "R";
	public static final String B = "B";

	public static final int INIT_R = 6;
	public static final int INIT_B = 0;
	public static final int N = INIT_R+INIT_B;

	public static final double meet_probability = 0.1;


	public final static int SAMPLINGS = 120;
	public final static double DEADLINE = 120;
	private static final int REPLICA = 10;
	private static final double EPS = 0.025;
	private static final double EPS2 = 0.2;
	private static final int K_STABLE = 10;
	private static final int K1 = 30;
	private static final int K2 = 20;
	private static final int K3 = 10;
	private static final int K4 = 5;
	private static final int K5 = 5;

	private static final int[] STEPS = {10, 50, 100};

	private static final int[] SCALES = {1, 10, 100, 1000};

	private static final int[] REPLICAS = {100, 500, 1000};

	public static void main(String[] argv)  {
//		runAllChecking();
		runAndPrintRange(100, 100, 30,"phi1_01", MainQest::getPhi1);
		runAndPrintRange(100, 100, 30,"phi2_01", (i, def) -> MainQest.getPhi2(i, 10, def));
		runAndPrintRange(100, 100, 30,"phi3_01", MainQest::getPhi3);
		runAndPrintRange(100, 500, 30,"phi1_01", MainQest::getPhi1);
		runAndPrintRange(100, 500, 30,"phi2_01", (i, def) -> MainQest.getPhi2(i, 10, def));
		runAndPrintRange(100, 500, 30,"phi3_01", MainQest::getPhi3);
		runAndPrintRange(100, 1000, 30,"phi1_01", MainQest::getPhi1);
		runAndPrintRange(100, 1000, 30,"phi2_01", (i, def) -> MainQest.getPhi2(i, 10, def));
		runAndPrintRange(100, 1000, 30,"phi3_01", MainQest::getPhi3);
//		runAndPrintExact(1, "phi1", MainQest::getPhi1, 30);

	}


	private static void runAndPrintExact(int scale, String label, Function<LIOAgentDefinitions,GlobalFormula<LIOAgent, LIOIndividualState>> formulaBuilder, int range) {
		LIOAgentDefinitions def = getAgentDefinition();
		LIOModel model = new LIOModel(def);
		GLoTLDiscreteTimeModelChecker modelChecker = new GLoTLDiscreteTimeModelChecker();
		ChachedFunction<LIOIndividualState, ProbabilityVector<LIOIndividualState>> cachedNext = new ChachedFunction<>(LIOIndividualState::next);
		LIOIndividualState initial = getInitialState(def, scale);
		GlobalFormula<LIOAgent, LIOIndividualState> formula = formulaBuilder.apply(def);
		double values = modelChecker.computeProbability(cachedNext, initial, formula, 0.001);
		//System.out.printf("#SCALE %d STEPS %d REPLICA %d\n\n",scale, range, replica);
		//System.out.println(label+" = ["+(DoubleStream.of(values).boxed().map(Object::toString).collect(Collectors.joining(",")))+"]");
	}

	private static void runAndPrintRange(int scale, int replica, int size, String label, BiFunction<Integer, LIOAgentDefinitions,GlobalFormula<LIOAgent, LIOIndividualState>> formulaBuilder) {
		LIOAgentDefinitions def = getAgentDefinition();
		LIOModel model = new LIOModel(def);
		GLoTLStatisticalModelChecker modelChecker = new GLoTLStatisticalModelChecker();
		LIOIndividualState initial = getInitialState(def, scale);
		long start = System.currentTimeMillis();
		double[] values = modelChecker.computeProbability(model.nextIndividuals(), initial, i -> formulaBuilder.apply(i, def), size, replica);
		long end = System.currentTimeMillis();
		//double[] values = modelChecker.computeProbability(model, initial, i -> formulaBuilder.apply(i, def), size, replica);
		System.out.printf("#SCALE %d STEPS %d REPLICA %d TIME %f\n\n",scale, size, replica, (end-start)/1000.0);
		System.out.println(label+"_"+scale+"_"+replica+" = ["+(DoubleStream.of(values).boxed().map(Object::toString).collect(Collectors.joining(",")))+"]");
	}

	private static void runAndPrintRangeExact(int scale, int replica, int size, String label, BiFunction<Integer, LIOAgentDefinitions,GlobalFormula<LIOAgent, LIOIndividualState>> formulaBuilder) {
		LIOAgentDefinitions def = getAgentDefinition();
		LIOModel model = new LIOModel(def);
		GLoTLStatisticalModelChecker modelChecker = new GLoTLStatisticalModelChecker();
		LIOIndividualState initial = getInitialState(def, scale);
		double[] values = modelChecker.computeProbability(model.nextIndividuals(), initial, i -> formulaBuilder.apply(i, def), size, replica);
		//double[] values = modelChecker.computeProbability(model, initial, i -> formulaBuilder.apply(i, def), size, replica);
		System.out.printf("#SCALE %d STEPS %d REPLICA %d\n\n",scale, size, replica);
		System.out.println(label+"_"+scale+"_"+replica+" = ["+(DoubleStream.of(values).boxed().map(Object::toString).collect(Collectors.joining(",")))+"]");
	}

	private static void runAllChecking( ) {
		StringBuilder output = new StringBuilder();
		LIOAgentDefinitions def = getAgentDefinition();
		LIOModel model = new LIOModel(def);
		GLoTLStatisticalModelChecker modelChecker = new GLoTLStatisticalModelChecker();

		for( int scale: SCALES) {
			Map<String, GlobalFormula<LIOAgent, LIOIndividualState>> map = getFormulas(def);
			LIOIndividualState initial = getInitialState(def, scale);
			for( int replica: REPLICAS) {
				for (Map.Entry<String, GlobalFormula<LIOAgent, LIOIndividualState>> e:map.entrySet()) {
					String name = e.getKey();
					GlobalFormula<LIOAgent, LIOIndividualState> formula = e.getValue();
					System.out.printf("Checking %s: scale=%d replica=%d\n",name,scale, replica);
					long start = System.currentTimeMillis();
					double p = modelChecker.computeProbability(model.nextIndividuals(), initial, formula, replica);
					long elapsed = System.currentTimeMillis()-start;
					String dataLine = String.format("%s %d %d %f, %f",name,scale, replica,p, (elapsed/1000.0));
					System.out.println(dataLine);
					output.append(dataLine).append("\n");
				}
			}
		}
		System.out.println("REPORT:");
		System.out.println(output);

	}

	private static Map<String, GlobalFormula<LIOAgent, LIOIndividualState>> getFormulas(LIOAgentDefinitions def) {
		Map<String, GlobalFormula<LIOAgent, LIOIndividualState>> map = new HashMap<>();
		map.put("\\phi_{bal}",getPhiBal(def));
		map.put("\\phi_{1}",getPhi1(def));
		map.put("\\phi_{2}",getPhi2(def));
		map.put("\\phi_{3}",getPhi3(def));
		//map.put("\\phi_{4}",getPhi4(def));
		return map;
	}

	private static LIOIndividualState getInitialState(LIOAgentDefinitions def, int scale) {
		String[] agents = IntStream.range(0, N*scale).boxed().map(i -> getAgent(def,scale,i)).toArray(String[]::new);
		return new LIOIndividualState(def,agents);
	}

	private static String getAgent(LIOAgentDefinitions def, int scale, Integer i) {
		if (i<=INIT_B*scale) { return B; }
		return R;
	}

	public static LIOAgentDefinitions getAgentDefinition() {
		LIOAgentDefinitions def = new LIOAgentDefinitions();
		LIOAgent agentR = def.addAgent("R");
		LIOAgent agentB = def.addAgent("B");
		LIOAgentAction redAction = def.addAction( "red", s -> s.fractionOf(agentB)*meet_probability );
		LIOAgentAction blueAction = def.addAction( "blue" , s -> s.fractionOf(agentR)*meet_probability );
		agentR.addAction(blueAction, agentB);
		agentB.addAction(redAction, agentR);
		return def;
	}

	public static LocalFormula<LIOAgent> balancedLocalFormula(LIOAgentDefinitions def) {
		LIOAgent agentB = def.getAgent("B");
		return new LocalAtomicFormula<>(agentB::equals);
	}

	public static LocalFormula<LIOAgent> hasChanged(LIOAgent a1, LIOAgent a2) {
		return LocalFormula.imply(new LocalAtomicFormula<>(a1::equals),new LocalNextFormula<>(new LocalAtomicFormula<>(a2::equals)));
	}

	public static LocalFormula<LIOAgent> hasChanged(LIOAgentDefinitions def) {
		LIOAgent agentB = def.getAgent("B");
		LIOAgent agentR = def.getAgent("R");
		return LocalFormula.disjunction(hasChanged(agentB,agentR),hasChanged(agentR,agentB));
	}

	public static LocalFormula<LIOAgent> phiStable(LIOAgent a1, LIOAgent a2) {
		return LocalFormula.imply(new LocalAtomicFormula<>(a1::equals),
					new LocalNextFormula<>(
						LocalFormula.imply(new LocalAtomicFormula<>(a2::equals),
									new LocalAlwaysFormula<>(0, K_STABLE, new LocalAtomicFormula<>(a2::equals))
								)
					)
				);
	}

	public static LocalFormula<LIOAgent> phiStable(LIOAgentDefinitions def) {
		LIOAgent agentB = def.getAgent("B");
		LIOAgent agentR = def.getAgent("R");
		return LocalFormula.conjunction(
			phiStable(agentB, agentR),
			phiStable(agentR, agentB)
		);
	}


	public static GlobalFormula<LIOAgent, LIOIndividualState> getPhiBal(LIOAgentDefinitions def) {
		DoublePredicate dPred = d -> (d>=0.5-EPS)&&(d<=0.5+EPS);
		return new GlobalFractionOfFormula<>(balancedLocalFormula(def), dPred);
	}

	public static GlobalFormula<LIOAgent, LIOIndividualState> getPhi1(LIOAgentDefinitions def) {
		return getPhi1(K1, def);
	}

	public static GlobalFormula<LIOAgent, LIOIndividualState> getPhi1(int k, LIOAgentDefinitions def) {
		return new GlobalEventuallyFormula<>(0, k, getPhiBal(def));
	}


	public static GlobalFormula<LIOAgent, LIOIndividualState> getPhi2(LIOAgentDefinitions def) {
		return getPhi2(K1, K2, def);
	}

	private static GlobalFormula<LIOAgent, LIOIndividualState> getPhi2(int k1, int k2, LIOAgentDefinitions def) {
		return new GlobalEventuallyFormula<>(0, k1, new GlobalAlwaysFormula<>(0, k2, getPhiBal(def)));
	}

	public static GlobalFormula<LIOAgent, LIOIndividualState> getPhi3(LIOAgentDefinitions def) {
		return getPhi3(K4, def);
	}

	private static GlobalFormula<LIOAgent, LIOIndividualState> getPhi3(int k4, LIOAgentDefinitions def) {
		return new GlobalAlwaysFormula<>(0, k4,
				GlobalFormula.imply(getPhiBal(def),
						new GlobalFractionOfFormula<>(
								phiStable(def),
								d -> (d >= 0.90))
				));
	}

	private static GlobalFormula<LIOAgent, LIOIndividualState> localStableAfter(int k4, LIOAgentDefinitions def) {
		return new GlobalAferFormula<>(k4,
				new GlobalFractionOfFormula<>(
								phiStable(def),
								d -> (d >= 0.90))
				);
	}


}
