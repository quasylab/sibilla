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
import quasylab.sibilla.core.simulator.sampling.Measure;
import quasylab.sibilla.core.simulator.sampling.SimpleMeasure;

import java.util.LinkedList;
import java.util.List;

public class CrowdDefinition extends PopulationModelDefinition {


    public static double LAMBDA_S = 1.0;
	public static double P_F = 1.0;
	public static int N = 10;
	public final static int SAMPLINGS = 100;
	public final static double DEADLINE = 10;
	private final static int TASKS = 5;
	private static final int REPLICA = 1000;

	public CrowdDefinition() {
		super();
		setParameter("N",N);
	}

	@Override
	protected PopulationRegistry generatePopulationRegistry() {
		PopulationRegistry reg = new PopulationRegistry();
		int N = (int) getParameter("N");
		for (int i=0; i<N; i++) {
			reg.register("A", i);
		}

		for (int i=0; i<N; i++) {
			reg.register("AM", i);
		}

		reg.register("M1");
		reg.register("M2");
		return reg;
	}

	@Override
	protected List<PopulationRule> getRules() {
		int N = (int) getParameter("N");
		PopulationRegistry r = getRegistry();
		List<PopulationRule> rules = new LinkedList<>();

		for( int i=0 ; i<N ; i++ ) {
			rules.add( new ReactionRule(
				"M1->A"+i,
				new Population[] { new Population(r.indexOf("A",i)) , new Population(r.indexOf("M1"))} ,
				new Population[] { new Population(r.indexOf("AM",i)) } ,
                    (t,s) -> LAMBDA_S/N
			));
		}

		for( int i=0 ; i<N ; i++ ) {
			rules.add( new ReactionRule(
				"M2->A"+i,
				new Population[] { new Population(r.indexOf("A",i)) , new Population(r.indexOf("M2"))} ,
				new Population[] { new Population(r.indexOf("AM",i)) } ,
                    (t,s) -> LAMBDA_S/N
			));
		}
		for( int i=0 ; i<N ; i++ ) {
			for( int j=0; j<N ; j++ ) {
				if (i!=j) {
					rules.add(
						new ReactionRule(
							"A"+i+"->A"+j,
							new Population[] { new Population(r.indexOf("AM",i)) , new Population(r.indexOf("A",j))} ,
							new Population[] { new Population(r.indexOf("A",i)) , new Population(r.indexOf("AM",j))} ,
                                (t,s) -> P_F*LAMBDA_S/N
						)
					);
				}
			}
		}
		for( int i=0 ; i<N ; i++ ) {
			rules.add( new ReactionRule(
				"A"+i+"->D",
				new Population[] { new Population(r.indexOf("AM",i)) } ,
				new Population[] { new Population(r.indexOf("A",i)) } ,
                    (t,s) -> (1-P_F)*LAMBDA_S
			));
		}

		return rules;
	}

	@Override
	protected List<Measure<PopulationState>> getMeasures() {
		int N = (int) getParameter("N");
		PopulationRegistry reg = getRegistry();
		LinkedList<Measure<PopulationState>> toReturn = new LinkedList<>();
		toReturn.add(new SimpleMeasure<>("MESSAGES", s -> runningMessages(N,reg,s)));
		return toReturn;
	}

	@Override
	protected void registerStates() {
		int N = (int) getParameter("N");
		setDefaultStateBuilder(new SimpleStateBuilder<>(0,args -> initialState(N,args)));
	}

	private PopulationState initialState(int N, double ... parameters) {
		PopulationRegistry reg = getRegistry();
		Population[] pop = new Population[N+1];
		pop[0] = new Population(reg.indexOf("M1"),1);
		for( int i=0 ; i<N ; i++) {
			pop[i+1] = new Population(reg.indexOf("A",i),1);
		}
		return new PopulationState(reg.size(),pop);

	}

	public static double runningMessages( int N, PopulationRegistry reg, PopulationState s ) {
		double sum = s.getOccupancy(reg.indexOf("M1"))+s.getOccupancy(reg.indexOf("M2"));
		for( int i=0 ; i<N ; i++ ) {
			sum += s.getOccupancy(reg.indexOf("AM",i));
		}
		return sum;
	}



}
