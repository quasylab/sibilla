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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The interface <code>YodaType</code> represents
 * the types handled by a Yoda LIOAgent
 */
public interface YodaType {


    static Map<String,YodaType> getMapOfYodaType() {
        HashMap<String, YodaType> typeMap = new HashMap<>();
        typeMap.put("int", YodaType.INTEGER_TYPE);
        typeMap.put("bool", YodaType.BOOLEAN_TYPE);
        typeMap.put("real", YodaType.REAL_TYPE);
        return typeMap;
    }

    enum YodaCodeType{
        NONE,
        BOOLEAN,
        INTEGER,
        REAL,
        LIST,
        RECORD
    }

    YodaType NONE_TYPE = new NoneType();
    YodaType BOOLEAN_TYPE = new BooleanType();
    YodaType INTEGER_TYPE = new IntegerType();
    YodaType REAL_TYPE = new RealType();


    YodaValue cast(YodaValue value);

    YodaCodeType code();

    default YodaType getContentType(){
        return NONE_TYPE;
    }

    boolean canBeAssignedTo(YodaType other);

    static boolean areCompatible(YodaType aType, YodaType anotherType) {
        return aType.canBeAssignedTo(anotherType)||anotherType.canBeAssignedTo(aType);
    }

    static YodaType merge(YodaType aType, YodaType anotherType) {
        if (aType.canBeAssignedTo(anotherType)) {
            return anotherType;
        }
        if (anotherType.canBeAssignedTo(aType)) {
            return aType;
        }
        return YodaType.NONE_TYPE;
    }

    boolean isNumericType();

    class NoneType implements YodaType {
        private NoneType(){}

        @Override
        public YodaValue cast(YodaValue value) {
            return YodaValue.NONE;
        }

        @Override
        public YodaCodeType code() {
            return YodaCodeType.NONE;
        }

        @Override
        public boolean canBeAssignedTo(YodaType other) {
            return false;
        }

        @Override
        public boolean isNumericType() {
            return false;
        }


    }

    class BooleanType implements YodaType {

        private BooleanType(){}

        @Override
        public YodaValue cast(YodaValue value) {
            switch (value.getType().code()){
                case NONE: return YodaValue.FALSE;
                case BOOLEAN: return value;
                default: return value.isTrue();
            }
        }

        @Override
        public YodaCodeType code() {
            return YodaCodeType.BOOLEAN;
        }

        @Override
        public boolean canBeAssignedTo(YodaType other) {
            return other == YodaType.BOOLEAN_TYPE;
        }

        @Override
        public boolean isNumericType() {
            return false;
        }


    }

    class IntegerType implements YodaType {

        private IntegerType(){}

        @Override
        public YodaValue cast(YodaValue value) {
            switch (value.getType().code()){
                case INTEGER: return value;
                case REAL: return value;
                default: return YodaValue.NONE;
            }
        }

        @Override
        public YodaCodeType code() {
            return YodaCodeType.INTEGER;
        }

        @Override
        public boolean canBeAssignedTo(YodaType other) {
            return other.isNumericType();
        }

        @Override
        public boolean isNumericType() {
            return true;
        }
    }

    class RealType implements YodaType {

        private RealType(){}


        @Override
        public YodaValue cast(YodaValue value) {
            switch (value.getType().code()){
                case REAL: return value;
                case INTEGER: return value;
                default: return  YodaValue.NONE;
            }
        }

        @Override
        public YodaCodeType code() {
            return YodaCodeType.REAL;
        }

        @Override
        public boolean canBeAssignedTo(YodaType other) {
            return other == YodaType.REAL_TYPE;
        }

        @Override
        public boolean isNumericType() {
            return true;
        }


    }

    class ListType implements YodaType {
        private final YodaType contentType;

        private ListType(YodaType contentType){
            this.contentType = contentType;
        }

        @Override
        public YodaValue cast(YodaValue value) {
            return YodaValue.NONE;
        }

        @Override
        public YodaCodeType code() {
            return YodaCodeType.LIST;
        }

        public YodaType getContentType(){return contentType;}

        @Override
        public boolean canBeAssignedTo(YodaType other) {
            if (other instanceof ListType) {
                return this.contentType.equals(((ListType) other).contentType);
            }
            return false;
        }

        @Override
        public boolean isNumericType() {
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ListType listType = (ListType) o;
            return Objects.equals(contentType, listType.contentType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(contentType);
        }
    }

    class RecordType implements YodaType {

        private final String name;

        private final Map<String, YodaType> fields;

        public RecordType(String name, Map<String, YodaType> fields) {
            this.name = name;
            this.fields = Map.copyOf(fields);
        }

        @Override
        public YodaValue cast(YodaValue value) {
            return YodaValue.NONE;
        }

        @Override
        public YodaCodeType code() {
            return YodaCodeType.RECORD;
        }

        @Override
        public boolean canBeAssignedTo(YodaType other) {
            return this == other;
        }

        @Override
        public boolean isNumericType() {
            return false;
        }

        public boolean hasField(String fieldName) {
            return this.fields.containsKey(fieldName);
        }

        public YodaType getType(String fieldName) {
            return this.fields.getOrDefault(fieldName, YodaType.NONE_TYPE);
        }

        public Set<String> getFields() {
            return new HashSet<>(this.fields.keySet());
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RecordType that = (RecordType) o;
            return Objects.equals(name, that.name) && Objects.equals(fields, that.fields);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, fields);
        }
    }
}
