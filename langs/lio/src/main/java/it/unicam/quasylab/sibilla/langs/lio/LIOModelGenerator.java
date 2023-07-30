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
import it.unicam.quasylab.sibilla.core.models.lio.*;
import it.unicam.quasylab.sibilla.langs.util.ErrorCollector;
import it.unicam.quasylab.sibilla.langs.util.SibillaParseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class LIOModelGenerator {

    private final CodePointCharStream sourceCode;
    private ParseTree parseTree;
    private ErrorCollector errorCollector;
    private SymbolTable symbolTable;

    public LIOModelGenerator(File file) throws IOException, LIOModelParseError {
        this(CharStreams.fromReader(new FileReader(file)));
    }

    public LIOModelGenerator(String sourceCode) throws LIOModelParseError {
        this(CharStreams.fromString(sourceCode));
    }

    public LIOModelGenerator(CodePointCharStream sourceCode) throws LIOModelParseError {
        this.sourceCode = sourceCode;
        generateParseTree();
        validateParseTree();
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


    public AgentsDefinition generateAgentsDefinition(EvaluationEnvironment environment) {
        return null;
    }


    public LIOModel generateInddividualModel(EvaluationEnvironment environment) {
        return null;//return new LIOModel<LIOIndividualState>(DefinitionGenerator.apply(environment), measureGenerator.apply(environment), predicateGenerator.apply(environment));
    }

    public LIOModelDefinition getMassModelDefinition() {
        return null;
    }

    public LIOModelDefinition getIndividualModelDefinition() {
        return null;
    }

    private EvaluationEnvironment generateEnvironment() {
        return this.parseTree.accept(new EvaluationEnvironmentGenerator());
    }

    private AgentsDefinition getAgentsDefinition(EvaluationEnvironment environment) {
        return null;
    }

}
