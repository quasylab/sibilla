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

package quasylab.sibilla.examples.pm.taxis;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.models.ModelDefinition;
import quasylab.sibilla.core.models.pm.*;
import quasylab.sibilla.core.models.pm.util.PopulationRegistry;
import quasylab.sibilla.core.simulator.sampling.Measure;

import java.util.LinkedList;
import java.util.List;

/**
 * Model for taxi
 *
 *      T ---> Taxis that are available
 *      U ---> Users that are awaiting for a taxi
 *      S ---> Users that are performing a short travel with a taxi
 *      L ---> Users that are performing a long travel with a taxi
 *      I ---> Users that are inactive, not asking for a taxi
 *
 */
public class TaxiDefinition extends PopulationModelDefinition {


    public final static int T = 0; //A Taxi ready to collect a citizen.
    public final static int W = 1; //A User waiting for a taxi
    public final static int S = 2; //A Taxi travelling on a short trip.
    public final static int L = 3; //A Taxi travelling on a long trip.
    public final static int I = 4; //An inactive user.
    public final static int D = 5; //A Taxi that is in the deposit.
    public final static int A = 6; //An angry user that has not yet received his taxi.
    public final static int B = 7; //A bored taxi driver that has not a user to serve.


    public final static int SCALE = 10;

    public final static int INIT_T = 0;
    public final static int INIT_W = 0;
    public final static int INIT_S = 0;
    public final static int INIT_L = 0;
    public final static int INIT_I = 2000;
    public final static int INIT_D = 50;
    public final static int INIT_A = 0;
    public final static int INIT_B = 0;


    public final static double N = INIT_T + INIT_W + INIT_S + INIT_L + INIT_I;



    public final static double LAMBDA_A = 1.0;   //  1 min -->  1 / 1 rate con cui U richiede un T

    public final static double LAMBDA_S = 1/2.0;   //  5 min -->  1 / 5 rate con cui U attende un T

    public final static double LAMBDA_D = 5.0;   //  5 min -->  1 / 5 rate con cui U attende un T

    public final static double LAMBDA_S_END = 1/15.0;  //  20 min -->  1 / 20 rate con cui U attende un T
    public final static double LAMBDA_L_END = 1/45.0;  //  60 min -->  1 / 60 rate con cui U attende un T

    public final static double LAMBDA_ANGRY = 1/30.0;
    public final static double LAMBDA_BORING = 1/15.0;

    public final static double PROB_SHORT = 0.5;
    private static final double WEIGHT = 100;

    public PopulationState initialState(double... parameters) {
        if (parameters.length != 8) {
            return new PopulationState( new int[] { INIT_T, INIT_W, INIT_S, INIT_L,INIT_I, INIT_D, INIT_A, INIT_B} );
        } else {
            return new PopulationState(new int[]{
                    (int) parameters[T],
                    (int) parameters[W],
                    (int) parameters[S],
                    (int) parameters[L],
                    (int) parameters[I],
                    (int) parameters[D],
                    (int) parameters[A],
                    (int) parameters[B]});
        }
    }


    public double rateTakeTaxiStandardUser(PopulationState s, double lambda_s, double weight, double p_s) {
        if (s.getOccupancy(W)==0) {
            return 0.0;
        } else {
            double probUserKind = s.getOccupancy(W)/(weight*s.getOccupancy(A)+s.getOccupancy(W));
            return s.getOccupancy(T)*probUserKind*lambda_s * p_s;
        }
    }

    public double rateTakeTaxiAngryUser(PopulationState s, double lambda_s, double weight, double p_s) {
        if (s.getOccupancy(A)==0) {
            return 0.0;
        } else {
            double probUserKind = (weight*s.getOccupancy(A))/(weight*s.getOccupancy(A)+s.getOccupancy(W));
            return s.getOccupancy(T)*probUserKind*lambda_s * p_s;
        }
    }


    @Override
    protected PopulationRegistry generatePopulationRegistry() {
        return PopulationRegistry.createRegistry("T", "W", "S", "L", "I", "D", "A", "B");
    }

