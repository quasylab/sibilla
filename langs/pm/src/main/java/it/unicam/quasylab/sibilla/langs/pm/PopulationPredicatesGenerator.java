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

import it.unicam.quasylab.sibilla.core.models.EvaluationEnvironment;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.models.pm.util.PopulationRegistry;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import it.unicam.quasylab.sibilla.core.simulator.sampling.SimpleMeasure;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class PopulationPredicatesGenerator extends PopulationModelBaseVisitor<Map<String, Predicate<PopulationState>>> {

    private final Map<String, Predicate<PopulationState>> measures;
    private final EvaluationEnvironment environment;
    private final PopulationRegistry registry;

    public PopulationPredicatesGenerator(EvaluationEnvironment environment, PopulationRegistry registry) {
        this.environment = environment;
        this.registry = registry;
        this.measures = new HashMap<>();
    }

    @Override
    public Map<String, Predicate<PopulationState>> visitModel(PopulationModelParser.ModelContext ctx) {
        ctx.element().forEach(e -> e.accept(this));
        return measures;
    }

    @Override
    protected Map<String, Predicate<PopulationState>> defaultResult() {
        return measures;
    }

    @Override
    public Map<String, Predicate<PopulationState>> visitPredicate_declaration(PopulationModelParser.Predicate_declarationContext ctx) {
        String name = ctx.name.getText();
        Function<String, Optional<SibillaValue>> evaluator = environment.getEvaluator();
        measures.put(name, ctx.expr().accept(new PopulationExpressionEvaluator(evaluator, registry).getPopulationPredicateEvaluator()));
        return measures;
    }


}
