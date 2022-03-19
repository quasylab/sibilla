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

import it.unicam.quasylab.sibilla.core.models.EvaluationEnvironment;
import it.unicam.quasylab.sibilla.core.models.TimeHomogeneousStepFunction;
import it.unicam.quasylab.sibilla.core.models.util.MappingState;
import it.unicam.quasylab.sibilla.core.models.util.VariableTable;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import it.unicam.quasylab.sibilla.core.simulator.util.WeightedStructure;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Identifies a function that can be used to build a MarkovChainModel.
 *
 * @param <M> class of build markov chain.
 */
@FunctionalInterface
public interface MarkovChainBuilder<M extends MarkovChainModel> {

    M apply(VariableTable variableTable, List<MappingStateUpdate> rules, Map<String, Measure<? super MappingState>> measuresTable);

}
