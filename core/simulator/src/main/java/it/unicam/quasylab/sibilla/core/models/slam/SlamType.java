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

package it.unicam.quasylab.sibilla.core.models.slam;

import java.util.function.Function;

/**
 * Identifies the classes of data types that can be used by a SLAM agent.
 */
public interface SlamType {

    static SlamType getTypeOf(String type) {
        switch (type) {
            case "int":     return SlamType.INTEGER_TYPE;
            case "real":    return SlamType.REAL_TYPE;
            case "boolean": return SlamType.BOOLEAN_TYPE;
            default:        return SlamType.NONE_TYPE;
        }
    }

    boolean isComparable();

    boolean canCastTo(SlamType resultType);

    boolean isNumericType();

    enum SlamCodeType {
        NONE,
        BOOLEAN,
        INTEGER,
        REAL,
        LIST
    }

    SlamType NONE_TYPE = new NoneType();

    SlamType BOOLEAN_TYPE = new BooleanType();

    SlamType INTEGER_TYPE = new IntegerType();

    SlamType REAL_TYPE = new RealType();

    Function<SlamType, SlamType> LIST_TYPE = ListType::new;

    /**
     * Returns the cast of the given value to this type.
     *
     * @param value a value to cast.
     * @return the cast of the given value to this type.
     */
    SlamValue cast(SlamValue value);

    /**
     * Returns the code associated with this type.
     *
     * @return the code associated with this type.
     */
    SlamCodeType code();

    abstract class AbstractType implements SlamType {

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof SlamType) {
                return this.code() == ((SlamType) obj).code();
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return this.code().toString();
        }

        @Override
        public boolean isComparable() {
            return false;
        }

        @Override
        public boolean canCastTo(SlamType resultType) {
            return false;
        }

        @Override
        public boolean isNumericType() {
            return false;
        }
    }

    /**
     * Returns the type of elements of the elements in this type.
     *
     * @return the type of elements of the elements in this type.
     */
    default SlamType getContentType() {
        return NONE_TYPE;
    }

    final class NoneType extends AbstractType {

        private NoneType() {}

        @Override
        public SlamValue cast(SlamValue value) {
            return SlamValue.NONE;
        }

        @Override
        public SlamCodeType code() {
            return SlamCodeType.NONE;
        }

    }

    final class BooleanType extends AbstractType {

        private BooleanType() {}


        @Override
        public SlamValue cast(SlamValue value) {
            switch (value.getType().code()) {
                case NONE: return SlamValue.FALSE;
                case BOOLEAN: return value;
                default: return value.isTrue();
            }
        }

        @Override
        public SlamCodeType code() {
            return SlamCodeType.BOOLEAN;
        }

    }


    class IntegerType extends AbstractType {


        @Override
        public SlamValue cast(SlamValue value) {
            switch (value.getType().code()) {
                case INTEGER: return value;
                case REAL: return value.intValue();
                default: return SlamValue.NONE;
            }
        }

        @Override
        public SlamCodeType code() {
            return SlamCodeType.INTEGER;
        }
    }

    class RealType extends AbstractType {
        @Override
        public SlamValue cast(SlamValue value) {
            switch (value.getType().code()) {
                case REAL: return value;
                case INTEGER: return value.realValue();
                default: return SlamValue.NONE;
            }
        }

        @Override
        public SlamCodeType code() {
            return SlamCodeType.REAL;
        }
    }

    class ListType extends AbstractType {

        private final SlamType contentType;

        private ListType(SlamType contentType) {
            this.contentType = contentType;
        }

        public SlamType getContentType() {
            return contentType;
        }

        @Override
        public SlamValue cast(SlamValue value) {
            return SlamValue.NONE;
        }

        @Override
        public SlamCodeType code() {
            return SlamCodeType.LIST;
        }
    }
}
