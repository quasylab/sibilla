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

import it.unicam.quasylab.sibilla.core.models.*;
import it.unicam.quasylab.sibilla.core.models.pm.*;
import it.unicam.quasylab.sibilla.core.models.pm.util.PopulationRegistry;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import it.unicam.quasylab.sibilla.core.simulator.sampling.SimpleMeasure;
import it.unicam.quasylab.sibilla.langs.util.ParseError;
import it.unicam.quasylab.sibilla.langs.util.SibillaParseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.DoubleBinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PopulationModelGenerator {

    private final CodePointCharStream source;
    private ParseTree parseTree;
    private final List<ModelBuildingError> errorList = new LinkedList<>();
    private ModelValidator validator;
    private boolean validated = false;
    private EvaluationEnvironment environment;


    public PopulationModelGenerator(String code) {
        this(CharStreams.fromString(code));
    }

    public PopulationModelGenerator(File file) throws IOException {
        this(CharStreams.fromReader(new FileReader(file)));
    }

    public PopulationModelGenerator(CodePointCharStream source) {
        this.source = source;
    }

    public ParseTree getParseTree() {
        if (this.parseTree == null) {
            generateParseTree();
        }
        return this.parseTree;
    }

    private void generateParseTree() {
        PopulationModelLexer lexer = new PopulationModelLexer(source);
        CommonTokenStream tokens =  new CommonTokenStream(lexer);
        PopulationModelParser parser = new PopulationModelParser(tokens);
        SibillaParseErrorListener errorListener = new SibillaParseErrorListener();
        parser.addErrorListener(errorListener);
        this.parseTree = parser.model();
        for (ParseError e: errorListener.getSyntaxErrorList()) {
            this.errorList.add(ModelBuildingError.syntaxError(e));
        }
    }

    public PopulationModelDefinition loadModel(String code) throws ModelGenerationException {
        return getPopulationModelDefinition();
    }

    public PopulationModelDefinition getPopulationModelDefinition() throws ModelGenerationException {
        validate();
        if (withErrors()) {
            throw new ModelGenerationException(this.errorList);
        }
        return new PopulationModelDefinition(generateEvaluationEnvironment(),
                this::generatePopulationRegistry,
                this::generateRules,
                this::generateMeasures,
                this::generateStateSet
                );
    }

    public boolean validate() {
        if (parseTree == null) {
            generateParseTree();
        }
        if (withErrors()) {
            return false;
        }
        if (!validated) {
            validated = true;
            this.validator = new ModelValidator(this.errorList);
            this.validator.visit(getParseTree());
            return this.validator.getNumberOfValidationErrors() <= 0;
        }
        return true;
    }

    public boolean withErrors() {
        return !this.errorList.isEmpty();
    }

    public StateSet<PopulationState> generateStateSet(EvaluationEnvironment environment, PopulationRegistry registry) {
        return this.getParseTree().accept(new StateSetGenerator(environment, registry));
    }
    private Map<String, Measure<PopulationState>> generateMeasures(EvaluationEnvironment environment, PopulationRegistry registry) {
        return this.getParseTree().accept(new PopulationMeasuresGenerator(environment, registry));
    }

    public List<PopulationRule> generateRules(EvaluationEnvironment environment, PopulationRegistry registry) {
        return this.getParseTree().accept(new PopulationRuleGenerator(environment, registry));
    }

    public PopulationRegistry generatePopulationRegistry(EvaluationEnvironment environment) {
        return this.getParseTree().accept(new PopulationRegistryGenerator(environment));
    }


    public SymbolTable getSymbolTable() {
        if (validator != null) {
            return validator.getSymbolTable();
        }
        return new BasicSymbolTable();
    }

    public EvaluationEnvironment generateEvaluationEnvironment() {
        validate();
        if (!withErrors()&&(this.environment==null)) {
            EnvironmentGenerator eg = new EnvironmentGenerator();
            this.parseTree.accept(eg);
            CachedValues constants = eg.getConstants();
            this.environment = new EvaluationEnvironment(eg.getParameters(), constants);
            return environment;
        }
        return null;
    }

    public static List<Integer> getValues(Function<String, Double> resolver, PopulationModelParser.RangeContext range) {
        ExpressionEvaluator evaluator = new ExpressionEvaluator(resolver);
        int min = evaluator.evalInteger(range.min);
        int max = evaluator.evalInteger(range.max);
        if (max<=min) {
            throw new IllegalArgumentException(ParseUtil.illegalInterval(min,max,range));
        }
        return IntStream.range(min,max).boxed().collect(Collectors.toList());
    }

    public static List<List<Integer>> merge(List<Integer> rangeValues, List<List<Integer>> collected) {
        List<List<Integer>> result = new LinkedList<>();
        for (Integer i: rangeValues) {
            for( List<Integer> lst: collected) {
                List<Integer> newList = new LinkedList<>(lst);
                newList.add(i);
                result.add(newList);
            }
        }
        return result;
    }

    public static List<List<Integer>> getValues(Function<String, Double> resolver, List<PopulationModelParser.RangeContext> rangeList) {
        List<List<Integer>> values = new LinkedList<>();
        values.add(new LinkedList<>());
        for (PopulationModelParser.RangeContext range: rangeList) {
            values = merge(getValues(resolver, range), values);
        }
        return values;
    }

    public static List<Map<String,Double>> addValues(Function<String, Double> resolver, String name, PopulationModelParser.RangeContext range, List<Map<String, Double>> maps) {
        List<Integer> values = getValues(resolver, range);
        List<Map<String, Double>> result = new LinkedList<>();
        for (Map<String, Double> map: maps) {
            for(int i: values) {
                Map<String, Double> newMap = new HashMap<>(map);
                newMap.put(name,(double) i);
                result.add(newMap);
            }
        }
        return result;
    }

    public static List<Map<String,Double>> getMaps(Function<String, Double> resolver, PopulationModelParser.Local_variablesContext local_variables) {
        return getMaps(resolver, local_variables, List.of( new HashMap<>()));
    }

    public static List<Map<String,Double>> getMaps(Function<String, Double> evaluator, PopulationModelParser.Local_variablesContext local_variables, PopulationModelParser.Guard_expressionContext guard) {
        if (local_variables == null) {
            return List.of(new HashMap<>());
        }
        List<Map<String,Double>> maps = PopulationModelGenerator.getMaps(evaluator,local_variables);
        if (guard != null) {
            maps = PopulationModelGenerator.filter(evaluator, guard, maps);
        }
        return maps;
    }

    public static List<Map<String,Double>> getMaps(Function<String, Double> resolver, PopulationModelParser.Local_variablesContext local_variables, List<Map<String,Double>> maps) {
        if (local_variables == null) {
            return maps;
        }
        for (PopulationModelParser.Local_variableContext lv: local_variables.variables){
            maps = addValues(resolver, lv.name.getText(), lv.range(), maps);
        }
        return maps;
    }

    public static List<Map<String,Double>> filter(Function<String, Double> resolver, PopulationModelParser.Guard_expressionContext guard, List<Map<String,Double>> values) {
        if (guard == null) {
            return values;
        }
        return values.stream().filter(m ->
            guard.expr().accept(new ExpressionEvaluator(combine(resolver,m)).getBooleanExpressionEvaluator())
            ).collect(Collectors.toList());
    }

    public static Function<String, Double> combine(Function<String, Double> resolver, Map<String, Double> map) {
        return s -> map.getOrDefault(s, resolver.apply(s));
    }

    public static double evalExpressionToDouble(Function<String, Double> resolver, PopulationModelParser.ExprContext expr) {
        return new ExpressionEvaluator(resolver).evalDouble(expr);
    }

    public static int evalExpressionToInteger(Function<String, Double> resolver, PopulationModelParser.ExprContext expr) {
        return new ExpressionEvaluator(resolver).evalInteger(expr);
    }

    public static int[] getIndexes(Function<String, Double> resolver, PopulationRegistry registry, PopulationModelParser.Species_expressionContext agent) {
        String name = agent.name.getText();
        int[] indexes;
        if (registry.isALabel(name)) {
            indexes = registry.evalLabel(name, agent.expr().stream().mapToDouble(e -> evalExpressionToInteger(resolver, e)).toArray());
        } else {
            indexes = PopulationModelGenerator.getIndexArray(registry,
                    resolver,
                    PopulationModelGenerator.getMaps(resolver, agent.local_variables(), agent.guard_expression()),
                    name,
                    agent.expr());
        }
        return indexes;
    }

    public static int getIndex(PopulationRegistry registry, Function<String, Double> resolver, String species, List<PopulationModelParser.ExprContext> args) {
        return registry.indexOf(species, args.stream().map(e -> evalExpressionToInteger(resolver, e)).toArray());
    }

    public static PopulationRegistry.Tuple getTuple(Function<String, Double> resolver, String species, List<PopulationModelParser.ExprContext> args) {
        return new PopulationRegistry.Tuple(species, args.stream().map(e -> evalExpressionToInteger(resolver, e)).toArray());
    }


    public static int[] getIndexArray(PopulationRegistry registry, Function<String, Double> resolver, List<Map<String,Double>> maps, String species, List<PopulationModelParser.ExprContext> args) {
        return maps.stream().mapToInt(m -> getIndex(registry, combine(resolver, m), species, args)).toArray();
    }




    public static Set<Integer> getIndexSet(PopulationRegistry registry, Function<String, Double> resolver, List<Map<String,Double>> maps, String species, List<PopulationModelParser.ExprContext> args) {
        return maps.stream().map(m -> getIndex(registry, combine(resolver, m), species, args)).collect(Collectors.toSet());
    }

    public static Set<PopulationRegistry.Tuple> getTupleSet(Function<String, Double> resolver, List<Map<String,Double>> maps, String species, List<PopulationModelParser.ExprContext> args) {
        return maps.stream().map(m -> getTuple(combine(resolver, m), species, args)).collect(Collectors.toSet());
    }

    public static Set<Integer> getIndexSet(PopulationRegistry registry, Function<String, Double> resolver, Map<String, Double> map, PopulationModelParser.Species_expressionContext se) {
        List<Map<String,Double>> localMaps = PopulationModelGenerator.getMaps(combine(resolver,map), se.local_variables(), se.guard_expression());
        return PopulationModelGenerator.getIndexSet(registry,combine(resolver,map),localMaps,se.name.getText(), se.expr());
    }


    public static Set<PopulationRegistry.Tuple> getTupleSet(Function<String, Double> resolver, PopulationModelParser.Species_expressionContext se) {
        List<Map<String,Double>> localMaps = PopulationModelGenerator.getMaps(resolver, se.local_variables(), se.guard_expression());
        return PopulationModelGenerator.getTupleSet(resolver,localMaps,se.name.getText(), se.expr());
    }

    public static Set<PopulationRegistry.Tuple> getTupleSet(Function<String, Double> resolver, List<PopulationModelParser.Species_expressionContext> seList) {
        return seList.stream().map(se -> getTupleSet(resolver,se)).flatMap(Collection::stream).collect(Collectors.toSet());
    }

    public static PopulationRegistry.Tuple[] getTupleArray(Function<String, Double> resolver, List<PopulationModelParser.Species_expressionContext> seList) {
        return getTupleSet(resolver, seList).toArray(new PopulationRegistry.Tuple[0]);
    }


    public static Function<double[],PopulationRegistry.Tuple[]> getLabelFunction(String[] variables, Function<String, Double> resolver, List<PopulationModelParser.Species_expressionContext> species_expression) {
        return d -> getTupleArray(combine(resolver, PopulationModelGenerator.getMap(variables,d)),species_expression);
    }



    public static List<Population> getPopulationList(PopulationRegistry registry, Function<String, Double> resolver, Map<String, Double> map, PopulationModelParser.Species_pattern_elementContext se) {
        Set<Integer> indexSet = getIndexSet(registry, resolver, map, se.species_expression());
        int size ;
        if (se.expr() != null) {
            size = evalExpressionToInteger(combine(resolver,map),se.expr());
        } else {
            size = 1;
        }
        return indexSet.stream().map(i -> new Population(i,size)).collect(Collectors.toList());
    }

    public static List<Population> getPopulationList(PopulationRegistry registry, Function<String, Double> resolver, Map<String, Double> map, List<PopulationModelParser.Species_pattern_elementContext> seList) {
        return seList.stream().map(se -> getPopulationList(registry, resolver, map, se)).flatMap(Collection::stream).collect(Collectors.toList());
    }

    public static Population[] getPopulationArray(PopulationRegistry registry, Function<String, Double> resolver, Map<String, Double> map, List<PopulationModelParser.Species_pattern_elementContext> seList) {
        return getPopulationList(registry, resolver, map, seList).toArray(new Population[0]);
    }

    public static RatePopulationFunction combine(RatePopulationFunction f1, DoubleBinaryOperator op, RatePopulationFunction f2) {
        return (n,s) -> op.applyAsDouble(f1.apply(n,s),f2.apply(n,s));
    }

    public static MeasureFunction<PopulationState> combine(MeasureFunction<PopulationState> f1, DoubleBinaryOperator op, MeasureFunction<PopulationState> f2) {
        return s -> op.applyAsDouble(f1.apply(s),f2.apply(s));
    }

    public static BiFunction<Double,Double,Boolean> getRelationOperator(String op) {
        if (op.equals("<"))  { return (x,y) -> x<y; }
        if (op.equals("<="))  { return (x,y) -> x<=y; }
        if (op.equals("=="))  { return Double::equals; }
        if (op.equals("!="))  { return (x,y) -> !x.equals(y); }
        if (op.equals(">"))  { return (x,y) -> x>y; }
        if (op.equals(">="))  { return (x,y) -> x>=y; }
        return (x,y) -> false;
    }

    public static DoubleBinaryOperator getOperator(String op) {
        if (op.equals("+")) {return Double::sum;}
        if (op.equals("-")) {return (x,y) -> x-y; }
        if (op.equals("%")) {return (x,y) -> x%y; }
        if (op.equals("*")) {return (x,y) -> x*y; }
        if (op.equals("/")) {return (x,y) -> x/y; }
        if (op.equals("//")) {return (x,y) -> (y==0.0?0.0:x/y); }
        return (x,y) -> Double.NaN;
    }

    public static Map<String,Double> getMap(String[] variables, double[] args) {
        Map<String, Double> map = new HashMap<>();
        IntStream.range(0,variables.length).sequential().forEach(i -> map.put(variables[i],args[i]));
        return map;
    }

}
