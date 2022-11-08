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

import it.unicam.quasylab.sibilla.core.models.ImmutableState;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The class <code>YodaSystem</code> represents
 * the enclosing system  available in the simulation
 * Each one has the following components:
 * <ul>
 *     <li>a global state containing the information of the available objects</li>
 *     <li>a list of available agents</li>
 *     <li>a GlobalStateUpdateFunction updating the global state</li>
 * </ul>
 */
public class YodaSystemState<S extends YodaScene> implements ImmutableState {

    private final YodaVariableMapping globalState;
    private final List<YodaAgent> agents;
    private final GlobalStateUpdateFunction globalStateUpdateFunction;
    private final S scene;

    public YodaSystemState(YodaVariableMapping globalState, List<YodaAgent> agents, S scene, GlobalStateUpdateFunction globalStateUpdateFunction) {
        this.globalState = globalState;
        this.agents = agents;
        this.globalStateUpdateFunction = globalStateUpdateFunction;
        this.scene = scene;
    }

    /**
     * This method returns the global state of the system
     *
     * @return the global state of the system
     */
    public YodaVariableMapping getGlobalState() {
        return globalState;
    }

    /**
     * This method returns the entire list of available agents
     *
     * @return the entire list of available agents
     */
    public List<YodaAgent> getAgents() {
        return agents;
    }

    /**
     * This method return a single YodaValue mapped to a variable of an agent with an index
     *
     * @param i an index
     * @param variable the variable we need to search
     * @return return a single YodaValue mapped to a variable of an agent with an index
     */
    public YodaValue getAgentsInfo(int i, YodaVariable variable) {
        return agents.get(i).getAgentInformation().getValue(variable);
    }

    /**
     * This method returns the scene used in the system
     *
     * @return the scene used in the system
     */
    public S getScene() {
        return scene;
    }

    /**
     * This method executes all the necessary methods to update the system
     *
     * @param rg a random generator
     */
    public void updateSystem(RandomGenerator rg){
        produceAllObservations(rg); //All the agents obs are created
        stepAllAgents(rg);          //All the agents do a step forward in the computation
        updateAllAgentInfo(rg);     //All the agents update the external state info
        updateGlobalState(rg);      //The system updates its global state
    }

    public YodaSystemState<S> next(RandomGenerator rg) {
        List<YodaAgent> newAgents = this.agents.stream().map(a -> a.next(rg, this)).collect(Collectors.toList());
        if (globalStateUpdateFunction!=null){
            YodaVariableMapping newGlobal = this.globalStateUpdateFunction.compute(rg, newAgents, this.globalState);
            return new YodaSystemState<>(newGlobal, newAgents, this.scene, this.globalStateUpdateFunction);
        }
        return new YodaSystemState<>(this.globalState, newAgents, this.scene, null);
    }

    /**
     * This method computes all the possible observations in the system for each agent
     *
     * @param rg a random generator
     */
    private void produceAllObservations(RandomGenerator rg) {
        agents.stream().forEach(a -> a.computeObservations(rg, this));
    }

    /**
     * This method updates all the available agents local states
     *
     * @param rg a random generator
     */
    private void stepAllAgents(RandomGenerator rg) {
        agents.stream().forEach(a -> a.step(rg));
    }

    /**
     * This method updates all the available agents global information
     *
     * @param rg a random generator
     */
    public void updateAllAgentInfo(RandomGenerator rg) {
        agents.stream().forEach(a -> a.updateInfo(rg));
    }

    /**
     * This method updates the global state of the system
     *
     * @param rg a random generator
     */
    public void updateGlobalState(RandomGenerator rg) {
        globalStateUpdateFunction.compute(rg, agents, globalState);
    }
}
