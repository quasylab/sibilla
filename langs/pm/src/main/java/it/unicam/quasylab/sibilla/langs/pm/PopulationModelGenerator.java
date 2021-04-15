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

package it.unicam.quasylab.sibilla.langs.pm;

import it.unicam.quasylab.sibilla.core.models.EvaluationEnvironment;
import it.unicam.quasylab.sibilla.core.models.ParametricValue;
import it.unicam.quasylab.sibilla.core.models.StateSet;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationModelDefinition;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationRule;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.models.pm.util.PopulationRegistry;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import it.unicam.quasylab.sibilla.core.simulator.sampling.SimpleMeasure;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PopulationModelGenerator {


    public PopulationModelDefinition getPopulationModelDefinition(ParseTree ctx) throws ModelGenerationException {
        ModelValidator mv = new ModelValidator();
        mv.visit(ctx);
        if (mv.getNumberOfValidationErrors()>0) {
            throw new ModelGenerationException(mv.getErrors());
        }
        SymbolTable table = mv.getSymbolTable();

        return new PopulationModelDefinition(generateEvaluationEnvironment(table),
                ee -> generatePopulationregistry(table,ee),
                (ee,st) -> generateRules(table,ee,st),
                (ee,st) -> generateMeasures(table,ee,st),
                (ee,st) -> generateStateSet(table,ee,st)
                );
    }

    private StateSet<PopulationState> generateStateSet(SymbolTable table, EvaluationEnvironment environment, PopulationRegistry registry) {
        ExpressionEvaluator evaluator = new ExpressionEvaluator(table,new HashMap<>(),environment);
        evaluator.setPopulationRegistry(registry);
        StateSet<PopulationState> stateSet = new StateSet<>();
        for(String name: table.systems()) {
            PopulationModelParser.System_declarationContext ctx = table.getSystemContext(name);
            String[] argArray = ctx.args.stream().map(t -> t.getText()).toArray(i -> new String[i]);
            stateSet.set(name,new ParametricValue<PopulationState>(argArray,args -> evaluator.getState(args,getContext(argArray,args),ctx.species_pattern())));
        }
        return stateSet;
    }

    private Map<String, Double> getContext(String[] argArray, double[] values) {
        HashMap<String,Double> context = new HashMap<>();
        for(int i=0 ;i<argArray.length;i++) {
            context.put(argArray[i],values[i]);
        }
        return context;
    }

    private Map<String, Measure<PopulationState>> generateMeasures(SymbolTable table, EvaluationEnvironment environment, PopulationRegistry registry) {
        Map<String, Measure<PopulationState>> measureMap = new HashMap<>();
        ExpressionEvaluator evaluator = new ExpressionEvaluator(table,new HashMap<>(),environment);
        evaluator.setPopulationRegistry(registry);
        for(String measure: table.measures()) {
            PopulationModelParser.Measure_declarationContext ctx = table.getMeasureContext(measure);
            measureMap.put(measure, new SimpleMeasure<PopulationState>(measure,evaluator.evalStateFunction(ctx.expr())));
        }
        return measureMap;
    }

    private List<PopulationRule> generateRules(SymbolTable table, EvaluationEnvironment environment, PopulationRegistry registry) {
        List<PopulationRule> rules = new LinkedList<>();
        RuleGeneratorVisitor ruleVisitor = new RuleGeneratorVisitor(table,environment,registry,rules);
        for(String name: table.rules()) {
            ruleVisitor.visit(table.getRuleContext(name));
        }
        return rules;
    }

    private PopulationRegistry generatePopulationregistry(SymbolTable table, EvaluationEnvironment environment) {
        String[] species = table.species();
        PopulationRegistry registy = new PopulationRegistry();
        ExpressionEvaluator evaluator = new ExpressionEvaluator(table,new HashMap<>(),environment);
        for (String name: species) {
            PopulationModelParser.Species_declarationContext ctx = table.getSpeciesContext(name);
            List<List<Integer>> parameterSpace = generateSpeciesParameterSpace(evaluator,ctx.range());
            if (parameterSpace.isEmpty()) {
                registy.register(name);
            } else {
                parameterSpace.stream().forEach(lst -> registy.register(name,(Object[]) lst.toArray(new Integer[0])));
            }
        }
        return registy;
    }

    private List<List<Integer>> generateSpeciesParameterSpace(ExpressionEvaluator evaluator, List<PopulationModelParser.RangeContext> range) {
        List<List<Integer>> space = new LinkedList<>();
        for (PopulationModelParser.RangeContext r: range) {
            space = expand(rangeSpace(evaluator.evalInt(r.min),evaluator.evalInt(r.max)), space);
        }
        return space;
    }

    private List<Integer> rangeSpace(int min, int max) {
        return IntStream.range(min,max).boxed().collect(Collectors.toCollection(LinkedList::new));
    }

    private List<List<Integer>> expand(List<Integer> elements, List<List<Integer>> bag) {
        if ((bag == null)||(bag.isEmpty())) {
            return elements.stream().map(List::of).collect(Collectors.toList());
        } else {
            List<List<Integer>> generated = new LinkedList<>();
            for (Integer e: elements) {
                for (List<Integer> lst: bag) {
                    List<Integer> newElement = new LinkedList<>();
                    newElement.addAll(lst);
                    newElement.add(e);
                    generated.add(newElement);
                }
            }
            return generated;
        }
    }

    private EvaluationEnvironment generateEvaluationEnvironment(SymbolTable table) {
        EvaluationEnvironment environment = new EvaluationEnvironment();
        ExpressionEvaluator evaluator = new ExpressionEvaluator(table,new HashMap<>(),environment);
        for(String str: table.parameters()) {
            environment.register(str, evaluator.visit(table.getContext(str)));
        }
        return environment;
    }



}
