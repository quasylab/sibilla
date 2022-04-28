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

package it.unicam.quasylab.sibilla.core.models.lgio;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a function associating each action with probability value.
 */
public class ActionProbabilityFunction {

    private final Map<AgentAction, Double> actionProbabilities;

    /**
     * Creates a new function associating to each action <code>act</code> the probability
     * <code>actionProbabilities.getOrDefault(act,0.0)</code>.
     * @param actionProbabilities map associating each action with a probability value.
     */
    public ActionProbabilityFunction(Map<AgentAction, Double> actionProbabilities) {
        this.actionProbabilities = actionProbabilities;
    }

    public ActionProbabilityFunction() {
        this(new HashMap<>());
    }

    public ActionProbabilityFunction sum(ActionProbabilityFunction other) {
        Map<AgentAction, Double> newProbabilityMapping = new HashMap<>(this.actionProbabilities);
        other.actionProbabilities.forEach((k,v) -> newProbabilityMapping.merge(k, v, Double::sum));
        return new ActionProbabilityFunction(newProbabilityMapping);
    }

    public ActionProbabilityFunction diff(ActionProbabilityFunction other) {
        Map<AgentAction, Double> newProbabilityMapping = new HashMap<>(this.actionProbabilities);
        other.actionProbabilities.forEach((k,v) -> newProbabilityMapping.merge(k, v, (v1, v2) -> v1-v2));
        return new ActionProbabilityFunction(newProbabilityMapping);
    }

    public ActionProbabilityFunction times(ActionProbabilityFunction other) {
        Map<AgentAction, Double> newProbabilityMapping = new HashMap<>(this.actionProbabilities);
        other.actionProbabilities.forEach((k,v) -> newProbabilityMapping.merge(k, v, (v1, v2) -> v1-v2));
        return new ActionProbabilityFunction(newProbabilityMapping);
    }

    public double get(AgentAction action) {
        return this.actionProbabilities.getOrDefault(action, 0.0);
    }

}
