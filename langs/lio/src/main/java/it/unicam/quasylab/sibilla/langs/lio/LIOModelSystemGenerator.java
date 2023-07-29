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
import it.unicam.quasylab.sibilla.core.util.values.SibillaInteger;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import it.unicam.quasylab.sibilla.langs.util.ErrorCollector;
import org.antlr.v4.runtime.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class is used to generate the systems declared in a model.
 */
public class LIOModelSystemGenerator extends LIOModelAgentDependentChecker {

    private final Map<String, List<Function<SibillaValue[], Map<Agent, Integer>>>> systems;

    public LIOModelSystemGenerator(ErrorCollector errors, AgentsDefinition definition, Map<String, SibillaValue> constantsAndParameters) {
        super(errors, definition, constantsAndParameters);
        this.systems = new HashMap<>();
    }

    @Override
    public Boolean visitElementSystem(LIOModelParser.ElementSystemContext ctx) {
        recordSystemDeclaration(ctx.name.getText(), ctx.args.stream().map(Token::getText).toArray(String[]::new), ctx.population);
        return true;
    }

    private void recordSystemDeclaration(String name, String[] parNames, LIOModelParser.PopulationExpressionContext population) {
        Map<String, Integer> variableIndex = getVariableIndexes(parNames);
    }

    private Function<SibillaValue[], Agent> evalAgentName(Map<String, Integer> variableIndex, String name, List<LIOModelParser.ExprContext> stateArguments) {
        List<Function<SibillaValue[],SibillaValue>> argumentsEvaluationFunctions = stateArguments.stream().map(expr -> expr.accept(ParametricExpressionEvaluator.getAgentDependentExpressionEvaluator(errors, variableIndex, constantsAndParameters))).collect(Collectors.toList());
        return args -> definition.getAgent(name, argumentsEvaluationFunctions.stream().map(f -> f.apply(args)).toArray(SibillaValue[]::new));
    }

    private ToIntFunction<SibillaValue[]> evalAgentMultiplicity(Map<String, Integer> variableIndex, LIOModelParser.ExprContext size) {
        if (size == null) {
            return args -> 1;
        } else {
            Function<SibillaValue[], SibillaValue> f = size.accept(ParametricExpressionEvaluator.getAgentDependentExpressionEvaluator(errors, variableIndex, constantsAndParameters));
            return args -> f.apply(args).intOf();
        }
    }


    private Map<String, Integer> getVariableIndexes(String[] args) {
        Map<String, Integer> map = new HashMap<>();
        for(int i=0; i<args.length; i++) {
            map.put(args[i], i);
        }
        return map;
    }


    /**
     * Returns the map containing associating each name with the list of functions used to generate the system
     * state defined in the model. This method should be invoked only
     * after the visit has been completed.
     *
     * @return the map containing associating each name with the list of functions used to generate the system
     * state defined in the model.
     */
    public Map<String, List<Function<SibillaValue[], Map<Agent, Integer>>>> getSystems() {
        return systems;
    }

    public class PopulationGenerator extends LIOModelBaseVisitor<BiFunction<Map<String, SibillaValue>, Map<Agent, Integer>, Map<Agent, Integer>>> {

        private final Set<String> variables;

        public PopulationGenerator(Set<String> variables) {
            this.variables = variables;
        }

        @Override
        public BiFunction<Map<String, SibillaValue>, Map<Agent, Integer>, Map<Agent, Integer>> visitPopulationExpressionFor(LIOModelParser.PopulationExpressionForContext ctx) {
            String varName = ctx.name.getText();
            Function<Map<String, SibillaValue>, SibillaValue> fromFunction = ctx.from.accept(ParametricExpressionEvaluator.getParametricExpressionEvaluator(errors, variables, constantsAndParameters));
            Function<Map<String, SibillaValue>, SibillaValue> toFunction = ctx.to.accept(ParametricExpressionEvaluator.getParametricExpressionEvaluator(errors, variables, constantsAndParameters));
            BiFunction<Map<String, SibillaValue>, Map<Agent, Integer>, Map<Agent, Integer>> bodyFunction = ctx.body.accept(this);
            return (args, map) -> {
                IntStream.range(fromFunction.apply(args).intOf(), toFunction.apply(args).intOf()).mapToObj(SibillaInteger::new).forEach(v -> {
                    SibillaValue old = args.put(varName, v);
                    Map<Agent, Integer> result = bodyFunction.apply(args, map);
                    if (old != null) {
                        args.put(varName, old);
                    } else {
                        args.remove(varName);
                    }
                });
                return map;
            };
        }


        @Override
        public BiFunction<Map<String, SibillaValue>, Map<Agent, Integer>, Map<Agent, Integer>> visitPopulationExpressionIfThenElse(LIOModelParser.PopulationExpressionIfThenElseContext ctx) {
            Predicate<Map<String, SibillaValue>> guardPredicate = getParametricPredicate(this.variables, ctx.guard);
            BiFunction<Map<String, SibillaValue>, Map<Agent, Integer>, Map<Agent, Integer>> thenPopulationFunction = ctx.thenPopulation.accept(this);
            BiFunction<Map<String, SibillaValue>, Map<Agent, Integer>, Map<Agent, Integer>> elsePopulationFunction = ctx.thenPopulation.accept(this);
            return (args, map) -> {
                if (guardPredicate.test(args)) {
                    return thenPopulationFunction.apply(args, map);
                } else {
                    return elsePopulationFunction.apply(args, map);
                }
            };
        }

        @Override
        public BiFunction<Map<String, SibillaValue>, Map<Agent, Integer>, Map<Agent, Integer>> visitPopulationExpressionParallel(LIOModelParser.PopulationExpressionParallelContext ctx) {
            BiFunction<Map<String, SibillaValue>, Map<Agent, Integer>, Map<Agent, Integer>> leftPopulationFunction = ctx.left.accept(this);
            BiFunction<Map<String, SibillaValue>, Map<Agent, Integer>, Map<Agent, Integer>> rightPopulationFunction = ctx.right.accept(this);
            return (args, map) -> rightPopulationFunction.apply(args, leftPopulationFunction.apply(args, map));
        }

        @Override
        public BiFunction<Map<String, SibillaValue>, Map<Agent, Integer>, Map<Agent, Integer>> visitPopulationExpressionAgent(LIOModelParser.PopulationExpressionAgentContext ctx) {
            String agentName = ctx.name.getText();
            ParametricExpressionEvaluator<Map<String, SibillaValue>> evaluator = ParametricExpressionEvaluator.getParametricExpressionEvaluator(errors, variables, constantsAndParameters);
            List<Function<Map<String, SibillaValue>, SibillaValue>> agentParametersEvaluationFunctions = ctx.stateArguments.stream().map(expr -> expr.accept(evaluator)).collect(Collectors.toList());
            ToIntFunction<Map<String, SibillaValue>> sizeEvaluator;
            if (ctx.size == null) {
                sizeEvaluator = m -> 1;
            } else {
                Function<Map<String, SibillaValue>, SibillaValue> sizeExpressionEvaluator = ctx.size.accept(evaluator);
                sizeEvaluator = m -> sizeExpressionEvaluator.apply(m).intOf();
            }
            return (args, map) -> {
                map.put(definition.getAgent(agentName, agentParametersEvaluationFunctions.stream().map(f -> f.apply(args)).toArray(SibillaValue[]::new)), sizeEvaluator.applyAsInt(args));
                return map;
            };
        }
    }
}
