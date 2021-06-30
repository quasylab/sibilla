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

import it.unicam.quasylab.sibilla.core.models.EvaluationEnvironment;
import it.unicam.quasylab.sibilla.core.models.StateSet;
import it.unicam.quasylab.sibilla.core.models.pm.*;
import it.unicam.quasylab.sibilla.core.models.pm.util.PopulationRegistry;
import it.unicam.quasylab.sibilla.core.simulator.DefaultRandomGenerator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestParser {

    public final String CODE1 = "const TEST = 10;";

    public final String CODE2 = "species S;\n" +
            "species E;\n" +
            "species I;\n" +
            "species R;\n" +
            "\n" +
            "rule s_to_e {\n" +
            "    S|I -[ #S*1.0*%I ]-> E|I\n" +
            "}\n" +
            "\n" +
            "rule e_to_i {\n" +
            "    E -[ 1.0 ]-> I\n" +
            "}\n" +
            "\n" +
            "rule i_to_r {\n" +
            "    I -[ 1.0 ]-> R\n" +
            "}\n" +
            "\n" +
            "rule r_to_s {\n" +
            "    R -[ 1.0 ]-> S\n" +
            "}\n" +
            "\n" +
            "system init = S<10>|I;";

    public final String CODE3 = "param x = 10;";

    public final String CODE4 = "species A of [0,10]*[0,10];";

    public final String CODE5 = "const N = 4;\n" +
            "species A of [0,N]*[0,N];";

    public final String CODE6 = "param N = 5;\n" +
            "species A of [0,N]*[0,N];";

    public final String CODE7 = "This is an evident error!";

    public final String CODE8 = "param lambda = 1.0;\n" +
            "const N1 = 10;\n" +
            "const N2 = 10;\n" +
            "const NU = 10;\n" +
            "species A0;\n" +
            "species A1;\n " +
            "species AU;\n" +
            "rule change01 { A0|A1 -[ #A0*%A1*lambda ]-> A0|AU }\n" +
            "rule change10 { A1|A0 -[ #A1*%A0*lambda ]-> A1|AU }\n" +
            "rule changeU0 { A0|AU -[ #A0*%AU*lambda ]-> A0|A0 }\n" +
            "rule changeU1 { A1|AU -[ #A1*%AU*lambda ]-> A1|A1 }\n" +
            "system init = A0<10>|A1<10>|AU<10>;";

    public final String CODE9 = "    param lambdaMeet = 1.0;\n" +
            "    param probInfection = 0.25; \n" +
            "    param recoverRate = 0.05;\n" +
            "    \n" +
            "    const startS = 90;\n" +
            "    const startI = 10;\n" +
            "    \n" +
            "    species S;\n" +
            "    species I;\n" +
            "    species R;\n" +
            "    \n" +
            "    rule infection {\n" +
            "        S|I -[ probInfection ]-> I|I\n" +
            "    }\n" +
            "    \n" +
            "    rule recovered {\n" +
            "        I -[ #I*recoverRate ]-> R\n" +
            "    }\n" +
            "    \n" +
            "    system init = S<startS>|I<startI>;\n";

    @Test
    public void testParsingWrongData() throws ModelGenerationException {
        PopulationModelGenerator generator = new PopulationModelGenerator(CODE7);
        assertFalse(generator.validate());
    }

    @Test
    public void testParsingConst() throws ModelGenerationException {
        PopulationModelGenerator generator = new PopulationModelGenerator(CODE1);
        assertTrue(generator.validate());
        SymbolTable st = generator.getSymbolTable();
        assertTrue(st.isAConst("TEST"));
        OldExpressionEvaluator evaluator = new OldExpressionEvaluator(st);
        assertEquals(10.0, evaluator.getValueOfConstant("TEST"));

    }

    @Test
    public void testParsingParam() throws ModelGenerationException {
        PopulationModelGenerator generator = new PopulationModelGenerator(CODE3);
        assertTrue(generator.validate());
        EvaluationEnvironment ee = generator.generateEvaluationEnvironment();
        assertArrayEquals(new String[]{"x"}, ee.getParameters());
        assertEquals(10, ee.get("x"));
    }

    @Test
    public void testParsingSEIRSpecies() throws ModelGenerationException {
        PopulationModelGenerator pmg = new PopulationModelGenerator(CODE2);
        assertTrue(pmg.validate());
        PopulationRegistry reg = pmg.generatePopulationRegistry(new EvaluationEnvironment());
        assertEquals(4, reg.size());
        assertTrue(reg.indexOf("S") >= 0);
        assertTrue(reg.indexOf("E") >= 0);
        assertTrue(reg.indexOf("I") >= 0);
        assertTrue(reg.indexOf("R") >= 0);
        assertEquals(-1, reg.indexOf("X"));
    }

    @Test
    public void testParsingParametricSpecies() throws ModelGenerationException {
        PopulationModelGenerator pmg = new PopulationModelGenerator(CODE4);
        EvaluationEnvironment env = pmg.generateEvaluationEnvironment();
        assertTrue(pmg.validate());
        PopulationRegistry reg = pmg.generatePopulationRegistry(env);
        assertEquals(100, reg.size());
        for(int i=0; i<10; i++) {
            for(int j=0; j<10;j++) {
                assertTrue(reg.indexOf("A",i,j)>-1);
            }
        }
    }

    @Test
    public void testParsingParametricSpeciesWithConstants() throws ModelGenerationException {
        PopulationModelGenerator pmg = new PopulationModelGenerator(CODE5);
        EvaluationEnvironment env = pmg.generateEvaluationEnvironment();
        assertTrue(pmg.validate());
        PopulationRegistry reg = pmg.generatePopulationRegistry(env);
        assertEquals(16, reg.size());
        for(int i=0; i<4; i++) {
            for(int j=0; j<4;j++) {
                assertTrue(reg.indexOf("A",i,j)>-1);
            }
        }
    }

    @Test
    public void testParsingParametricSpeciesWithParameters() throws ModelGenerationException {
        PopulationModelGenerator pmg = new PopulationModelGenerator(CODE6);
        assertTrue(pmg.validate());
        EvaluationEnvironment env = pmg.generateEvaluationEnvironment();
        PopulationRegistry reg = pmg.generatePopulationRegistry(env);
        assertEquals(25, reg.size());
        for(int i=0; i<5; i++) {
            for(int j=0; j<5;j++) {
                assertTrue(reg.indexOf("A",i,j)>-1);
            }
        }
    }

    @Test
    public void testParsingParametricSpeciesWithParametersChangeDefault() throws ModelGenerationException {
        PopulationModelGenerator pmg = new PopulationModelGenerator(CODE6);
        assertTrue(pmg.validate());
        EvaluationEnvironment env = pmg.generateEvaluationEnvironment();
        env.set("N",10);
        PopulationRegistry reg = pmg.generatePopulationRegistry(env);
        assertEquals(100, reg.size());
        for(int i=0; i<10; i++) {
            for(int j=0; j<10;j++) {
                assertTrue(reg.indexOf("A",i,j)>-1);
            }
        }
    }

    @Test
    public void testParsingSEIRRules() throws ModelGenerationException {
        PopulationModelGenerator pmg = new PopulationModelGenerator(CODE2);
        assertTrue(pmg.validate());
        PopulationRegistry reg = pmg.generatePopulationRegistry(new EvaluationEnvironment());
        List<PopulationRule> rules = pmg.generateRules(new EvaluationEnvironment(), reg);
        assertEquals(4,rules.size());
        PopulationRule rule = rules.get(0);
        Population[] pop = new Population[] { new Population(reg.indexOf("S"),1), new Population(reg.indexOf("I"),1)};
        PopulationState state = reg.createPopulationState(pop);
        PopulationTransition transition = rule.apply(new DefaultRandomGenerator(),0.0, state);
        assertNotNull(transition);
        assertEquals(0.5,transition.getRate());
        assertEquals("s_to_e",transition.getName());
        PopulationState newState = state.apply( transition.apply(new DefaultRandomGenerator()) );
        assertEquals(0,newState.getOccupancy(reg.indexOf("S")));
        assertEquals(1,newState.getOccupancy(reg.indexOf("E")));
        assertEquals(1,newState.getOccupancy(reg.indexOf("I")));
        assertEquals(0,newState.getOccupancy(reg.indexOf("R")));
    }

    @Test
    public void testParsingSIRRules() throws ModelGenerationException {
        PopulationModelGenerator pmg = new PopulationModelGenerator(CODE9);
        assertTrue(pmg.validate());
        EvaluationEnvironment env = pmg.generateEvaluationEnvironment();
        PopulationRegistry reg = pmg.generatePopulationRegistry(env);
        List<PopulationRule> rules = pmg.generateRules(env, reg);
        assertEquals(2,rules.size());
        PopulationRule rule = rules.get(0);
        Population[] pop = new Population[] { new Population(reg.indexOf("S"),90), new Population(reg.indexOf("I"),10)};
        PopulationState state = reg.createPopulationState(pop);
        PopulationTransition transition = rule.apply(new DefaultRandomGenerator(),0.0, state);
        assertNotNull(transition);
        assertEquals(0.5,transition.getRate());
        assertEquals("s_to_e",transition.getName());
        PopulationState newState = state.apply( transition.apply(new DefaultRandomGenerator()) );
        assertEquals(0,newState.getOccupancy(reg.indexOf("S")));
        assertEquals(1,newState.getOccupancy(reg.indexOf("E")));
        assertEquals(1,newState.getOccupancy(reg.indexOf("I")));
        assertEquals(0,newState.getOccupancy(reg.indexOf("R")));
    }

    @Test
    public void testParsingSEIRSystem() throws ModelGenerationException {
        PopulationModelGenerator pmg = new PopulationModelGenerator(CODE2);
        assertTrue(pmg.validate());
        EvaluationEnvironment env = new EvaluationEnvironment();
        PopulationRegistry reg = pmg.generatePopulationRegistry(env);
        StateSet<PopulationState> states = pmg.generateStateSet(env,reg);
        assertEquals(1,states.states().length);
    }

    @Test
    public void testThreeStateProtocol() throws ModelGenerationException {
        PopulationModelGenerator generator = new PopulationModelGenerator(CODE8);
        assertTrue(generator.validate());
        PopulationModelDefinition pdef = generator.getPopulationModelDefinition();
        assertNotNull(pdef);
    }

    @Test
    public void testParsingSIRSystem() throws ModelGenerationException {
        PopulationModelGenerator pmg = new PopulationModelGenerator(CODE9);
        assertTrue(pmg.validate());
        EvaluationEnvironment env = new EvaluationEnvironment();
        PopulationRegistry reg = pmg.generatePopulationRegistry(env);
        assertEquals(3,reg.size());
        StateSet<PopulationState> states = pmg.generateStateSet(env,reg);
        assertEquals(1,states.states().length);
        PopulationModelDefinition def = pmg.getPopulationModelDefinition();
        assertNotNull(def);
    }

}