    @Override
    protected List<PopulationRule> getRules() {
        PopulationRegistry reg = getRegistry();
        int T = reg.indexOf("T"); //A Taxi ready to collect a citizen.
        int W = reg.indexOf("W"); //A User waiting for a taxi
        int S = reg.indexOf("S"); //A Taxi travelling on a short trip.
        int L = reg.indexOf("L"); //A Taxi travelling on a long trip.
        int I = reg.indexOf("I"); //An inactive user.
        int D = reg.indexOf("D"); //A Taxi that is in the deposit.
        int A = reg.indexOf("A"); //An angry user that has not yet received his taxi.
        int B = reg.indexOf("B"); //A bored taxi driver that has not a user to serve.

        double lambda_a;
        double lambda_s;
        double lambda_short_end;
        double lambda_long_end;
        double p_s;
        double lambda_d;
        double lambda_angry;
        double lambda_boring;
        double weight = WEIGHT;

        lambda_a = LAMBDA_A;
        lambda_s = LAMBDA_S;
        lambda_short_end = LAMBDA_S_END;
        lambda_long_end = LAMBDA_L_END;
        p_s = PROB_SHORT;
        lambda_d = LAMBDA_D;
        lambda_angry = LAMBDA_ANGRY;
        lambda_boring = LAMBDA_BORING;

        LinkedList<PopulationRule> rules = new LinkedList<>();

        PopulationRule rule_user_arrival = new ReactionRule(
                "user_arrival",
                new Population[] { new Population(I)} ,
                new Population[] { new Population(W)},
                (t,s) ->lambda_a*s.getOccupancy(I));

        PopulationRule rule_user_angry = new ReactionRule(
                "user_angry",
                new Population[] { new Population(W)} ,
                new Population[] { new Population(A)},
                (t,s) ->lambda_angry*s.getOccupancy(W));

        PopulationRule rule_taxi_short = new ReactionRule(
                "taxi_short",
                new Population[] { new Population(T), new Population(W)} ,
                new Population[] { new Population(S)},
                (t,s) -> rateTakeTaxiStandardUser(s,lambda_s,weight,p_s));

        PopulationRule rule_taxi_short_angry = new ReactionRule(
                "taxi_short_angry",
                new Population[] { new Population(T), new Population(A)} ,
                new Population[] { new Population(S)},
                (t,s) -> rateTakeTaxiAngryUser(s,lambda_s,weight,p_s));

        PopulationRule rule_taxi_long = new ReactionRule(
                "taxi_long",
                new Population[] { new Population(T), new Population(W)} ,
                new Population[] { new Population(L)},
                (t,s) -> rateTakeTaxiStandardUser(s,lambda_s,weight,1-p_s));

        PopulationRule rule_taxi_long_angry = new ReactionRule(
                "taxi_long_angry",
                new Population[] { new Population(T), new Population(A)} ,
                new Population[] { new Population(L)},
                (t,s) -> rateTakeTaxiAngryUser(s,lambda_s,weight,1-p_s));

        PopulationRule rule_short_end = new ReactionRule(
                "short_end",
                new Population[] { new Population(S)} ,
                new Population[] { new Population(T), new Population(I)},
                (t,s) ->s.getOccupancy(S) * lambda_short_end);

        PopulationRule rule_long_end = new ReactionRule(
                "long_end",
                new Population[] { new Population(L)} ,
                new Population[] { new Population(T), new Population(I)},
                (t,s) ->s.getOccupancy(L) * lambda_long_end );

        PopulationRule rule_taxi_exit = new ReactionRule(
                "taxi_exit",
                new Population[] { new Population(D)} ,
                new Population[] { new Population(T)},
                (t,s) ->lambda_d*s.getOccupancy(D));


        rules.add(rule_user_arrival);
        rules.add(rule_user_angry);
        rules.add(rule_taxi_short);
        rules.add(rule_taxi_long);
        rules.add(rule_taxi_short_angry);
        rules.add(rule_taxi_long_angry);
        rules.add(rule_short_end);
        rules.add(rule_long_end);
        rules.add(rule_taxi_exit);

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
}
