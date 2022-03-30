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


import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestParser {

    public final String CODE1 =
                    "const alpha = 0.25;\n" +
                    "const beta = 0.25;\n" +
                    "action toRed = alpha;\n" +
                    "action toBlue = beta;\n" +
                    "state R {\n" +
                    "toBlue.B\n" +
                    "}\n" +
                    "state B {\n" +
                    "toRed.R\n" +
                    "}" +
                    "system test(r,b)=R#r]|B#b;";

    public final String CODE2 =
            "const alpha = 0.25;\n" +
            "const alpha = 0.25;\n";

    @Test
    public void codeOneShouldBeParsed() {
        assertNotNull(getParseTree(CODE1));
    }

    @Test
    public void codeTwoShouldBeParsed() {
        assertNotNull(getParseTree(CODE1));
    }

//
//    @Test
//    @Disabled
//    public void aSymbolTableShouldBeObtainedFromCode1() {
//        ParseTree tree = getParseTree(CODE1);
//        SymbolTable table = getSymbolTable(getParseTree(CODE1));
//        assertEquals(7,table.getNames().size());
//    }
//
//    @Test
//    @Disabled
//    public void noErrorShouldBeInTheSymbolTableObtainedFromCode1() {
//        ParseTree tree = getParseTree(CODE1);
//        SymbolCollector collector = new SymbolCollector();
//        collector.visit(tree);
//        assertTrue(collector.getErrors().isEmpty());
//    }
//
//    @Test
//    @Disabled
//    public void oneErrorShouldBeInTheSymbolTableObtainedFromCode2() {
//        ParseTree tree = getParseTree(CODE2);
//        SymbolCollector collector = new SymbolCollector();
//        collector.visit(tree);
//        assertEquals(1,collector.getErrors().size());
//    }
//
//    @Test
//    @Disabled
//    public void allTheExceptedNamesShouldBeInTheSymbolTableOfCode1() {
//        ParseTree tree = getParseTree(CODE1);
//        SymbolTable table = getSymbolTable(getParseTree(CODE1));
//        assertEquals(7,table.size());
//        assertTrue(table.isAConstant("alpha"),"alpha should be a constant");
//        assertTrue(table.isAConstant("beta"),"beta should be a constant");
//        assertTrue(table.isAnAction("toRed"),"toRed should be an action");
//        assertTrue(table.isAnAction("toBlue"),"toBlue should be an action");
//        assertTrue(table.isAState("B"),"B should be a state");
//        assertTrue(table.isAState("R"),"R should be a statte");
//        assertTrue(table.isASystem("test"),"test should be a system");
//    }
//
//    @Test
//    @Disabled
//    public void codeOneShouldPassChecking() {
//        ParseTree tree = getParseTree(CODE1);
//        SymbolTable table = getSymbolTable(getParseTree(CODE1));
//        ModelCorrectnessChecker checker = new ModelCorrectnessChecker(table);
//        boolean flag = tree.accept(checker);
//        assertTrue(checker.getErrors().isEmpty());
//    }
//
//
//    private SymbolTable getSymbolTable(ParseTree tree) {
//        SymbolCollector collector = new SymbolCollector();
//        collector.visit(tree);
//        return collector.getSymbolTable();
//    }
//
//

    private ParseTree getParseTree(String code) {
        LIOModelLexer lexer = new LIOModelLexer(CharStreams.fromString(code));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        LIOModelParser parser = new LIOModelParser(tokens);
        return parser.model();
    }


}
