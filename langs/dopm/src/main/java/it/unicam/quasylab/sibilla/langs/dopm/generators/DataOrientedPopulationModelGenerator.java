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

package it.unicam.quasylab.sibilla.langs.dopm.generators;

import it.unicam.quasylab.sibilla.core.models.EvaluationEnvironment;
import it.unicam.quasylab.sibilla.core.models.dopm.DataOrientedPopulationModelDefinition;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelLexer;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelParser;
import it.unicam.quasylab.sibilla.langs.dopm.errors.ModelBuildingError;
import it.unicam.quasylab.sibilla.langs.dopm.generators.exceptions.ModelGenerationException;
import it.unicam.quasylab.sibilla.langs.dopm.symbols.BaseSymbolTable;
import it.unicam.quasylab.sibilla.langs.dopm.symbols.SymbolTable;
import it.unicam.quasylab.sibilla.langs.dopm.validators.ModelValidator;
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

public class DataOrientedPopulationModelGenerator {

    private final CodePointCharStream source;
    private ParseTree parseTree;
    private final List<ModelBuildingError> errorList = new LinkedList<>();
    private ModelValidator validator;
    private boolean validated = false;
    private EvaluationEnvironment environment;


    public DataOrientedPopulationModelGenerator(String code) {
        this(CharStreams.fromString(code));
    }

    public DataOrientedPopulationModelGenerator(File file) throws IOException {
        this(CharStreams.fromReader(new FileReader(file)));
    }

    public DataOrientedPopulationModelGenerator(CodePointCharStream source) {
        this.source = source;
    }

    public ParseTree getParseTree() {
        if (this.parseTree == null) {
            generateParseTree();
        }
        return this.parseTree;
    }

    private void generateParseTree() {
        DataOrientedPopulationModelLexer lexer = new DataOrientedPopulationModelLexer(source);
        CommonTokenStream tokens =  new CommonTokenStream(lexer);
        DataOrientedPopulationModelParser parser = new DataOrientedPopulationModelParser(tokens);
        SibillaParseErrorListener errorListener = new SibillaParseErrorListener();
        parser.addErrorListener(errorListener);
        this.parseTree = parser.model();
        for (ParseError e: errorListener.getErrorCollector().getSyntaxErrorList()) {
            this.errorList.add(ModelBuildingError.syntaxError(e));
        }
    }

    public DataOrientedPopulationModelDefinition loadModel(String code) throws ModelGenerationException {
        return getPopulationModelDefinition();
    }

    public DataOrientedPopulationModelDefinition getPopulationModelDefinition() throws ModelGenerationException {
        validate();
        if (withErrors()) {
            throw new ModelGenerationException(this.errorList);
        }
        return new DataOrientedPopulationModelDefinition(
                this.parseTree.accept(new StateSetGenerator()),
                this.parseTree.accept(new MeasuresGenerator()),
                this.parseTree.accept(new PredicatesGenerator()),
                this.parseTree.accept(new RulesGenerator())
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
        return new BaseSymbolTable();
    }

}
