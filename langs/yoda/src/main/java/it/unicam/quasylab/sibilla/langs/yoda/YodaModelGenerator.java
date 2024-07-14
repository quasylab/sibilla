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

package it.unicam.quasylab.sibilla.langs.yoda;

import it.unicam.quasylab.sibilla.core.models.EvaluationEnvironment;
import it.unicam.quasylab.sibilla.core.models.ParametricDataSet;
import it.unicam.quasylab.sibilla.core.models.yoda.*;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import it.unicam.quasylab.sibilla.langs.util.ErrorCollector;
import it.unicam.quasylab.sibilla.langs.util.SibillaParseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class YodaModelGenerator {

    private final CodePointCharStream source;
    //private ModelValidator validator;
    private ErrorCollector errorCollector;
    //private boolean validated = false;
    private ParseTree parseTree;
    private YodaElementNameRegistry elementNameRegistry;
    private YodaVariableRegistry variableRegistry;

    public YodaModelGenerator(CodePointCharStream source) throws YodaModelGenerationException {
        this.source = source;

    }

    public YodaModelGenerator(String string) throws YodaModelGenerationException {
        this(CharStreams.fromString(string));
    }

    public YodaModelGenerator(File file) throws IOException, YodaModelGenerationException {
        this(CharStreams.fromReader(new FileReader(file)));
    }

    public YodaModelGenerator (URL url) throws URISyntaxException, IOException, YodaModelGenerationException{
        this(new File(url.toURI()));
    }

    public void getParseTree() throws YodaModelGenerationException {
        if (this.parseTree==null){
            generateParseTree();
            validateParseTree();
        }
    }

    private void generateParseTree() throws YodaModelGenerationException {
        YodaModelLexer lexer = new YodaModelLexer(source);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        YodaModelParser parser = new YodaModelParser(tokens);
        SibillaParseErrorListener errorListener = new SibillaParseErrorListener();
        parser.addErrorListener(errorListener);
        this.parseTree = parser.model();
        this.errorCollector = errorListener.getErrorCollector();
        if (this.errorCollector.withErrors()) {
            throw new YodaModelGenerationException(this.errorCollector);
        }
    }

    public void validateParseTree() throws YodaModelGenerationException {
        YodaModelValidator validator = new YodaModelValidator(this.errorCollector);
        if (!validator.validate(parseTree)) {
            throw new YodaModelGenerationException(this.errorCollector);
        }
        this.elementNameRegistry = validator.getElementNameRegistry();
    }

    private boolean withErrors() {
        return errorCollector.withErrors();
    }

    public YodaModelDefinition getYodaModelDefinition() throws YodaModelGenerationException {
        getParseTree();
        if (withErrors()) {
            throw new YodaModelGenerationException(errorCollector);
        }
        return new YodaModelDefinition(
                getEvaluationEnvironment(),
                new YodaModelGeneratorElementProvider()
        );
    }

    protected ParametricDataSet<Function<RandomGenerator, YodaSystemState>> generateSystemStates(Function<String, Optional<YodaFunction>> functions, EvaluationEnvironment environment) {
        YodaConfigurationsGenerator generator = new YodaConfigurationsGenerator(functions,
                environment.getEvaluator(),
                getYodaAgentsDefinitions(functions, environment.getEvaluator()), getYodaVariableRegistry(), getYodaElementNameRegistry() );
        this.parseTree.accept(generator);
        return generator.getGeneratedConfigurations();
    }

    protected Map<String, Predicate<YodaSystemState>> generatePredicates(Function<String, Optional<YodaFunction>> functions, EvaluationEnvironment environment) {
        YodaPredicateGenerator generator = new YodaPredicateGenerator(functions, environment.getEvaluator(), getYodaVariableRegistry(), getYodaElementNameRegistry());
        this.parseTree.accept(generator);
        return generator.getPredicates();
    }

    protected Map<String, Measure<YodaSystemState>> generateMeasures(Function<String, Optional<YodaFunction>> functions, EvaluationEnvironment environment) {
        YodaMeasuresGenerator generator = new YodaMeasuresGenerator(functions, environment.getEvaluator(), getYodaVariableRegistry(), getYodaElementNameRegistry());
        this.parseTree.accept(generator);
        return generator.getMeasures();
    }

    public Function<String, Optional<YodaFunction>> generateDeclaredFunctions(EvaluationEnvironment evaluationEnvironment) {
        YodaFunctionGenerator yodaFunctionGenerator = new YodaFunctionGenerator(evaluationEnvironment.getEvaluator(), getYodaVariableRegistry());
        this.parseTree.accept(yodaFunctionGenerator);
        return yodaFunctionGenerator::getFunction;
    }


    public EvaluationEnvironment getEvaluationEnvironment() throws YodaModelGenerationException {
        ConstantsAndParametersEvaluator evaluator = new ConstantsAndParametersEvaluator();
        this.parseTree.accept(evaluator);
        return new EvaluationEnvironment(evaluator.getParameters(), this::evalConstantsAndParameters);//evaluateConstants());
    }

    private Map<String, SibillaValue> evalConstantsAndParameters(Map<String, SibillaValue> parameters) {
        ConstantsAndParametersEvaluator evaluator = new ConstantsAndParametersEvaluator(parameters);
        this.parseTree.accept(evaluator);
        return evaluator.getConstantsAndParameters();
    }

    public YodaVariableRegistry getYodaVariableRegistry() {
        if (this.variableRegistry == null) {
            YodaVariableCollector collector = new YodaVariableCollector();
            this.parseTree.accept(collector);
            this.variableRegistry = collector.getVariableRegistry();
        }
        return this.variableRegistry;
    }

    public YodaElementNameRegistry getYodaElementNameRegistry() {
        return this.elementNameRegistry;
    }

    public YodaAgentsDefinitions getYodaAgentsDefinitions(Function<String, Optional<YodaFunction>> functions, Function<String, Optional<SibillaValue>> nameResolver) {
        YodaAgentsDefinitionsGenerator generator = new YodaAgentsDefinitionsGenerator(functions, this.elementNameRegistry, getYodaVariableRegistry(), nameResolver);
        this.parseTree.accept(generator);
        return generator.getAgentsDefinitions();
    }


    public static Function<String, Optional<SibillaValue>> getNameEvaluationFunction(Map<String, SibillaValue> map) {
        return name -> {
            if (map.containsKey(name)) {
                return Optional.of(map.get(name));
            } else {
                return Optional.empty();
            }
        };
    }

    class YodaModelGeneratorElementProvider implements YodaModelElementProvider {

        private EvaluationEnvironment evaluationEnvironment;
        private Function<String, Optional<YodaFunction>> functions;

        @Override
        public void setEvaluationEnvironment(EvaluationEnvironment evaluationEnvironment) {
            this.evaluationEnvironment = evaluationEnvironment;
            this.functions = generateDeclaredFunctions(evaluationEnvironment);
        }

        @Override
        public Map<String, Measure<YodaSystemState>> getMeasures() {
            return generateMeasures(functions, evaluationEnvironment);
        }

        @Override
        public Map<String, Predicate<YodaSystemState>> getPredicates() {
            return generatePredicates(functions, evaluationEnvironment);
        }

        @Override
        public ParametricDataSet<Function<RandomGenerator, YodaSystemState>> getSystemStates() {
            return generateSystemStates(functions, evaluationEnvironment);
        }
    }

}
