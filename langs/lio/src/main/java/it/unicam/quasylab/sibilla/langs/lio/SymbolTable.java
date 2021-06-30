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

import it.unicam.quasylab.sibilla.core.markov.VectorState;

import java.util.Set;

public class SymbolTable {

    public boolean isAState(String text) {
        return false;
    }

    public boolean isAnAction(String text) {
        return false;
    }

    public boolean isDeclared(String name) {
        return false;
    }

    public void addParameter(LIOModelParser.ParamContext ctx) throws DuplicatedNameException {
    }

    public void addConstant(LIOModelParser.ConstantContext ctx) throws DuplicatedNameException {
    }

    public void addAction(LIOModelParser.ActionContext ctx) throws DuplicatedNameException {
    }

    public void addState(LIOModelParser.StateContext ctx) throws DuplicatedNameException {
    }

    public void addSystem(LIOModelParser.SystemContext ctx) throws DuplicatedNameException {
    }

    public Set<String> getNames() {
        return null;
    }

    public boolean isAConstant(String alpha) {
        return false;
    }

    public boolean isASystem(String test) {
        return false;
    }

    public int size() {
        return 0;
    }

    public boolean isAParameter(String name) {
        return false;
    }
}
