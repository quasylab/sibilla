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
import it.unicam.quasylab.sibilla.core.util.values.SibillaDouble;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class EnvironmentGenerator extends PopulationModelBaseVisitor<Boolean> {

    private final Map<String,SibillaValue> values;
    private final Map<String, SibillaValue> parameters;

    public EnvironmentGenerator() {
        this(new HashMap<>());
    }

    public EnvironmentGenerator(Map<String, SibillaValue> parameters) {
        this.values = new HashMap<>();
        this.parameters = parameters;
    }

    @Override
    public Boolean visitModel(PopulationModelParser.ModelContext ctx) {
        ctx.element().forEach(e -> e.accept(this));
        return true;
    }

    @Override
    public Boolean visitConst_declaration(PopulationModelParser.Const_declarationContext ctx) {
        Function<Function<String,Optional<SibillaValue>>,SibillaValue> evaluator =
                resolver-> ctx.expr().accept(new ExpressionEvaluator(resolver));
        values.put(ctx.name.getText(), evaluator.apply(this::getValueOf));
        return true;
    }

    private Optional<SibillaValue> getValueOf(String name) {
        if (values.containsKey(name)) return Optional.of(values.get(name));
        if (parameters.containsKey(name)) return Optional.of(parameters.get(name));
        return Optional.empty();
    }

    @Override
    public Boolean visitParam_declaration(PopulationModelParser.Param_declarationContext ctx) {
        if (!parameters.containsKey(ctx.name.getText())) {
            ExpressionEvaluator evaluator = new ExpressionEvaluator(this::getValueOf);
            parameters.put(ctx.name.getText(), ctx.expr().accept(evaluator));
        }
        this.values.put(ctx.name.getText(), parameters.get(ctx.name.getText()));
        return true;
    }

    @Override
    protected Boolean defaultResult() {
        return true;
    }

    public Map<String,SibillaValue> getParameters() {
        return this.parameters;
    }

    @Override
    protected Boolean aggregateResult(Boolean aggregate, Boolean nextResult) {
        return aggregate & nextResult;
    }

    public Map<String, SibillaValue> getValues() {
        return this.values;
    }
}
