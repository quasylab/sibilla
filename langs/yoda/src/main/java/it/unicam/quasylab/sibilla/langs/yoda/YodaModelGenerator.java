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

import it.unicam.quasylab.sibilla.core.models.CachedValues;
import it.unicam.quasylab.sibilla.core.models.EvaluationEnvironment;
import it.unicam.quasylab.sibilla.core.models.yoda.YodaModelDefinition;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import it.unicam.quasylab.sibilla.langs.util.ErrorCollector;
import it.unicam.quasylab.sibilla.langs.util.SibillaParseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class YodaModelGenerator {

    private final CodePointCharStream source;
    //private ModelValidator validator;
    private ErrorCollector errorCollector;
    //private boolean validated = false;
    private ParseTree parseTree;
    private SymbolTable table;

    public YodaModelGenerator(CodePointCharStream source) throws YodaModelGenerationException {
        this.source = source;

    }

    public YodaModelGenerator(String string) throws YodaModelGenerationException {
        this(CharStreams.fromString(string));
    }

    public YodaModelGenerator(File file) throws IOException, YodaModelGenerationException {
        this(CharStreams.fromReader(new FileReader(file)));
    }

    public ParseTree getParseTree() throws YodaModelGenerationException {
        if (this.parseTree==null){
            generateParseTree();
            validateParseTree();
        }
        return this.parseTree;
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
            throw new YodaModelGenerationException("Syntax Error!", this.errorCollector);
        }
    }

    public void validateParseTree() throws YodaModelGenerationException {
        YodaModelValidator validator = new YodaModelValidator(this.errorCollector);
        if (!this.parseTree.accept(validator)) {
            throw new YodaModelGenerationException("Syntax Error!", this.errorCollector);
        }
        this.table = validator.getTable();
    }

    private boolean withErrors() {
        return errorCollector.withErrors();
    }

    public YodaModelDefinition getYodaModelDefinition() throws YodaModelGenerationException {
        return new YodaModelDefinition(
                getEvaluationEnvironment()
                //TODO inserire parametri
        );
    }

    public EvaluationEnvironment getEvaluationEnvironment() throws YodaModelGenerationException {
        return new EvaluationEnvironment(evaluateParameters(), evaluateConstants());
    }

    //TODO
    private Map<String, SibillaValue> evaluateParameters() throws YodaModelGenerationException {
        return null;
    }

    //TODO
    private CachedValues evaluateConstants() throws YodaModelGenerationException {
        return null;
    }


}
