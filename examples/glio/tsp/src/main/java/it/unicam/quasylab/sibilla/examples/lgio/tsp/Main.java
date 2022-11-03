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
package it.unicam.quasylab.sibilla.examples.lgio.tsp;

import it.unicam.quasylab.sibilla.core.models.lgio.*;

import java.util.Map;
import java.util.function.Predicate;

/**
 * @author loreti
 *
 */
public class Main {

	public static final String S0 = "S0";
	public static final String S1 = "S1";
	public static final String SU = "S?";

	public static final String x = "x";
	public static final String y = "y";
	public static final String pm = "pm";

	public static final int INIT_S0 = 10;
	public static final int INIT_S1 = 10;
	public static final int INIT_SU = 80;

	public static final int N = INIT_S0+INIT_S1+INIT_SU;




	public static void main(String[] argv)  {
		AttributeRegistry registry = new AttributeRegistry(x, y, pm);
		Map<String, AgentBehaviour> behaviours = getBehaviours(registry);

	}

	private static Map<String, AgentBehaviour> getBehaviours(AttributeRegistry registry) {
		AgentBehaviour bS0 = new AgentBehaviour(S0);
		AgentBehaviour bS1 = new AgentBehaviour(S1);
		AgentBehaviour bSU = new AgentBehaviour(SU);
		AgentAction lost = new AgentAction("lost");
		AgentAction believe0 = new AgentAction("believe0");
		AgentAction believe1 = new AgentAction("believe1");
		AgentAction swtch = new AgentAction("switch");
		Predicate<AgentAttributes> isActive = registry.getPredicate(pm, d -> d==1);
		bS0.addStep(isActive, lost, new AgentStep(bSU));
		bS0.addStep(
				swtch,
				new AgentStep(
					registry.set(pm, AttributeRandomExpression.ifThenElse(isActive,0.0,1.0)),bS0));
		bS1.addStep(isActive, lost, new AgentStep(bSU));
		bS1.addStep(
				swtch,
				new AgentStep(
						registry.set(pm, AttributeRandomExpression.ifThenElse(isActive,0.0,1.0)),bS1));
		bSU.addStep(isActive, believe0, new AgentStep(bS0));
		bSU.addStep(isActive, believe1, new AgentStep(bS1));
		bSU.addStep(
				swtch,
				new AgentStep(
						registry.set(pm, AttributeRandomExpression.ifThenElse(isActive,0.0,1.0)),bSU));
		return Map.of(S0, bS0, S1, bS1, SU, bSU);
	}


	private static void runAndPrint() {//int scale, int step, int replica, String label, BiFunction<Integer,AgentsDefinition,DiscreteTimePathChecker<LIOIndividualState, Boolean>> builder) {
//		AgentsDefinition def = getAgentDefinition();
//		LIOModel<LIOIndividualState> model = new LIOModel<>(def);
//		DiscreteTimePathChecker<LIOIndividualState, Boolean> f = builder.apply(scale,def);
//		LIOIndividualState initial = getInitialState(def, scale);
//		DiscreteTimeAgentSMC<LIOIndividualState,Agent> smc = new DiscreteTimeAgentSMC<>(model,LIOIndividualState[]::new);
//		double[] values = smc.compute(initial,f,step,replica);
//		System.out.printf("#SCALE %d STEPS %d REPLICA %d\n\n",scale, step, replica);
//		System.out.println(label+" = ["+(DoubleStream.of(values).boxed().map(Object::toString).collect(Collectors.joining(",")))+"]");
	}

	private static void runAllChecking( ) {


	}

	public static ActionProbabilityFunction probabilityFunction() {
		return null;
	}


}
