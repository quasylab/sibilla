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

package it.unicam.quasylab.sibilla.core.models.markov;

import it.unicam.quasylab.sibilla.core.models.ContinuousTimeMarkovProcess;
import it.unicam.quasylab.sibilla.core.models.StepFunction;
import it.unicam.quasylab.sibilla.core.models.util.MappingState;
import it.unicam.quasylab.sibilla.core.models.util.VariableTable;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import it.unicam.quasylab.sibilla.core.simulator.util.WeightedLinkedList;
import it.unicam.quasylab.sibilla.core.simulator.util.WeightedStructure;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.List;
import java.util.Map;

/**
 * This class is used to identify CTMCs.
 */
public class CTMCModel extends MarkovChainModel implements ContinuousTimeMarkovProcess<MappingState> {

    /**
     * Create a new CTMC whose states have the given variables and transition probability is induced by the given function.
     *
     * @param stateVariables state variables.
     * @param rules function used to induce transition probability.
     * @param measuresTable declared measures.
     */
    public CTMCModel(VariableTable stateVariables, List<MappingStateUpdate> rules, Map<String, Measure<? super MappingState>> measuresTable) {
        super(stateVariables, rules, measuresTable);
    }

    @Override
    public WeightedStructure<? extends StepFunction<MappingState>> getTransitions(RandomGenerator r, double time, MappingState state) {
        WeightedLinkedList<StepFunction<MappingState>> result = new WeightedLinkedList<>();
        for (MappingStateUpdate update: this.rules) {
            if (update.isEnabled(state)) {
                double weight = update.weightOf(state);
                if (weight>0) {
                    result.add(weight, ((r1, now, dt) -> update.apply(state)));
                }
            }
        }
        return result;
    }

}
