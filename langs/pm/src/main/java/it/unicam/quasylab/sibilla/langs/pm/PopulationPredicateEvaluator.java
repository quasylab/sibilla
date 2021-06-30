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

import it.unicam.quasylab.sibilla.core.models.MeasureFunction;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.models.pm.RatePopulationFunction;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class PopulationPredicateEvaluator extends PopulationModelBaseVisitor<Predicate<PopulationState>> {

    private final PopulationExpressionEvaluator rateExpressionEvaluator;

    public PopulationPredicateEvaluator(PopulationExpressionEvaluator rateExpressionEvaluator) {
        this.rateExpressionEvaluator = rateExpressionEvaluator;
    }

    @Override
    public Predicate<PopulationState> visitNegationExpression(PopulationModelParser.NegationExpressionContext ctx) {
        return ctx.arg.accept(this).negate();
    }

    @Override
    public Predicate<PopulationState> visitTrueValue(PopulationModelParser.TrueValueContext ctx) {
        return s -> true;
    }

    @Override
    public Predicate<PopulationState> visitRelationExpression(PopulationModelParser.RelationExpressionContext ctx) {
        MeasureFunction<PopulationState> left = ctx.left.accept(rateExpressionEvaluator);
        MeasureFunction<PopulationState> right = ctx.right.accept(rateExpressionEvaluator);
        BiFunction<Double, Double, Boolean> op = PopulationModelGenerator.getRelationOperator(ctx.op.getText());
        return s -> op.apply(left.apply(s),right.apply(s));
    }

    @Override
    public Predicate<PopulationState> visitBracketExpression(PopulationModelParser.BracketExpressionContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public Predicate<PopulationState> visitOrExpression(PopulationModelParser.OrExpressionContext ctx) {
        Predicate<PopulationState> left = ctx.left.accept(this);
        Predicate<PopulationState> right = ctx.right.accept(this);
        return left.or(right);
    }

    @Override
    public Predicate<PopulationState> visitIfThenElseExpression(PopulationModelParser.IfThenElseExpressionContext ctx) {
        Predicate<PopulationState> guard = ctx.guard.accept(this);
        Predicate<PopulationState> thenBranch = ctx.thenBranch.accept(this);
        Predicate<PopulationState> elseBranch = ctx.elseBranch.accept(this);
        return s -> (guard.test(s)?thenBranch.test(s):elseBranch.test(s));
    }

    @Override
    public Predicate<PopulationState> visitFalseValue(PopulationModelParser.FalseValueContext ctx) {
        return s -> false;
    }

    @Override
    public Predicate<PopulationState> visitAndExpression(PopulationModelParser.AndExpressionContext ctx) {
        Predicate<PopulationState> left = ctx.left.accept(this);
        Predicate<PopulationState> right = ctx.right.accept(this);
        return left.and(right);
    }
}
