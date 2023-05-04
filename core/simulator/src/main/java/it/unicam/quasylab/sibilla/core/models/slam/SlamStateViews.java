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

package it.unicam.quasylab.sibilla.core.models.slam;

import it.unicam.quasylab.sibilla.core.models.slam.data.AgentStore;
import it.unicam.quasylab.sibilla.core.models.slam.data.SlamValue;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * This class aggregates the views that can be associated at a state. These are functions that extract data
 * from a state. Two kinds of views are considered: local views and global views. Local views depend on the state
 * of a specific agent, while global views are computed by considering values from a global point of view.
 */
public final class SlamStateViews {

    private final Map<String, Predicate<SlamState>>  globalPredicate = new HashMap<>();
    private final Map<String, Function<SlamState, SlamValue>> globalViews = new HashMap<>();
    private final Map<String, Function<AgentStore, Map<String, Function<SlamState, SlamValue>>>> localViews = new HashMap<>();

    /**
     * Returns the global predicate with the given name.
     *
     * @param name name of a predicate.
     * @return the global predicate with the given name.
     */
    public Predicate<SlamState> getPredicate(String name) {
        return globalPredicate.get(name);
    }

    /**
     * Records a global predicate with the given name.
     *
     * @param name name of the recorded predicate.
     * @param predicate a predicate.
     */
    public void setPredicate(String name, Predicate<SlamState> predicate) {
        this.globalPredicate.put(name, predicate);
    }

    /**
     * Returns the global view associated with the given name.
     *
     * @param name name of a global view.
     * @return the function associated with the given view.
     */
    public Function<SlamState, SlamValue> getGlobalView(String name) {
        return globalViews.get(name);
    }

    /**
     * Sets the global view associated with the given name.
     *
     * @param name name of a global view.
     * @param globalFunction the function associated with the given view.
     */
    public void setGlovalView(String name, Function<SlamState, SlamValue> globalFunction) {
        this.globalViews.put(name, globalFunction);
    }

    /**
     * Returns the function used to build local views of the given agent.
     *
     * @param agentName agent name.
     * @return the function used to build local views of the given agent.
     */
    public Function<AgentStore, Map<String, Function<SlamState, SlamValue>>> getLocalView(String agentName) {
        return localViews.get(agentName);
    }

    /**
     * Sets the function used to build local views of the given agent.
     *
     * @param agentName agent name.
     * @param localFunctions the function used to build local views of the given agent.
     */
    public void setLocalView(String agentName, Function<AgentStore, Map<String, Function<SlamState, SlamValue>>> localFunctions) {
        this.localViews.put(agentName, localFunctions);
    }

}
