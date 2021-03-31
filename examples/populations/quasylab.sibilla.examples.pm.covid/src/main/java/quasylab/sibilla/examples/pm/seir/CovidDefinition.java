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

package quasylab.sibilla.examples.pm.seir;

import it.unicam.quasylab.sibilla.core.models.pm.*;
import it.unicam.quasylab.sibilla.core.models.pm.util.PopulationRegistry;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;

import java.util.LinkedList;
import java.util.List;

public class CovidDefinition extends PopulationModelDefinition {

    public final static int SCALE = 150;
    public final static int INIT_S = 99*SCALE;
    public final static int INIT_A = 1*SCALE;
    public final static int INIT_G = 0*SCALE;
    public final static int INIT_R = 0*SCALE;
    public final static int INIT_D = 0*SCALE;
    public final static double N = INIT_S + INIT_A + INIT_G + INIT_R + INIT_D;

    public final static double LAMBDA_MEET = 4;
    public final static double PROB_TRANSMISSION = 0.1;
    public final static double LAMBDA_R_A = 1 / 7.0;
    public final static double LAMBDA_R_G = 1 / 15.0;

    private final static double PROB_ASINT = 0.8;
    private final static double PROB_A_G = 0.5;
    private final static double PROB_DEATH = 0.02;


    @Override
    protected PopulationRegistry generatePopulationRegistry() {
        return PopulationRegistry.createRegistry("S","A","G","R","D");
    }

    @Override
    protected List<PopulationRule> getRules() {
        PopulationRegistry registry = getRegistry();
        int S = registry.indexOf("S");
        int A = registry.indexOf( "A");
        int G = registry.indexOf("G");
        int R = registry.indexOf("R");
        int D = registry.indexOf("D");

        LinkedList<PopulationRule> rules = new LinkedList<>();
        double lambda = getParameter("lambdaMeet");
        PopulationRule rule_S_A_A = new ReactionRule(
                "S->A",
                new Population[] { new Population(S), new Population(A)} ,
                new Population[] { new Population(A), new Population(A)},
                (t,s) -> PROB_ASINT*s.getOccupancy(S)* PROB_TRANSMISSION*lambda *(s.getOccupancy(A)/N));

        PopulationRule rule_S_G_A = new ReactionRule(
                "S->G",
                new Population[] { new Population(S), new Population(A)} ,
                new Population[] { new Population(G), new Population(A)},
                (t,s) -> (1-PROB_ASINT)*s.getOccupancy(S)* PROB_TRANSMISSION*lambda *(s.getOccupancy(A)/N));

        PopulationRule rule_S_A_G = new ReactionRule(
                "S->A",
                new Population[] { new Population(S), new Population(G)} ,
                new Population[] { new Population(A), new Population(G)},
                (t,s) -> PROB_ASINT*s.getOccupancy(S)* PROB_TRANSMISSION*lambda *(s.getOccupancy(G)/N));

        PopulationRule rule_S_G_G = new ReactionRule(
                "S->A",
                new Population[] { new Population(S), new Population(G)} ,
                new Population[] { new Population(G), new Population(G)},
                (t,s) -> (1-PROB_ASINT)*s.getOccupancy(S)* PROB_TRANSMISSION*lambda *(s.getOccupancy(G)/N));

        PopulationRule rule_A_R = new ReactionRule(
                "I->R",
                new Population[] { new Population(A) },
                new Population[] { new Population(R) },
                (t,s) -> s.getOccupancy(A)*LAMBDA_R_A*(1-PROB_A_G)
        );

        PopulationRule rule_A_G = new ReactionRule(
                "I->R",
                new Population[] { new Population(A) },
                new Population[] { new Population(G) },
                (t,s) -> s.getOccupancy(A)*LAMBDA_R_A*PROB_A_G
        );

        PopulationRule rule_G_R = new ReactionRule(
                "I->R",
                new Population[] { new Population(G) },
                new Population[] { new Population(R) },
                (t,s) -> s.getOccupancy(G)*LAMBDA_R_G*(1-PROB_DEATH)
        );

        PopulationRule rule_G_D = new ReactionRule(
                "I->R",
                new Population[] { new Population(G) },
                new Population[] { new Population(D) },
                (t,s) -> s.getOccupancy(G)*LAMBDA_R_G*PROB_DEATH
        );
        rules.add(rule_S_A_A);
        rules.add(rule_S_G_A);
        rules.add(rule_S_A_G);
        rules.add(rule_S_G_G);
        rules.add(rule_A_G);
        rules.add(rule_A_R);
        rules.add(rule_G_R);
        rules.add(rule_G_D);
        return rules;
    }

    @Override
    protected List<Measure<PopulationState>> getMeasures() {
        return null;
    }

    @Override
    protected void registerStates() {
        setDefaultStateBuilder(this::initialState);
    }

    public CovidDefinition() {
        super();
        setParameter("lambdaMeet",1.0);
    }


    @Override
    public PopulationState state(String name, double... parameters) {
        if (name.equals("init")) {
            return state(parameters);
        }
        throw new IllegalArgumentException(String.format("State %s is unknown!",name));
    }

    public PopulationState initialState(double... parameters) {
        PopulationRegistry registry = getRegistry();
        int S = registry.indexOf("S");
        int A = registry.indexOf( "A");
        int G = registry.indexOf("G");
        int R = registry.indexOf("R");
        int D = registry.indexOf("D");
        if (parameters.length != 5) {
            return new PopulationState( new int[] { INIT_S, INIT_A, INIT_G, INIT_R, INIT_D } );
        } else {
            return new PopulationState( new int[] {
                    (int) parameters[S],
                    (int) parameters[A],
                    (int) parameters[G],
                    (int) parameters[R],
                    (int) parameters[D] });
        }
    }


}
