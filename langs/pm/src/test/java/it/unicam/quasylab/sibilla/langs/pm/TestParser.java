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

package it.unicam.quasylab.sibilla.langs.pm;

import it.unicam.quasylab.sibilla.core.models.pm.PopulationModelDefinition;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestParser {

    public final String CODE1 = "const TEST = 10;";


    @Test
    public void testParsingConst() {
        PopulationModelLexer lexer = new PopulationModelLexer(CharStreams.fromString(CODE1));
        CommonTokenStream tokens =  new CommonTokenStream(lexer);
        PopulationModelParser parser = new PopulationModelParser(tokens);
        ParseTree tree = parser.model();
        assertEquals(0,parser.getNumberOfSyntaxErrors());
    }

    public final String CODE2 = "species S;\n" +
            "species E;\n" +
            "species I;\n" +
            "species R;\n";

    @Test
    public void testParsingSEIR() throws ModelGenerationException {
        PopulationModelLexer lexer = new PopulationModelLexer(CharStreams.fromString(CODE2));
        CommonTokenStream tokens =  new CommonTokenStream(lexer);
        PopulationModelParser parser = new PopulationModelParser(tokens);
        ParseTree tree = parser.model();
        //assertEquals(0,parser.getNumberOfSyntaxErrors());
        PopulationModelGenerator pmg = new PopulationModelGenerator();
        PopulationModelDefinition pmd = pmg.getPopulationModelDefinition(tree);
        assertNotNull(pmd);
    }

}
