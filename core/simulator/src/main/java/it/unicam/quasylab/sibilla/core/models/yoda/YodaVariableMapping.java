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

package it.unicam.quasylab.sibilla.core.models.yoda;

import it.unicam.quasylab.sibilla.core.util.datastructures.Pair;
import it.unicam.quasylab.sibilla.core.util.datastructures.SibillaMap;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Instances of this class are used to associate values with variables.
 */
public class YodaVariableMapping {

    private final SibillaMap<YodaVariable, SibillaValue> map;

    /**
     * Creates an empty mapping.
     */
    public YodaVariableMapping() {
        this(new SibillaMap<>());
    }

    public YodaVariableMapping(Map<YodaVariable, SibillaValue> map) {
        this(SibillaMap.of(map));
    }

    public YodaVariableMapping(SibillaMap<YodaVariable, SibillaValue> map) {
        this.map = map;
    }

    /**
     * This method returns the value associated to an input variable
     *
     * @param variable the variable to search
     * @return the value associated to an input variable
     */
    public SibillaValue getValue(YodaVariable variable) {
        return this.map.get(variable).orElse(SibillaValue.ERROR_VALUE);
    }


    /**
     * This method sets an input value to a certain input variable
     *
     * @param variable the variable to set
     * @param value the value that is associated to the variable
     */
    public YodaVariableMapping setValue(YodaVariable variable, SibillaValue value) {
        return new YodaVariableMapping(this.map.add(variable, value));
    }

    /**
     * Returns the variable mapping obtained from this one by setting each variable to the value associated to it
     * in the given map.
     *
     * @param map the map containing all the assignment to perform.
     * @return the variable mapping obtained from this one by setting each variable to the value associated to it
     * in the given map.
     */
    public YodaVariableMapping setAll(Map<YodaVariable, SibillaValue> map) {
        SibillaMap<YodaVariable, SibillaValue> newMap = this.map.addAll(map);
        if (this.map == newMap) return this;
        return new YodaVariableMapping(this.map.addAll(map));
    }

    public YodaVariableMapping setAll(List<YodaVariableUpdate> lst) {
        //if ((map == null)||(map.isEmpty())) return this;
        SibillaMap<YodaVariable, SibillaValue> newMap = this.map;
        for (YodaVariableUpdate u : lst) {
            newMap = newMap.add(u.getVariable(), u.getValue());
        }
        return new YodaVariableMapping(newMap);
    }


    /**
     * Returns true if the given variable is defined in this mapping.
     *
     * @param var the variable whose definition is tested
     * @return true if the given variable is defined in this mapping.
     */
    public boolean isDefined(YodaVariable var) {
        return this.map.containsKey(var);
    }

    /**
     * Returns the mapping obtained from this one by setting all the assignment contained in the given mapping.
     *
     * @param initialAssignment the assignment containing the mapping to assign.
     * @return the mapping obtained from this one by setting all the assignment contained in the given mapping.
     */
    public YodaVariableMapping setAll(YodaVariableMapping initialAssignment) {
        return new YodaVariableMapping(this.map.apply(initialAssignment::getOrDefault));
    }

    /**
     * Returns the value associated to the given variable. If the variable is not defined in this mapping, the
     * given default value is returned.
     *
     * @param var the variable whose value is retrieved from this map
     * @param value the value returned if the given variable is not defined in this map.
     * @return the value associated to the given variable. If the variable is not defined in this mapping, the
     * given default value is returned.
     */
    public SibillaValue getOrDefault(YodaVariable var, SibillaValue value) {
        return this.map.get(var).orElse(value);
    }

    @Override
    public String toString() {
        return map.toString();
    }

    public void forEach(BiConsumer<YodaVariable, SibillaValue> consumer) {
        map.forEach(consumer);
    }
}
