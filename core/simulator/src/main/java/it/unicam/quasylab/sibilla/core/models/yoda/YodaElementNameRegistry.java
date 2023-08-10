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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class is used to record and build agent names. The usage of this class
 */
public class YodaElementNameRegistry {

    private final Map<String, YodaElementName> names;

    private final Map<String, Set<YodaElementName>> groups;

    public YodaElementNameRegistry() {
        this.names = new HashMap<>();
        this.groups = new HashMap<>();
    }

    /**
     * Returns a new instance of {@link YodaElementName} with the given name, if another instance
     * in this registry already exists with the same name, an {@link IllegalStateException} is
     * thrown.
     *
     * @param name the name of the new created instance.
     * @return a new instance of {@link YodaElementName} with the given name.
     * @throws IllegalStateException is thrown if another instance with the same name already exists
     * in this registry.
     */
    public synchronized YodaElementName newInstance(String name) {
        if (names.containsKey(name)) {
            throw new IllegalStateException("Duplicated agent name: "+name);
        }
        YodaElementName yodaAgentName = new YodaElementName(name, names.size());
        names.put(name, yodaAgentName);
        return yodaAgentName;
    }

    /**
     * Returns the instance with the given name.
     *
     * @param name an agent name.
     * @return the instance with the given name.
     */
    public synchronized YodaElementName get(String name) {
        return this.names.get(name);
    }

    /**
     * Registers all the names in the given set.
     *
     * @param names the name of the agents to register.
     * @throws IllegalStateException is thrown if another instance exists in this registry with a name in
     * the given set.
     */
    public synchronized void create(Set<String> names) {
        names.forEach(this::newInstance);
    }

    public void addGroup(String name, Set<YodaElementName> elements) {
        this.groups.put(name, elements);
    }

    public Set<YodaElementName> getGroup(String name) {
        return this.groups.getOrDefault(name, Set.of());
    }
}
