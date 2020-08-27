/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 * Copyright (C) 2020.
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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package quasylab.sibilla.langs.pm;

import org.antlr.v4.runtime.ParserRuleContext;

import java.util.HashMap;

public class BasicSymbolTable implements SymbolTable {

    private HashMap<String, SymbolInfo> symbolTable;

    public BasicSymbolTable() {
        this.symbolTable = new HashMap<>();
    }

    @Override
    public SymbolInfo addSymbol(String name, ParserRuleContext context, SymbolType type) {
        return addSymbol(name,new SymbolInfo(type,context));
    }

    @Override
    public SymbolInfo addSymbol(String name, SymbolInfo symbolInfo) {
        return symbolTable.put(name,symbolInfo);
    }

    private void checkAndThrowExceptionIfDuplicated(String name, ParserRuleContext context) throws DuplicatedSymbolException {
        if (symbolTable.containsKey(name)) {
            throw new DuplicatedSymbolException(name,symbolTable.get(name).getContext(),context);
        }
    }

    @Override
    public boolean isDefined(String name) {
        return symbolTable.containsKey(name);
    }

    @Override
    public SymbolType getType(String name) {
        if (symbolTable.containsKey(name)) {
            return symbolTable.get(name).getType();
        } else {
            return SymbolType.ERROR;
        }
    }

    @Override
    public ParserRuleContext getContext(String name) {
        SymbolInfo info = symbolTable.get(name);
        if (info != null) {
            return info.getContext();
        } else {
            return null;
        }
    }
}
