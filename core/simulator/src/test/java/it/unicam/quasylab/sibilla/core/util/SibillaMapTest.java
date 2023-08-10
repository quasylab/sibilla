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

package it.unicam.quasylab.sibilla.core.util;

import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class SibillaMapTest {

    @Test
    void shouldContainAnElementAfterAdd() {
        SibillaMap<String, Integer> map = new SibillaMap<>();
        assertFalse(map.containsKey("TEST"));
        map = map.add("TEST", 0);
        assertTrue(map.containsKey("TEST"));
    }

    @Test
    void getShouldBeEmpty() {
        SibillaMap<String, Integer> map = new SibillaMap<>();
        assertTrue(map.get("TEST").isEmpty());
    }

    @Test
    void getShouldNotBeEmptyAfterAdd() {
        SibillaMap<String, Integer> map = new SibillaMap<>();
        assertTrue(map.get("TEST").isEmpty());
        map = map.add("TEST", 5);
        assertTrue(map.get("TEST").isPresent());
    }


    @Test
    void getShouldGetTheRightValue() {
        SibillaMap<String, Integer> map = new SibillaMap<>();
        assertTrue(map.get("TEST").isEmpty());
        map = map.add("TEST", 5);
        assertEquals(Integer.valueOf(5), map.get("TEST").get());
    }

    @Test
    void getShouldAlwaysReturnTheRightValue() {
        int size = 10000;
        Integer[] keys = IntStream.range(0, size).boxed().toArray(Integer[]::new);
        Double[] values = IntStream.range(0, size).mapToDouble(i -> 3*i).boxed().toArray(Double[]::new);
        Map<Integer, Double> map = IntStream.range(0, size).boxed().collect(Collectors.toMap(i -> keys[i], i -> values[i]));
        SibillaMap<Integer, Double> testedMap = SibillaMap.of(map);
        for(int i=0; i<size; i++) {
            assertEquals(values[i], testedMap.get(i).orElse(Double.NaN));
        }
    }


    @Test
    void containsKey() {
    }

    @Test
    void shouldBeEmpty() {
        SibillaMap<String, Integer> map = new SibillaMap<>();
        assertTrue(map.isEmpty());
    }
}