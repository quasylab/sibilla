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

import it.unicam.quasylab.sibilla.core.models.lio.LIOAgent;
import it.unicam.quasylab.sibilla.core.models.lio.LIOAgentDefinitions;
import it.unicam.quasylab.sibilla.core.models.lio.LIOAgentName;
import it.unicam.quasylab.sibilla.core.models.lio.LIOConfigurationsSupplier;
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

    private final Map<String, Function<SibillaValue[], Map<LIOAgent, Integer>>> systems;

    private final Map<String, Integer> systemArity;

    private String defaltConfiguration;

    public LIOModelSystemGenerator(ErrorCollector errors, LIOAgentDefinitions definition, Map<String, SibillaValue> constantsAndParameters) {
        super(errors, definition, constantsAndParameters);
        this.systems = new HashMap<>();
        systemArity = new HashMap<>();
    }

    @Override
    public Boolean visitElementSystem(LIOModelParser.ElementSystemContext ctx) {
        recordSystemDeclaration(ctx.name.getText(), ctx.args.stream().map(Token::getText).toArray(String[]::new), ctx.population);
        return true;
    }

    private void recordSystemDeclaration(String name, String[] parNames, LIOModelParser.PopulationExpressionContext population) {
        if (defaltConfiguration == null) {
            this.defaltConfiguration = name;
        }
        Map<String, Integer> variableIndex = getVariableIndexes(parNames);
        this.systemArity.put(name, parNames.length);
        BiFunction<Map<String, SibillaValue>, Map<LIOAgent, Integer>, Map<LIOAgent, Integer>> populationEvaluationFunction = population.accept(new PopulationGenerator(variableIndex.keySet()));
        this.systems.put(name, args -> populationEvaluationFunction.apply(getMapFromArgs(variableIndex, args), new HashMap<>()));
    }

    private Map<String, SibillaValue> getMapFromArgs(Map<String, Integer> variableIndex, SibillaValue[] args) {
        return variableIndex.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> args[e.getValue()]));
    }

    private Function<SibillaValue[], LIOAgent> evalAgentName(Map<String, Integer> variableIndex, String name, List<LIOModelParser.ExprContext> stateArguments) {
        List<Function<SibillaValue[],SibillaValue>> argumentsEvaluationFunctions = stateArguments.stream().map(expr -> expr.accept(ParametricExpressionEvaluator.getAgentDependentExpressionEvaluator(errors, variableIndex, constantsAndParameters))).collect(Collectors.toList());
        return args -> {
            LIOAgentName agentName = new LIOAgentName(name, argumentsEvaluationFunctions.stream().map(f -> f.apply(args)).toArray(SibillaValue[]::new));
            LIOAgent agent = definition.getAgent(agentName);
            if (agent == null) {
                throw new IllegalStateException("Illegal use of agent name "+agentName+"!");
            }
            return agent;
        };
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
    public LIOConfigurationsSupplier getSystems() {
        return new LIOConfigurationsSupplier(definition, systemArity, systems, defaltConfiguration);
    }

    public class PopulationGenerator extends LIOModelBaseVisitor<BiFunction<Map<String, SibillaValue>, Map<LIOAgent, Integer>, Map<LIOAgent, Integer>>> {

        private final Set<String> variables;

        public PopulationGenerator(Set<String> variables) {
            this.variables = variables;
        }

        @Override
        public BiFunction<Map<String, SibillaValue>, Map<LIOAgent, Integer>, Map<LIOAgent, Integer>> visitPopulationExpressionFor(LIOModelParser.PopulationExpressionForContext ctx) {
            String varName = ctx.name.getText();
            Function<Map<String, SibillaValue>, SibillaValue> fromFunction = ctx.from.accept(ParametricExpressionEvaluator.getParametricExpressionEvaluator(errors, variables, constantsAndParameters));
            Function<Map<String, SibillaValue>, SibillaValue> toFunction = ctx.to.accept(ParametricExpressionEvaluator.getParametricExpressionEvaluator(errors, variables, constantsAndParameters));
            BiFunction<Map<String, SibillaValue>, Map<LIOAgent, Integer>, Map<LIOAgent, Integer>> bodyFunction = ctx.body.accept(this);
            return (args, map) -> {
                IntStream.range(fromFunction.apply(args).intOf(), toFunction.apply(args).intOf()).mapToObj(SibillaInteger::new).forEach(v -> {
                    SibillaValue old = args.put(varName, v);
                    Map<LIOAgent, Integer> result = bodyFunction.apply(args, map);
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
        public BiFunction<Map<String, SibillaValue>, Map<LIOAgent, Integer>, Map<LIOAgent, Integer>> visitPopulationExpressionIfThenElse(LIOModelParser.PopulationExpressionIfThenElseContext ctx) {
            Predicate<Map<String, SibillaValue>> guardPredicate = getParametricPredicate(this.variables, ctx.guard);
            BiFunction<Map<String, SibillaValue>, Map<LIOAgent, Integer>, Map<LIOAgent, Integer>> thenPopulationFunction = ctx.thenPopulation.accept(this);
            BiFunction<Map<String, SibillaValue>, Map<LIOAgent, Integer>, Map<LIOAgent, Integer>> elsePopulationFunction = ctx.thenPopulation.accept(this);
            return (args, map) -> {
                if (guardPredicate.test(args)) {
                    return thenPopulationFunction.apply(args, map);
                } else {
                    return elsePopulationFunction.apply(args, map);
                }
            };
        }

        @Override
        public BiFunction<Map<String, SibillaValue>, Map<LIOAgent, Integer>, Map<LIOAgent, Integer>> visitPopulationExpressionParallel(LIOModelParser.PopulationExpressionParallelContext ctx) {
            BiFunction<Map<String, SibillaValue>, Map<LIOAgent, Integer>, Map<LIOAgent, Integer>> leftPopulationFunction = ctx.left.accept(this);
            BiFunction<Map<String, SibillaValue>, Map<LIOAgent, Integer>, Map<LIOAgent, Integer>> rightPopulationFunction = ctx.right.accept(this);
            return (args, map) -> rightPopulationFunction.apply(args, leftPopulationFunction.apply(args, map));
        }

        @Override
        public BiFunction<Map<String, SibillaValue>, Map<LIOAgent, Integer>, Map<LIOAgent, Integer>> visitPopulationExpressionAgent(LIOModelParser.PopulationExpressionAgentContext ctx) {
            String name = ctx.name.getText();
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
                LIOAgentName agentName = new LIOAgentName(name, agentParametersEvaluationFunctions.stream().map(f -> f.apply(args)).toArray(SibillaValue[]::new));
                LIOAgent agent = definition.getAgent(agentName);
                if (agent == null) {
                    throw new IllegalStateException("Illegal use of agent name "+agentName+"!");
                }
                map.put(agent, sizeEvaluator.applyAsInt(args));
                return map;
            };
        }
    }
}
