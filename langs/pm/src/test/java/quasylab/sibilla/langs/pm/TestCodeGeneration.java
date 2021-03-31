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

package quasylab.sibilla.langs.pm;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;

public class TestCodeGeneration {

    @Test
    public void testTrue() {
        testGeneratedExpressionCode("true","true");
    }

    @Test
    public void testFalse() {
        testGeneratedExpressionCode("false","false");
    }

    @Test
    public void testSum() {
        testGeneratedExpressionCode("2+2","(2 + 2)");
    }

    @Test
    public void testDif() {
        testGeneratedExpressionCode("2-2","(2 - 2)");
    }

    @Test
    public void testModulo() {
        testGeneratedExpressionCode("2 % 2","(2 % 2)");
    }

    @Test
    public void testMul() {
        testGeneratedExpressionCode("2 * 2","(2 * 2)");
    }

    @Test
    public void testDiv() {
        testGeneratedExpressionCode("2 // 2","PopulationModelDefinition.fraction(2, 2)");
    }

    @Test
    public void testAnd1() {
        testGeneratedExpressionCode("2 & 2","(2 && 2)");
    }

    @Test
    public void testAnd2() {
        testGeneratedExpressionCode("2 && 2","(2 && 2)");
    }

    @Test
    public void testOr1() {
        testGeneratedExpressionCode("2 || 2","(2 || 2)");
    }

    @Test
    public void testOr2() {
        testGeneratedExpressionCode("2 | 2","(2 || 2)");
    }

    @Test
    public void testNot() {
        testGeneratedExpressionCode("!true","(! true)");
    }

    @Test
    public void testPow() {
        testGeneratedExpressionCode("2 ^ 2","Math.pow(2, 2)");
    }

    @Test
    public void testReference() {
        testGeneratedExpressionCode("avar","__avar__");
    }

    @Test
    public void testRelation() {
        testGeneratedExpressionCode("2 < 2","(2 < 2)");
        testGeneratedExpressionCode("2 <= 2","(2 <= 2)");
        testGeneratedExpressionCode("2 == 2","(2 == 2)");
        testGeneratedExpressionCode("2 > 2","(2 > 2)");
        testGeneratedExpressionCode("2 >= 2","(2 >= 2)");
    }

    @Test
    public void testBrackets() {
        testGeneratedExpressionCode("((2+2))","(2 + 2)");
    }


    @Test
    public void testPopulationFraction() {
        testGeneratedExpressionCode("%A<1,2,3>","state.getFraction(_SPECIES_A__1_2_3)");
    }

    @Test
    public void testPopulationOccupancy() {
        testGeneratedExpressionCode("#A<1,2,3>","state.getOccupancy(_SPECIES_A__1_2_3)");
    }

    @Test
    public void testNow() {
        testGeneratedExpressionCode("now","now");
    }

    @Test
    public void testUnary() {
        testGeneratedExpressionCode("+2","(+ 2)");
        testGeneratedExpressionCode("-2","(- 2)");
    }



    @Test
    public void testIfThenElse() {
        testGeneratedExpressionCode("(true?1:2)","(true?1:2)");
    }

    @Test
    public void testReal() {
        testGeneratedExpressionCode("3.5","3.5");
    }


    private void testGeneratedExpressionCode(String code, String result) {
        PopulationModelLexer lexer = new PopulationModelLexer(CharStreams.fromString(code));
        CommonTokenStream tokens =  new CommonTokenStream(lexer);
        PopulationModelParser parser = new PopulationModelParser(tokens);
        ParseTree tree = parser.expr();
        StringTemplateGenerator stringTemplateGenerator = new StringTemplateGenerator(null);
        assertEquals(result, stringTemplateGenerator.visit(tree).render());
    }


}
