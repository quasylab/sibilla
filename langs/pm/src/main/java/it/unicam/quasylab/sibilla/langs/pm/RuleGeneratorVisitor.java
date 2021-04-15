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
import it.unicam.quasylab.sibilla.core.models.MeasureFunction;
import it.unicam.quasylab.sibilla.core.models.pm.Population;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationRule;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.models.pm.ReactionRule;
import it.unicam.quasylab.sibilla.core.models.pm.util.PopulationRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class RuleGeneratorVisitor extends PopulationModelBaseVisitor<Boolean> {

    private final PopulationRegistry registry;
    private final List<PopulationRule> rules;
    private final HashMap<String, Double> context;
    private final ExpressionEvaluator expressionEvaluator;
    private String ruleName;

    public RuleGeneratorVisitor(SymbolTable table, EvaluationEnvironment environment, PopulationRegistry registry, List<PopulationRule> rules) {
        this.registry = registry;
        this.rules = rules;
        this.context = new HashMap<String,Double>();
        this.expressionEvaluator = new ExpressionEvaluator(table,context,environment);
        this.expressionEvaluator.setPopulationRegistry(registry);
    }

    @Override
    public Boolean visitRule_declaration(PopulationModelParser.Rule_declarationContext ctx) {
        ruleName = ctx.name.getText();
        visit(ctx.rule_body());
        return true;
    }

    @Override
    public Boolean visitFor_statement(PopulationModelParser.For_statementContext ctx) {
        String var = ctx.name.getText();
        int min = expressionEvaluator.evalInt(ctx.range().min);
        int max = expressionEvaluator.evalInt(ctx.range().max);
        Double old = context.get(var);
        for( double i=min; i<max; i++) {
            context.put(var,i);
            visit(ctx.next);
        }
        if (old == null) {
            context.remove(var);
        } else {
            context.put(var,old);
        }
        return true;
    }

    @Override
    public Boolean visitWhen_statement(PopulationModelParser.When_statementContext ctx) {
        if (expressionEvaluator.evalBool(ctx.guard)) {
            visit(ctx.arg);
        }
        return true;
    }

    @Override
    public Boolean visitRule_body(PopulationModelParser.Rule_bodyContext ctx) {
        Predicate<PopulationState> guard = expressionEvaluator.evalStatePredicate(ctx.guard);
        MeasureFunction<PopulationState> rateFunction = expressionEvaluator.evalStateFunction(ctx.rate);
        Population[] pre = expressionEvaluator.evalPopulationPattern(ctx.pre);
        Population[] post = expressionEvaluator.evalPopulationPattern(ctx.post);
        rules.add(new ReactionRule(ruleName+context.toString(),guard,pre,post,(t,s) -> rateFunction.apply(s)));
        return true;
    }
}
