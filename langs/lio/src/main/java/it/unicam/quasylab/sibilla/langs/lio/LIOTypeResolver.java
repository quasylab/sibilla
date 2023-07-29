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

package it.unicam.quasylab.sibilla.langs.lio;

import java.util.Map;
import java.util.Optional;

/**
 * Instances of this interface are used to resolve types associated with names.
 */
@FunctionalInterface
public interface LIOTypeResolver {

    /**
     * Returns the optional containing the type associated with the given name. The result is empty if no
     * type info is available.
     *
     * @param name the name to resolve
     * @return the optional containing the type associated with the given name
     */
    Optional<LIOType> get(String name);

    /**
     * Returns the type resolver that uses the one given as parameter when this resolver does not have info
     * on a name.
     *
     * @param other the type solver to use when this has not info
     * @return the type resolver that uses the one given as parameter when this resolver does not have info
     * on a name
     */
    default LIOTypeResolver orElse(LIOTypeResolver other) {
        return n -> {
            Optional<LIOType> result = this.get(n);
            if (result.isEmpty()) return other.get(n);
            return result;
        };
    }


    /**
     * Returns the type resolver that uses the given map.
     *
     * @param typeMap a map associating names to types
     * @return the type resolver that uses the given map
     */
    static LIOTypeResolver resolverOf(Map<String, LIOType> typeMap) {
        return n -> {
            if (typeMap.containsKey(n)) {
                return Optional.of(typeMap.get(n));
            } else {
                return Optional.empty();
            }
        };
    }
}
