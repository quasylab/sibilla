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

package it.unicam.quasylab.sibilla.core.tools.glotl;

import it.unicam.quasylab.sibilla.core.models.DiscreteModel;
import it.unicam.quasylab.sibilla.core.models.ImmutableState;
import it.unicam.quasylab.sibilla.core.models.IndexedState;
import it.unicam.quasylab.sibilla.core.simulator.DefaultRandomGenerator;
import it.unicam.quasylab.sibilla.core.simulator.SimulationEnvironment;
import it.unicam.quasylab.sibilla.core.simulator.Trajectory;
import it.unicam.quasylab.sibilla.core.tools.glotl.global.GlobalAferFormula;
import it.unicam.quasylab.sibilla.core.tools.glotl.global.GlobalFormula;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GLoTLStatisticalModelChecker {

    public <S extends ImmutableState & IndexedState<A>,A> double computeProbability(DiscreteModel<S> model, S state, GlobalFormula<A,S> formula, int replica) {
        DefaultRandomGenerator rg = new DefaultRandomGenerator();
        SimulationEnvironment se = new SimulationEnvironment();
        GLoTLPathChecker<A,S> pathChecker = new GLoTLPathChecker<>(formula);
        int counter = 0;
        double deadline = formula.getTimeHorizon();
        for(int i=0; i<replica; i++) {
            Trajectory<S> trajectory = se.sampleTrajectory(rg, model, state, deadline);
            if (pathChecker.test(trajectory)) {
                counter++;
            }
        }
        return ((double) counter)/replica;
    }

    public <S extends ImmutableState & IndexedState<A>,A> double[] computeProbability(DiscreteModel<S> model, S state, GlobalFormula<A,S> formula, int from, int to, int replica) {
        if (from>= to) {
            throw new IllegalArgumentException();
        }
        return computeProbability(model, state, i -> new GlobalAferFormula<>(i+from, formula), to-from, replica);
    }

    public <S extends ImmutableState & IndexedState<A>,A> double[] computeProbability(DiscreteModel<S> model, Function<RandomGenerator,S> stateBuilder, IntFunction<GlobalFormula<A, S>> formulaBuilder, int size, int replica) {
        DefaultRandomGenerator rg = new DefaultRandomGenerator();
        SimulationEnvironment se = new SimulationEnvironment();
        int[] counterArray = new int[size];
        List<GlobalFormula<A, S>> formulas = IntStream.range(0, size).mapToObj(formulaBuilder).collect(Collectors.toList());
        List<GLoTLPathChecker<A,S>> pathCheckers = formulas.stream().map(GLoTLPathChecker::new).collect(Collectors.toList());
        double deadline = formulas.stream().mapToDouble(GlobalFormula::getTimeHorizon).max().orElse(0.0);
        for(int i=0; i<replica; i++) {
            Trajectory<S> trajectory = se.sampleTrajectory(rg, model, stateBuilder.apply(rg), deadline);
            int j = 0;
            for (GLoTLPathChecker<A,S> pc: pathCheckers) {
                if (pc.test(trajectory)) {
                    counterArray[j]++;
                }
                j++;
            }
        }
        return IntStream.of(counterArray).sequential().mapToDouble(c -> ((double) c)/replica).toArray();
    }

    public <S extends ImmutableState & IndexedState<A>,A> double[] computeProbability(DiscreteModel<S> model, S state, IntFunction<GlobalFormula<A, S>> formulaBuilder, int size, int replica) {
        return computeProbability(model, (Function<RandomGenerator, S>) rg -> state, formulaBuilder, size, replica);
    }


}
