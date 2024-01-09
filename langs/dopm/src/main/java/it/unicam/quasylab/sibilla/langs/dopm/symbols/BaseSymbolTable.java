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
package it.unicam.quasylab.sibilla.langs.dopm.symbols;

import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelParser;
import it.unicam.quasylab.sibilla.langs.dopm.symbols.exceptions.DuplicatedSymbolException;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BaseSymbolTable implements SymbolTable {

    private final HashMap<String, DataOrientedPopulationModelParser.Measure_declarationContext> measures = new HashMap<>();

    private final HashMap<String,DataOrientedPopulationModelParser.Predicate_declarationContext> predicates = new HashMap<>();

    private final HashMap<String,DataOrientedPopulationModelParser.Species_declarationContext> species = new HashMap<>();

    private final HashMap<String,DataOrientedPopulationModelParser.Rule_declarationContext> rules = new HashMap<>();

    private final HashMap<String,DataOrientedPopulationModelParser.System_declarationContext> systems = new HashMap<>();


    private void checkAndThrowExceptionIfDuplicated(String name, ParserRuleContext context) throws DuplicatedSymbolException {
        ParserRuleContext existing = getContext(name);
        if (existing!=null) {
            throw new DuplicatedSymbolException(name,existing,context);
        }
    }

    @Override
    public void addMeasure(String name, DataOrientedPopulationModelParser.Measure_declarationContext context) throws DuplicatedSymbolException {
        checkAndThrowExceptionIfDuplicated(name,context);
        measures.put(name,context);
    }

    @Override
    public void addPredicate(String name, DataOrientedPopulationModelParser.Predicate_declarationContext context) throws DuplicatedSymbolException {
        checkAndThrowExceptionIfDuplicated(name,context);
        predicates.put(name,context);
    }

    @Override
    public void addSpecies(String name, DataOrientedPopulationModelParser.Species_declarationContext context) throws DuplicatedSymbolException {
        checkAndThrowExceptionIfDuplicated(name,context);
        species.put(name,context);
    }

    @Override
    public void addRule(String name, DataOrientedPopulationModelParser.Rule_declarationContext context) throws DuplicatedSymbolException {
        checkAndThrowExceptionIfDuplicated(name,context);
        rules.put(name,context);
    }

    @Override
    public void addSystem(String name, DataOrientedPopulationModelParser.System_declarationContext context) throws DuplicatedSymbolException {
        checkAndThrowExceptionIfDuplicated(name,context);
        systems.put(name,context);
    }
    
    @Override
    public String[] rules() {
        return rules.keySet().toArray(new String[0]);
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
    public ParserRuleContext getContext(String name) {
        if (measures.containsKey(name)) { return measures.get(name); }
        if (rules.containsKey(name)) { return rules.get(name); }
        if (species.containsKey(name)) { return species.get(name); }
        if (systems.containsKey(name)) { return systems.get(name); }
        return null;
    }

    @Override
    public boolean isDefined(String name) {
        return getContext(name) != null;
    }

    @Override
    public DataOrientedPopulationModelParser.Species_declarationContext getSpeciesContext(String name) {
        return species.get(name);
    }

    @Override
    public DataOrientedPopulationModelParser.Rule_declarationContext getRuleContext(String name) {
        return rules.get(name);
    }

    @Override
    public DataOrientedPopulationModelParser.Measure_declarationContext getMeasureContext(String name) {
        return measures.get(name);
    }

    @Override
    public DataOrientedPopulationModelParser.System_declarationContext getSystemContext(String name) {
        return systems.get(name);
    }


    @Override
    //public Set<String> checkLocalVariables(DataOrientedPopulationModelParser.Var_ass_listContext local_variables) throws DuplicatedSymbolException {
    public Set<String> checkLocalVariables(DataOrientedPopulationModelParser.Var_ass_listContext local_variables) throws DuplicatedSymbolException {
        Set<String> localVariables = new HashSet<>();
        if (local_variables != null) {
            for(DataOrientedPopulationModelParser.Var_assContext lv: local_variables.var_ass()) {
                //checkAndThrowExceptionIfDuplicated(lv.name.getText(),lv);
                localVariables.add(lv.name.getText());
            }
        }
        return localVariables;
    }

    @Override
    //public Set<String> checkLocalVariables(List<Token> args, ParserRuleContext ctx) throws DuplicatedSymbolException
    public Set<String> checkLocalVariables(List<Token> args, ParserRuleContext ctx) throws DuplicatedSymbolException {
        Set<String> localVariables = new HashSet<>();
        for(Token lv: args) {
            //checkAndThrowExceptionIfDuplicated(lv.getText(),ctx);
            localVariables.add(lv.getText());
        }
        return localVariables;
    }
}
