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

import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * This functional interface is used to represent the functions used to compute the value of a
 * sensing attribute of an agent.
 */
@FunctionalInterface
public interface YodaAttributeSensingFunction {

    /**
     * Returns the value perceived by the given agent when it is running in the given system.
     *
     * @param rg random generator used to sample random values
     * @param state the system state where the agent is operating
     * @param agent the agent that is perceiving the value
     * @return the value perceived by the given agent when it is running in the given system.
     */
    SibillaValue apply(RandomGenerator rg, YodaSystemState state, YodaAgent agent);

}
