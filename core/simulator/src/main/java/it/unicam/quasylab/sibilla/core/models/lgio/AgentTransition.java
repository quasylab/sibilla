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

import java.util.function.Predicate;

/**
 * Model the possible behaviour of an agent triggered by the execution of an action.
 */
public class AgentTransition {

    private final Predicate<AgentAttributes> guard;
    private final AgentAction action;
    private final AgentStep step;

    /**
     * Creates a new transition that is enabled when the <code>guard</code> is satisfied. The transition
     * consists in executing the given <code>action</code> and triggering the given <code>step</code>.
     *
     * @param guard predicate indicating when the transition is enabled.
     * @param action action executed in the transition.
     * @param step effect of the transition.
     */
    public AgentTransition(Predicate<AgentAttributes> guard, AgentAction action, AgentStep step) {
        this.guard = guard;
        this.action = action;
        this.step = step;
    }

    public AgentTransition(AgentAction action, AgentStep step) {
        this(null, action, step);
    }

    public Predicate<AgentAttributes> getGuard() {
        return guard;
    }

    public AgentAction getAction() {
        return action;
    }

    public AgentStep getStep() {
        return step;
    }

    public boolean isEnabled(AgentAttributes attributes) {
        return (guard == null || guard.test(attributes));
    }
}
