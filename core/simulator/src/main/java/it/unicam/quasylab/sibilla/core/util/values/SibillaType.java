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

package it.unicam.quasylab.sibilla.core.util.values;

import java.util.Optional;

/**
 * Instances of this class are used to represent types of {@link SibillaValue}s.
 */
public interface SibillaType<S extends SibillaValue> {

    SibillaBooleanType BOOLEAN_TYPE = new SibillaBooleanType();
    SibillaIntegerType INTEGER_TYPE = new SibillaIntegerType();
    SibillaDoubleType DOUBLE_VALUE = new SibillaDoubleType();

    <T extends SibillaValue> Optional<S> cast(T value);


}
