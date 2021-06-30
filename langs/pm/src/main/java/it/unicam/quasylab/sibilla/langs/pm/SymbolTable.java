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

package it.unicam.quasylab.sibilla.langs.pm;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.util.List;
import java.util.Set;

public interface SymbolTable {

    void addVariable(String name, ParserRuleContext context, SymbolType type) throws DuplicatedSymbolException;

    void addConstant(String name, PopulationModelParser.Const_declarationContext context, SymbolType type) throws DuplicatedSymbolException;

    void addParameter(String name, PopulationModelParser.Param_declarationContext context, SymbolType type) throws DuplicatedSymbolException;

    void addMeasure(String name, PopulationModelParser.Measure_declarationContext context) throws DuplicatedSymbolException;

    void addSpecies(String name, PopulationModelParser.Species_declarationContext context) throws DuplicatedSymbolException;

    void addRule(String name, PopulationModelParser.Rule_declarationContext context) throws DuplicatedSymbolException;

    void deleteVariable(String name);

    void addSystem(String name, PopulationModelParser.System_declarationContext context) throws DuplicatedSymbolException;

    void addLabel(String name, PopulationModelParser.Label_declarationContext ctx) throws DuplicatedSymbolException;

    String[] rules();

    String[] constants();

    String[] species();

    String[] systems();

    String[] measures();

    String[] parameters();

    String[] labels();

    int arity(String species);

    boolean isAMeasure(String name);

    boolean isASpecies(String name);

    boolean isARule(String name);

    boolean isAConst(String name);

    boolean isALabel(String name);

    boolean isAParameter(String name);

    boolean isAVariable(String name);

    SymbolType getType(String name);

    ParserRuleContext getContext(String name);

    boolean isDefined(String name);

    PopulationModelParser.Species_declarationContext getSpeciesContext(String name);

    PopulationModelParser.Rule_declarationContext getRuleContext(String name);

    default boolean isAReference(String name) {
        return isAParameter(name)||isAConst(name)||isAVariable(name);
    }

    PopulationModelParser.Measure_declarationContext getMeasureContext(String measure);

    PopulationModelParser.System_declarationContext getSystemContext(String name);

    PopulationModelParser.Const_declarationContext getConstantDeclarationContext(String name);

    PopulationModelParser.Param_declarationContext getParameterContext(String name);

    PopulationModelParser.Label_declarationContext getLabelContext(String name);

    Set<String> checkLocalVariables(PopulationModelParser.Local_variablesContext local_variables) throws DuplicatedSymbolException;

    Set<String> checkLocalVariables(List<Token> args, ParserRuleContext ctx) throws DuplicatedSymbolException;
}
