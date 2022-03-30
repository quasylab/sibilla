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

import it.unicam.quasylab.sibilla.langs.util.ParseError;

import java.util.LinkedList;
import java.util.List;

/**
 * Visitor used to collect all the symbols from a parse tree.
 */
public class SymbolCollector extends LIOModelBaseVisitor<Boolean> {

    private final SymbolTable table;
    private final LinkedList<ParseError> errors;

    public SymbolCollector() {
        this(new LinkedList<>());
    }

    public SymbolCollector(LinkedList<ParseError> errors) {
        this(new SymbolTable(),errors);
    }

    public SymbolCollector(SymbolTable table, LinkedList<ParseError> errors) {
        this.table = table;
        this.errors = errors;
    }

    public SymbolTable getSymbolTable() {
        return table;
    }

    private ParseError duplicatedNameError(DuplicatedNameException e) {
        return new ParseError(e.getMessage(),e.getLine(),e.getCharPositionInLine());
    }


    @Override
    public Boolean visitParam(LIOModelParser.ParamContext ctx) {
        try {
            table.addParameter(ctx);
            return true;
        } catch (DuplicatedNameException e) {
            errors.add(duplicatedNameError(e));
            return false;
        }
    }

    @Override
    public Boolean visitConstant(LIOModelParser.ConstantContext ctx) {
        try {
            table.addConstant(ctx);
            return true;
        } catch (DuplicatedNameException e) {
            errors.add(duplicatedNameError(e));
            return false;
        }
    }

    @Override
    public Boolean visitAction(LIOModelParser.ActionContext ctx) {
        try {
            table.addAction(ctx);
            return true;
        } catch (DuplicatedNameException e) {
            errors.add(duplicatedNameError(e));
            return false;
        }
    }

    @Override
    public Boolean visitState(LIOModelParser.StateContext ctx) {
        try {
            table.addState(ctx);
            return true;
        } catch (DuplicatedNameException e) {
            errors.add(duplicatedNameError(e));
            return false;
        }
    }

    @Override
    public Boolean visitSystem(LIOModelParser.SystemContext ctx) {
        try {
            table.addSystem(ctx);
            return true;
        } catch (DuplicatedNameException e) {
            errors.add(duplicatedNameError(e));
            return false;
        }
    }

    public List<ParseError> getErrors() {
        return errors;
    }
}
