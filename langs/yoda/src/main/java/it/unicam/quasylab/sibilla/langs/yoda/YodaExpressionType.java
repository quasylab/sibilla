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

package it.unicam.quasylab.sibilla.langs.yoda;

import it.unicam.quasylab.sibilla.core.models.yoda.YodaType;

/**
 * This class represents the type of values resulting from the evaluation of an expression.
 */
public final class YodaExpressionType {

    private final YodaType type;

    private final boolean isRandom;

    private final boolean isPopulation;

    /**
     * Returns the type of the expression.
     *
     * @return the type of the expression.
     */
    public YodaType getType() {
        return type;
    }

    /**
     * Returns true if the resulting value depends on a random expression.
     *
     * @return true if the resulting value depends on a random expression.
     */
    public boolean isRandom() {
        return isRandom;
    }

    /**
     * Returns true if the resulting value depends on populaiton state.
     * @return true if the resulting value depends on populaiton state.
     */
    public boolean isPopulation() {
        return isPopulation;
    }

    public boolean isAgentDependent() {
        return isAgentDependent;
    }

    private final boolean isAgentDependent;


    public YodaExpressionType(YodaType type, boolean isRandom, boolean isPopulation, boolean isAgentDependent) {
        this.type = type;
        this.isRandom = isRandom;
        this.isPopulation = isPopulation;
        this.isAgentDependent = isAgentDependent;
    }

    public YodaExpressionType cast(YodaType type) {
        return new YodaExpressionType(type, this.isRandom, this.isPopulation, this.isAgentDependent);
    }



}
