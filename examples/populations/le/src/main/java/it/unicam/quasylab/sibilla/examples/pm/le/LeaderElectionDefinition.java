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

package it.unicam.quasylab.sibilla.examples.pm.le;

import it.unicam.quasylab.sibilla.core.models.pm.*;
import it.unicam.quasylab.sibilla.core.models.pm.util.PopulationRegistry;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import it.unicam.quasylab.sibilla.core.simulator.sampling.SimpleMeasure;

import java.util.LinkedList;
import java.util.List;

public class LeaderElectionDefinition {

//
//    public final static int INIT_C = 100;
//    public final static int INIT_S0 = 0;
//    public final static int INIT_S1 = 0;
//    public final static int INIT_F = 0;
//    public final static int INIT_L = 0;
//    public final static double N = INIT_C+INIT_F+INIT_L;
//
//    public final static double SELECT_RATE = 1.0;
//    public final static double COM_RATE = 1.0;
//    public final static double WAITING_RATE = 20.0;
//    public final static double MEET_PROB = 0.1;
//
//    @Override
//    protected PopulationRegistry generatePopulationRegistry() {
//        return PopulationRegistry.createRegistry("C", "S0", "S1", "F", "L");
//    }
//
//    @Override
//    protected List<PopulationRule> getRules() {
//        PopulationRegistry reg = getRegistry();
//        int C = reg.indexOf("C");
//        int S0 = reg.indexOf("S0");
//        int S1 = reg.indexOf("S1");
//        int F = reg.indexOf("F");
//        int L = reg.indexOf("L");
//        LinkedList<PopulationRule> rules = new LinkedList<>();
//        PopulationRule rule_C_S0 = new ReactionRule(
//                "C->S0",
//                new Population[] { new Population(C) } ,
//                new Population[] { new Population(S0) },
//                (t,s) -> s.getOccupancy(C)*0.5*SELECT_RATE
//        );
//
//        PopulationRule rule_C_S1 = new ReactionRule(
//                "C->S1",
//                new Population[] { new Population(C) } ,
//                new Population[] { new Population(S1) },
//                (t,s) -> s.getOccupancy(C)*0.5*SELECT_RATE);
//
//
//
//        PopulationRule rule_S0_S1 = new ReactionRule(
//                "S0*S1->F*C",
//                new Population[] { new Population(S0) , new Population(S1) } ,
//                new Population[] { new Population(F) , new Population(C) } ,
//                (t,s) -> (
//                        s.getOccupancy(S0)*s.getOccupancy(S1)/(s.getOccupancy(S0,S1)*(s.getOccupancy(S0,S1)-1) )
//                                *s.getOccupancy(S0,S1)
//                                *COM_RATE
//                )
//        ) ;
//
//
//        PopulationRule rule_S0_S0 = new ReactionRule(
//                "S0*S1->F*C",
//                new Population[] { new Population(S0,2)} ,
//                new Population[] { new Population(C,2)} ,
//                (t,s) -> (
//                        s.getOccupancy(S0)*(s.getOccupancy(S0)-1)/(s.getOccupancy(S0,S1)*(s.getOccupancy(S0,S1)-1) )
//                                *s.getOccupancy(S0,S1)/2
//                                *COM_RATE
//                )
//        ) ;
//
//
//        PopulationRule rule_S1_S1 = new ReactionRule(
//                "S0*S1->F*C",
//                new Population[] { new Population(S1,2) } ,
//                new Population[] { new Population(C,2) } ,
//                (t,s) -> (
//                        s.getOccupancy(S1)*(s.getOccupancy(S1)-1)/(s.getOccupancy(S0,S1)*(s.getOccupancy(S0,S1)-1) )
//                                *s.getOccupancy(S0,S1)/2
//                                *COM_RATE
//                )
//        ) ;
//
//
//        PopulationRule rule_S0_L = new ReactionRule(
//                "S0 ->F",
//                new Population[] { new Population(S0) } ,
//                new Population[] { new Population(L) } ,
//                (t,s) -> s.getOccupancy(S0)*WAITING_RATE);
//
//        PopulationRule rule_S1_L = new ReactionRule(
//                "S1 ->F",
//                new Population[] { new Population(S1) } ,
//                new Population[] { new Population(L) } ,
//                (t,s) -> s.getOccupancy(S1)*WAITING_RATE);
//
//
//
//        PopulationRule rule_L_C = new ReactionRule(
//                "L -> C",
//                new Population[] { new Population(L) } ,
//                new Population[] { new Population(C) } ,
//                (t,s) -> s.getOccupancy(S0,S1,C)/N*s.getOccupancy(L)*COM_RATE);
//
//
//        rules.add(rule_C_S0);
//        rules.add(rule_C_S1);
//        rules.add(rule_S0_S1);
//        rules.add(rule_S0_S0);
//        rules.add(rule_S1_S1);
//        rules.add(rule_L_C);
//        rules.add(rule_S0_L);
//        rules.add(rule_S1_L);
//        return rules;
//    }
//
//    @Override
//    protected List<Measure<PopulationState>> getMeasures() {
//        PopulationRegistry reg = getRegistry();
//        int C = reg.indexOf("C");
//        int S0 = reg.indexOf("S0");
//        int S1 = reg.indexOf("S1");
//        int F = reg.indexOf("F");
//        int L = reg.indexOf("L");
//        LinkedList<Measure<PopulationState>> measures = new LinkedList<>();
//        measures.add(new SimpleMeasure<>("supplicants", s -> s.getOccupancy(C,S0,S1)));
//        return null;
//    }
//
//    @Override
//    protected void registerStates() {
//        setDefaultState(new StateBuilder<>(this::initialState));
//    }
//
//    private PopulationState initialState(double[] doubles) {
//        return new PopulationState(getRegistry().size(), new Population(getRegistry().indexOf("C"),INIT_C));
//    }
//


}
