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

import java.util.function.Function;

/**
 * The interface <code>YodaType</code> represents
 * the types handled by a Yoda Agent
 */
public interface YodaType {

    enum YodaCodeType{
        NONE,
        BOOLEAN,
        INTEGER,
        REAL,
        LIST
    }

    YodaType NONE_TYPE = new NoneType();
    YodaType BOOLEAN_TYPE = new BooleanType();
    YodaType INTEGER_TYPE = new IntegerType();
    YodaType REAL_TYPE = new RealType();
    Function <YodaType, YodaType> LIST_TYPE = ListType::new;

    YodaValue cast(YodaValue value);

    YodaCodeType code();

    default YodaType getContentType(){
        return NONE_TYPE;
    }

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
    }

    class IntegerType implements YodaType {

        private IntegerType(){}

        @Override
        public YodaValue cast(YodaValue value) {
            switch (value.getType().code()){
                case INTEGER: return value;
                case REAL: return value.integerValue();
                default: return YodaValue.NONE;
            }
        }

        @Override
        public YodaCodeType code() {
            return YodaCodeType.INTEGER;
        }
    }

    class RealType implements YodaType {

        private RealType(){}


        @Override
        public YodaValue cast(YodaValue value) {
            switch (value.getType().code()){
                case REAL: return value;
                case INTEGER: return value.realValue();
                default: return  YodaValue.NONE;
            }
        }

        @Override
        public YodaCodeType code() {
            return YodaCodeType.REAL;
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


    }
}
