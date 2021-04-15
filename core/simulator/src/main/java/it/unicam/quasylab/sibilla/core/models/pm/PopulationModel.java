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

package it.unicam.quasylab.sibilla.core.models.pm;

import it.unicam.quasylab.sibilla.core.models.MarkovProcess;
import it.unicam.quasylab.sibilla.core.models.StepFunction;
import it.unicam.quasylab.sibilla.core.models.pm.util.PopulationRegistry;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import it.unicam.quasylab.sibilla.core.simulator.util.WeightedElement;
import it.unicam.quasylab.sibilla.core.simulator.util.WeightedLinkedList;
import it.unicam.quasylab.sibilla.core.simulator.util.WeightedStructure;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.IntStream;

/**
 * This class implements a population model. This class is parametrised with
 * respect to types <code>S</code> and and <code>T</code>. The former is the
 * data type used to identify population species in the population vector.
 * Parameter <code>T</code> identifies environment
 *
 * @author loreti
 */
public class PopulationModel implements MarkovProcess<PopulationState> {

    private static final long serialVersionUID = 6871037109869821108L;

    private final PopulationRegistry registry;

    private final List<PopulationRule> rules;

    private final Map<String, Measure<PopulationState>> measuresTable;

    public PopulationModel(PopulationRegistry registry,
        List<PopulationRule> rules,
        Map<String, Measure<PopulationState>> measuresTable) {
        this.registry = registry;
        this.rules = rules;
        this.measuresTable = measuresTable;
    }

    @Override
    public WeightedStructure<StepFunction<PopulationState>> getTransitions(RandomGenerator r, double now,
                                                                           PopulationState state) {
        WeightedLinkedList<StepFunction<PopulationState>> activities = new WeightedLinkedList<>();
        for (PopulationRule rule : rules) {
            PopulationTransition tra = rule.apply(r, now, state);
            if (tra != null) {
                activities.add(new WeightedElement<>(tra.getRate(), (rnd, t, dt) -> state.apply(tra.apply(rnd))));
            }
        }
        return activities;
    }

    public static Map<String, Integer> createPopulation(String... species) {
        HashMap<String, Integer> map = new HashMap<>();
        IntStream.range(0, species.length).forEach(i -> map.put(species[i], i));
        return map;
    }

    public static PopulationState vectorOf(int... species) {
        return new PopulationState(species);
    }

    @Override
    public int stateByteArraySize() {
        return registry.size() * 4;
    }

    @Override
    public byte[] serializeState(PopulationState state) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializeState(baos, state);
        byte[] toReturn = baos.toByteArray();
        baos.close();
        return toReturn;
    }

    @Override
    public void serializeState(ByteArrayOutputStream toSerializeInto, PopulationState state) throws IOException {
        for (int vectorSingleValue : state.getPopulationVector()) {
            toSerializeInto.write(ByteBuffer.allocate(4).putInt(vectorSingleValue).array());
        }
    }

    @Override
    public PopulationState deserializeState(byte[] bytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        PopulationState deserialized = deserializeState(bais);
        bais.close();
        return deserialized;
    }

    @Override
    public PopulationState deserializeState(ByteArrayInputStream bais) throws IOException {
        int length = registry.size();
        int[] vector = new int[length];
        for (int i = 0; i < length; i++) {
            vector[i] = ByteBuffer.wrap(bais.readNBytes(4)).getInt();
        }
        return new PopulationState(vector);
    }

    @Override
    public String[] measures() {
        return measuresTable.keySet().toArray(new String[0]);
    }

    @Override
    public double measure(String m, PopulationState state) {
        Measure<PopulationState> measure = measuresTable.get(m);
        if (measure == null) {
            throw new IllegalArgumentException("Species " + m + " is unknown!");
        }
        return measure.measure(state);
    }

    @Override
    public Measure<PopulationState> getMeasure(String m) {
        return measuresTable.get(m);
    }

    public void addMeasures(List<Measure<PopulationState>> measures) {
        if (measures != null) {
            measures.forEach(m -> measuresTable.put(m.getName(), m));
        }
    }

    public int indexOf(String label, Object... args) {
        return registry.indexOf(label, args);
    }
}
