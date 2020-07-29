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

package quasylab.sibilla.examples.servers.client;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.models.ModelDefinition;
import quasylab.sibilla.core.models.pm.*;
import quasylab.sibilla.core.simulator.sampling.SamplingCollection;
import quasylab.sibilla.core.simulator.sampling.SamplingFunction;
import quasylab.sibilla.core.simulator.sampling.StatisticSampling;

import java.io.Serializable;

public class SEIRModelDefinition implements PopulationModelDefinition, Serializable {

    public final static int S = 0;
    public final static int E = 1;
    public final static int I = 2;
    public final static int R = 3;

    public final static int INIT_S = 99;
    public final static int INIT_E = 0;
    public final static int INIT_I = 1;
    public final static int INIT_R = 0;
    public final static double N = INIT_S + INIT_E + INIT_I + INIT_R;

    public final static double LAMBDA_E = 1;
    public final static double LAMBDA_I = 1 / 3.0;
    public final static double LAMBDA_R = 1 / 7.0;
    public final static double LAMBDA_DECAY = 1 / 30.0;


    @Override
    public int stateArity() {
        return 4;
    }

    @Override
    public int modelArity() {
        return 0;
    }

    @Override
    public PopulationState state(double... parameters) {
        return new PopulationState(new int[]{INIT_S, INIT_E, INIT_I, INIT_R});
    }

    @Override
    public Model<PopulationState> createModel(double... args) {
        PopulationRule rule_S_E = new ReactionRule(
                "S->E",
                new Population[]{new Population(S), new Population(I)},
                new Population[]{new Population(E), new Population(I)},
                (t, s) -> s.getOccupancy(S) * LAMBDA_E * (s.getOccupancy(I) / N));

        PopulationRule rule_E_I = new ReactionRule(
                "E->I",
                new Population[]{new Population(E)},
                new Population[]{new Population(I)},
                (t, s) -> s.getOccupancy(E) * LAMBDA_I
        );

        PopulationRule rule_I_R = new ReactionRule(
                "I->R",
                new Population[]{new Population(I)},
                new Population[]{new Population(R)},
                (t, s) -> s.getOccupancy(I) * LAMBDA_R
        );


        PopulationRule rule_R_S = new ReactionRule(
                "R->S",
                new Population[]{new Population(R)},
                new Population[]{new Population(S)},
                (t, s) -> s.getOccupancy(R) * LAMBDA_DECAY
        );

        PopulationModel f = new PopulationModel(this);
        f.addRule(rule_S_E);
        f.addRule(rule_E_I);
        f.addRule(rule_I_R);
        // f.addRule(rule_R_S);
        return f;
    }


    public static double fractionOfS(PopulationState s) {
        return s.getFraction(S);
    }

    public static double fractionOfI(PopulationState s) {
        return s.getFraction(I);
    }

    public static double fractionOfE(PopulationState s) {
        return s.getFraction(E);
    }

    public static double fractionOfR(PopulationState s) {
        return s.getFraction(R);
    }

    public static SamplingFunction<PopulationState> getCollection(int SAMPLINGS, double DEADLINE) {
        SamplingCollection<PopulationState> collection = new SamplingCollection<>();
        collection.add(StatisticSampling.measure("S", SAMPLINGS, DEADLINE, SEIRModelDefinition::fractionOfS));
        collection.add(StatisticSampling.measure("E", SAMPLINGS, DEADLINE, SEIRModelDefinition::fractionOfE));
        collection.add(StatisticSampling.measure("I", SAMPLINGS, DEADLINE, SEIRModelDefinition::fractionOfI));
        collection.add(StatisticSampling.measure("R", SAMPLINGS, DEADLINE, SEIRModelDefinition::fractionOfR));
        return collection;
    }
}
