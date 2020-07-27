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

package quasylab.sibilla.core.models.pm;

import org.apache.commons.math3.random.RandomGenerator;
import quasylab.sibilla.core.models.MarkovProcess;
import quasylab.sibilla.core.models.StepFunction;
import quasylab.sibilla.core.past.State;
import quasylab.sibilla.core.simulator.sampling.Sample;
import quasylab.sibilla.core.simulator.util.WeightedElement;
import quasylab.sibilla.core.simulator.util.WeightedLinkedList;
import quasylab.sibilla.core.simulator.util.WeightedStructure;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * This class implements a population model. This class is parametrised with
 * respect to types <code>S</code> and and <code>T</code>. The former is the
 * data type used to identify population species in the population vector.
 * Parameter <code>T</code> identifies environment
 *
 * @author loreti
 */
public class PopulationModel implements MarkovProcess<PopulationState>, Serializable {

    private static final long serialVersionUID = 6871037109869821108L;

    private final PopulationModelDefinition modelDefinition;

    private LinkedList<PopulationRule> rules;

    public PopulationModel() {
        this(null);
    }

    public PopulationModel(PopulationModelDefinition modelDefinition) {
        this.rules = new LinkedList<PopulationRule>();
        this.modelDefinition = modelDefinition;
    }

    @Override
    public WeightedStructure<StepFunction<PopulationState>> getTransitions(RandomGenerator r, double now, PopulationState state) {
        WeightedLinkedList<StepFunction<PopulationState>> activities =
                new WeightedLinkedList<>();
        for (PopulationRule rule : rules) {
            PopulationTransition tra = rule.apply(r, now, state);
            if (tra != null) {
                activities.add(
                        new WeightedElement<>(
                                tra.getRate(),
                                (rnd, t, dt) -> state.apply(tra.apply(rnd))
                        )
                );
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


    public void addRule(PopulationRule rule) {
        this.rules.add(rule);
    }

    public void addRules(Collection<PopulationRule> rules) {
        this.rules.addAll(rules);
    }

    @Override
    public PopulationModelDefinition getModelDefinition() {
        return modelDefinition;
    }


    @Override
    public int stateByteArraySize() {
        return modelDefinition.stateArity() * 4;
    }

    @Override
    public byte[] serializeState(PopulationState state) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int vectorSingleValue : state.getPopulationVector()) {
            baos.write(ByteBuffer.allocate(4).putInt(vectorSingleValue).array());
        }
        byte[] toReturn = baos.toByteArray();
        baos.close();
        return toReturn;
    }

    @Override
    public PopulationState deserializeState(byte[] bytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        int length = modelDefinition.stateArity();
        int[] vector = new int[length];
        for (int i = 0; i < length; i++) {
            vector[i] = ByteBuffer.wrap(bais.readNBytes(4)).getInt();
        }
        bais.close();
        return new PopulationState(vector);
    }

    @Override
    public PopulationState deserializeState(ByteArrayInputStream toDeserializeFrom) throws IOException {
        //TODO
        return null;
    }


}
