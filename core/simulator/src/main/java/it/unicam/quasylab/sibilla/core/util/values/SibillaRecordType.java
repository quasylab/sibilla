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

import java.util.Map;
import java.util.Optional;

/**
 * This class represents the type of a sibilla record.
 */
public class SibillaRecordType implements SibillaType<SibillaRecord> {

    private final Map<String, SibillaType<?>> fieldType;

    /**
     * Creates a new record type with the given fields.
     *
     * @param fieldType types of record fiels.
     */
    public SibillaRecordType(Map<String, SibillaType<?>> fieldType) {
        this.fieldType = fieldType;
    }

    /**
     * Returns the type of the given field. Returns null if the field is not present in this record.
     *
     * @param name a field name.
     * @return the type of the given field.
     */
    public SibillaType<?> getTypeOf(String name) {
        return fieldType.get(name);
    }

    @Override
    public <T extends SibillaValue> Optional<SibillaRecord> cast(T value) {
        return Optional.empty();
    }
}
