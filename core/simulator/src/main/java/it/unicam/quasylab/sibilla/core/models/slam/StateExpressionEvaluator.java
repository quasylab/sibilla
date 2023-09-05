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


import it.unicam.quasylab.sibilla.core.models.slam.agents.SlamAgent;
import it.unicam.quasylab.sibilla.core.models.slam.data.AgentStore;

import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

/**
 * This interface is used to provide a global view of a Slam state and permits evaluating
 * expressions and predicates over the configurations of all the agents.
 */
public interface StateExpressionEvaluator {

    /**
     * Evaluates the expression on all the agents memory and return the min value.
     *
     * @param expr the expression to evaluate.
     * @return the min value among all the evaluations.
     */
    double getMinOf(ToDoubleFunction<AgentStore> expr);

    /**
     * Evaluates the expression on all the agents satisfying the given predicate and return the min value.
     *
     * @param expr expression to evaluate.
     * @param filter filter used to select agents.
     * @return the min value among all the evaluations.
     */
    double getMinOf(ToDoubleFunction<AgentStore> expr, Predicate<SlamAgent> filter);

    /**
     * Evaluates the expression on all the agents satisfying the given predicate and return the max value.
     *
     * @param expr the expression to evaluate.
     * @return the max value among all the evaluations.
     */
    double getMaxOf(ToDoubleFunction<AgentStore> expr);

    /**
     * Evaluates the expression on all the agents satisfying the given predicate and return the max value.
     *
     * @param expr expression to evaluate.
     * @param filter filter used to select agents.
     * @return the max value among all the evaluations.
     */
    double getMaxOf(ToDoubleFunction<AgentStore> expr, Predicate<SlamAgent> filter);

    /**
     * Evaluates the expression on all the agents satisfying the given predicate and return the mean value.
     *
     * @param expr expression to evaluate.
     * @return the mean value among all the evaluations.
     */
    double getMeanOf(ToDoubleFunction<AgentStore> expr);

    /**
     * Evaluates the expression on all the agents satisfying the given predicate and return the mean value.
     *
     * @param expr expression to evaluate.
     * @param filter filter used to select agents.
     * @return the mean value among all the evaluations.
     */
    double getMeanOf(ToDoubleFunction<AgentStore> expr, Predicate<SlamAgent> filter);

    /**
     * Checks if there exists an agent satisfying the given predicate.
     *
     * @param p a predicate on agent memory
     * @return true there exists an agent satisfying the given predicate.
     */
    boolean exists(Predicate<SlamAgent> p);

    /**
     * Checks if there all the agents satisfy the given predicate.
     *
     * @param p a predicate on agent memory
     * @return true there all the agents satisfying the given predicate.
     */
    boolean forAll(Predicate<SlamAgent> p);

    /**
     * Returns the sum of the given expression evaluated with the store of the agents satisfying the given predicate.
     *
     * @param expr the expression to sum
     * @param filter the predicate used to select the agents
     * @return the sum of the given expression evaluated with the store of the agents satisfying the given predicate.
     */
    double getSumOf(ToDoubleFunction<AgentStore> expr, Predicate<SlamAgent> filter);

    /**
     * Returns the sum of the given expression evaluated with the store of all the agents.
     *
     * @param expr the expression to sum
     * @return the sum of the given expression evaluated with the store of the all the agents.
     */
    double getSumOf(ToDoubleFunction<AgentStore> expr);

    /**
     * Returns the number of agents satisfying the given predicate.
     *
     * @param filter the predicate used to select agents to cound.
     * @return the number of agents satisfying the given predicate
     */
    int count(Predicate<SlamAgent> filter);

}
