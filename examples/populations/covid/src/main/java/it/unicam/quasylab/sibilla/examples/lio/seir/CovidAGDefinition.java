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

package it.unicam.quasylab.sibilla.examples.lio.seir;

import it.unicam.quasylab.sibilla.core.models.EvaluationEnvironment;
import it.unicam.quasylab.sibilla.core.models.pm.*;
import it.unicam.quasylab.sibilla.core.models.pm.util.PopulationRegistry;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;

import java.util.LinkedList;
import java.util.List;

public class CovidAGDefinition {


    public final static int SCALE = 100;
    public final static int INIT_S = 99*SCALE;
    public final static int INIT_A = 1*SCALE;
    public final static int INIT_G = 0*SCALE;
    public final static int INIT_R = 0*SCALE;
    public final static int INIT_D = 0*SCALE;
    public final static int INIT_IG = 0*SCALE;
    public final static int INIT_IA = 0*SCALE;
    public final static double N = INIT_S + INIT_A + INIT_G + INIT_R + INIT_D+INIT_IG+INIT_IG;

    public final static double LAMBDA_MEET = 4;
    public final static double PROB_TRANSMISSION = 0.1;
    public final static double LAMBDA_R_A = 1 / 7.0;
    public final static double LAMBDA_R_G = 1 / 15.0;
    public final static double LAMBDA_I_G = 1 / 3.0;
    public final static double LAMBDA_I_A = 1 / 5.0;

    public final static int SAMPLINGS = 120;
    public final static double DEADLINE = 120;
    private static final int REPLICA = 10;
    private final static int TASKS = 5;
    private final static double PROB_ASINT = 0.8;
    private final static double PROB_A_G = 0.5;
    private final static double PROB_DEATH = 0.02;

    public static PopulationRegistry generatePopulationRegistry(EvaluationEnvironment environment) {
        return PopulationRegistry.createRegistry("S", "A", "G", "R", "D", "IG", "IA");
    }

    public static List<PopulationRule> generateRules(EvaluationEnvironment environment, PopulationRegistry registry) {
        int S = registry.indexOf("S");
        int A = registry.indexOf("A");;
        int G = registry.indexOf("G");;
        int R = registry.indexOf("R");;
        int D = registry.indexOf("D");;
        int IG = registry.indexOf("IG");;
        int IA = registry.indexOf("IA");;
        PopulationRule rule_S_A_A = new ReactionRule(
                "S->A",
                new Population[] { new Population(S), new Population(A)} ,
                new Population[] { new Population(A), new Population(A)},
                (t,s) -> PROB_ASINT*s.getOccupancy(S)* PROB_TRANSMISSION*LAMBDA_MEET *(s.getOccupancy(A)/N));

        PopulationRule rule_S_G_A = new ReactionRule(
                "S->A",
                new Population[] { new Population(S), new Population(A)} ,
                new Population[] { new Population(G), new Population(A)},
                (t,s) -> (1-PROB_ASINT)*s.getOccupancy(S)* PROB_TRANSMISSION*LAMBDA_MEET *(s.getOccupancy(A)/N));

        PopulationRule rule_S_A_G = new ReactionRule(
                "S->A",
                new Population[] { new Population(S), new Population(G)} ,
                new Population[] { new Population(A), new Population(G)},
                (t,s) -> PROB_ASINT*s.getOccupancy(S)* PROB_TRANSMISSION*LAMBDA_MEET *(s.getOccupancy(G)/N));

        PopulationRule rule_S_G_G = new ReactionRule(
                "S->A",
                new Population[] { new Population(S), new Population(G)} ,
                new Population[] { new Population(G), new Population(G)},
                (t,s) -> (1-PROB_ASINT)*s.getOccupancy(S)* PROB_TRANSMISSION*LAMBDA_MEET *(s.getOccupancy(G)/N));

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

        PopulationRule rule_IA_R = new ReactionRule(
                "I->R",
                new Population[] { new Population(IA) },
                new Population[] { new Population(R) },
                (t,s) -> s.getOccupancy(IA)*LAMBDA_R_A*(1-PROB_A_G)
        );

        PopulationRule rule_IA_IG = new ReactionRule(
                "I->R",
                new Population[] { new Population(IA) },
                new Population[] { new Population(IG) },
                (t,s) -> s.getOccupancy(IA)*LAMBDA_R_A*PROB_A_G
        );

        PopulationRule rule_G_IG = new ReactionRule(
                "I->R",
                new Population[] { new Population(G) },
                new Population[] { new Population(IG) },
                (t,s) -> s.getOccupancy(G)*LAMBDA_I_G
        );

        PopulationRule rule_A_IA = new ReactionRule(
                "I->R",
                new Population[] { new Population(A) },
                new Population[] { new Population(IA) },
                (t,s) -> s.getOccupancy(A)*LAMBDA_I_A
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

        PopulationRule rule_IG_R = new ReactionRule(
                "I->R",
                new Population[] { new Population(IG) },
                new Population[] { new Population(R) },
                (t,s) -> s.getOccupancy(IG)*LAMBDA_R_G*(1-PROB_DEATH)
        );

        PopulationRule rule_IG_D = new ReactionRule(
                "I->R",
                new Population[] { new Population(IG) },
                new Population[] { new Population(D) },
                (t,s) -> s.getOccupancy(IG)*LAMBDA_R_G*PROB_DEATH
        );

        LinkedList<PopulationRule> rules = new LinkedList<>();
        rules.add(rule_S_A_A);
        rules.add(rule_S_G_A);
        rules.add(rule_S_A_G);
        rules.add(rule_S_G_G);
        rules.add(rule_A_G);
        rules.add(rule_A_R);
        rules.add(rule_G_R);
        rules.add(rule_G_D);
        rules.add(rule_G_IG);
        rules.add(rule_IG_R);
        rules.add(rule_IG_R);
        rules.add(rule_IA_IG);
        rules.add(rule_A_IA);
        rules.add(rule_IA_R);
        return rules;
    }

    public static PopulationState initialState() {
        return new PopulationState( new int[] { INIT_S, INIT_A, INIT_G, INIT_R, INIT_D, INIT_IG , INIT_IA } );
    }


}
