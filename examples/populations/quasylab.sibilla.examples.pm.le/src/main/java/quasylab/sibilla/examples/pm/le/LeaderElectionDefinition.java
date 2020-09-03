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

package quasylab.sibilla.examples.pm.le;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.models.ModelDefinition;
import quasylab.sibilla.core.models.pm.*;

public class LeaderElectionDefinition implements ModelDefinition<PopulationState> {

    public final static int C = 0;
    public final static int S0 = 1;
    public final static int S1 = 2;
    public final static int F = 3;
    public final static int L = 4;

    public final static int INIT_C = 100;
    public final static int INIT_S0 = 0;
    public final static int INIT_S1 = 0;
    public final static int INIT_F = 0;
    public final static int INIT_L = 0;
    public final static double N = INIT_C+INIT_F+INIT_L;

    public final static double SELECT_RATE = 1.0;
    public final static double COM_RATE = 1.0;
    public final static double WAITING_RATE = 20.0;
    public final static double MEET_PROB = 0.1;



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
        return new PopulationState(new int[] { INIT_C, INIT_S0, INIT_S1, INIT_F , INIT_L });
    }

    @Override
    public Model<PopulationState> createModel() {
        PopulationRule rule_C_S0 = new ReactionRule(
                "C->S0",
                new Population[] { new Population(C) } ,
                new Population[] { new Population(S0) },
                (t,s) -> s.getOccupancy(C)*0.5*SELECT_RATE
        );

        PopulationRule rule_C_S1 = new ReactionRule(
                "C->S1",
                new Population[] { new Population(C) } ,
                new Population[] { new Population(S1) },
                (t,s) -> s.getOccupancy(C)*0.5*SELECT_RATE);



        PopulationRule rule_S0_S1 = new ReactionRule(
                "S0*S1->F*C",
                new Population[] { new Population(S0) , new Population(S1) } ,
                new Population[] { new Population(F) , new Population(C) } ,
                (t,s) -> (
                        s.getOccupancy(S0)*s.getOccupancy(S1)/(s.getOccupancy(S0,S1)*(s.getOccupancy(S0,S1)-1) )
                                *s.getOccupancy(S0,S1)
                                *COM_RATE
                )
        ) ;


        PopulationRule rule_S0_S0 = new ReactionRule(
                "S0*S1->F*C",
                new Population[] { new Population(S0,2)} ,
                new Population[] { new Population(C,2)} ,
                (t,s) -> (
                        s.getOccupancy(S0)*(s.getOccupancy(S0)-1)/(s.getOccupancy(S0,S1)*(s.getOccupancy(S0,S1)-1) )
                                *s.getOccupancy(S0,S1)/2
                                *COM_RATE
                )
        ) ;


        PopulationRule rule_S1_S1 = new ReactionRule(
                "S0*S1->F*C",
                new Population[] { new Population(S1,2) } ,
                new Population[] { new Population(C,2) } ,
                (t,s) -> (
                        s.getOccupancy(S1)*(s.getOccupancy(S1)-1)/(s.getOccupancy(S0,S1)*(s.getOccupancy(S0,S1)-1) )
                                *s.getOccupancy(S0,S1)/2
                                *COM_RATE
                )
        ) ;


        PopulationRule rule_S0_L = new ReactionRule(
                "S0 ->F",
                new Population[] { new Population(S0) } ,
                new Population[] { new Population(L) } ,
                (t,s) -> s.getOccupancy(S0)*WAITING_RATE);

        PopulationRule rule_S1_L = new ReactionRule(
                "S1 ->F",
                new Population[] { new Population(S1) } ,
                new Population[] { new Population(L) } ,
                (t,s) -> s.getOccupancy(S1)*WAITING_RATE);



        PopulationRule rule_L_C = new ReactionRule(
                "L -> C",
                new Population[] { new Population(L) } ,
                new Population[] { new Population(C) } ,
                (t,s) -> s.getOccupancy(S0,S1,C)/N*s.getOccupancy(L)*COM_RATE);


        PopulationModel f = new PopulationModel(5);
        f.addRule(rule_C_S0);
        f.addRule(rule_C_S1);
        f.addRule(rule_S0_S1);
        f.addRule(rule_S0_S0);
        f.addRule(rule_S1_S1);
        f.addRule(rule_L_C);
        f.addRule(rule_S0_L);
        f.addRule(rule_S1_L);
        return f;
    }

    public static double numberOfSupplicants(PopulationState s) {
        return s.getOccupancy(C)+s.getOccupancy(S0)+s.getOccupancy(S1);
    }

    public static double numberOfFollowers(PopulationState s) {
        return s.getOccupancy(F);
    }

    public static double numberOfLeaders(PopulationState s) {
        return s.getOccupancy(L);
    }


}
