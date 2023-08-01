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
import it.unicam.quasylab.sibilla.core.models.pm.PopulationRule;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.models.pm.RatePopulationFunction;
import it.unicam.quasylab.sibilla.core.models.pm.ReactionRule;
import it.unicam.quasylab.sibilla.core.models.pm.util.PopulationRegistry;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PopulationRuleGenerator extends PopulationModelBaseVisitor<List<PopulationRule>> {

    private final EvaluationEnvironment environment;
    private final PopulationRegistry registry;
    private List<PopulationRule> rules;


    public PopulationRuleGenerator(EvaluationEnvironment environment, PopulationRegistry registry) {
        this.environment = environment;
        this.registry = registry;
        this.rules = new LinkedList<>();
    }

    @Override
    public List<PopulationRule> visitModel(PopulationModelParser.ModelContext ctx) {
        ctx.element().forEach(e -> e.accept(this));
        return rules;
    }

    @Override
    public List<PopulationRule> visitRule_declaration(PopulationModelParser.Rule_declarationContext ctx) {
        Function<String, Optional<SibillaValue>> evaluator = environment.getEvaluator();
        List<Map<String,SibillaValue>> maps = PopulationModelGenerator.getMaps(evaluator, ctx.local_variables(), ctx.guard_expression());
        rules.addAll(getRules(ctx.name.getText(), evaluator, maps, ctx.rule_body()));
        return rules;
    }


    public List<PopulationRule> getRules(String name, Function<String,Optional<SibillaValue>> evaluator, List<Map<String,SibillaValue>> maps, PopulationModelParser.Rule_bodyContext body) {
        return maps.stream().map(m -> getRule(name, evaluator, m, body)).collect(Collectors.toList());
    }

    public PopulationRule getRule(String name, Function<String,Optional<SibillaValue>> evaluator, Map<String,SibillaValue> map, PopulationModelParser.Rule_bodyContext body) {
        RateExpressionEvaluator expressionEvaluator =  new RateExpressionEvaluator(PopulationModelGenerator.combine(evaluator,map), registry);
        RatePopulationFunction biPredicate = (body.guard==null?null:body.guard.accept(expressionEvaluator));
        Predicate<PopulationState> predicate = null;
        if (biPredicate != null) {
            predicate = s -> biPredicate.apply(0.0,s).booleanOf();
        }
        return new ReactionRule(
                name+(map.isEmpty()?"":map.toString()),
                predicate,
                PopulationModelGenerator.getPopulationArray(registry, evaluator, map, body.pre.species_pattern_element()),
                PopulationModelGenerator.getPopulationArray(registry, evaluator, map, body.post.species_pattern_element()),
                body.rate.accept(expressionEvaluator)
        );
    }


}
