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

import it.unicam.quasylab.sibilla.core.models.lio.Agent;
import it.unicam.quasylab.sibilla.core.models.lio.AgentsDefinition;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import it.unicam.quasylab.sibilla.langs.util.ErrorCollector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class LIOModelAgentDependentChecker extends LIOModelParseTreeChecker {
    protected final AgentsDefinition definition;

    protected final Map<String, SibillaValue> constantsAndParameters;

    public LIOModelAgentDependentChecker(ErrorCollector errors, AgentsDefinition definition, Map<String, SibillaValue> constantsAndParameters) {
        super(errors);
        this.definition = definition;
        this.constantsAndParameters = constantsAndParameters;
    }


    protected CollectiveExpressionEvaluationFunction evalCollectiveExpression(LIOModelParser.ExprContext expr) {
        return expr.accept(new StateExpressionEvaluator(errors, definition, constantsAndParameters));
    }


    protected Map<String, Integer> getVariableIndexes(List<LIOModelParser.AgentParameterDeclarationContext> agentParameters) {
        Map<String, Integer> variableIndex = new HashMap<>();
        for (LIOModelParser.AgentParameterDeclarationContext par: agentParameters) {
            variableIndex.put(par.name.getText(), variableIndex.size());
        }
        return variableIndex;
    }


    protected Predicate<SibillaValue[]> getAgentIndexPredicate(Map<String, Integer> variableIndexes, LIOModelParser.ExprContext guard) {
        if (guard == null) {
            return args -> true;
        }
        Function<SibillaValue[], SibillaValue> agentIndexesExpression = guard.accept(ParametricExpressionEvaluator.getAgentDependentExpressionEvaluator(this.errors, variableIndexes, constantsAndParameters));
        return args -> agentIndexesExpression.apply(args).booleanOf();
    }

    protected Predicate<Map<String, SibillaValue>> getParametricPredicate(Set<String> args, LIOModelParser.ExprContext guard) {
        if (guard == null) {
            return m -> true;
        }
        Function<Map<String, SibillaValue>, SibillaValue> agentIndexesExpression = guard.accept(new ParametricExpressionEvaluator<>(this.errors, str -> (args.contains(str)?m -> m.get(str):null), constantsAndParameters));
        return m -> agentIndexesExpression.apply(m).booleanOf();
    }


    protected SibillaValue evalGlobalExpression(LIOModelParser.ExprContext value) {
        return value.accept(new GlobalExpressionEvaluator(this.errors, constantsAndParameters));
    }

}
