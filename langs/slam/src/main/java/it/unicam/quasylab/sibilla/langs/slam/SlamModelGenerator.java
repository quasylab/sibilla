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

import it.unicam.quasylab.sibilla.core.models.EvaluationEnvironment;
import it.unicam.quasylab.sibilla.core.models.ModelDefinition;
import it.unicam.quasylab.sibilla.core.models.ParametricDataSet;
import it.unicam.quasylab.sibilla.core.models.ParametricValue;
import it.unicam.quasylab.sibilla.core.models.slam.*;
import it.unicam.quasylab.sibilla.core.models.slam.agents.AgentFactory;
import it.unicam.quasylab.sibilla.core.models.slam.agents.SlamAgent;
import it.unicam.quasylab.sibilla.core.models.slam.agents.SlamAgentDefinitions;
import it.unicam.quasylab.sibilla.core.models.slam.data.AgentStore;
import it.unicam.quasylab.sibilla.core.models.slam.data.SlamValue;
import it.unicam.quasylab.sibilla.core.models.slam.data.VariableRegistry;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import it.unicam.quasylab.sibilla.core.simulator.sampling.SimpleMeasure;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import it.unicam.quasylab.sibilla.langs.util.ErrorCollector;
import it.unicam.quasylab.sibilla.langs.util.SibillaParseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SlamModelGenerator {

    private final CodePointCharStream code;
    private SlamModelParser.ModelContext modelContext;
    private SymbolTable table;
    private ParseTree parseTree;
    private final ErrorCollector errorCollector = new ErrorCollector();
    private SlamModelValidator validator;
    private VariableRegistry variableRegistry;
    private boolean isValidated = false;

    public SlamModelGenerator(File file) throws IOException {
        this(CharStreams.fromReader(new FileReader(file)));
    }

    public SlamModelGenerator(CodePointCharStream code) {
        this.code = code;
    }

    public SlamModelGenerator(String code) {
        this(CharStreams.fromString(code));
    }

    public ParseTree getParseTree() throws SlamModelGenerationException {
        if (this.parseTree == null) {
            generateParseTree();
            validateParseTree();
        }
        return this.parseTree;
    }

    private VariableRegistry getVariableRegistry() {
        if (this.variableRegistry == null) {
            SlamVariableRegistryGenerator variableRegistryGenerator = new SlamVariableRegistryGenerator();
            this.parseTree.accept(variableRegistryGenerator);
            this.variableRegistry = variableRegistryGenerator.getVariableRegistry();
        }
        return this.variableRegistry;
    }

    private void validateParseTree() throws SlamModelGenerationException {
        this.validator = new SlamModelValidator(errorCollector);
        if (this.parseTree.accept(validator)) {
            throw new SlamModelGenerationException(errorCollector);
        } {
            this.isValidated = true;
        }
    }

    private void generateParseTree() throws SlamModelGenerationException {
        SlamModelLexer lexer = new SlamModelLexer(code);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SlamModelParser parser = new SlamModelParser(tokens);
        SibillaParseErrorListener errorListener = new SibillaParseErrorListener(this.errorCollector);
        parser.addErrorListener(errorListener);
        this.parseTree = parser.model();
        if (this.errorCollector.withErrors()) {
            this.parseTree = null;
            throw new SlamModelGenerationException(this.errorCollector);
        }
    }


    public EvaluationEnvironment getEvaluationEnvironment() {
        EvaluationEnvironment environment = new EvaluationEnvironment();
        //SlamScalarExpressionEvaluator evaluator = new SlamScalarExpressionEvaluator(environment.getEvaluator());
        //modelContext.params.forEach(p -> environment.set(p.name.getText(), p.value.accept(evaluator).toDouble()));
        return environment;
    }

    public Map<String, SlamValue> evalConstants(EvaluationEnvironment environment) {
        HashMap<String, SlamValue> constants = new HashMap<>();
        //SlamScalarExpressionEvaluator evaluator = new SlamScalarExpressionEvaluator(environment.getEvaluator());
        //modelContext.consts.forEach(c -> constants.put(c.name.getText(), c.value.accept(evaluator)));
        return constants;
    }

    public MessageRepository getMessageRepository() {
        return validator.getMessageRepository();
    }

    public ModelDefinition<SlamState> getDefinition() throws SlamModelGenerationException {
        generateParseTree();
        return new SlamModelDefinition(this::getAgentDefinition, getMessageRepository(), this::getMeasureTable, this::getPredicateTable, this::getStates);
    }


    private ParametricDataSet<Function<RandomGenerator, SlamState>> getStates(EvaluationEnvironment environment, SlamAgentDefinitions agentDefinitions) {
        return this.parseTree.accept(new SlamStateGenerator(environment.getEvaluator(), agentDefinitions));
    }

    private Map<String, Predicate<? super SlamState>> getPredicateTable(EvaluationEnvironment environment, SlamAgentDefinitions agentDefinitions) {
        return this.parseTree.accept(new PredicateTableGenerator(environment.getEvaluator(), agentDefinitions));
    }

    private Map<String, Measure<? super SlamState>> getMeasureTable(EvaluationEnvironment environment, SlamAgentDefinitions agentDefinitions) {
        return this.parseTree.accept(new MeasureTableGenerator(environment.getEvaluator(), agentDefinitions));
    }

    private SlamAgentDefinitions getAgentDefinition(EvaluationEnvironment environment) {
        SlamAgentDefinitionsGenerator agentDefinitionsGenerator = new SlamAgentDefinitionsGenerator(getVariableRegistry(), environment.getEvaluator(), getMessageRepository());
        this.parseTree.accept(agentDefinitionsGenerator);
        return agentDefinitionsGenerator.getAgentDefinitions();
    }

    private class MeasureTableGenerator extends SlamModelBaseVisitor<Map<String, Measure<? super SlamState>>> {

        private final Function<String, Optional<SibillaValue>> evaluator;
        private final SlamAgentDefinitions agentDefinitions;

        public MeasureTableGenerator(Function<String, Optional<SibillaValue>> evaluator, SlamAgentDefinitions agentDefinitions) {
            super();
            this.evaluator = evaluator;
            this.agentDefinitions = agentDefinitions;
        }

        @Override
        protected Map<String, Measure<? super SlamState>> defaultResult() {
            return new HashMap<>();
        }

        @Override
        protected Map<String, Measure<? super SlamState>> aggregateResult(Map<String, Measure<? super SlamState>> aggregate, Map<String, Measure<? super SlamState>> nextResult) {
            HashMap<String, Measure<? super SlamState>> result = new HashMap<>(aggregate);
            result.putAll(nextResult);
            return result;
        }

        @Override
        public Map<String, Measure<? super SlamState>> visitDeclarationMeasure(SlamModelParser.DeclarationMeasureContext ctx) {
            Function<SlamExpressionEvaluationParameters, SibillaValue> measureFunction = ctx.expr().accept(new SlamExpressionEvaluator(ExpressionContext.MEASURE, evaluator, variableRegistry, agentDefinitions));
            return Map.of(ctx.name.getText(), new SimpleMeasure<>(ctx.name.getText(), s -> measureFunction.apply(new SlamExpressionEvaluationParameters(s)).doubleOf()));
        }
    }

    private class PredicateTableGenerator extends SlamModelBaseVisitor<Map<String, Predicate<? super SlamState>>> {

        private final Function<String, Optional<SibillaValue>> evaluator;
        private final SlamAgentDefinitions agentDefinitions;

        public PredicateTableGenerator(Function<String, Optional<SibillaValue>> evaluator, SlamAgentDefinitions agentDefinitions) {
            super();
            this.evaluator = evaluator;
            this.agentDefinitions = agentDefinitions;
        }

        @Override
        protected Map<String, Predicate<? super SlamState>> defaultResult() {
            return new HashMap<>();
        }

        @Override
        protected Map<String, Predicate<? super SlamState>> aggregateResult(Map<String, Predicate<? super SlamState>> aggregate, Map<String, Predicate<? super SlamState>> nextResult) {
            HashMap<String, Predicate<? super SlamState>> result = new HashMap<>(aggregate);
            result.putAll(nextResult);
            return result;
        }

        @Override
        public Map<String, Predicate<? super SlamState>> visitDeclarationMeasure(SlamModelParser.DeclarationMeasureContext ctx) {
            Function<SlamExpressionEvaluationParameters, SibillaValue> measureFunction = ctx.expr().accept(new SlamExpressionEvaluator(ExpressionContext.MEASURE, evaluator, variableRegistry, agentDefinitions));
            return Map.of(ctx.name.getText(), (SlamState s) -> measureFunction.apply(new SlamExpressionEvaluationParameters(s)).booleanOf());
        }
    }

    private class SlamStateGenerator extends SlamModelBaseVisitor<ParametricDataSet<Function<RandomGenerator, SlamState>>> {
        private final Function<String, Optional<SibillaValue>> evaluator;
        private final SlamAgentDefinitions agentDefinitions;
        private final ParametricDataSet<Function<RandomGenerator, SlamState>> states;

        public SlamStateGenerator(Function<String, Optional<SibillaValue>> evaluator, SlamAgentDefinitions agentDefinitions) {
            this.evaluator = evaluator;
            this.agentDefinitions = agentDefinitions;
            this.states = new ParametricDataSet<>();
        }

        @Override
        protected ParametricDataSet<Function<RandomGenerator, SlamState>> defaultResult() {
            return states;
        }

        @Override
        protected ParametricDataSet<Function<RandomGenerator, SlamState>> aggregateResult(ParametricDataSet<Function<RandomGenerator, SlamState>> aggregate, ParametricDataSet<Function<RandomGenerator, SlamState>> nextResult) {
            return aggregate;
        }

        @Override
        public ParametricDataSet<Function<RandomGenerator, SlamState>> visitDeclarationSystem(SlamModelParser.DeclarationSystemContext ctx) {
            String name = ctx.name.getText();
            ParametricValue<Function<RandomGenerator, SlamState>> parametricValue = generateParametricValue(ctx);
            states.set(name, parametricValue);
            return states;
        }

        private ParametricValue<Function<RandomGenerator, SlamState>> generateParametricValue(SlamModelParser.DeclarationSystemContext ctx) {
            BiFunction<AgentStore,RandomGenerator, SlamState> stateGenerator = getStateGenerator(ctx.agents);
            String[] parameters = ctx.params.stream().map(p -> p.name.getText()).toArray(String[]::new);
            return new ParametricValue<>(parameters, args -> (r -> stateGenerator.apply(variableRegistry.getStore(parameters, args), r)));
        }

        private BiFunction<AgentStore, RandomGenerator, SlamState> getStateGenerator(List<SlamModelParser.AgentExpressionContext> agents) {
            List<BiFunction<AgentStore, RandomGenerator, List<AgentFactory>>> agentFactory = agents.stream().map(this::generateAgentFactoryList).collect(Collectors.toList());
            return (s, r) ->
                    SlamState.newSlamState(agentFactory.stream().map(f -> f.apply(s, r)).flatMap(Collection::stream).collect(Collectors.toList()));
        }

        private BiFunction<AgentStore, RandomGenerator, List<AgentFactory>> generateAgentFactoryList(SlamModelParser.AgentExpressionContext agentExpressionContext) {
            BiFunction<AgentStore, RandomGenerator, AgentFactory> agentFactory = generateAgentFactory(agentExpressionContext);
            if (agentExpressionContext.copies == null) {
                return (s, r) -> List.of(agentFactory.apply(s, r));
            } else {
                Function<SlamExpressionEvaluationParameters, SibillaValue> copies = agentExpressionContext.copies.accept(new SlamExpressionEvaluator(ExpressionContext.SYSTEM, evaluator, variableRegistry, agentDefinitions));
                return (s, r) -> IntStream.range(0, copies.apply(new SlamExpressionEvaluationParameters(r, s)).intOf()).mapToObj(i -> agentFactory.apply(s, r)).collect(Collectors.toList());
            }
        }

        private BiFunction<AgentStore, RandomGenerator, AgentFactory> generateAgentFactory(SlamModelParser.AgentExpressionContext agentExpressionContext) {
            String agentName = agentExpressionContext.name.getText();
            List<Function<SlamExpressionEvaluationParameters, SibillaValue>> argEvaluations = agentExpressionContext.args.stream().map(e -> e.accept(new SlamExpressionEvaluator(ExpressionContext.SYSTEM, evaluator, variableRegistry, agentDefinitions))).collect(Collectors.toList());
            if (agentExpressionContext.state == null) {
                return (s, r) -> agentDefinitions.getAgentFactory(agentName, argEvaluations.stream().map(e -> e.apply(new SlamExpressionEvaluationParameters(r, s))).toArray(SibillaValue[]::new));
            } else {
                String state = agentExpressionContext.state.getText();
                return (s, r) -> agentDefinitions.getAgentFactory(agentName, state, argEvaluations.stream().map(e -> e.apply(new SlamExpressionEvaluationParameters(r, s))).toArray(SibillaValue[]::new));
            }
        }
    }
}
