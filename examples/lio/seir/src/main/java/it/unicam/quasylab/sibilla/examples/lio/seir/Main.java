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
package it.unicam.quasylab.sibilla.examples.lio.seir;

import it.unicam.quasylab.sibilla.core.models.lio.*;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationModel;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.simulator.DefaultRandomGenerator;
import it.unicam.quasylab.sibilla.core.simulator.SimulationEnvironment;
import it.unicam.quasylab.sibilla.core.simulator.Trajectory;
import it.unicam.quasylab.sibilla.core.simulator.sampling.SamplingFunction;

import java.io.FileNotFoundException;
import java.net.UnknownHostException;
import java.util.stream.IntStream;

/**
 * @author loreti
 *
 */
public class Main {

	public static final String S = "S";
	public static final String E = "E";
	public static final String I = "I";
	public static final String R = "R";

	public static final int INIT_S = 9;
	public static final int INIT_E = 0;
	public static final int INIT_I = 1;
	public static final int INIT_R = 0;
	public static final int N = INIT_E+INIT_I+INIT_R+INIT_S;

	public static final double infection_probability = 0.25;
	public static final double e_to_i_probability = 1.0/7.0; //After the infection you have an average of 7 days of incubation.
	public static final double i_to_r_probability = 1.0/15.0; //An agent is recovered after 15 days.
	public static final double r_to_s_probability = 1.0/360; //A recovered agent becomes susceptible after 6 months.
	public static final double external_infection_probability = 0.01;


	public final static int SAMPLINGS = 120;
	public final static double DEADLINE = 120;
	private static final int REPLICA = 10;

	public static void main(String[] argv) throws FileNotFoundException, InterruptedException, UnknownHostException {
		AgentsDefinition def = getAgentDefinition();
		LIOModel<LIOIndividualState> model = new LIOModel<>(def);
		LIOIndividualState initial = getInitialState(def, 100000);
		SimulationEnvironment se = new SimulationEnvironment();
		Trajectory<LIOIndividualState> trj = se.sampleTrajectory(new DefaultRandomGenerator(),model,initial,100);
	}



	private static LIOIndividualState getInitialState(AgentsDefinition def, int scale) {
		String[] agents = IntStream.range(0, N*scale).boxed().map(i -> getAgent(def,scale,i)).toArray(i -> new String[i]);
		return new LIOIndividualState(def,agents);
	}

	private static String getAgent(AgentsDefinition def, int scale, Integer i) {
		if (i<=INIT_S*scale) { return S; }
		i = i-INIT_S*scale;
		if (i<=INIT_E*scale) { return E; }
		i = i-INIT_E*scale;
		if (i<=INIT_I*scale) { return I; }
		return R;
	}

	public static AgentsDefinition getAgentDefinition() {
		AgentsDefinition def = new AgentsDefinition();
		Agent agentS = def.addAgent("S");
		Agent agentE = def.addAgent("E");
		Agent agentI = def.addAgent("I");
		Agent agentR = def.addAgent("R");
		AgentAction exposed = def.addAction("exposed", s -> s.fractionOf(agentI)*infection_probability );
		AgentAction infected = def.addAction( "infected" , s -> e_to_i_probability );
		AgentAction recovered = def.addAction( "recovered" , s -> i_to_r_probability );
		AgentAction lost = def.addAction("loss" , s -> r_to_s_probability );
		AgentAction external = def.addAction("external" , s -> external_infection_probability );
		agentS.addAction(exposed, agentE);
		agentS.addAction(external, agentE);
		agentE.addAction(infected, agentI);
		agentI.addAction(recovered, agentR);
		agentR.addAction(lost, agentS);
		return def;
	}

	


}
