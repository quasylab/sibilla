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
import it.unicam.quasylab.sibilla.core.models.ParametricDataSet;
import it.unicam.quasylab.sibilla.core.models.pm.*;
import it.unicam.quasylab.sibilla.core.models.pm.util.PopulationRegistry;
import it.unicam.quasylab.sibilla.core.simulator.DefaultRandomGenerator;
import it.unicam.quasylab.sibilla.core.util.values.SibillaDouble;
import it.unicam.quasylab.sibilla.core.util.values.SibillaInteger;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

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

    public final String ENERGY_CODE ="param N = 3; /* User maximum consumption */\n" +
            "param M = 5; /* Producer maximum capacity */\n" +
            "\n" +
            "species U of [0,N];\n" +
            "species R of [0,N];\n" +
            "species F of [0,N];\n" +
            "species P of [0,M];\n" +
            "/* species FP; */\n" +
            "\n" +
            "const malfunctionRate = 0.5;\n" +
            "const repairRate = 0.5;\n" +
            "const gainRate = 1.0;\n" +
            "const releaseRate = 1.0;\n" +
            "const failRequestRate = 0.5;\n" +
            "const requestRate = 1.0; \n" +
            "\n" +
            "/*\n" +
            "rule producer_malfunction for i in [0,M]{\n" +
            "    P[i] -[ #P[i] * malfunctionRate ]-> FP\n" +
            "}\n" +
            "\n" +
            "rule producer_repaired{\n" +
            "    FP -[ #FP * repairRate ]-> P[M]\n" +
            "}\n" +
            "\n" +
            "rule user_requests_service for i in [0,N]{\n" +
            "    U[i] -[ #U[i]*requestRate ]-> U[i+1]\n" +
            "}\n" +
            "*/\n" +
            "rule user_gains_service for i in [0,N-1] and j in [1,M]{\n" +
            "    R[i]|P[j] -[ #R[i]*gainRate ]-> U[i+1]|P[j-1]\n" +
            "}\n" +
            "/*\n" +
            "rule user_releases_service for i in [1,N] and j in [0,M-1]{\n" +
            "    U[i]|P[j] -[ #U[i] * releaseRate * %P[j] ]-> U[i-1]|P[j+1]\n" +
            "}\n" +
            "\n" +
            "rule failed_request for i in [0,N]{\n" +
            "    R[i] -[ #R[i] * failRequestRate * %P[0] ]-> F[i]\n" +
            "}\n" +
            "\n" +
            "rule failed_user_gains_service for i in [0,N-1] and j in [1,M]{\n" +
            "    F[i]|P[j] -[ #F[i] * gainRate * %P[j] ]-> U[i+1]|P[j-1]\n" +
            "}\n" +
            "*/\n" +
            "\n" +
            "system init = U[0]|P[4];\n";

    private final static String CDOE_TSP = "species S0;\n" +
            "species S1;\n" +
            "species SU;\n" +
            "\n" +
            "const meetRate = 1.0;\n" +
            "\n" +
            "rule su_to_s1 {\n" +
            "    SU|S1 -[ #SU*meetRate*%S1 ]-> S1|S1\n" +
            "}\n" +
            "\n" +
            "rule su_to_s0 {\n" +
            "    SU|S0 -[ #SU*meetRate*%S0 ]-> S0|S0\n" +
            "}\n" +
            "\n" +
            "rule s1_to_su {\n" +
            "    S1|S0 -[ #S1*meetRate*%S0 ]-> SU|S0\n" +
            "}\n" +
            "\n" +
            "rule s0_to_su {\n" +
            "    S0|S1 -[ #S0*meetRate*%S1 ]-> SU|S1\n" +
            "}\n" +
            "\n" +
            "param scale = 1.0;\n" +
            "\n" +
            "system balanced = S0<1*scale>|S1<1*scale>|SU<8*scale>;\n" +
            "\n" +
            "\n" +
            "system custom(s0,s1,su) = S0<s0>|S1<s1>|SU<su>;\n" +
            "\n" +
            "predicate consensus = (%S1==1.0)||(%S0==1.0);";

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
        assertEquals(new SibillaInteger(10), ee.get("x"));
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
        env.set("N",new SibillaDouble(10));
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
        assertEquals(0.25,transition.getRate());
        assertEquals("infection",transition.getName());
        PopulationState newState = state.apply( transition.apply(new DefaultRandomGenerator()) );
        assertEquals(89,newState.getOccupancy(reg.indexOf("S")));
        assertEquals(11,newState.getOccupancy(reg.indexOf("I")));
    }

    @Test
    public void testParsingSEIRSystem() throws ModelGenerationException {
        PopulationModelGenerator pmg = new PopulationModelGenerator(CODE2);
        assertTrue(pmg.validate());
        EvaluationEnvironment env = new EvaluationEnvironment();
        PopulationRegistry reg = pmg.generatePopulationRegistry(env);
        ParametricDataSet<Function<RandomGenerator, PopulationState>> states = pmg.generateStateSet(env,reg);
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
        ParametricDataSet<Function<RandomGenerator, PopulationState>> states = pmg.generateStateSet(env,reg);
        assertEquals(1,states.states().length);
        PopulationModelDefinition def = pmg.getPopulationModelDefinition();
        assertNotNull(def);
    }

    @Test
    public void testParsingENERGYystem() throws ModelGenerationException {
        PopulationModelGenerator pmg = new PopulationModelGenerator(ENERGY_CODE);
        assertTrue(pmg.validate());
        EvaluationEnvironment env = pmg.generateEvaluationEnvironment();
        PopulationRegistry reg = pmg.generatePopulationRegistry(env);
        assertEquals(14,reg.size());
        List<PopulationRule> rules = pmg.generateRules(env,reg);
        assertEquals(8, rules.size());
        PopulationState state = reg.createPopulationState(new Population[] {
                new Population(reg.indexOf("R",0),1),
                new Population(reg.indexOf("P",1),1),
                new Population(reg.indexOf("P",2),1),
                new Population(reg.indexOf("P",3),1),
                new Population(reg.indexOf("P",4),1),
        });
        PopulationTransition t = rules.get(0).apply(new DefaultRandomGenerator(),0.0,state);
        assertNotNull(t);
        assertTrue(t.getRate()>0);
    }

    @Test
    public void testThreeStatesProtocol() throws ModelGenerationException {
        PopulationModelGenerator pmg = new PopulationModelGenerator(CDOE_TSP);
        assertTrue(pmg.validate());
        EvaluationEnvironment env = pmg.generateEvaluationEnvironment();
        PopulationRegistry reg = pmg.generatePopulationRegistry(env);
        PopulationModelDefinition def = pmg.getPopulationModelDefinition();
        PopulationModel model = def.createModel();
        PopulationState state = def.state("balanced").apply(new DefaultRandomGenerator());
        //model.getTransitions()

    }

}
