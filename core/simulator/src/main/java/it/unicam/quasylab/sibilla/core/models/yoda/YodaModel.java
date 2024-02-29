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

package it.unicam.quasylab.sibilla.core.models.yoda;

import it.unicam.quasylab.sibilla.core.models.*;
import it.unicam.quasylab.sibilla.core.simulator.DiscreteTimeSimulationStepFunction;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

public class YodaModel implements DiscreteTimeModel<YodaSystemState> {

    private final Map<String, Measure<YodaSystemState>> measureMap;

    private final Map<String, Predicate<YodaSystemState>> predicateMap;

    public YodaModel(Map<String, Measure<YodaSystemState>> measureMap, Map<String, Predicate<YodaSystemState>> predicateMap) {
        this.measureMap = measureMap;
        this.predicateMap = predicateMap;
    }


    @Override
    public YodaSystemState sampleNextState(RandomGenerator r, double time, YodaSystemState state) {
        return state.next(r);
    }


    @Override
    public int stateByteArraySize() {
        return 0;
    }

    @Override
    public byte[] byteOf(YodaSystemState state) throws IOException {
        return new byte[0];
    }

    @Override
    public YodaSystemState fromByte(byte[] bytes) throws IOException {
        return null;
    }

    @Override
    public String[] measures() {
        return measureMap.keySet().toArray(new String[0]);
    }

    @Override
    public double measure(String m, YodaSystemState state) {
        Measure<YodaSystemState> measure = measureMap.get(m);
        if (m == null) {
            return Double.NaN;
        } else {
            return measure.measure(state);
        }
    }

    @Override
    public Measure<YodaSystemState> getMeasure(String m) {
        return measureMap.get(m);
    }

    @Override
    public Predicate<YodaSystemState> getPredicate(String name) {
        return predicateMap.get(name);
    }

    @Override
    public String[] predicates() {
        return predicateMap.keySet().toArray(new String[0]);
    }

    public DiscreteTimeSimulationStepFunction<YodaSystemState> getDiscreteTimeStepFunction() {
        return (rg,state) -> state.next(rg);
    }

    @Override
    public Map<String, Map<String, ToDoubleFunction<YodaSystemState>>> trace(YodaSystemState state) {
        return state.getAgents().stream().collect(Collectors.toMap(a -> a.getName().getName()+"_"+a.getId(), YodaAgent::getTraceFunctions));
    }

    @Override
    public Map<String, Function<YodaSystemState,Function<String, SibillaValue>>> getNameSolver(YodaSystemState state) {
        return state.getAgents().stream().collect(
                Collectors.toMap(a -> a.getName().getName()+"_"+a.getId(), YodaAgent::getNameResolver));
    }
}
