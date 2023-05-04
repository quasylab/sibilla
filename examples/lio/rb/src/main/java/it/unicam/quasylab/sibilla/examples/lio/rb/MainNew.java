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
import it.unicam.quasylab.sibilla.core.tools.glotl.GLoTLStatisticalModelChecker;
import it.unicam.quasylab.sibilla.core.tools.glotl.global.GlobalAlwaysFormula;
import it.unicam.quasylab.sibilla.core.tools.glotl.global.GlobalEventuallyFormula;
import it.unicam.quasylab.sibilla.core.tools.glotl.global.GlobalFormula;
import it.unicam.quasylab.sibilla.core.tools.glotl.global.GlobalFractionOfFormula;
import it.unicam.quasylab.sibilla.core.tools.glotl.local.LocalAlwaysFormula;
import it.unicam.quasylab.sibilla.core.tools.glotl.local.LocalAtomicFormula;
import it.unicam.quasylab.sibilla.core.tools.glotl.local.LocalFormula;
import it.unicam.quasylab.sibilla.core.tools.glotl.local.LocalNextFormula;

import java.util.HashMap;
import java.util.Map;
import java.util.function.DoublePredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * @author loreti
 *
 */
public class MainNew {

	public static final String R = "R";
	public static final String B = "B";

	public static final int INIT_R = 10;
	public static final int INIT_B = 0;
	public static final int N = INIT_R+INIT_B;

	public static final double meet_probability = 0.5;


	public final static int SAMPLINGS = 120;
	public final static double DEADLINE = 120;
	private static final int REPLICA = 10;
	private static final double EPS = 0.1;
	private static final double EPS2 = 0.2;
	private static final int K_STABLE = 10;
	private static final int K1 = 10;
	private static final int K3 = 10;
	private static final int K4 = 5;
	private static final int K5 = 5;

	private static final int[] STEPS = {10, 50, 100};

	private static final int[] SCALES = {1, 10, 100, 1000};

	private static final int[] REPLICAS = {100, 500, 1000};

	public static void main(String[] argv)  {
		//runAllChecking();
		//runAndPrint(1, 31, 100,"phi3_100",Main::getPhi3);
		//runAndPrint(1, 31, 1000,"phi3_1000",Main::getPhi3);
		//runAndPrint(1, 31, 10000,"phi3_10000",Main::getPhi3);
		runAndPrint(100, 100, "phibal_100",MainNew::getPhiBal, 100);
		runAndPrint(100, 100, "phi4_100",MainNew::getPhi4, 100);
		//runAndPrint(1, 31, 1000,"phibal_1000",Main::getPhiBal);
		//runAndPrint(100, 31, 100,"phiBal_025_100",Main::getPhiBal);
		//runAndPrint(100, 31, 1000,"phiBal_025_1000",Main::getPhiBal);
		//runAndPrint(100, 31, 10000,"phiBal_025_10000",Main::getPhiBal);
//		runAndPrint(1, 31, 100,"phi1_100",Main::getPhi1);
//		runAndPrint(1, 31, 1000,"phi1_1000",Main::getPhi1);
//		runAndPrint(1, 31, 10000,"phi1_10000",Main::getPhi1);
//		runAndPrint(1, 31, 100,"phi2_100",Main::getPhi2);
//		runAndPrint(1, 31, 1000,"phi2_1000",Main::getPhi2);
//		runAndPrint(1, 31, 10000,"phi2_10000",Main::getPhi2);
//		runAndPrint(1, 31, 1000,"phi4_05_1000",Main::getPhi4);
//		runAndPrint(1, 31, 10000,"phi4_05_10000",Main::getPhi4);
	}


	private static void runAndPrintExact(int scale, int replica, String label, Function<AgentsDefinition,GlobalFormula<Agent, LIOIndividualState>> formulaBuilder, int range) {
		AgentsDefinition def = getAgentDefinition();
		LIOModel<LIOIndividualState> model = new LIOModel<>(def);
		GLoTLStatisticalModelChecker modelChecker = new GLoTLStatisticalModelChecker();
		LIOIndividualState initial = getInitialState(def, scale);
		GlobalFormula<Agent, LIOIndividualState> formula = formulaBuilder.apply(def);
		double[] values = modelChecker.computeProbability(model, initial, formula, 0, 50, replica);
		System.out.printf("#SCALE %d STEPS %d REPLICA %d\n\n",scale, range, replica);
		System.out.println(label+" = ["+(DoubleStream.of(values).boxed().map(Object::toString).collect(Collectors.joining(",")))+"]");
	}

	private static void runAndPrint(int scale, int replica, String label, Function<AgentsDefinition,GlobalFormula<Agent, LIOIndividualState>> formulaBuilder, int range) {
		AgentsDefinition def = getAgentDefinition();
		LIOModel<LIOIndividualState> model = new LIOModel<>(def);
		GLoTLStatisticalModelChecker modelChecker = new GLoTLStatisticalModelChecker();
		LIOIndividualState initial = getInitialState(def, scale);
		GlobalFormula<Agent, LIOIndividualState> formula = formulaBuilder.apply(def);
		double[] values = modelChecker.computeProbability(model, initial, formula, 0, 50, replica);
		System.out.printf("#SCALE %d STEPS %d REPLICA %d\n\n",scale, range, replica);
		System.out.println(label+" = ["+(DoubleStream.of(values).boxed().map(Object::toString).collect(Collectors.joining(",")))+"]");
	}


