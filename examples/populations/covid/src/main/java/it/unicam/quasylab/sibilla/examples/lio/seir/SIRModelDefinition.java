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
import it.unicam.quasylab.sibilla.core.models.Model;
import it.unicam.quasylab.sibilla.core.models.ModelDefinition;
import it.unicam.quasylab.sibilla.core.models.pm.*;
import it.unicam.quasylab.sibilla.core.models.pm.util.PopulationRegistry;

import java.util.LinkedList;
import java.util.List;

public class SIRModelDefinition {

    public final static int S = 0;
    public final static int I = 1;
    public final static int R = 2;

    public final static int SCALE = 100;
    public final static int INIT_S = 99 * SCALE;
    public final static int INIT_I = 1 * SCALE;
    public final static int INIT_R = 0 * SCALE;
    public final static double N = INIT_S + INIT_I + INIT_R;

    public final static double LAMBDA_MEET = 4;
    public final static double PROB_TRANSMISSION = 0.1;
    public final static double LAMBDA_R = 1 / 15.0;

    public static PopulationRegistry createPopulationRegistry(EvaluationEnvironment environment) {
        return PopulationRegistry.createRegistry("S", "I", "R");
    }

    public static PopulationState geneateState(double... args) {
        return new PopulationState(new int[] { INIT_S, INIT_I, INIT_R });
    }

    public static List<PopulationRule> generateRules(EvaluationEnvironment environment, PopulationRegistry registry) {
        PopulationRule rule_S_I = new ReactionRule("S->I", new Population[] { new Population(S), new Population(I) },
                new Population[] { new Population(I), new Population(I) },
                (t, s) -> s.getOccupancy(S) * PROB_TRANSMISSION * LAMBDA_MEET * (s.getOccupancy(I) / N));

        PopulationRule rule_I_R = new ReactionRule("I->R", new Population[] { new Population(I) },
                new Population[] { new Population(R) }, (t, s) -> s.getOccupancy(I) * LAMBDA_R);

        LinkedList<PopulationRule> rules = new LinkedList<>();
        rules.add(rule_S_I);
        rules.add(rule_I_R);
        return rules;
    }

}
