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

import it.unicam.quasylab.sibilla.core.models.slam.agents.SlamAgentDefinitions;
import it.unicam.quasylab.sibilla.core.models.slam.agents.AgentName;
import it.unicam.quasylab.sibilla.core.models.slam.agents.SlamAgent;
import it.unicam.quasylab.sibilla.core.models.slam.data.AgentStore;
import it.unicam.quasylab.sibilla.core.models.slam.data.VariableRegistry;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Generates
 */
public class AgentPatternGenerator extends SlamModelBaseVisitor<BiPredicate<AgentStore, SlamAgent>> {
    private final Function<String, Optional<SibillaValue>> globalAssignments;
    private final VariableRegistry registry;

    private final SlamAgentDefinitions agentDefinitions;

    public AgentPatternGenerator(Function<String, Optional<SibillaValue>> globalAssignments, VariableRegistry registry, SlamAgentDefinitions agentDefinitions) {
        this.globalAssignments = globalAssignments;
        this.registry = registry;
        this.agentDefinitions = agentDefinitions;
    }

    @Override
    public BiPredicate<AgentStore, SlamAgent> visitAgentPatternNegation(SlamModelParser.AgentPatternNegationContext ctx) {
        BiPredicate<AgentStore, SlamAgent> argumentEvaluation = ctx.arg.accept(this);
        return argumentEvaluation.negate();
    }

    @Override
    public BiPredicate<AgentStore, SlamAgent> visitAgentPatternBrackets(SlamModelParser.AgentPatternBracketsContext ctx) {
        return ctx.agentPattern().accept(this);
    }

    @Override
    public BiPredicate<AgentStore, SlamAgent> visitAgentPatternAny(SlamModelParser.AgentPatternAnyContext ctx) {
        return (store, agent) -> true;
    }

    @Override
    public BiPredicate<AgentStore, SlamAgent> visitAgentPatternNamed(SlamModelParser.AgentPatternNamedContext ctx) {
        AgentName agentName = agentDefinitions.getAgentName(ctx.name.getText());
        Function<SlamExpressionEvaluationParameters, SibillaValue> expressionEvaluator = ctx.guard.accept(new SlamExpressionEvaluator(ExpressionContext.AGENT_PATTERN, globalAssignments, registry, agentDefinitions));
        return (store, agent) ->
                agent.getAgentName().equals(agentName)
                    &&SlamExpressionEvaluationParameters.evalAgentPredicate(store, agent.getAgentMemory(), expressionEvaluator);
    }

    @Override
    public BiPredicate<AgentStore, SlamAgent> visitAgentPatternProperty(SlamModelParser.AgentPatternPropertyContext ctx) {
        Function<SlamExpressionEvaluationParameters, SibillaValue> expressionEvaluator = ctx.guard.accept(new SlamExpressionEvaluator(ExpressionContext.AGENT_PATTERN, globalAssignments, registry, agentDefinitions));
        return (store, agent) -> SlamExpressionEvaluationParameters.evalAgentPredicate(store, agent.getAgentMemory(), expressionEvaluator);
    }

    @Override
    public BiPredicate<AgentStore, SlamAgent> visitAgentPatternConjunction(SlamModelParser.AgentPatternConjunctionContext ctx) {
        BiPredicate<AgentStore, SlamAgent> leftEvaluation = ctx.left.accept(this);
        BiPredicate<AgentStore, SlamAgent> rightEvaluation = ctx.right.accept(this);
        return leftEvaluation.and(rightEvaluation);
    }

    @Override
    public BiPredicate<AgentStore, SlamAgent> visitAgentPatternDisjunction(SlamModelParser.AgentPatternDisjunctionContext ctx) {
        BiPredicate<AgentStore, SlamAgent> leftEvaluation = ctx.left.accept(this);
        BiPredicate<AgentStore, SlamAgent> rightEvaluation = ctx.right.accept(this);
        return leftEvaluation.or(rightEvaluation);
    }
}
