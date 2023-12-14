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

import it.unicam.quasylab.sibilla.core.simulator.util.WeightedStructure;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.function.ToDoubleFunction;

/**
 * The class <code>YodaAgent</code> represents
 * the agents available in the simulation
 * Each one has the following components:
 * <ul>
 *     <li>a unique identifier</li>
 *     <li>a unique name string</li>
 *     <li>a local state containing the information known to the agent</li>
 *     <li>a external state containing the information unknown to the agent</li>
 *     <li>a set of observations done by the agent</li>
 *     <li>a behaviour that select the actions</li>
 *     <li>a YodaAgentSensingFunction to determine the observations</li>
 *     <li>a YodaAgentEnvironmentalAttributeUpdateFunction to update the external state of the agent</li>
 * </ul>
 *
 */
public final class YodaAgent extends YodaSceneElement {

    private final YodaVariableMapping agentAttributes;
    private final YodaVariableMapping agentObservations;
    private final YodaBehaviour agentBehaviour;
    private final YodaAgentSensingFunction observationsUpdateFunction;
    private final YodaAgentEnvironmentalAttributeUpdateFunction environmentalAttributeUpdateFunction;

    /**
     * Creates a new instance with the given parameters.
     *
     * @param identifier
     * @param agentName
     * @param agentAttributes
     * @param environmentalAttributes
     * @param agentObservations
     * @param agentBehaviour
     * @param observationsUpdateFunction
     * @param environmentalAttributeUpdateFunction
     */
    public YodaAgent(int identifier,
                     YodaElementName agentName,
                     YodaVariableMapping agentAttributes,
                     YodaVariableMapping environmentalAttributes,
                     YodaVariableMapping agentObservations,
                     YodaBehaviour agentBehaviour,
                     YodaAgentSensingFunction observationsUpdateFunction,
                     YodaAgentEnvironmentalAttributeUpdateFunction environmentalAttributeUpdateFunction) {
        super(agentName, identifier, environmentalAttributes);
        this.agentAttributes = agentAttributes;
        this.agentObservations = agentObservations;
        this.agentBehaviour = agentBehaviour;
        this.observationsUpdateFunction = observationsUpdateFunction;
        this.environmentalAttributeUpdateFunction = environmentalAttributeUpdateFunction;
    }


    /**
     * This method returns the agent local state.
     *
     * @return the agent local state
     */
    public YodaVariableMapping getAgentAttributes() {
        return agentAttributes;
    }

    /**
     * This method returns the agent observations
     *
     * @return the agent observations
     */
    public YodaVariableMapping getAgentObservations() {
        return agentObservations;
    }

    /**
     * This method returns the agent behaviour
     *
     * @return the agent behaviour
     */
    public YodaBehaviour getAgentBehaviour() {
        return agentBehaviour;
    }


    /**
     * Returns the next state of this agent.
     *
     * @param rg random generator used to evaluate rando expressions
     * @param state global system state.
     * @return the next state of this agent.
     */
    public YodaAgent next(RandomGenerator rg, YodaSystemState state) {
        YodaVariableMapping newObservations = observe(rg, state);
        WeightedStructure<YodaAction> actionSet = this.agentBehaviour.evaluate(this.agentAttributes, newObservations);
        YodaAction selectedAction = this.agentBehaviour.selectAction(rg, actionSet);
        YodaVariableMapping newKnowledge = this.agentAttributes;
        if (selectedAction != null) {
            newKnowledge = selectedAction.performAction(rg, this.agentAttributes);
        }
        YodaVariableMapping newEnvironmentalAttributes = this.environmentalAttributeUpdateFunction.compute(rg, newKnowledge, this.environmentalAttributes);
        return new YodaAgent(this.getId(), this.getName(), newKnowledge, newEnvironmentalAttributes, newObservations, this.agentBehaviour, this.observationsUpdateFunction, this.environmentalAttributeUpdateFunction);
    }

    /**
     * Returns the agent observations when it is running in a given system state.
     *
     * @param rg random generator used to sample random values
     * @param state the state where the observations are computed
     * @return the agent observations when it is running in a given system state.
     */
    public YodaVariableMapping observe(RandomGenerator rg, YodaSystemState state) {
        return this.observationsUpdateFunction.compute(rg, state, this);
    }

    public SibillaValue get(YodaVariable var) {
        if (agentAttributes.isDefined(var)) {
            return agentAttributes.getValue(var);
        }
        if (agentObservations.isDefined(var)) {
            return agentObservations.getValue(var);
        }
        return environmentalAttributes.getValue(var);
    }

    public Map<String, ToDoubleFunction<YodaSystemState>> getTraceFunctions() {
        HashMap<String, ToDoubleFunction<YodaSystemState>> result = new HashMap<>();
        this.agentAttributes.forEach((var, val) -> result.put(var.getName(), s -> s.get(this.getId()).get(var).doubleOf()));
        this.agentObservations.forEach((var, val) -> result.put(var.getName(), s -> s.get(this.getId()).get(var).doubleOf()));
        return result;
    }

}
