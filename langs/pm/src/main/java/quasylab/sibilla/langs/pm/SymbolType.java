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

package quasylab.sibilla.langs.pm;

public enum SymbolType {
    INT,
    REAL,
    BOOLEAN,
    ERROR;

    public static SymbolType merge(SymbolType t1, SymbolType t2) {
        switch (t1) {
            case INT:  return ((t2==INT)||(t2==REAL)?t2:ERROR);
            case REAL: return ((t2==INT)||(t2==REAL)?REAL:ERROR);
            default:return (t2==t1?t1:ERROR);
        }
    }

    public boolean isANumber() {
        return (this==ERROR)||(this==INT)||(this==REAL);
    }

    public boolean isABoolean() {
        return (this==ERROR)||(this==BOOLEAN);
    }

    @Override
    public String toString() {
        switch (this) {
            case INT: return "int";
            case REAL: return "real";
            case BOOLEAN: return "boolean";
            default:
                return super.toString();
        }
    }

    public boolean isCompatible(SymbolType t) {
        if (this.isANumber()) {
            return t.isANumber();
        } else {
            return this==t;
        }
    }

    public boolean isInteger() {
        return (this==INT)||(this==ERROR);
    }

    public String javaType() {
        switch (this) {
            case INT: return "int";
            case REAL: return "double";
            case BOOLEAN: return "boolean";
            default: return "Object";
        }
    }
}
