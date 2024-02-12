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

package it.unicam.quasylab.sibilla.langs.enba.generators;

import it.unicam.quasylab.sibilla.core.models.carma.targets.enba.ENBAModelDefinition;
import it.unicam.quasylab.sibilla.langs.enba.ExtendedNBALexer;
import it.unicam.quasylab.sibilla.langs.enba.ExtendedNBAParser;
import it.unicam.quasylab.sibilla.langs.enba.errors.ModelBuildingError;
import it.unicam.quasylab.sibilla.langs.enba.generators.exceptions.ModelGenerationException;
import it.unicam.quasylab.sibilla.langs.enba.symbols.SymbolTable;
import it.unicam.quasylab.sibilla.langs.enba.validators.ModelValidator;
import it.unicam.quasylab.sibilla.langs.util.ParseError;
import it.unicam.quasylab.sibilla.langs.util.SibillaParseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class ENBAModelGenerator {
    private final CodePointCharStream source;
    private ParseTree parseTree;
    private final List<ModelBuildingError> errorList = new LinkedList<>();
    private ModelValidator validator;
    private boolean validated = false;

    public ENBAModelGenerator(String code) {
        this(CharStreams.fromString(code));
    }

    public ENBAModelGenerator(File file) throws IOException {
        this(CharStreams.fromReader(new FileReader(file)));
    }

    public ENBAModelGenerator(CodePointCharStream source) {
        this.source = source;
    }

    public ParseTree getParseTree() {
        if (this.parseTree == null) {
            generateParseTree();
        }
        return this.parseTree;
    }

    private void generateParseTree() {
        ExtendedNBALexer lexer = new ExtendedNBALexer(source);
        CommonTokenStream tokens =  new CommonTokenStream(lexer);
        ExtendedNBAParser parser = new ExtendedNBAParser(tokens);
        SibillaParseErrorListener errorListener = new SibillaParseErrorListener();
        parser.addErrorListener(errorListener);
        this.parseTree = parser.model();
        for (ParseError e: errorListener.getErrorCollector().getSyntaxErrorList()) {
            this.errorList.add(ModelBuildingError.syntaxError(e));
        }
    }

    public ENBAModelDefinition loadModel(String code) throws ModelGenerationException {
        return getModelDefinition();
    }

    public ENBAModelDefinition getModelDefinition() throws ModelGenerationException {
        validate();
        if (withErrors()) {
            throw new ModelGenerationException(this.errorList);
        }
        return new ENBAModelDefinition(
                this.parseTree.accept(new StateSetGenerator(this.validator.getTable())),
                this.parseTree.accept(new MeasuresGenerator(this.validator.getTable())),
                this.parseTree.accept(new PredicatesGenerator(this.validator.getTable())),
                this.parseTree.accept(new RulesGenerator(this.validator.getTable()))
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

    public SymbolTable getSymbolTable() {
        if (validator != null) {
            return validator.getTable();
        }
        return new SymbolTable();
    }

}