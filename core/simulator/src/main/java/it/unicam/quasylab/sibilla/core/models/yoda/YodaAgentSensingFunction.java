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

import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * The functional interface is used to describe a function used to compute the observations of an
 * agent.
 */
@FunctionalInterface
public interface YodaAgentSensingFunction {

    /**
     * Returns the variable mapping containing the value sensed by the given agent in the given system.
     *
     * @param rg random generator used to sample random values
     * @param system system state where the agent is running
     * @param agent agent for which the sensing values are computed
     * @return the variable mapping containing the value sensed by the given agent in the given system.
     */
    YodaVariableMapping compute(RandomGenerator rg, YodaSystemState system, YodaAgent agent);

    /**
     * Returns the sensing function obtained from the computation of single attributes.
     *
     * @param sensingFunctionMap a map associating each attribute with the function used to sense its value.
     * @return the sensing function obtained from the computation of single attributes.
     */
    static YodaAgentSensingFunction of(Map<YodaVariable, YodaAttributeSensingFunction> sensingFunctionMap) {
        return (rg, system, agent) -> new YodaVariableMapping(sensingFunctionMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().apply(rg, system, agent))));
    }

}
