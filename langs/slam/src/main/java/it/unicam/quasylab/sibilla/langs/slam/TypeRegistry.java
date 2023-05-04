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

package it.unicam.quasylab.sibilla.langs.slam;

import it.unicam.quasylab.sibilla.core.models.slam.data.SlamType;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a {@link TypeSolver} implemented as a registry mapping names to types.
 */
public class TypeRegistry implements TypeSolver {

    private final Map<String, SlamType> typeMap;

    /**
     * Creates an empty registry.
     */
    public TypeRegistry() {
        this(new HashMap<>());
    }

    public TypeRegistry(Map<String, SlamType> typeMap) {
        this.typeMap = typeMap;
    }


    /**
     * Records that the given name has the given type. Returns <code>true</code> if
     * no binging for the given name already exists, <code>false</code> otherwise.
     *
     * @param name a name.
     * @param type a type.
     * @return <code>true</code> if no binging for the given name already exists,
     * <code>false</code> otherwise.
     */
    public boolean add(String name, SlamType type) {
        return this.typeMap.put(name, type)==null;
    }

    @Override
    public SlamType typeOf(String name) {
        return typeMap.getOrDefault(name, SlamType.NONE_TYPE);
    }
}
