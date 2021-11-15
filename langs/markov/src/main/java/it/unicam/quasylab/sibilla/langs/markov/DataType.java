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

package it.unicam.quasylab.sibilla.langs.markov;

import java.util.function.BinaryOperator;
import java.util.function.DoubleBinaryOperator;
import java.util.function.IntBinaryOperator;

public enum DataType {

    INTEGER,
    REAL,
    BOOLEAN,
    NONE;

    public boolean isSubtypeOf(DataType other) {
        return (this==NONE)||(other==NONE)||(this==other)||((this==REAL)&&(other==INTEGER));
    }

    public static DataType merge(DataType t1, DataType t2) {
        if (t1==t2) {
            return t2;
        }
        if (t1.isSubtypeOf(t2)) {
            return t1;
        }
        if (t2.isSubtypeOf(t1)) {
            return t2;
        }
        return null;
    }


    @Override
    public String toString() {
        switch (this) {
            case BOOLEAN: return "bool";
            case INTEGER: return "int";
            case REAL: return "real";
        }
        return "none";
    }

    public boolean isANumber() {
        return (this==NONE)||(this==INTEGER)||(this==REAL);
    }


}
