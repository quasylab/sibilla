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

package it.unicam.quasylab.sibilla.core.models.lio;

import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LIOConfigurationsSupplier {

    private final LIOAgentDefinitions agentsDefinition;
    private final Map<String, Integer> configurationsArity;
    private final Map<String, Function<SibillaValue[], Map<LIOAgent, Integer>>> configurations;
    private final String defaultConfiguration;

    public LIOConfigurationsSupplier(LIOAgentDefinitions agentsDefinition, Map<String, Integer> systemArity, Map<String, Function<SibillaValue[], Map<LIOAgent, Integer>>> systems, String defaultConfiguration) {
        this.agentsDefinition = agentsDefinition;
        this.configurationsArity = systemArity;
        this.configurations = systems;
        this.defaultConfiguration = defaultConfiguration;
    }

    public boolean isDefined(String name) {
        return configurations.containsKey(name);
    }

    public int arity() {
        return arity(getDefaultConfiguration());
    }

    public int arity(String name) {
        return configurationsArity.getOrDefault(name, -1);
    }


    private String getDefaultConfiguration() {
        return defaultConfiguration;
    }

    public String[] states() {
        return configurations.keySet().stream().sorted().toArray(String[]::new);
    }

    public Function<RandomGenerator, LIOState> getDefault(double[] args) {
        return getConfiguration(defaultConfiguration, args);
    }

    private Function<RandomGenerator, LIOState> getConfiguration(String name, double[] args) {
        LIOState state = getConfigurationOfCountingElements(name, args);
        return rg -> state;
    }

    public String getInfo(String name) {
        return "Info!";
    }


    public LIOState getConfigurationOfCountingElements(String name, double[] args) {
        Function<SibillaValue[], Map<LIOAgent, Integer>> agentBuildingFunctionList = getAgentBuildingFunctionList(name, args);
        SibillaValue[] sibillaValuesArgs = SibillaValue.of(args);
        int[] countingState = new int[agentsDefinition.numberOfAgents()];
        Map<LIOAgent, Integer> countingMap = agentBuildingFunctionList.apply(sibillaValuesArgs);
        countingMap.forEach((a,i) -> countingState[a.getIndex()] += i);
        return new LIOCountingState(agentsDefinition, countingState);
    }

    private Function<SibillaValue[], Map<LIOAgent, Integer>> getAgentBuildingFunctionList(String name, double[] args) {
        Function<SibillaValue[], Map<LIOAgent, Integer>> agentBuildingFunctionList = this.configurations.get(name);
        if (agentBuildingFunctionList == null) {
            throw new IllegalArgumentException(String.format("Configuration %s is unknown", name));
        }
        if (args.length != this.configurationsArity.get(name)) {
            throw new IllegalArgumentException(String.format("Illegal number of parameters for configuration %s: expected %d, are %d", name, this.configurationsArity.get(name), args.length));
        }
        return agentBuildingFunctionList;
    }

    public LIOState getConfigurationOfIndividuals(String name, double[] args) {
        Function<SibillaValue[], Map<LIOAgent, Integer>> agentBuildingFunctionList = getAgentBuildingFunctionList(name, args);
        SibillaValue[] sibillaValuesArgs = SibillaValue.of(args);
        List<LIOAgent> agents = agentBuildingFunctionList.apply(sibillaValuesArgs)
                .entrySet().stream().flatMap(e -> Collections.nCopies(e.getValue(), e.getKey()).stream())
                .collect(Collectors.toList());
        return new LIOIndividualState(agentsDefinition, new ArrayList<>(agents));
    }

    public String getDefaultConfigurationName() {
        return defaultConfiguration;
    }
}
