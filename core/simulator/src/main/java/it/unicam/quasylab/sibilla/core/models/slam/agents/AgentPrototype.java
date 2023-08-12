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

package it.unicam.quasylab.sibilla.core.models.slam.agents;

import it.unicam.quasylab.sibilla.core.models.slam.*;
import it.unicam.quasylab.sibilla.core.models.slam.data.AgentStore;
import it.unicam.quasylab.sibilla.core.models.slam.data.SlamType;
import it.unicam.quasylab.sibilla.core.models.slam.data.SlamValue;

import java.util.function.Function;

/**
 * Instances of this class are used to build an LIOAgent given a set of arguments.
 */
public class AgentPrototype {
    private final AgentName agentName;
    private final SlamType[] parameters;
    private Function<SlamValue[], AgentStore> storeProvider = s -> new AgentStore();
    private AgentBehaviourOld agentBehaviour = null;
    private AgentTimePassingFunction timePassingFunctionProvider =  (r, t, s) -> {};
    private PerceptionFunction perceptionFunction = (r, s, m) -> {};


    /**
     * Creates a new prototytpe.
     *
     * @param agentName                     agent name.
     */
    public AgentPrototype(AgentName agentName, SlamType[] parameters) {
        this.agentName = agentName;
        this.parameters = parameters;
    }


    /**
     * Returns a new agent having the given id and built by using the given values.
     *
     * @param agentId   agent id of the returned agent.
     * @param values    values used to build the agent.
     * @return          a new agent having the given id and built by using the given values.
     */
    public Agent getAgent(int agentId, SlamValue[] values) {
        return new Agent(this,
                agentId,
                storeProvider.apply(values),
                agentBehaviour.initialState(),
                timePassingFunctionProvider,
                perceptionFunction
        );
    }

    /**
     * Returns the agent factory function used to create an agent from the given value.
     *
     * @param values values used to build the agent in the returned factory.
     * @return the agent factory function used to create an agent from the given value.
     */
    public AgentFactory getAgentFactory(SlamValue[] values) {
        return i -> this.getAgent(i, values);
    }

    /**
     * Returns the array of parameters of this template.
     * @return the array of parameters of this template.
     */
    public SlamType[] getParameters() {
        return parameters;
    }

    /**
     * Returns the function used to build the agent store.
     * @return the function used to build the agent store.
     */
    public Function<SlamValue[], AgentStore> getStoreProvider() {
        return storeProvider;
    }

    /**
     * Sets the function used to build the agent store.
     * @param storeProvider the function to use to build the agent store.
     */
    public void setStoreProvider(Function<SlamValue[], AgentStore> storeProvider) {
        this.storeProvider = storeProvider;
    }

    /**
     * Returns the behaviour of this template.
     * @return the behaviour of this template.
     */
    public AgentBehaviourOld getAgentBehaviour() {
        return agentBehaviour;
    }

    /**
     * Sets the behaviour of this template.
     *
     * @param agentBehaviour the behaviour to use in this template.
     */
    public void setAgentBehaviour(AgentBehaviourOld agentBehaviour) {
        this.agentBehaviour = agentBehaviour;
    }

    /**
     * Returns the time passing function associated with this template.
     *
     * @return the time passing function associated with this template.
     */
    public AgentTimePassingFunction getTimePassingFunctionProvider() {
        return timePassingFunctionProvider;
    }

    /**
     * Sets the time passing function.
     *
     * @param timePassingFunctionProvider the time passing function to use.
     */
    public void setTimePassingFunctionProvider(AgentTimePassingFunction timePassingFunctionProvider) {
        this.timePassingFunctionProvider = timePassingFunctionProvider;
    }

    /**
     * Returns the perception function of this template.
     *
     * @return the perception function of this template.
     */
    public PerceptionFunction getPerceptionFunction() {
        return perceptionFunction;
    }

    /**
     * Sets the perception function to use in this template.
     * @param perceptionFunction the perception function to use in this template.
     */
    public void setPerceptionFunction(PerceptionFunction perceptionFunction) {
        this.perceptionFunction = perceptionFunction;
    }

    /**
     * Returns the name of this template.
     *
     * @return the name of this template.
     */
    public AgentName getAgentName() {
        return agentName;
    }
}
