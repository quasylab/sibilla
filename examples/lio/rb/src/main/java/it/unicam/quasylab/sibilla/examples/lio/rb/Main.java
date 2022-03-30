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
import it.unicam.quasylab.sibilla.core.tools.DiscreteTimeAgentSMC;
import it.unicam.quasylab.sibilla.core.tools.DiscreteTimePathChecker;

import java.io.FileNotFoundException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.DoublePredicate;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * @author loreti
 *
 */
public class Main {

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
	private static final int K_STABLE = 5;
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
		//runAndPrint(1, 31, 100,"phibal_100",Main::getPhiBal);
		//runAndPrint(1, 31, 1000,"phibal_1000",Main::getPhiBal);
		runAndPrint(1, 31, 100,"phiBal_025_100",Main::getPhiBal);
		runAndPrint(1, 31, 1000,"phiBal_025_1000",Main::getPhiBal);
		runAndPrint(1, 31, 10000,"phiBal_025_10000",Main::getPhiBal);
//		runAndPrint(1, 31, 100,"phi1_100",Main::getPhi1);
//		runAndPrint(1, 31, 1000,"phi1_1000",Main::getPhi1);
//		runAndPrint(1, 31, 10000,"phi1_10000",Main::getPhi1);
//		runAndPrint(1, 31, 100,"phi2_100",Main::getPhi2);
//		runAndPrint(1, 31, 1000,"phi2_1000",Main::getPhi2);
//		runAndPrint(1, 31, 10000,"phi2_10000",Main::getPhi2);
//		runAndPrint(1, 31, 100,"phi4_05_100",Main::getPhi4);
//		runAndPrint(1, 31, 1000,"phi4_05_1000",Main::getPhi4);
//		runAndPrint(1, 31, 10000,"phi4_05_10000",Main::getPhi4);
	}


	private static void runAndPrint(int scale, int step, int replica, String label, BiFunction<Integer,AgentsDefinition,DiscreteTimePathChecker<LIOIndividualState, Boolean>> builder) {
		AgentsDefinition def = getAgentDefinition();
		LIOModel<LIOIndividualState> model = new LIOModel<>(def);
		DiscreteTimePathChecker<LIOIndividualState, Boolean> f = builder.apply(scale,def);
		LIOIndividualState initial = getInitialState(def, scale);
		DiscreteTimeAgentSMC<LIOIndividualState,Agent> smc = new DiscreteTimeAgentSMC<>(model,LIOIndividualState[]::new);
		double[] values = smc.compute(initial,f,step,replica);
		System.out.printf("#SCALE %d STEPS %d REPLICA %d\n\n",scale, step, replica);
		System.out.println(label+" = ["+(DoubleStream.of(values).boxed().map(Object::toString).collect(Collectors.joining(",")))+"]");
	}

	private static void runAllChecking( ) {
		StringBuilder output = new StringBuilder();
		AgentsDefinition def = getAgentDefinition();
		LIOModel<LIOIndividualState> model = new LIOModel<>(def);

		for( int scale: SCALES) {
			Map<String, DiscreteTimePathChecker<LIOIndividualState, Boolean>> map = getFormulas(scale,def);
			LIOIndividualState initial = getInitialState(def, scale);
			for( int step: STEPS) {
				for( int replica: REPLICAS) {
					for (Map.Entry<String, DiscreteTimePathChecker<LIOIndividualState, Boolean>> e:map.entrySet()) {
						String name = e.getKey();
						DiscreteTimePathChecker<LIOIndividualState, Boolean> formula = e.getValue();
						DiscreteTimeAgentSMC<LIOIndividualState,Agent> smc = new DiscreteTimeAgentSMC<>(model,LIOIndividualState[]::new);
						System.out.printf("Checking %s: scale=%d step=%d replica=%d\n",name,scale,step,replica);
						long start = System.currentTimeMillis();
						smc.compute(initial,formula,step,replica);
						long elapsed = System.currentTimeMillis()-start;
						String dataLine = String.format("%s %d %d %d %f",name,scale,step,replica,(elapsed/1000.0));
						System.out.println(dataLine);
						output.append(dataLine).append("\n");
					}
				}
			}
		}
		System.out.println("REPORT:");
		System.out.println(output);

	}

	private static Map<String, DiscreteTimePathChecker<LIOIndividualState, Boolean>> getFormulas(int scale, AgentsDefinition def) {
		Map<String, DiscreteTimePathChecker<LIOIndividualState, Boolean>> map = new HashMap<>();
//		map.put("\\phi_{bal}",getPhiBal(scale,def));
		map.put("\\phi_{1}",getPhi1(scale,def));
//		map.put("\\phi_{2}",getPhi2(scale,def));
//		map.put("\\phi_{3}",getPhi3(scale,def));
//		map.put("\\phi_{4}",getPhi4(scale,def));
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
		AgentsDefinition def = new AgentsDefinition();
		Agent agentR = def.addAgent("R");
		Agent agentB = def.addAgent("B");
		AgentAction redAction = def.addAction("red", s -> s.fractionOf(agentB)*meet_probability );
		AgentAction blueAction = def.addAction( "blue" , s -> s.fractionOf(agentR)*meet_probability );
		agentR.addAction(blueAction, agentB);
		agentB.addAction(redAction, agentR);
		return def;
	}

	public static DiscreteTimePathChecker<Agent,Boolean> balancedLocalFormula(AgentsDefinition def) {
		Agent agentB = def.getAgent("B");
		return DiscreteTimeAgentSMC.getAtomic(agentB::equals);
	}

	public static DiscreteTimePathChecker<Agent,Boolean> hasChanged(Agent a1, Agent a2) {
		return DiscreteTimeAgentSMC.getImply(DiscreteTimeAgentSMC.getAtomic(a1::equals),DiscreteTimeAgentSMC.getNext(DiscreteTimeAgentSMC.getAtomic(a2::equals)));
	}

	public static DiscreteTimePathChecker<Agent,Boolean> hasChanged(AgentsDefinition def) {
		Agent agentB = def.getAgent("B");
		Agent agentR = def.getAgent("R");
		return DiscreteTimeAgentSMC.getDisjunction(hasChanged(agentB,agentR),hasChanged(agentR,agentB));
	}

	public static DiscreteTimePathChecker<Agent,Boolean> phiStable(Agent a1, Agent a2) {
		return DiscreteTimeAgentSMC.getImply(
			DiscreteTimeAgentSMC.getAtomic(a1::equals) ,
			DiscreteTimeAgentSMC.getNext(
					DiscreteTimeAgentSMC.getImply(
						DiscreteTimeAgentSMC.getAtomic(a2::equals) ,
						DiscreteTimeAgentSMC.getGlobally(K_STABLE, DiscreteTimeAgentSMC.getAtomic(a2::equals))
					)
			)
		);
	}

	public static DiscreteTimePathChecker<Agent,Boolean> phiStable(AgentsDefinition def) {
		Agent agentB = def.getAgent("B");
		Agent agentR = def.getAgent("R");
		return DiscreteTimeAgentSMC.getConjunction(
			phiStable(agentB, agentR),
			phiStable(agentR, agentB)
		);
	}


	public static DiscreteTimePathChecker<LIOIndividualState, Boolean> getPhiBal(int scale, AgentsDefinition def) {
		DoublePredicate dPred = d -> (d>=0.5-EPS)&&(d<=0.5+EPS);
		return DiscreteTimeAgentSMC.getFractionOf(scale*N, balancedLocalFormula(def), dPred);
	}

	public static DiscreteTimePathChecker<LIOIndividualState, Boolean> getPhi1(int scale, AgentsDefinition def) {
		return DiscreteTimeAgentSMC.getEventually(K1, getPhiBal(scale, def));
	}

	public static DiscreteTimePathChecker<LIOIndividualState, Boolean> getPhi2(int scale, AgentsDefinition def) {
		DiscreteTimePathChecker<LIOIndividualState, Boolean> phiBal = getPhiBal(scale,def);
		return DiscreteTimeAgentSMC.getImply(DiscreteTimeAgentSMC.getNegation(phiBal),DiscreteTimeAgentSMC.getEventually(K3,DiscreteTimeAgentSMC.getGlobally(K4,phiBal)));
	}

	public static DiscreteTimePathChecker<LIOIndividualState, Boolean> getPhi3(int scale, AgentsDefinition def) {
		DiscreteTimePathChecker<LIOIndividualState, Boolean> phiBal = getPhiBal(scale,def);
		return DiscreteTimeAgentSMC.getImply(phiBal,DiscreteTimeAgentSMC.getFractionOf(scale*N,hasChanged(def),d -> d<EPS2));
	}

	public static DiscreteTimePathChecker<LIOIndividualState, Boolean> getPhi4(int scale, AgentsDefinition def) {
		return DiscreteTimeAgentSMC.getFractionOf(
				scale*N,
				phiStable(def),
				d -> (d >= 0.9)
		);
	}

}
