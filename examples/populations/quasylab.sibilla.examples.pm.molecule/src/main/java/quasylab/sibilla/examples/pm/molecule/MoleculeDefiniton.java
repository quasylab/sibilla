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

package quasylab.sibilla.examples.pm.molecule;

import it.unicam.quasylab.sibilla.core.models.pm.*;
import it.unicam.quasylab.sibilla.core.models.pm.util.PopulationRegistry;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;

import java.util.LinkedList;
import java.util.List;

public class MoleculeDefiniton extends PopulationModelDefinition {


    public final static int Na = 0;
    public final static int Cl = 1;
    public final static int NaPositive = 2;
    public final static int ClNegative = 3;

    public final static int SCALE = 100;

    public final static int INIT_Na = 99*SCALE;
    public final static int INIT_Cl = 1*SCALE;
    public final static int INIT_NaPositive = 0*SCALE;
    public final static int INIT_ClNegative = 0*SCALE;

    public final static double N = INIT_Na + INIT_Cl + INIT_NaPositive + INIT_ClNegative;

    public final static double E1RATE = 100;
    public final static double E2RATE = 10;

    public final static double LAMBDA = 10;

    @Override
    protected PopulationRegistry generatePopulationRegistry() {
        return PopulationRegistry.createRegistry("Na","Cl","NaPositive","ClNegative");
    }

    @Override
    protected List<PopulationRule> getRules() {
        PopulationRegistry reg = getRegistry();
        LinkedList<PopulationRule> rules = new LinkedList<>();
        int Na = reg.indexOf("Na");
        int Cl = reg.indexOf("Cl");
        int NaPositive = reg.indexOf("NaPositive");
        int ClNegative = reg.indexOf("ClNegative");

        double lambda = getParameter("lambda");


        // Na + Cl -> Na+ + Cl-

        PopulationRule rule_Na_Cl__NaP_ClM = new ReactionRule(
                "Na + Cl -> Na+ + Cl-",
                new Population[] { new Population(Na), new Population(Cl)} ,
                new Population[] { new Population(NaPositive), new Population(ClNegative)},
                (t,s) ->s.getOccupancy(Na) * s.getOccupancy(Cl) * lambda * E1RATE);


        // Na+ + Cl- -> Na + Cl

        PopulationRule rule_NaP_ClM__Na_Cl = new ReactionRule(
                "Na+ + Cl- -> Na + Cl",
                new Population[] { new Population(NaPositive), new Population(ClNegative)},
                new Population[] { new Population(Na), new Population(Cl)} ,
                (t,s) -> s.getOccupancy(NaPositive) * s.getOccupancy(ClNegative) * lambda * E2RATE);

        rules.add(rule_Na_Cl__NaP_ClM);
        rules.add(rule_NaP_ClM__Na_Cl);

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

    public MoleculeDefiniton() {
        super();
        setParameter("lambda",LAMBDA);
    }


    public PopulationState initialState(double... parameters) {
        PopulationRegistry reg = getRegistry();
        int Na = reg.indexOf("Na");
        int Cl = reg.indexOf("Cl");
        int NaPositive = reg.indexOf("NaPositive");
        int ClNegative = reg.indexOf("ClNegative");
        if (parameters.length != 4) {
            return new PopulationState( 4 , new Population(Na,INIT_Na),
                    new Population(Cl,INIT_Cl),
                    new Population(NaPositive, INIT_NaPositive),
                    new Population(ClNegative, INIT_ClNegative));
        } else {
            return new PopulationState( 4 , new Population(Na,(int) parameters[Na]),
                    new Population(Cl, (int) parameters[Cl]),
                    new Population(NaPositive, (int) parameters[NaPositive]),
                    new Population(ClNegative, (int) parameters[ClNegative]));
        }
    }
}
