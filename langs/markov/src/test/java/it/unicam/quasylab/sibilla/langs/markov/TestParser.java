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

package it.unicam.quasylab.sibilla.langs.markov;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestParser {

    private final static String CODE1 = "state { [0 .. 10] x; }\n" +
            "rules {\n" +
            "[] (x<10) -> (x'=x+1);\n" +
            "[] (x>0)  -> (x'=x-1);\n" +
            "}\n" +
            "init() = [x=0];";

    private final static String CODE2 = "const upstate {  [0 .. 10] x; }\n" +
            "rules {\n" +
            "[] (x<10) -> (x'=x+1);\n" +
            "[] (x>0)  -> (x'=x-1);\n" +
            "}";

    @Test
    public void testParseCode1() {
        MarkovChainModelGenerator generator = new MarkovChainModelGenerator(CODE1);
        assertTrue(generator.validate());
    }

}
