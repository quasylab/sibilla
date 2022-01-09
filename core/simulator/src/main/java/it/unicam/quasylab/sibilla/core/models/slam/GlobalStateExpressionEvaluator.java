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


import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

/**
 * This interface is used to provide a global view of a Slam state and permits evaluating
 * expressions and predicates over the configurations of all the agents.
 */
public interface GlobalStateExpressionEvaluator {

    /**
     * Evaluates the expression on all the agents memory and return the min value.
     *
     * @param expr the expression to evaluate.
     * @return the min value among all the evaluations.
     */
    double getMinOf(ToDoubleFunction<AgentMemory> expr);

    /**
     * Evaluates the expression on all the agents satisfying the given predicate and return the min value.
     *
     * @param expr expression to evaluate.
     * @param filter filter used to select agents.
     * @return the min value among all the evaluations.
     */
    double getMinOf(ToDoubleFunction<AgentMemory> expr, Predicate<Agent> filter);

    /**
     * Evaluates the expression on all the agents satisfying the given predicate and return the max value.
     *
     * @param expr the expression to evaluate.
     * @return the max value among all the evaluations.
     */
    double getMaxOf(ToDoubleFunction<AgentMemory> expr);

    /**
     * Evaluates the expression on all the agents satisfying the given predicate and return the max value.
     *
     * @param expr expression to evaluate.
     * @param filter filter used to select agents.
     * @return the max value among all the evaluations.
     */
    double getMaxOf(ToDoubleFunction<AgentMemory> expr, Predicate<Agent> filter);

    /**
     * Evaluates the expression on all the agents satisfying the given predicate and return the mean value.
     *
     * @param expr expression to evaluate.
     * @return the mean value among all the evaluations.
     */
    double getMeanOf(ToDoubleFunction<AgentMemory> expr);

    /**
     * Evaluates the expression on all the agents satisfying the given predicate and return the mean value.
     *
     * @param expr expression to evaluate.
     * @param filter filter used to select agents.
     * @return the mean value among all the evaluations.
     */
    double getMeanOf(ToDoubleFunction<AgentMemory> expr, Predicate<Agent> filter);

    /**
     * Checks if there exists an agent satisfying the given predicate.
     *
     * @param p a predicate on agent memory
     * @return true there exists an agent satisfying the given predicate.
     */
    boolean exists(Predicate<Agent> p);

    /**
     * Checks if there all the agents satisfy the given predicate.
     *
     * @param p a predicate on agent memory
     * @return true there all the agents satisfying the given predicate.
     */
    boolean forAll(Predicate<Agent> p);

}
