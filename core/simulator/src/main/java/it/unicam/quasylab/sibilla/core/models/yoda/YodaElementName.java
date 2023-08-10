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

import java.util.Objects;

/**
 * Instances of this class are used to represent the name of an element operating in a YODA system. It is guaranteed that
 * for each pair of instances <code>o1</code> and <code>o2</code>>,
 * <code>o1.agentId == o2.agentID</code> if and only if <code>o1.name.equals(o2.name)</code>.
 * To guarantee this property, agent names should be obtained via a
 * {@link YodaElementNameRegistry}.
 */
public class YodaElementName {

    private final String name;

    private final int elementId;


    /**
     * Creates a new instance with the given name and index.
     *
     * @param name agent name
     * @param agentId agent name id
     */
    protected YodaElementName(String name, int elementId) {
        this.name = name;
        this.elementId = elementId;
    }

    /**
     * Returns the string with this name.
     *
     * @return the string with this name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the id of this name.
     *
     * @return the id of this name.
     */
    public int getElementId() {
        return elementId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YodaElementName that = (YodaElementName) o;
        return elementId == that.elementId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementId);
    }

    @Override
    public String toString() {
        return name;
    }
}
