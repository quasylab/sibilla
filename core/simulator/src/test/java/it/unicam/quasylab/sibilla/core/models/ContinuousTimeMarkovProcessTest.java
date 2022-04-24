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

package it.unicam.quasylab.sibilla.core.models;

import it.unicam.quasylab.sibilla.core.simulator.DefaultRandomGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContinuousTimeMarkovProcessTest {

    private final static int SIZE = 100;
    private final static long SEED = 10;

    @Test
    void sampleExponentialDistribution() {
        DefaultRandomGenerator rg = new DefaultRandomGenerator();

        for(int i=0; i<SIZE;i++) {
            assertTrue(ContinuousTimeMarkovProcess.sampleExponentialDistribution(10.0,rg)>0);
        }

    }

    @Test
    void randomGeneratorShouldNotSampleZero() {
        DefaultRandomGenerator rg = new DefaultRandomGenerator();
        rg.setSeed(SEED);
        for(int i=0; i<SIZE;i++) {
            assertTrue(rg.nextDouble()!=0);
        }

    }

    @Test
    void randomGeneratorShouldNotSampleOne() {
        DefaultRandomGenerator rg = new DefaultRandomGenerator();
        rg.setSeed(SEED);
        for(int i=0; i<SIZE;i++) {
            assertTrue(rg.nextDouble()!=1);
        }

    }

}