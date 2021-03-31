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

package it.unicam.quasylab.sibilla.core.models.quasylab.sibilla.core.models.agents;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class VariableMapping {

    private final Map<String,Double> map;

    public VariableMapping(SetVariable ... variables) {
        this.map = new HashMap<>();
        Arrays.stream(variables).sequential().forEach(s -> map.put(s.getVariable(),s.getValue()));
    }

    public VariableMapping(Map<String,Double> map) {
        this.map = map;
    }

    public double get(String variable) {
        return map.getOrDefault(variable,Double.NaN);
    }

    public VariableMapping set(SetVariable... command) {
        VariableMapping copy = this.copy();
        Arrays.stream(command).sequential().forEach(c -> copy.map.put(c.getVariable(),c.getValue()));
        return copy;
    }

    public VariableMapping copy() {
        HashMap<String,Double> newMap = new HashMap<>(map);
        return new VariableMapping(newMap);
    }

    public int size() {
        return map.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VariableMapping that = (VariableMapping) o;
        return map.equals(that.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map);
    }

    @Override
    public String toString() {
        return "VariableMapping{" +
                "map=" + map.entrySet().stream().map(e -> e.getKey()+"->"+e.getValue()).reduce((x,y)->x+";"+y)+
                '}';
    }
}
