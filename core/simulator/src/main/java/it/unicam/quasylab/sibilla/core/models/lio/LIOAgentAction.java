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

/**
 * Identifies an action performed by an agent.
 */
public final class LIOAgentAction {

    private final int index;
    private final String name;

    /**
     * Create a new LIOAgentAction.
     *
     * @param name action name.
     * @param index action index.
     */
    public LIOAgentAction(String name, int index) {
        this.name = name;
        this.index = index;
    }

    /**
     * Return the action index.
     *
     * @return the action index.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Return the action name.
     *
     * @return the action name.
     */
    public String getName() {
        return name;
    }


}
