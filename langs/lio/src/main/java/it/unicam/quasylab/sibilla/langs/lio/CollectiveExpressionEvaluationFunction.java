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

package it.unicam.quasylab.sibilla.langs.lio;

import it.unicam.quasylab.sibilla.core.models.lio.LIOCollective;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

/**
 * This functional interface represents the evaluation of an expression over a given collective.
 */
@FunctionalInterface
public interface CollectiveExpressionEvaluationFunction {

    /**
     * Returns the evaluation of this expression on the given collective.
     *
     * @param collective collective used to evaluate this expression.
     * @return the evaluation of this expression on the given collective.
     */
    SibillaValue eval(LIOCollective collective);


    default double toDouble(LIOCollective collective) {
        return eval(collective).doubleOf();
    }

    default boolean toBoolean(LIOCollective collective) {
        return eval(collective).booleanOf();
    }
}
