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

import java.util.Objects;

/**
 * Identifies the action an agent can execute.
 */
public class AgentAction {

    private final String actionName;

    /**
     * Creates an action with the given name.
     *
     * @param actionName action name.
     */
    public AgentAction(String actionName) {
        this.actionName = actionName;
    }


    /**
     * Returns the action name.
     * @return  the action name.
     */
    public String getActionName() {
        return actionName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgentAction that = (AgentAction) o;
        return Objects.equals(getActionName(), that.getActionName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getActionName());
    }

    @Override
    public String toString() {
        return getActionName();
    }
}
