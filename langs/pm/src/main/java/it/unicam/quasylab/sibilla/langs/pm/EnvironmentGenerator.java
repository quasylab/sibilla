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

import it.unicam.quasylab.sibilla.core.models.CachedValues;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class EnvironmentGenerator extends PopulationModelBaseVisitor<Boolean> {

    private final Map<String,Function<Function<String,Double>,Double>> definitions;
    private final Map<String,Double> constants;
    private final Map<String,Double> parameters;

    public EnvironmentGenerator() {
        this.constants = new HashMap<>();
        this.parameters = new HashMap<>();
        this.definitions = new HashMap<>();
    }

    @Override
    public Boolean visitModel(PopulationModelParser.ModelContext ctx) {
        ctx.element().forEach(e -> e.accept(this));
        return true;
    }

    @Override
    public Boolean visitConst_declaration(PopulationModelParser.Const_declarationContext ctx) {
        Function<Function<String,Double>,Double> evaluator =
                resolver-> ctx.expr().accept(new ExpressionEvaluator(resolver));
        definitions.put(ctx.name.getText(), evaluator);
        constants.put(ctx.name.getText(), evaluator.apply(this::getValueOf));
        return true;
    }

    private Double getValueOf(String name) {
        return constants.getOrDefault(name,parameters.getOrDefault(name, Double.NaN));
    }

    @Override
    public Boolean visitParam_declaration(PopulationModelParser.Param_declarationContext ctx) {
        ExpressionEvaluator evaluator = new ExpressionEvaluator(this::getValueOf);
        parameters.put(ctx.name.getText(), ctx.expr().accept(evaluator));
        return true;
    }

    @Override
    protected Boolean defaultResult() {
        return true;
    }

    public Map<String,Double> getParameters() {
        return this.parameters;
    }

    public CachedValues getConstants() {
        return new CachedValues(definitions, constants);
    }
}
