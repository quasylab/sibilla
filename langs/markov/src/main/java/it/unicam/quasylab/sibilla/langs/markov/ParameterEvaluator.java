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

package it.unicam.quasylab.sibilla.langs.markov;

import it.unicam.quasylab.sibilla.core.util.values.SibillaDouble;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

import java.util.HashMap;
import java.util.Map;

public class ParameterEvaluator extends MarkovChainModelBaseVisitor<Map<String, SibillaValue>> {

    private final Map<String, SibillaValue> parameters;


    public ParameterEvaluator() {
        this.parameters = new HashMap<>();
    }

    @Override
    public Map<String, SibillaValue> visitModel(MarkovChainModelParser.ModelContext ctx) {
        for (MarkovChainModelParser.ElementContext element : ctx.element()) {
            element.accept(this);
        }
        return this.parameters;
    }

    @Override
    public Map<String, SibillaValue> visitConst_declaration(MarkovChainModelParser.Const_declarationContext ctx) {
        return this.parameters;
    }

    @Override
    public Map<String, SibillaValue> visitParam_declaration(MarkovChainModelParser.Param_declarationContext ctx) {
        this.parameters.put(ctx.name.getText(), new SibillaDouble(Double.parseDouble(ctx.value.getText())));
        return this.parameters;
    }
}
