/*
 *  Sibilla:  a Java framework designed to support analysis of Collective
 *  Adaptive Systems.
 *
 *              Copyright (C) ${YEAR}.
 *
 *  See the NOTICE file distributed with this work for additional information
 *  regarding copyright ownership.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *    or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package it.unicam.quasylab.sibilla.langs.stl;

import it.unicam.quasylab.sibilla.langs.slam.StlModelLexer;
import it.unicam.quasylab.sibilla.langs.slam.StlModelParser;
import it.unicam.quasylab.sibilla.langs.util.ErrorCollector;
import it.unicam.quasylab.sibilla.langs.util.SibillaParseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.function.ToDoubleFunction;

public class StlLoader {

    private final CodePointCharStream source;
    private StlModelParser.ModelContext parseTree;
    private ErrorCollector errorCollector = new ErrorCollector();

    public StlLoader(CodePointCharStream source) {
        this.source = source;
    }

    public StlLoader(String source) {
        this(CharStreams.fromString(source));
    }

    public StlLoader(File source) throws IOException {
        this(CharStreams.fromReader(new FileReader(source)));
    }

    public StlLoader(URL source) throws IOException, URISyntaxException {
        this(new File(source.toURI()));
    }


    private void generateParseTree() throws StlModelGenerationException {
        StlModelLexer lexer = new StlModelLexer(source);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        StlModelParser parser = new StlModelParser(tokens);
        SibillaParseErrorListener errorListener = new SibillaParseErrorListener();
        parser.addErrorListener(errorListener);
        this.parseTree = parser.model();
        this.errorCollector = errorListener.getErrorCollector();
        if (this.errorCollector.withErrors()) {
            throw new StlModelGenerationException(this.errorCollector);
        }
    }

    public <S> StlMonitorFactory<S> getModelFactory(Map<String, ToDoubleFunction<S>> measures) throws StlModelGenerationException {
        if (this.parseTree==null){
            generateParseTree();
        }
        StlMonitorFactoryVisitor<S> visitor = new StlMonitorFactoryVisitor<>(errorCollector, measures);
        this.parseTree.accept(visitor);
        return visitor.getFactory();
    }

}