	private static void runAllChecking( ) {
		StringBuilder output = new StringBuilder();
		AgentsDefinition def = getAgentDefinition();
		LIOModel<LIOIndividualState> model = new LIOModel<>(def);
		GLoTLStatisticalModelChecker modelChecker = new GLoTLStatisticalModelChecker();

		for( int scale: SCALES) {
			Map<String, GlobalFormula<Agent, LIOIndividualState>> map = getFormulas(def);
			LIOIndividualState initial = getInitialState(def, scale);
			for( int replica: REPLICAS) {
				for (Map.Entry<String, GlobalFormula<Agent, LIOIndividualState>> e:map.entrySet()) {
					String name = e.getKey();
					GlobalFormula<Agent, LIOIndividualState> formula = e.getValue();
					System.out.printf("Checking %s: scale=%d replica=%d\n",name,scale, replica);
					long start = System.currentTimeMillis();
					double p = modelChecker.computeProbability(model, initial, formula, replica);
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

	private static Map<String, GlobalFormula<Agent, LIOIndividualState>> getFormulas(AgentsDefinition def) {
		Map<String, GlobalFormula<Agent, LIOIndividualState>> map = new HashMap<>();
		map.put("\\phi_{bal}",getPhiBal(def));
		map.put("\\phi_{1}",getPhi1(def));
		map.put("\\phi_{2}",getPhi2(def));
		map.put("\\phi_{3}",getPhi3(def));
		map.put("\\phi_{4}",getPhi4(def));
		return map;
	}

	private static LIOIndividualState getInitialState(AgentsDefinition def, int scale) {
		String[] agents = IntStream.range(0, N*scale).boxed().map(i -> getAgent(def,scale,i)).toArray(String[]::new);
		return new LIOIndividualState(def,agents);
	}

	private static String getAgent(AgentsDefinition def, int scale, Integer i) {
		if (i<=INIT_B*scale) { return B; }
		return R;
	}

	public static AgentsDefinition getAgentDefinition() {
		String[] agents = new String[] { "R", "B" };
		String[] actions = new String[] { "red", "blue" };
		AgentsDefinition def = new AgentsDefinition(agents, actions);
		Agent agentR = def.getAgent("R");
		Agent agentB = def.getAgent("B");
		AgentAction redAction = def.setActionProbability( "red", s -> s.fractionOf(agentB)*meet_probability );
		AgentAction blueAction = def.setActionProbability( "blue" , s -> s.fractionOf(agentR)*meet_probability );
		agentR.addAction(blueAction, agentB);
		agentB.addAction(redAction, agentR);
		return def;
	}

	public static LocalFormula<Agent> balancedLocalFormula(AgentsDefinition def) {
		Agent agentB = def.getAgent("B");
		return new LocalAtomicFormula<>(agentB::equals);
	}

	public static LocalFormula<Agent> hasChanged(Agent a1, Agent a2) {
		return LocalFormula.imply(new LocalAtomicFormula<>(a1::equals),new LocalNextFormula<>(new LocalAtomicFormula<>(a2::equals)));
	}

	public static LocalFormula<Agent> hasChanged(AgentsDefinition def) {
		Agent agentB = def.getAgent("B");
		Agent agentR = def.getAgent("R");
		return LocalFormula.disjunction(hasChanged(agentB,agentR),hasChanged(agentR,agentB));
	}

	public static LocalFormula<Agent> phiStable(Agent a1, Agent a2) {
		return LocalFormula.imply(new LocalAtomicFormula<>(a1::equals),
					new LocalNextFormula<>(
						LocalFormula.imply(new LocalAtomicFormula<>(a2::equals),
									new LocalAlwaysFormula<>(0, K_STABLE, new LocalAtomicFormula<>(a2::equals))
								)
					)
				);
	}

	public static LocalFormula<Agent> phiStable(AgentsDefinition def) {
		Agent agentB = def.getAgent("B");
		Agent agentR = def.getAgent("R");
		return LocalFormula.conjunction(
			phiStable(agentB, agentR),
			phiStable(agentR, agentB)
		);
	}


	public static GlobalFormula<Agent, LIOIndividualState> getPhiBal(AgentsDefinition def) {
		DoublePredicate dPred = d -> (d>=0.5-EPS)&&(d<=0.5+EPS);
		return new GlobalFractionOfFormula<>(balancedLocalFormula(def), dPred);
	}

	public static GlobalFormula<Agent, LIOIndividualState> getPhi1( AgentsDefinition def) {
		return new GlobalEventuallyFormula<>(0, K1, getPhiBal(def));
	}

	public static GlobalFormula<Agent, LIOIndividualState> getPhi2(AgentsDefinition def) {
		GlobalFormula<Agent, LIOIndividualState> phiBal = getPhiBal(def);
		return GlobalFormula.imply(GlobalFormula.negation(phiBal),new GlobalEventuallyFormula<>(0, K3,new GlobalAlwaysFormula<>(0, K4,phiBal)));
	}

	public static GlobalFormula<Agent, LIOIndividualState> getPhi3(AgentsDefinition def) {
		GlobalFormula<Agent, LIOIndividualState> phiBal = getPhiBal(def);
		return GlobalFormula.imply(phiBal,new GlobalFractionOfFormula<>(hasChanged(def),d -> d<EPS2));
	}

	public static GlobalFormula<Agent, LIOIndividualState> getPhi4(AgentsDefinition def) {
		return new GlobalFractionOfFormula<>(
				phiStable(def),
				d -> (d >= 0.95)
		);
	}



}
