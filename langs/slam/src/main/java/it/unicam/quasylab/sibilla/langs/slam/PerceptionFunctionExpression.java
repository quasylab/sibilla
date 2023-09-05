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

package it.unicam.quasylab.sibilla.langs.slam;

import it.unicam.quasylab.sibilla.core.models.slam.StateExpressionEvaluator;
import it.unicam.quasylab.sibilla.core.models.slam.data.AgentStore;
import it.unicam.quasylab.sibilla.core.models.slam.data.AgentVariable;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.function.UnaryOperator;

public interface PerceptionFunctionExpression {

    SibillaValue apply(RandomGenerator rg, StateExpressionEvaluator stateExpressionEvaluator, AgentStore store);

    static AgentStore perceive(Map<AgentVariable, PerceptionFunctionExpression> assignments, RandomGenerator rg, StateExpressionEvaluator stateExpressionEvaluator, AgentStore store) {
        AgentStore current = store;
        for (Map.Entry<AgentVariable, PerceptionFunctionExpression> assignment: assignments.entrySet()) {
            current = current.set(assignment.getKey(), assignment.getValue().apply(rg, stateExpressionEvaluator, store));
        }
        return current;
    }

    static PerceptionFunctionExpression apply(DoubleUnaryOperator op, PerceptionFunctionExpression function) {
        return (rg, stateExpressionEvaluator, store) -> SibillaValue.apply(op, function.apply(rg, stateExpressionEvaluator, store));
    }

    static PerceptionFunctionExpression apply(DoubleBinaryOperator op, PerceptionFunctionExpression f, PerceptionFunctionExpression g) {
        return (rg, stateExpressionEvaluator, store) -> SibillaValue.apply(op, f.apply(rg, stateExpressionEvaluator, store), g.apply(rg, stateExpressionEvaluator, store));
    }

    static PerceptionFunctionExpression apply(UnaryOperator<SibillaValue> op, PerceptionFunctionExpression function) {
        return (rg, stateExpressionEvaluator, store) -> op.apply(function.apply(rg, stateExpressionEvaluator, store));
    }

    static PerceptionFunctionExpression apply(BinaryOperator<SibillaValue> op, PerceptionFunctionExpression f, PerceptionFunctionExpression g) {
        return (rg, stateExpressionEvaluator, store) -> op.apply(f.apply(rg, stateExpressionEvaluator, store), g.apply(rg, stateExpressionEvaluator, store));
    }

}
