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
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package quasylab.sibilla.examples.pm.crowds;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.models.pm.*;
import quasylab.sibilla.core.models.pm.util.PopulationRegistry;

import java.util.LinkedList;
import java.util.List;

public class CrowdDefinition extends PopulationModelDefinition {

	public static double LAMBDA_S = 1.0;
	public static double P_F = 1.0;
	public static int N = 10;
	public PopulationRegistry r = new PopulationRegistry();
	public final static int SAMPLINGS = 100;
	public final static double DEADLINE = 10;
	private final static int TASKS = 5;
	private static final int REPLICA = 1000;

	public CrowdDefinition() {
		for (int i = 0; i < N; i++) {
			r.register("A", i);
		}
		for (int i = 0; i < N; i++) {
			r.register("AM", i);
		}

		r.register("M1");
		r.register("M2");
	}

	@Override
	public int stateArity() {
		return 0;
	}

	@Override
	public String[] states() {
		return new String[0];
	}

	@Override
	public PopulationState state(String name, double... parameters) {
		return null;
	}

	@Override
	public PopulationState state(double... parameters) {
		Population[] population = new Population[N + 1];
		for (int i = 0; i < N; i++) {
			population[i] = new Population(r.indexOf("A", i), 1);
		}
		population[N] = new Population(r.indexOf("M1"), 1);
		return new PopulationState(r.size(), population);
	}

	@Override
	public Model<PopulationState> createModel() {
		List<PopulationRule> rules = new LinkedList<PopulationRule>();

		for (int i = 0; i < N; i++) {
			rules.add(new ReactionRule("M1->A" + i,
					new Population[] { new Population(r.indexOf("A", i)), new Population(r.indexOf("M1")) },
					new Population[] { new Population(r.indexOf("AM", i)) }, (t, s) -> LAMBDA_S / N));
		}

		for (int i = 0; i < N; i++) {
			rules.add(new ReactionRule("M2->A" + i,
					new Population[] { new Population(r.indexOf("A", i)), new Population(r.indexOf("M2")) },
					new Population[] { new Population(r.indexOf("AM", i)) }, (t, s) -> LAMBDA_S / N));
		}
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				if (i != j) {
					rules.add(new ReactionRule("A" + i + "->A" + j,
							new Population[] { new Population(r.indexOf("AM", i)), new Population(r.indexOf("A", j)) },
							new Population[] { new Population(r.indexOf("A", i)), new Population(r.indexOf("AM", j)) },
							(t, s) -> P_F * LAMBDA_S / N));
				}
			}
		}
		for (int i = 0; i < N; i++) {
			rules.add(new ReactionRule("A" + i + "->D", new Population[] { new Population(r.indexOf("AM", i)) },
					new Population[] { new Population(r.indexOf("A", i)) }, (t, s) -> (1 - P_F) * LAMBDA_S));
		}

		PopulationModel f = new PopulationModel(r.size(), this);
		// f.addState("init",initialState(1));//2 per M2
		f.addRules(rules);
		return f;
	}
}
