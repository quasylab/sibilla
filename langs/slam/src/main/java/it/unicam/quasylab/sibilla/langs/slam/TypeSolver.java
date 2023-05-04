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

import javax.swing.text.html.Option;
import java.util.Optional;

/**
 * This functional interface is used to associate symbols with a type.
 */
@FunctionalInterface
public interface TypeSolver {

    /**
     * Returns an optional containing the type associated with the given name.
     *
     * @param name name of a symbol.
     * @return an optional containing the type associated with the given name.
     */
    SlamType typeOf(String name);

    static TypeSolver combine(TypeSolver outerTypeContext, TypeSolver innerTypeSolver) {
        return name -> {
            SlamType type = innerTypeSolver.typeOf(name);
            if (type != SlamType.NONE_TYPE) {
                return type;
            } else {
                return outerTypeContext.typeOf(name);
            }
        };
    }

}
