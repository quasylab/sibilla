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

import java.io.Serializable;

/**
 * The interface <code>YodaVariableMapping</code> represents
 * the mapping between a variable and a value
 */
public interface YodaVariableMapping extends Serializable {

    /**
     * This method returns the value associated to an input variable
     *
     * @param variable the variable to search
     * @return the value associated to an input variable
     */
    YodaValue getValue(YodaVariable variable);

    /**
     * This method sets an input value to a certain input variable
     *
     * @param variable an input variable
     * @param value an input value
     */
    void setValue(YodaVariable variable, YodaValue value);

    /**
     * This method returns a copy of the original map
     *
     * @return a copy of the original map
     */
    YodaVariableMapping copy();

}
