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

import java.util.Objects;

/**
 * This is a class that is used to identify univocally an agent behaviour. It consists of an agentId, that is an
 * integer, and an agentName, that is a nonempty String.
 */
public final class AgentName {

    /**
     * This is a unique id that is associated to each agent name. The
     */
    private final int agentId;

    private final String agentName;


    /**
     * Creates a new agent name with the given id and the given name.
     *
     * @param agentId agent id.
     * @param agentName agent name.
     */
    AgentName(int agentId, String agentName) {
        Objects.requireNonNull(agentName);
        this.agentId = agentId;
        this.agentName = agentName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgentName agentName1 = (AgentName) o;
        return agentId == agentName1.agentId;
    }

    @Override
    public int hashCode() {
        return agentId;
    }

    /**
     * Returns the agent id.
     *
     * @return  the agent id.
     */
    public int getAgentId() {
        return agentId;
    }

    /**
     * Returns the agent name.
     * @return  the agent name.
     */
    public String getAgentName() {
        return agentName;
    }
}
