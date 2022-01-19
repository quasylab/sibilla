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

/**
 * Instances of this class are used to represent the info associated with an
 * agent variable. Each variable is identified by:
 * <ul>
 *     <li>a <i>name</i></li>;
 *     <li>an <i>index</i></li>;
 *     <li>a <i>type.</i></li>.
 * </ul>
 *
 * It is assumed that variables are identified by their indexes. This means that for each pair of
 * <code>AgentVariable</code>s <code>x</code> and <code>y</code>, if <code>x.getIndex()==y.getIndex()</code> then
 * <code>x.getName().equals(y.getName())</code> and <code>x.getType().equals(y.getType())</code>.
 *
 * To support creation of variables, the class {@link VariableRegistry} can be used.
 */
public final class AgentVariable {

    private final String name;
    private final int index;
    private final SlamType type;

    public AgentVariable(String name, int index, SlamType type) {
        this.name = name;
        this.index = index;
        this.type = type;
    }

    /**
     * Returns the name of this variable.
     *
     * @return returns the name of this variable.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the index of this variable.
     *
     * @return the index of this variable.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns the index of this variable.
     *
     * @return the index of this variable.
     */
    public SlamType getType() {
        return type;
    }
}
