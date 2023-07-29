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

/**
 * Enumeration used represents types of values occurring in a LIO Expressions.
 */
public enum LIOType {

    /**
     * Type associated with error values.
     */
    LIO_NONE,

    /**
     * Boolean type.
     */
    LIO_BOOLEAN,

    /**
     * Integer type.
     */
    LIO_INTEGER,

    /**
     * Real type.
     */
    LIO_REAL;


    public boolean compatibleWith(LIOType actual) {
        return (this==actual)
                ||(actual==LIO_NONE)
                ||((this==LIO_REAL)&&(actual==LIO_INTEGER));
    }

    public String messageText() {
        switch (this) {
            case LIO_NONE: return "none";
            case LIO_BOOLEAN: return "a boolean";
            case LIO_INTEGER: return "an integer";
            case LIO_REAL: return "a real";
        }
        return "";
    }

    public boolean isANumber() {
        return (this==LIO_NONE)||(this==LIO_INTEGER)||(this==LIO_REAL);
    }

    public static LIOType combine(LIOType t1, LIOType t2) {
        if (t1.compatibleWith(t2)) {
            return t1;
        }
        if (t2.compatibleWith(t1)) {
            return t2;
        }
        return LIO_NONE;
    }

    public boolean isABoolean() {
        return (this==LIO_NONE)||(this==LIO_BOOLEAN);
    }

}
