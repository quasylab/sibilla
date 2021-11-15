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

package it.unicam.quasylab.sibilla.core.runtime;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class SibillaRuntimeTest {

    public final String CODE = "param lambda = 1.0;\n" +
            "param N1 = 10;\n" +
            "param N2 = 10;\n" +
            "param NU = 10;\n" +
            "const N = N1+N2+NU;\n"+
            "species A0;\n" +
            "species A1;\n " +
            "species AU;\n" +
            "rule change01 { A0|A1 -[ #A0*%A1*lambda ]-> A0|AU }\n" +
            "rule change10 { A1|A0 -[ #A1*%A0*lambda ]-> A1|AU }\n" +
            "rule changeU0 { A0|AU -[ #A0*%AU*lambda ]-> A0|A0 }\n" +
            "rule changeU1 { A1|AU -[ #A1*%AU*lambda ]-> A1|A1 }\n" +
            "system init = A0<10>|A1<10>|AU<10>;";

    public final String CODE2 = "param N = 10;\n" +
            "\n" +
            "species GA;\n" +
            "species GB;\n" +
            "species CA;\n" +
            "species CB;\n" +
            "\n" +
            "param lambda = 1.0;\n" +
            "\n" +
            "label typeA = {GA, CA}\n" +
            "label typeB = {GB, CB}\n" +
            "\n" +
            "rule groupieChangeA {\n" +
            "    GB -[ #GB*lambda*%typeA ]-> GA\n" +
            "}\n" +
            "\n" +
            "rule groupieChangeB {\n" +
            "    GA -[ #GA*lambda*%typeB ]-> GB\n" +
            "}\n" +
            "\n" +
            "rule celebrityChangeA {\n" +
            "    CB -[ #GB*lambda*%typeB ]-> CA\n" +
            "}\n" +
            "\n" +
            "rule celebrityChangeB {\n" +
            "    CA -[ #GB*lambda*%typeA ]-> CB\n" +
            "}\n" +
            "\n" +
            "system balancedGroupies = GA<N>|GB<N>;\n" +
            "\n" +
            "system groupiesAndCelebrities = GA<N-1>|GB<N-1>|CA|CB;";

    @Test
    public void shouldSelectPopulationModule() throws CommandExecutionException {
        SibillaRuntime sr = new SibillaRuntime();
        assertTrue(Arrays.deepEquals(new String[] {PopulationModelModule.MODULE_NAME}, sr.getModules()));
        sr.loadModule(PopulationModelModule.MODULE_NAME);
    }

    @Test
    public void shouldLoadASpecificationFromString() throws CommandExecutionException, LoadException {
        SibillaRuntime sr = getRuntimeWithModule();
        sr.load(CODE);
    }

    @Test
    public void shouldInstantiateASystemFromName() throws CommandExecutionException, LoadException {
        SibillaRuntime sr = getRuntimeWithModule();
        sr.load(CODE);
        assertEquals(1,sr.getInitialConfigurations().length);
        assertEquals("init",sr.getInitialConfigurations()[0]);
        sr.setConfiguration("init");
    }

    @Test
    public void shouldSimulate() throws CommandExecutionException, LoadException {
        SibillaRuntime sr = getRuntimeWithModule();
        sr.load(CODE);
        sr.setConfiguration("init");
        sr.addAllMeasures();
        sr.setDeadline(100);
        sr.setDt(1);
        sr.simulate("test");
    }


    private SibillaRuntime getRuntimeWithModule() throws CommandExecutionException {
        SibillaRuntime sr = new SibillaRuntime();
        sr.loadModule(PopulationModelModule.MODULE_NAME);
        return sr;
    }

    @Test
    public void shouldLoadAndSimulate() throws CommandExecutionException, LoadException {
        SibillaRuntime sr = getRuntimeWithModule();
        sr.load(CODE2);
        sr.setConfiguration("balancedGroupies");
        sr.addMeasure("#GB");
        sr.setDeadline(100);
        sr.setDt(1);
        sr.simulate("test");
        sr.printData("test");
    }


}