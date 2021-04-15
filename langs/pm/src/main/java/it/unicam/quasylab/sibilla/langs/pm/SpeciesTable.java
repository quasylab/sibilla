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

import java.util.HashMap;
import java.util.Map;

public class SpeciesTable {

    private final Map<String,PopulationModelParser.Species_declarationContext> table;

    public SpeciesTable() {
        this.table = new HashMap<>();
    }

    public int getSpeciesArity(String name) {
        PopulationModelParser.Species_declarationContext ctx = table.get(name);
        if (ctx != null) {
            return ctx.range().size();
        } else {
            return 0;
        }
    }

    public boolean isDefined(String name) {
        return table.containsKey(name);
    }

    public ParserRuleContext addSpecies(PopulationModelParser.Species_declarationContext ctx) {
        return table.put(ctx.name.getText(),ctx);
    }

    public ParserRuleContext getContext(String name) {
        return table.get(name);
    }
}
