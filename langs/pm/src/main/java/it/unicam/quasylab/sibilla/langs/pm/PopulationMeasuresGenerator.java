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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class PopulationMeasuresGenerator extends PopulationModelBaseVisitor<Map<String, Measure<PopulationState>>> {

    private final Map<String, Measure<PopulationState>> measures;
    private final EvaluationEnvironment environment;
    private final PopulationRegistry registry;

    public PopulationMeasuresGenerator(EvaluationEnvironment environment, PopulationRegistry registry) {
        this.environment = environment;
        this.registry = registry;
        this.measures = new HashMap<>();
    }

    @Override
    public Map<String, Measure<PopulationState>> visitModel(PopulationModelParser.ModelContext ctx) {
        ctx.element().forEach(e -> e.accept(this));
        return measures;
    }

    @Override
    protected Map<String, Measure<PopulationState>> defaultResult() {
        return measures;
    }

    @Override
    public Map<String, Measure<PopulationState>> visitMeasure_declaration(PopulationModelParser.Measure_declarationContext ctx) {
        String name = ctx.name.getText();
        Function<String, Double> evaluator = environment.getEvaluator();
        List<Map<String,Double>> maps = PopulationModelGenerator.getMaps(evaluator, ctx.local_variables(), ctx.guard_expression());
        maps.stream().map(m -> getMeasure(name, evaluator, m, ctx.expr())).forEach(m -> measures.put(m.getName(), m));
        return super.visitMeasure_declaration(ctx);
    }

    private Measure<PopulationState> getMeasure(String name, Function<String, Double> evaluator, Map<String, Double> m, PopulationModelParser.ExprContext expr) {
        return new SimpleMeasure<>(name+m.toString(), expr.accept(
                new PopulationExpressionEvaluator(PopulationModelGenerator.combine(evaluator, m), registry))
        );
    }


}
