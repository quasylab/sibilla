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
import java.util.Map;

/**
 * An instance of this class is used to record all the variables that occurr in a Yoda specification.
 */
public class YodaVariableRegistry {

    private final Map<String, YodaVariable> variables = new HashMap<>();

    /**
     * Returns the variable with the given name or null if it does not exist.
     *
     * @param name vatiable name
     * @return the variable with the given name or null if it does not exist.
     */
    public synchronized YodaVariable get(String name) {
        return variables.get(name);
    }

    /**
     * Returns true if a variable with the given name exists in this registry.
     *
     * @param name variable name.
     * @return true if a variable with the given name exists in this registry.
     */
    public synchronized boolean exists(String name) {
        return variables.containsKey(name);
    }

    /**
     * This method is invoked to create a new variable with the given name. It returns true if the variable has
     * been successfully created, false if a variable with the same name already exists in this registry.
     *
     * @param name variable name.
     * @return true if the variable has been successfully created, false if a variable with the same name
     * already exists in this registry.
     */
    public synchronized boolean add(String name) {
        if (this.variables.containsKey(name)) {
            return false;
        } else {
            this.variables.put(name, new YodaVariable(this.variables.size(), name));
            return true;
        }
    }

}
