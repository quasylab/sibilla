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

package it.unicam.quasylab.sibilla.langs.markov;

import it.unicam.quasylab.sibilla.core.models.CachedValues;
import it.unicam.quasylab.sibilla.core.models.EvaluationEnvironment;
import it.unicam.quasylab.sibilla.core.models.ParametricValue;
import it.unicam.quasylab.sibilla.core.models.ParametricDataSet;
import it.unicam.quasylab.sibilla.core.models.markov.CTMCModel;
import it.unicam.quasylab.sibilla.core.models.markov.DTMCModel;
import it.unicam.quasylab.sibilla.core.models.markov.MappingStateUpdate;
import it.unicam.quasylab.sibilla.core.models.markov.MarkovChainDefinition;
import it.unicam.quasylab.sibilla.core.models.util.MappingState;
import it.unicam.quasylab.sibilla.core.models.util.VariableTable;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import it.unicam.quasylab.sibilla.core.simulator.sampling.SimpleMeasure;
import it.unicam.quasylab.sibilla.core.util.values.SibillaDouble;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import it.unicam.quasylab.sibilla.langs.util.ErrorCollector;
import it.unicam.quasylab.sibilla.langs.util.SibillaParseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public class MarkovChainModelGenerator {

    private final static String CTMC = "ctmc";
    private final static String DTMC = "dtmc";

    private final CodePointCharStream source;
    private MarkovChainModelParser.ModelContext parseTree;
    private final ErrorCollector errorList = new ErrorCollector();
    private boolean validated = false;
    private ModelValidator validator;

    public MarkovChainModelGenerator(CodePointCharStream source) {
        this.source = source;
    }

    public MarkovChainModelGenerator(String code) {
        this(CharStreams.fromString(code));
    }

    public MarkovChainModelGenerator(File file) throws IOException {
        this(CharStreams.fromReader(new FileReader(file)));
    }


    public ParseTree getParseTree() {
        if (this.parseTree == null) {
            generateParseTree();
        }
        return this.parseTree;
    }

    private void generateParseTree() {
        MarkovChainModelLexer lexer = new MarkovChainModelLexer(source);
        CommonTokenStream tokens =  new CommonTokenStream(lexer);
        MarkovChainModelParser parser = new MarkovChainModelParser(tokens);
        SibillaParseErrorListener errorListener = new SibillaParseErrorListener(errorList);
        parser.addErrorListener(errorListener);
        this.parseTree = parser.model();
    }

    public MarkovChainDefinition<?> getMarkovChainDefinition() {
        if (!validate()) {
           return null;
        }
        if (isContinuous()) {
            return getCTMCModelDefinition();
        } else {
            return getDTMCModelDefinition();
        }
    }

    private boolean isContinuous() {
        return getParseTree().accept(new IsContinuousModelChecker());
    }

    private MarkovChainDefinition<DTMCModel> getDTMCModelDefinition() {
        return new MarkovChainDefinition<>(
            getEvaluationEnvironment(),
            this::getStates,
            DTMCModel::new,
            this::getVariables,
            this::getRules,
            this::getMeasures);
    }

    private List<MappingStateUpdate> getRules(EvaluationEnvironment environment, VariableTable variables) {
        return getParseTree().accept(new RuleGenerator(environment.getEvaluator(), variables));
    }

    private VariableTable getVariables(EvaluationEnvironment environment) {
        return getParseTree().accept(new StateVariableVisitor(environment.getEvaluator()));
    }

    private MarkovChainDefinition<CTMCModel> getCTMCModelDefinition() {
        return new MarkovChainDefinition<>(
                getEvaluationEnvironment(),
                this::getStates,
                CTMCModel::new,
                this::getVariables,
                this::getRules,
                this::getMeasures);
    }

    public EvaluationEnvironment getEvaluationEnvironment() {
        return new EvaluationEnvironment(evalParameters(), evalConstants());
    }

    private Map<String, SibillaValue> evalParameters() {
        return getParseTree().accept(new ParameterEvaluator());
    }

    public ParametricDataSet<Function<RandomGenerator, MappingState>> getStates(EvaluationEnvironment resolver, VariableTable table) {
        return getParseTree().accept(new StateGeneratorVisitor(resolver.getEvaluator(), table, validator::getTypeOf));
    }

    public Map<String, Measure<? super MappingState>> getMeasures(EvaluationEnvironment env, VariableTable variables) {
        return getParseTree().accept(new MeasureGenerator(env.getEvaluator(), variables));
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
            return this.validator.validate(getParseTree());
        }
        return true;
    }

    private boolean withErrors() {
        return errorList.withErrors();
    }

    private CachedValues evalConstants() {
        return getParseTree().accept(new ConstantsEvaluator(validator.getTypes()));
    }


    public static class IsContinuousModelChecker extends MarkovChainModelBaseVisitor<Boolean> {
        @Override
        public Boolean visitModel(MarkovChainModelParser.ModelContext ctx) {
            if (ctx.model_type() == null) {
                return true;
            } else {
                return ctx.model_type().accept(this);
            }
        }

        @Override
        public Boolean visitCtmcModel(MarkovChainModelParser.CtmcModelContext ctx) {
            return true;
        }

        @Override
        public Boolean visitDtmcModel(MarkovChainModelParser.DtmcModelContext ctx) {
            return false;
        }
    }



    private class StateGeneratorVisitor extends MarkovChainModelBaseVisitor<ParametricDataSet<Function<RandomGenerator, MappingState>>> {

        private final Function<String, Double> resolver;
        private final VariableTable table;
        private final Function<String, DataType> types;
        private ParametricDataSet<Function<RandomGenerator, MappingState>> stateSet;

        public StateGeneratorVisitor(Function<String, Double> resolver, VariableTable table, Function<String,DataType> types) {
            this.resolver = resolver;
            this.table = table;
            this.types = types;
        }

        @Override
        public ParametricDataSet<Function<RandomGenerator, MappingState>> visitModel(MarkovChainModelParser.ModelContext ctx) {
            return ctx.state_declaration().accept(this);
        }

        @Override
        public ParametricDataSet<Function<RandomGenerator, MappingState>> visitSingle_declaration(MarkovChainModelParser.Single_declarationContext ctx) {
            return new ParametricDataSet<>(generateParametricValue(ctx.variables().vars.stream().map(Token::getText).toArray(String[]::new), ctx.assignments()));
        }

        private ParametricValue<Function<RandomGenerator, MappingState>> generateParametricValue(String[] vars, MarkovChainModelParser.AssignmentsContext assignments) {
            Map<String, Integer> index = getIndex(vars);
            Map<String, ToIntFunction<double[]>> values = getVariableInitializers(index, assignments.variable_assignment());
            return new ParametricValue<>(vars, args -> (rg -> table.getMappingStateOf(apply(args, values))));
        }

        private Map<String, Integer> apply(double[] args, Map<String, ToIntFunction<double[]>> values) {
            Map<String, Integer> result = new HashMap<>();
            for (Map.Entry<String, ToIntFunction<double[]>> e: values.entrySet()) {
                result.put(e.getKey(), e.getValue().applyAsInt(args));
            }
            return result;
        }

        private Map<String, ToIntFunction<double[]>> getVariableInitializers(Map<String, Integer> index, List<MarkovChainModelParser.Variable_assignmentContext> variable_assignment) {
            Map<String, ToIntFunction<double[]>> values = new HashMap<>();
            for (MarkovChainModelParser.Variable_assignmentContext va : variable_assignment) {
                values.put(va.name.getText(), getEvalFunction(index, va.name.getText(), va.value));
            }
            return values;
        }

        private ToIntFunction<double[]> getEvalFunction(Map<String, Integer> index, String name, MarkovChainModelParser.ExprContext value) {
            return args -> ExpressionEvaluator.evalInteger(validator::getTypeOf, combine(args, index, resolver), value);
        }


        @Override
        public ParametricDataSet<Function<RandomGenerator, MappingState>> visitMultiple_declarations(MarkovChainModelParser.Multiple_declarationsContext ctx) {
            ParametricDataSet<Function<RandomGenerator, MappingState>> result = new ParametricDataSet<>();
            for (MarkovChainModelParser.Init_declarationsContext decl : ctx.init_declarations()) {
                ParametricValue<Function<RandomGenerator, MappingState>> parValue = generateParametricValue(decl.variables().vars.stream().map(Token::getText).toArray(String[]::new), decl.assignments());
                result.set(decl.name.getText(), parValue);
                if (decl.defaultToken != null) {
                    result.setDefaultState(parValue);
                }
            }
            return result;
        }
    }

    public static Function<String, Double> combine(double[] args, Map<String, Integer> index, Function<String, Double> resolver) {
        return s -> {
            if (index.containsKey(s)) {
                return args[index.get(s)];
            } else {
                return resolver.apply(s);
            }
        };
    }

    public static Map<String, Integer> getIndex(String[] vars) {
        Map<String, Integer> index = new HashMap<>();
        for(int i=0; i<vars.length; i++) {
            index.put(vars[i], i);
        }
        return index;
    }


    private class StateVariableVisitor extends MarkovChainModelBaseVisitor<VariableTable> {

        private final Function<String, Double> evaluator;
        private VariableTable variableTable = null;
        private int varIndex = 0;

        public StateVariableVisitor(Function<String, Double> evaluator) {
            super();
            this.evaluator = evaluator;
        }

        @Override
        public VariableTable visitModel(MarkovChainModelParser.ModelContext ctx) {
            this.variableTable = new VariableTable(ctx.state_declaration().variable_declaration().size());
            for (MarkovChainModelParser.Variable_declarationContext vd : ctx.state_declaration().variable_declaration()) {
                vd.accept(this);
            }
            return variableTable;
        }

        @Override
        public VariableTable visitVariable_declaration(MarkovChainModelParser.Variable_declarationContext ctx) {
            String varName = ctx.name.getText();
            int min = ExpressionEvaluator.evalInteger(validator::getTypeOf, evaluator, ctx.min);
            int max = ExpressionEvaluator.evalInteger(validator::getTypeOf, evaluator, ctx.max);
            //TODO: handle here the fact that min must be < max!
            variableTable.record(varIndex++, varName, min, max);
            return variableTable;
        }

    }

    private class RuleGenerator extends MarkovChainModelBaseVisitor<List<MappingStateUpdate>> {

        private final Function<String, Double> resolver;
        private final VariableTable variables;
        private Predicate<MappingState> guard;
        private ToDoubleFunction<MappingState> weight;
        private LinkedList<MappingStateUpdate> updates;

        public RuleGenerator(Function<String, Double> resolver, VariableTable variables) {
            this.resolver = resolver;
            this.variables = variables;
        }

        @Override
        public List<MappingStateUpdate> visitModel(MarkovChainModelParser.ModelContext ctx) {
            return ctx.rules_declaration().accept(this);
        }

        @Override
        public List<MappingStateUpdate> visitRules_declaration(MarkovChainModelParser.Rules_declarationContext ctx) {
            this.updates = new LinkedList<>();
            for (MarkovChainModelParser.Rule_caseContext ruleCase : ctx.rule_case()) {
                this.guard = StateExpressionEvaluator.evalStatePredicate(validator::getTypeOf, resolver, variables, ruleCase.guard);
                for (MarkovChainModelParser.StepContext step : ruleCase.step()) {
                    if (step.weight == null) {
                        this.weight = s -> 1.0;
                    } else {
                        this.weight = StateExpressionEvaluator.evalToDoubleFunction(validator::getTypeOf, resolver, variables, step.weight);
                    }
                    step.updates().accept(this);
                }
            }
            return updates;
        }

        @Override
        public List<MappingStateUpdate> visitEmptyUpdate(MarkovChainModelParser.EmptyUpdateContext ctx) {
            this.updates.add(new MappingStateUpdate(this.guard, this.weight, Map.of()));
            return this.updates;
        }

        @Override
        public List<MappingStateUpdate> visitListUpdate(MarkovChainModelParser.ListUpdateContext ctx) {
            Map<Integer, ToIntFunction<MappingState>> variablesUpdate = ctx.variable_update().stream().collect(
                    Collectors.toMap(
                        vu -> variables.indexOf(getVariableNameFromTarget(vu.target.getText())),
                        vu -> StateExpressionEvaluator.evalToIntFunction(validator::getTypeOf, resolver, variables, vu.expr())
                    )
            );
            this.updates.add(new MappingStateUpdate(this.guard, this.weight, Map.of()));
            return super.visitListUpdate(ctx);
        }

        private String getVariableNameFromTarget(String target) {
            return target.substring(0, target.length()-1);
        }
    }

    private class MeasureGenerator extends MarkovChainModelBaseVisitor<Map<String, Measure<? super MappingState>>> {


        private final Function<String, Double> resolver;
        private final VariableTable variables;
        private final TreeMap<String, Measure<? super MappingState>> measures;

        public MeasureGenerator(Function<String, Double> resolver, VariableTable variables) {
            this.resolver = resolver;
            this.variables = variables;
            this.measures = new TreeMap<>();
        }

        @Override
        public Map<String, Measure<? super MappingState>> visitModel(MarkovChainModelParser.ModelContext ctx) {
            for (MarkovChainModelParser.Measure_declarationContext m : ctx.measure_declaration()) {
                m.accept(this);
            }
            return measures;
        }

        @Override
        public Map<String, Measure<? super MappingState>> visitMeasure_declaration(MarkovChainModelParser.Measure_declarationContext ctx) {
            String name = ctx.name.getText();
            ToDoubleFunction<MappingState> measureFunction = StateExpressionEvaluator.evalToDoubleFunction(validator::getTypeOf, resolver, variables, ctx.expr());
            measures.put(name, new SimpleMeasure<>(name, measureFunction::applyAsDouble));
            return measures;
        }
    }
}