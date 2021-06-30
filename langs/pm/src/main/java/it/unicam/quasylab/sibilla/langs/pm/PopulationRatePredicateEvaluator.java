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

import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.models.pm.RatePopulationFunction;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public class PopulationRatePredicateEvaluator extends PopulationModelBaseVisitor<BiPredicate<Double, PopulationState>> {

    private final RateExpressionEvaluator rateExpressionEvaluator;

    public PopulationRatePredicateEvaluator(RateExpressionEvaluator rateExpressionEvaluator) {
        this.rateExpressionEvaluator = rateExpressionEvaluator;
    }

    @Override
    public BiPredicate<Double, PopulationState> visitNegationExpression(PopulationModelParser.NegationExpressionContext ctx) {
        return ctx.arg.accept(this).negate();
    }

    @Override
    public BiPredicate<Double, PopulationState> visitTrueValue(PopulationModelParser.TrueValueContext ctx) {
        return (n,s) -> true;
    }

    @Override
    public BiPredicate<Double, PopulationState> visitRelationExpression(PopulationModelParser.RelationExpressionContext ctx) {
        RatePopulationFunction left = ctx.left.accept(rateExpressionEvaluator);
        RatePopulationFunction right = ctx.right.accept(rateExpressionEvaluator);
        BiFunction<Double, Double, Boolean> op = PopulationModelGenerator.getRelationOperator(ctx.op.getText());
        return (n,s) -> op.apply(left.apply(n,s),right.apply(n,s));
    }

    @Override
    public BiPredicate<Double, PopulationState> visitBracketExpression(PopulationModelParser.BracketExpressionContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public BiPredicate<Double, PopulationState> visitOrExpression(PopulationModelParser.OrExpressionContext ctx) {
        BiPredicate<Double, PopulationState> left = ctx.left.accept(this);
        BiPredicate<Double, PopulationState> right = ctx.right.accept(this);
        return left.or(right);
    }

    @Override
    public BiPredicate<Double, PopulationState> visitIfThenElseExpression(PopulationModelParser.IfThenElseExpressionContext ctx) {
        BiPredicate<Double, PopulationState> guard = ctx.guard.accept(this);
        BiPredicate<Double, PopulationState> thenBranch = ctx.thenBranch.accept(this);
        BiPredicate<Double, PopulationState> elseBranch = ctx.elseBranch.accept(this);
        return (n,s) -> (guard.test(n,s)?thenBranch.test(n,s):elseBranch.test(n,s));
    }

    @Override
    public BiPredicate<Double, PopulationState> visitFalseValue(PopulationModelParser.FalseValueContext ctx) {
        return (n,s) -> false;
    }

    @Override
    public BiPredicate<Double, PopulationState> visitAndExpression(PopulationModelParser.AndExpressionContext ctx) {
        BiPredicate<Double, PopulationState> left = ctx.left.accept(this);
        BiPredicate<Double, PopulationState> right = ctx.right.accept(this);
        return left.and(right);
    }
}
