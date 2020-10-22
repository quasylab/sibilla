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
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package quasylab.sibilla.examples.pm.seir;

import it.unicam.quasylab.sibilla.core.models.pm.*;
import it.unicam.quasylab.sibilla.core.models.pm.util.PopulationRegistry;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;

import java.util.LinkedList;
import java.util.List;

public class SEIRModelDefinition extends PopulationModelDefinition {


    public final static int INIT_S = 99;
    public final static int INIT_E = 0;
    public final static int INIT_I = 1;
    public final static int INIT_R = 0;
    public final static double N = INIT_S + INIT_E + INIT_I + INIT_R;

    public final static double LAMBDA_E = 1;
    public final static double LAMBDA_I = 1 / 3.0;
    public final static double LAMBDA_R = 1 / 7.0;
    public final static double LAMBDA_DECAY = 1/30.0;

    @Override
    protected PopulationRegistry generatePopulationRegistry() {
        return PopulationRegistry.createRegistry("S", "E", "I", "R");
    }

    @Override
    protected List<PopulationRule> getRules() {
        PopulationRegistry reg = getRegistry();
        int S = reg.indexOf("S");
        int E = reg.indexOf("E");
        int I = reg.indexOf("I");
        int R = reg.indexOf("R");

        LinkedList<PopulationRule> rules = new LinkedList<>();
        PopulationRule rule_S_E = new ReactionRule(
                "S->E",
                new Population[] { new Population(S), new Population(I)} ,
                new Population[] { new Population(E), new Population(I)},
                (t,s) -> s.getOccupancy(S)*LAMBDA_E*(s.getOccupancy(I)/N));

        PopulationRule rule_E_I = new ReactionRule(
                "E->I",
                new Population[] { new Population(E) },
                new Population[] { new Population(I) },
                (t,s) -> s.getOccupancy(E)*LAMBDA_I
        );

        PopulationRule rule_I_R = new ReactionRule(
                "I->R",
                new Population[] { new Population(I) },
                new Population[] { new Population(R) },
                (t,s) -> s.getOccupancy(I)*LAMBDA_R
        );


        PopulationRule rule_R_S = new ReactionRule(
                "R->S",
                new Population[] { new Population(R) },
                new Population[] { new Population(S) },
                (t,s) -> s.getOccupancy(R)*LAMBDA_DECAY
        );

        rules.add(rule_S_E);
        rules.add(rule_E_I);
        rules.add(rule_I_R);
        rules.add(rule_R_S);
        return rules;
    }

    @Override
    protected List<Measure<PopulationState>> getMeasures() {
        return null;
    }

    @Override
    protected void registerStates() {
        setDefaultStateBuilder(new SimpleStateBuilder<>(this::initialState));
    }


    public PopulationState initialState(double... parameters) {
        return new PopulationState( new int[] { INIT_S, INIT_I, INIT_R } );
    }



}
