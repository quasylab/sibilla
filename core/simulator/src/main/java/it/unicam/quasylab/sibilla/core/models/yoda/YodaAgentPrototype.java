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


import it.unicam.quasylab.sibilla.core.util.values.SibillaRandomBiFunction;

public class YodaAgentPrototype {

    private final YodaElementName agentName;

    private final YodaVariableMapping initialAgentAttributes;

    private final YodaVariableMapping initialEnvironmentalAttributes;

    private final YodaVariableMapping initialAgentObservations;

    private YodaBehaviour agentBehaviour;

    private SibillaRandomBiFunction<YodaSystemState, YodaAgent, YodaVariableMapping> observationsUpdateFunction;
    private SibillaRandomBiFunction<YodaVariableMapping, YodaVariableMapping, YodaVariableMapping> environmentalAttributeUpdateFunction;


    public YodaAgentPrototype(YodaElementName agentName, YodaVariableMapping initialAgentAttributes, YodaVariableMapping initialEnvironmentalAttributes, YodaVariableMapping initialAgentObservations) {
        this.agentName = agentName;
        this.initialAgentAttributes = initialAgentAttributes;
        this.initialEnvironmentalAttributes = initialEnvironmentalAttributes;
        this.initialAgentObservations = initialAgentObservations;
    }

    public YodaAgent getAgent(YodaVariableMapping initialAssignment, int agentId) {
        return new YodaAgent(agentId,
                agentName,
                initialAgentAttributes.setAll(initialAssignment),
                initialEnvironmentalAttributes.setAll(initialAssignment),
                initialAgentObservations.setAll(initialAssignment), agentBehaviour, observationsUpdateFunction, environmentalAttributeUpdateFunction);
    }

    public YodaElementName getAgentName() {
        return this.agentName;
    }

    public YodaVariableMapping getInitialAgentAttributes() {
        return initialAgentAttributes;
    }

    public YodaVariableMapping getInitialEnvironmentalAttributes() {
        return initialEnvironmentalAttributes;
    }

    public YodaVariableMapping getInitialAgentObservations() {
        return initialAgentObservations;
    }

    public YodaBehaviour getAgentBehaviour() {
        return agentBehaviour;
    }

    public void setAgentBehaviour(YodaBehaviour agentBehaviour) {
        this.agentBehaviour = agentBehaviour;
    }

    public SibillaRandomBiFunction<YodaSystemState, YodaAgent, YodaVariableMapping> getObservationsUpdateFunction() {
        return observationsUpdateFunction;
    }

    public void setObservationsUpdateFunction(SibillaRandomBiFunction<YodaSystemState, YodaAgent, YodaVariableMapping> observationsUpdateFunction) {
        this.observationsUpdateFunction = observationsUpdateFunction;
    }

    public SibillaRandomBiFunction<YodaVariableMapping, YodaVariableMapping, YodaVariableMapping> getEnvironmentalAttributeUpdateFunction() {
        return environmentalAttributeUpdateFunction;
    }

    public void setEnvironmentalAttributeUpdateFunction(SibillaRandomBiFunction<YodaVariableMapping, YodaVariableMapping, YodaVariableMapping> environmentalAttributeUpdateFunction) {
        this.environmentalAttributeUpdateFunction = environmentalAttributeUpdateFunction;
    }

    public void setAgentSensing(SibillaRandomBiFunction<YodaSystemState, YodaAgent, YodaVariableMapping> observationsUpdateFunction) {
        this.observationsUpdateFunction = observationsUpdateFunction;
    }

}



