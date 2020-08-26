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

import org.antlr.v4.runtime.ParserRuleContext;

public class NestedSymbolTable implements SymbolTable {

    private final SymbolTable localSymbolTable;
    private final SymbolTable inheritedSymbolTable;

    public NestedSymbolTable(SymbolTable inheritedSymbolTable) {
        this(new BasicSymbolTable(),inheritedSymbolTable);
    }

    public NestedSymbolTable(SymbolTable localSymbolTable, SymbolTable inheritedSymbolTable) {
        this.localSymbolTable = localSymbolTable;
        this.inheritedSymbolTable = inheritedSymbolTable;
    }

    @Override
    public SymbolInfo addSymbol(String name, ParserRuleContext context, SymbolType type) {
        return localSymbolTable.addSymbol(name,context,type);
    }

    @Override
    public SymbolInfo addSymbol(String name, SymbolInfo symbolInfo) {
        return localSymbolTable.addSymbol(name,symbolInfo);
    }

    @Override
    public boolean isDefined(String name) {
        return localSymbolTable.isDefined(name)||inheritedSymbolTable.isDefined(name);
    }

    @Override
    public SymbolType getType(String name) {
        SymbolType type = localSymbolTable.getType(name);
        return (type==null?inheritedSymbolTable.getType(name):type);
    }

    @Override
    public ParserRuleContext getContext(String name) {
        ParserRuleContext ctx = localSymbolTable.getContext(name);
        return (ctx==null?inheritedSymbolTable.getContext(name):ctx);
    }
}
