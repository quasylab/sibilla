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

import it.unicam.quasylab.sibilla.core.models.EvaluationEnvironment;
import it.unicam.quasylab.sibilla.core.models.ParametricDataSet;
import it.unicam.quasylab.sibilla.core.models.lio.*;
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
import java.util.Map;
import java.util.function.Function;

public class LIOModelGenerator {

    private final CodePointCharStream sourceCode;
    private ParseTree parseTree;
    private ErrorCollector errorCollector;


    public LIOModelGenerator(File file) throws IOException, LIOModelParseError {
        this(CharStreams.fromReader(new FileReader(file)));
    }

    public LIOModelGenerator(String sourceCode) throws LIOModelParseError {
        this(CharStreams.fromString(sourceCode));
    }

    public LIOModelGenerator(CodePointCharStream sourceCode) {
        this.sourceCode = sourceCode;
    }

    private void validateParseTree() throws LIOModelParseError {
        LIOModelValidator validator = new LIOModelValidator(this.errorCollector);
        if (!this.parseTree.accept(validator)) {
            throw new LIOModelParseError("Syntax Error!", this.errorCollector);
        }
        
    }

    private void generateParseTree() throws LIOModelParseError {
        LIOModelLexer lexer = new LIOModelLexer(sourceCode);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        LIOModelParser parser = new LIOModelParser(tokens);
        SibillaParseErrorListener errorListener = new SibillaParseErrorListener();
        parser.addErrorListener(errorListener);
        this.parseTree = parser.model();
        this.errorCollector = errorListener.getErrorCollector();
        if (this.errorCollector.withErrors()) {
            throw new LIOModelParseError("Syntax Error!", this.errorCollector);
        }
    }


    public EvaluationEnvironment generateEvaluationEnvironment() {
        ErrorCollector errorCollector = new ErrorCollector();
        LIOModelConstantsAndParametersEvaluator evaluator = new LIOModelConstantsAndParametersEvaluator(errorCollector);
        this.parseTree.accept(evaluator);
        return new EvaluationEnvironment(evaluator.getParameters(), evaluator.getConstantsAndParameters(), this::evaluateConstantsAndParameters);
    }

    private Map<String, SibillaValue> evaluateConstantsAndParameters(Map<String, SibillaValue> stringSibillaValueMap) {
        LIOModelConstantsAndParametersEvaluator evaluator = new LIOModelConstantsAndParametersEvaluator(errorCollector);
        this.parseTree.accept(evaluator);
        return evaluator.getConstantsAndParameters();
    }

    public LIOModelDefinition getDefinition() throws LIOModelParseError {
        generateParseTree();
        validateParseTree();
        return new LIOModelDefinition(generateEvaluationEnvironment(), this::agentsDefinitionGenerator, this::modelGenerator, this::configurationsGenerator);
    }

    private ParametricDataSet<Function<RandomGenerator, LIOState>> configurationsGenerator(EvaluationEnvironment environment, AgentsDefinition agentsDefinition) {
        LIOModelSystemGenerator systemGenerator = new LIOModelSystemGenerator(this.errorCollector, agentsDefinition, environment.getValues());
        this.parseTree.accept(systemGenerator);
        return systemGenerator.getSystems();
    }

    private LIOModel modelGenerator(EvaluationEnvironment environment, AgentsDefinition agentsDefinition) {
        LIOModelMeasuresGenerator measuresGenerator = new LIOModelMeasuresGenerator(this.errorCollector, agentsDefinition, environment.getValues());
        LIOModelPredicateGenerator predicatesGenerator = new LIOModelPredicateGenerator(this.errorCollector, agentsDefinition, environment.getValues());
        this.parseTree.accept(measuresGenerator);
        this.parseTree.accept(predicatesGenerator);
        return new LIOModel(agentsDefinition, measuresGenerator.getMeasures(), predicatesGenerator.getPredicates());
    }

    private AgentsDefinition agentsDefinitionGenerator(EvaluationEnvironment environment) {
        AgentsDefinition agentsDefinition = new AgentsDefinition();
        this.parseTree.accept(new LIOModelAgentStatesGenerator(this.errorCollector, agentsDefinition, environment.getValues()));
        this.parseTree.accept(new LIOModelActionsGenerator(this.errorCollector, agentsDefinition, environment.getValues()));
        this.parseTree.accept(new LIOModelAgentStepGenerator(this.errorCollector, agentsDefinition, environment.getValues()));
        return agentsDefinition;
    }

}
