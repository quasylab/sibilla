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

public interface SymbolTable {

    void addVariable(String name, ParserRuleContext context, SymbolType type) throws DuplicatedSymbolException;

    void addConstant(String name, PopulationModelParser.Const_declarationContext context, SymbolType type) throws DuplicatedSymbolException;

    void addParameter(String name, PopulationModelParser.Param_declarationContext context, SymbolType type) throws DuplicatedSymbolException;

    void addMeasure(String name, PopulationModelParser.Measure_declarationContext context) throws DuplicatedSymbolException;

    void addSpecies(String name, PopulationModelParser.Species_declarationContext context) throws DuplicatedSymbolException;

    void addRule(String name, PopulationModelParser.Rule_declarationContext context) throws DuplicatedSymbolException;

    void deleteVariable(String name);

    void addSystem(String name, PopulationModelParser.System_declarationContext context) throws DuplicatedSymbolException;

    String[] rules();

    String[] constants();

    String[] species();

    String[] systems();

    String[] measures();

    int arity(String species);

    boolean isAReference(String name);

    boolean isAMeasure(String name);

    boolean isASpecies(String name);

    boolean isARule(String name);

    SymbolType getType(String name);

    ParserRuleContext getContext(String name);
}
