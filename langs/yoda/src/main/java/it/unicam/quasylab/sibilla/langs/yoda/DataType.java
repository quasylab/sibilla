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

package it.unicam.quasylab.sibilla.langs.yoda;


public enum DataType {

    INTEGER,
    REAL,
    BOOLEAN,
    CHAR,
    STRING,
    NONE;

    /**
     * Check if the DataType is a number or not
     *
     * @return true if it is a number
     */
    public boolean isANumber(){return (this==NONE)||(this==INTEGER)||(this==REAL);}

    /**
     * Check if the input DataType is a subtype of the one compared
     *
     * @param other the other type to be compared
     * @return true if the other is a subtype of this
     */
    public boolean isSubtypeOf (DataType other){
        return (this==NONE)||(other==NONE)||(this==other)||((this==REAL)&&(other==INTEGER))||((this==STRING)&&(other==CHAR));
    }

    /**
     * Merge two DataType inputs
     *
     * @param t1 the first data type
     * @param t2 the second data type
     * @return one of the two data type after being merged
     */
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
    public String toString(){
        switch (this){
            case INTEGER: return "int";
            case REAL: return  "real";
            case BOOLEAN: return "bool";
            case CHAR: return  "char";
            case STRING: return  "string";
        }
        return "none";
    }



}
