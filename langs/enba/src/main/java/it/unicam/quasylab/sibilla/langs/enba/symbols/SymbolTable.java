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
package it.unicam.quasylab.sibilla.langs.enba.symbols;

import it.unicam.quasylab.sibilla.langs.enba.ExtendedNBAParser;
import it.unicam.quasylab.sibilla.langs.enba.symbols.exceptions.DuplicatedSymbolException;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.util.*;

public class SymbolTable {

    private final HashMap<String, ExtendedNBAParser.Channel_declarationContext> channels = new HashMap<>();
    private final Map<String, List<Variable>> channelsVariables = new LinkedHashMap<>();
    private final HashMap<String, ExtendedNBAParser.Measure_declarationContext> measures = new HashMap<>();
    private final HashMap<String,ExtendedNBAParser.Predicate_declarationContext> predicates = new HashMap<>();
    private final Map<String,ExtendedNBAParser.Species_declarationContext> species = new LinkedHashMap<>();
    private final Map<String, List<Variable>> speciesVars = new LinkedHashMap<>();
    private final HashMap<String,ExtendedNBAParser.System_declarationContext> systems = new HashMap<>();


    private void checkAndThrowExceptionIfDuplicated(String name, ParserRuleContext context) throws DuplicatedSymbolException {
        ParserRuleContext existing = getContext(name);
        if (existing!=null) {
            throw new DuplicatedSymbolException(name,existing,context);
        }
    }

    public void addChannel(String name, ExtendedNBAParser.Channel_declarationContext context) throws DuplicatedSymbolException {
        checkAndThrowExceptionIfDuplicated(name,context);
        channels.put(name,context);
        this.channelsVariables.put(name, new ArrayList<>());
    }

    public void addMeasure(String name, ExtendedNBAParser.Measure_declarationContext context) throws DuplicatedSymbolException {
        checkAndThrowExceptionIfDuplicated(name,context);
        measures.put(name,context);
    }


    public void addPredicate(String name, ExtendedNBAParser.Predicate_declarationContext context) throws DuplicatedSymbolException {
        checkAndThrowExceptionIfDuplicated(name,context);
        predicates.put(name,context);
    }


    public void addSpecies(String name, ExtendedNBAParser.Species_declarationContext context) throws DuplicatedSymbolException {
        checkAndThrowExceptionIfDuplicated(name,context);
        species.put(name,context);
        this.speciesVars.put(name, new ArrayList<>());
    }


    public void addSpeciesVar(String species, ExtendedNBAParser.Var_declContext context) throws DuplicatedSymbolException {
        if(this.species.containsKey(species)) {
            String varName = context.name.getText();
            Optional<Variable> duplicate = this.speciesVars.get(species)
                    .stream()
                    .filter(v -> v.name().equals(varName))
                    .findFirst();
            if(duplicate.isPresent()) {
                throw new DuplicatedSymbolException(context.name.getText(), duplicate.get().context(), context);
            }
            this.speciesVars.get(species).add(
                    new Variable(
                        varName,
                        Type.fromString(context.type.getText()),
                        context
                    )
            );
        }
    }
    public void addChannelVar(String channel, ExtendedNBAParser.Var_declContext context) throws DuplicatedSymbolException {
        if(this.channels.containsKey(channel)) {
            String varName = context.name.getText();
            Optional<Variable> duplicate = this.channelsVariables.get(channel)
                    .stream()
                    .filter(v -> v.name().equals(varName))
                    .findFirst();
            if(duplicate.isPresent()) {
                throw new DuplicatedSymbolException(context.name.getText(), duplicate.get().context(), context);
            }
            this.channelsVariables.get(channel).add(
                    new Variable(
                            varName,
                            Type.fromString(context.type.getText()),
                            context
                    )
            );
        }
    }
    public Optional<List<Variable>> getSpeciesVariables(String name) {
        return Optional.ofNullable(this.speciesVars.get(name));
    }

    public Optional<List<Variable>> getChannelVariables(String name) {
        return Optional.ofNullable(this.channelsVariables.get(name));
    }

    public int getSpeciesId(String species) {
        return new ArrayList<>(this.species.keySet()).indexOf(species);
    }

    public void addSystem(String name, ExtendedNBAParser.System_declarationContext context) throws DuplicatedSymbolException {
        checkAndThrowExceptionIfDuplicated(name,context);
        systems.put(name,context);
    }


    public String[] species() {
        return species.keySet().toArray(new String[0]);
    }


    public String[] systems() {
        return systems.keySet().toArray(new String[0]);
    }


    public String[] measures() {
        return measures.keySet().toArray(new String[0]);
    }
    public String[] channels() {
        return channels.keySet().toArray(new String[0]);
    }

    public boolean isAChannel(String name) {
        return channels.containsKey(name);
    }

    public boolean isAMeasure(String name) {
        return measures.containsKey(name);
    }

    public boolean isASpecies(String name) {
        return species.containsKey(name);
    }

    public ParserRuleContext getContext(String name) {
        if (channels.containsKey(name)) { return channels.get(name); }
        if (measures.containsKey(name)) { return measures.get(name); }
        if (species.containsKey(name)) { return species.get(name); }
        if (systems.containsKey(name)) { return systems.get(name); }
        return null;
    }


    public boolean isDefined(String name) {
        return getContext(name) != null;
    }


    public boolean isSpeciesVarDefined(String species, String name) {
        return false;
    }


    public ExtendedNBAParser.Species_declarationContext getSpeciesContext(String name) {
        return species.get(name);
    }

    public ExtendedNBAParser.Measure_declarationContext getMeasureContext(String name) {
        return measures.get(name);
    }


    public ExtendedNBAParser.System_declarationContext getSystemContext(String name) {
        return systems.get(name);
    }
}
