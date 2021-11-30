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

import it.unicam.quasylab.sibilla.langs.util.ErrorCollector;
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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public class YodaModelGenerator {

    private final CodePointCharStream source;
    private ModelValidator validator;
    private final ErrorCollector errorCollector = new ErrorCollector();
    private boolean validated = false;
    private YodaModelParser.ModelContext parseTree;




    public YodaModelGenerator(CodePointCharStream source){ this.source = source; }

    public YodaModelGenerator(String string){ this(CharStreams.fromString(string));}

    public ParseTree getParseTree(){
        if (this.parseTree==null){
            generateParseTree();
        }
        return this.parseTree;
    }

    private void generateParseTree() {
        YodaModelLexer lexer = new YodaModelLexer(source);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        YodaModelParser parser = new YodaModelParser(tokens);
        SibillaParseErrorListener errorListener = new SibillaParseErrorListener(errorCollector);
        parser.addErrorListener(errorListener);
        this.parseTree = parser.model();

    }
}
