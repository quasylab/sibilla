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
import java.util.LinkedList;
import java.util.List;

public class BasicSymbolTable implements SymbolTable {

    private final HashMap<String,SymbolInfo> references = new HashMap<>();

    private final HashMap<String,PopulationModelParser.Measure_declarationContext> measures = new HashMap<>();

    private final HashMap<String,PopulationModelParser.Species_declarationContext> species = new HashMap<>();

    private final HashMap<String,PopulationModelParser.Rule_declarationContext> rules = new HashMap<>();

    private final HashMap<String,PopulationModelParser.System_declarationContext> systems = new HashMap<>();

    private final List<String> constants = new LinkedList<>();
    private final List<String> parameters = new LinkedList<>();



    private SymbolInfo addReference(String name, SymbolInfo symbolInfo) throws DuplicatedSymbolException {
        return references.put(name,symbolInfo);
    }

    private void checkAndThrowExceptionIfDuplicated(String name, ParserRuleContext context) throws DuplicatedSymbolException {
        ParserRuleContext existing = getContext(name);
        if (existing!=null) {
            throw new DuplicatedSymbolException(name,existing,context);
        }
    }


    @Override
    public void addVariable(String name, ParserRuleContext context, SymbolType type) throws DuplicatedSymbolException {
        checkAndThrowExceptionIfDuplicated(name,context);
        addReference(name, new SymbolInfo(type,context));
    }

    @Override
    public void addConstant(String name, PopulationModelParser.Const_declarationContext context, SymbolType type) throws DuplicatedSymbolException {
        checkAndThrowExceptionIfDuplicated(name,context);
        addReference(name, new SymbolInfo(type,context));
        constants.add(name);
    }

    @Override
    public void addParameter(String name, PopulationModelParser.Param_declarationContext context, SymbolType type) throws DuplicatedSymbolException {
        checkAndThrowExceptionIfDuplicated(name,context);
        addReference(name, new SymbolInfo(type,context));
        parameters.add(name);
    }

    @Override
    public void addMeasure(String name, PopulationModelParser.Measure_declarationContext context) throws DuplicatedSymbolException {
        checkAndThrowExceptionIfDuplicated(name,context);
        measures.put(name,context);
    }

    @Override
    public void addSpecies(String name, PopulationModelParser.Species_declarationContext context) throws DuplicatedSymbolException {
        checkAndThrowExceptionIfDuplicated(name,context);
        species.put(name,context);
    }

    @Override
    public void addRule(String name, PopulationModelParser.Rule_declarationContext context) throws DuplicatedSymbolException {
        checkAndThrowExceptionIfDuplicated(name,context);
        rules.put(name,context);
    }

    @Override
    public void deleteVariable(String name) {
        references.remove(name);
    }

    @Override
    public void addSystem(String name, PopulationModelParser.System_declarationContext context) throws DuplicatedSymbolException {
        checkAndThrowExceptionIfDuplicated(name,context);
        systems.put(name,context);

    }

    @Override
    public String[] rules() {
        return rules.keySet().toArray(new String[0]);
    }

    @Override
    public String[] constants() {
        return constants.toArray(new String[0]);
    }

    @Override
    public String[] species() {
        return species.keySet().toArray(new String[0]);
    }

    @Override
    public String[] systems() {
        return systems.keySet().toArray(new String[0]);
    }

    @Override
    public String[] measures() {
        return measures.keySet().toArray(new String[0]);
    }

    @Override
    public int arity(String name) {
        PopulationModelParser.Species_declarationContext ctx = species.get(name);
        return ctx.range().size();
    }

    @Override
    public boolean isAReference(String name) {
        return references.containsKey(name);
    }

    @Override
    public boolean isAMeasure(String name) {
        return measures.containsKey(name);
    }

    @Override
    public boolean isASpecies(String name) {
        return species.containsKey(name);
    }

    @Override
    public boolean isARule(String name) {
        return rules.containsKey(name);
    }

    @Override
    public SymbolType getType(String name) {
        SymbolInfo ctx = references.get(name);
        if (ctx != null) {
            return ctx.getType();
        } else {
            return SymbolType.ERROR;
        }
    }

    @Override
    public ParserRuleContext getContext(String name) {
        SymbolInfo info = references.get(name);
        if (info != null) {
            return info.getContext();
        }
        if (measures.containsKey(name)) { return measures.get(name); };
        if (rules.containsKey(name)) { return rules.get(name); };
        if (species.containsKey(name)) { return species.get(name); };
        if (systems.containsKey(name)) { return systems.get(name); };
        return null;
    }

//    private HashMap<String, SymbolInfo> symbolTable;
//
//    public BasicSymbolTable() {
//        this.symbolTable = new HashMap<>();
//    }
//
//    @Override
//    public SymbolInfo addSymbol(String name, ParserRuleContext context, SymbolType type) {
//        return addSymbol(name,new SymbolInfo(type,context));
//    }
//
//    @Override
//    public SymbolInfo addSymbol(String name, SymbolInfo symbolInfo) {
//        return symbolTable.put(name,symbolInfo);
//    }
//
//    private void checkAndThrowExceptionIfDuplicated(String name, ParserRuleContext context) throws DuplicatedSymbolException {
//        if (symbolTable.containsKey(name)) {
//            throw new DuplicatedSymbolException(name,symbolTable.get(name).getContext(),context);
//        }
//    }
//
//    @Override
//    public boolean isAReference(String name) {
//        return symbolTable.containsKey(name);
//    }
//
//    @Override
//    public SymbolType getType(String name) {
//        if (symbolTable.containsKey(name)) {
//            return symbolTable.get(name).getType();
//        } else {
//            return SymbolType.ERROR;
//        }
//    }
//
//    @Override
//    public ParserRuleContext getContext(String name) {
//        SymbolInfo info = symbolTable.get(name);
//        if (info != null) {
//            return info.getContext();
//        } else {
//            return null;
//        }
//    }
}
