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

import it.unicam.quasylab.sibilla.core.models.lio.*;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import it.unicam.quasylab.sibilla.langs.lio.LIOModelGenerator;
import it.unicam.quasylab.sibilla.langs.lio.LIOModelParseError;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class LIOModelModuleTest {


    @Test
    void shouldLoadLIOModule() throws CommandExecutionException {
        assertNotNull(getRuntimeWithModule());
    }

    @Test
    void shouldLoadResource() throws CommandExecutionException {
        assertNotNull(getResource("lio/rb.lio"));
    }

    @Test
    void shouldLoadRBModel() throws CommandExecutionException {
        SibillaRuntime sr = getRuntimeWithModule();
        sr.load(getResource("lio/rb.lio"));
    }

    @Test
    void shouldSetInitialConfigurationRB() throws CommandExecutionException {
        SibillaRuntime sr = getRuntimeWithModule();
        sr.load(getResource("lio/rb.lio"));
        sr.setConfiguration("InitialConfiguration");
    }

    @Test
    void shouldSimulateRB() throws CommandExecutionException {
        SibillaRuntime sr = getRuntimeWithModule();
        sr.load(getResource("lio/rb.lio"));
        sr.setConfiguration("InitialConfiguration");
        sr.setDeadline(100);
        sr.setDt(1.0);
        sr.simulate("test");
    }

    @Test
    void shouldLoadRBPModel() throws CommandExecutionException {
        SibillaRuntime sr = getRuntimeWithModule();
        sr.load(getResource("lio/rbp.lio"));
    }

    @Test
    void shouldSetInitialConfigurationRBP() throws CommandExecutionException {
        SibillaRuntime sr = getRuntimeWithModule();
        sr.load(getResource("lio/rbp.lio"));
        sr.setConfiguration("InitialConfiguration");
    }

    @Test
    void shouldSimulateRBP() throws CommandExecutionException {
        SibillaRuntime sr = getRuntimeWithModule();
        sr.load(getResource("lio/rbp.lio"));
        sr.setConfiguration("InitialConfiguration");
        sr.setDeadline(100);
        sr.setDt(1.0);
        sr.simulate("test");
    }


    private URL getResource(String name) {
        return getClass().getClassLoader().getResource(name);
    }

    private SibillaRuntime getRuntimeWithModule() throws CommandExecutionException {
        SibillaRuntime sr = new SibillaRuntime();
        sr.loadModule(LIOModelModule.MODULE_NAME);
        return sr;
    }


    @Test
    public void testRBProbabilityDefinitions() throws LIOModelParseError, URISyntaxException, IOException {
        LIOModelDefinition definition = loadModelDefinition("lio/rb.lio");
        LIOAgentDefinitions definitions = definition.createModel().getAgentDefinitions();
        assertNotNull(definitions.getAgent("R"));
        assertNotNull(definitions.getAgent("B"));
        assertNotNull(definitions.addAction("changeInRed"));
        LIOActionsProbability probability = definitions.getActionProbability(definitions.getCountingState(new int[] { 100, 100 }));
        assertEquals(0.5*0.25, probability.probabilityOf(definitions.getAction("changeInBlue")));
        assertEquals(0.5*0.25, probability.probabilityOf(definitions.getAction("changeInRed")));
    }

    @Test
    public void testRBPProbabilityDefinitions() throws LIOModelParseError, URISyntaxException, IOException {
        LIOModelDefinition definition = loadModelDefinition("lio/rbp.lio");
        LIOAgentDefinitions definitions = definition.createModel().getAgentDefinitions();
        int k = definition.getParameterValue("K").intOf();
        for(int i=0; i<k+1; i++) {
            assertNotNull(definitions.getAgent("R", SibillaValue.of(i)));
            assertNotNull(definitions.getAgent("B", SibillaValue.of(i)));
        }
        Map<LIOAgent, Integer> countingMap = new HashMap<>(
                IntStream.range(0, k+1).mapToObj(i -> definitions.getAgent("R", SibillaValue.of(i))).collect(Collectors.toMap(a -> a, a -> 3)));
        countingMap.putAll((
                IntStream.range(0, k+1).mapToObj(i -> definitions.getAgent("B", SibillaValue.of(i))).collect(Collectors.toMap(a -> a, a -> 1))));
        LIOState state = definitions.getCountingState(countingMap);
        LIOActionsProbability probability = definitions.getActionProbability(state);
        assertEquals(0.25*0.25, probability.probabilityOf(definitions.getAction("perceiveB")));
        assertEquals(0.75*0.25, probability.probabilityOf(definitions.getAction("perceiveR")));
    }

    @Test
    public void testRBPMeasures() throws LIOModelParseError, URISyntaxException, IOException {
        LIOModelDefinition definition = loadModelDefinition("lio/rbp.lio");
        LIOAgentDefinitions definitions = definition.createModel().getAgentDefinitions();
        int k = definition.getParameterValue("K").intOf();
        for(int i=0; i<k+1; i++) {
            assertNotNull(definitions.getAgent("R", SibillaValue.of(i)));
            assertNotNull(definitions.getAgent("B", SibillaValue.of(i)));
        }
        Map<LIOAgent, Integer> countingMap = new HashMap<>(
                IntStream.range(0, k+1).mapToObj(i -> definitions.getAgent("R", SibillaValue.of(i))).collect(Collectors.toMap(a -> a, a -> 3)));
        countingMap.putAll((
                IntStream.range(0, k+1).mapToObj(i -> definitions.getAgent("B", SibillaValue.of(i))).collect(Collectors.toMap(a -> a, a -> 1))));
        LIOState state = definitions.getCountingState(countingMap);
        LIOModel model = definition.createModel();
        assertEquals(0.75, model.measure("fractionOfR", state));
        assertEquals(0.25, model.measure("fractionOfB", state));
        assertEquals(0.75*2/6, model.measure("fractionOfRWithHighConfidence", state));
        assertEquals(0.25*2/6, model.measure("fractionOfBWithHighConfidence", state));
        assertEquals(0.75*2/6, model.measure("fractionOfRWithLowConfidence", state));
        assertEquals(0.25*2/6, model.measure("fractionOfBWithLowConfidence", state));
    }

    @Test
    public void testRBMeasures() throws LIOModelParseError, URISyntaxException, IOException {
        LIOModelDefinition definition = loadModelDefinition("lio/rb.lio");
        LIOAgentDefinitions definitions = definition.createModel().getAgentDefinitions();
        assertNotNull(definitions.getAgent("R"));
        assertNotNull(definitions.getAgent("B"));
        LIOState state = definitions.getCountingState(new int[] { 100, 100 });
        LIOModel model = definition.createModel();
        assertEquals(0.5, model.measure("%B", state));
        assertEquals(0.5, model.measure("%R", state));
    }


    private LIOModelDefinition loadModelDefinition(String name) throws LIOModelParseError, URISyntaxException, IOException {
        return new LIOModelGenerator(getResource(name)).getDefinition();
    }

}