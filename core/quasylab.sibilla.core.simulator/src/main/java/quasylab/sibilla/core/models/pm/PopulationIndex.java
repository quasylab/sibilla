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

package quasylab.sibilla.core.models.pm;

import java.util.TreeMap;
import java.util.stream.IntStream;

public class PopulationIndex {

    private int counter;
    private final TreeMap<String,Integer> speciesIndex;

    public PopulationIndex(String[] species) {
        this();
        initIndex(species);
    }

    public PopulationIndex() {
        speciesIndex = new TreeMap<>();
    }

    private void initIndex(String[] species) {
        IntStream.range(0,species.length).forEach(i -> registerSpecies(species[i]));
    }

    public String[] getSpecies() {
        return speciesIndex.keySet().toArray(new String[0]);
    }

    public synchronized int indexOf(String m) {
        return speciesIndex.getOrDefault(m,-1);
    }

    public synchronized int registerSpecies(String str) {
        speciesIndex.put(str,counter++);
        return counter;
    }
}
