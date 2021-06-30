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
import it.unicam.quasylab.sibilla.core.models.pm.util.PopulationRegistry;

import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

public class PopulationExpressionEvaluator extends PopulationModelBaseVisitor<MeasureFunction<PopulationState>> {

    private final Function<String, Double> resolver;
    private final PopulationRegistry registry;

    public PopulationExpressionEvaluator(Function<String, Double> resolver, PopulationRegistry registry) {
        this.resolver = resolver;
        this.registry = registry;
    }

    @Override
    protected MeasureFunction<PopulationState> defaultResult() {
        return s -> Double.NaN;
    }

    @Override
    public MeasureFunction<PopulationState> visitExponentExpression(PopulationModelParser.ExponentExpressionContext ctx) {
        MeasureFunction<PopulationState> left = ctx.left.accept(this);
        MeasureFunction<PopulationState> right = ctx.right.accept(this);
        return s -> Math.pow(left.apply(s),right.apply(s));
    }

    @Override
    public MeasureFunction<PopulationState> visitIntValue(PopulationModelParser.IntValueContext ctx) {
        int v = Integer.parseInt(ctx.getText());
        return s-> v;
    }

    @Override
    public MeasureFunction<PopulationState> visitBracketExpression(PopulationModelParser.BracketExpressionContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public MeasureFunction<PopulationState> visitPopulationFractionExpression(PopulationModelParser.PopulationFractionExpressionContext ctx) {
        int[] indexes = PopulationModelGenerator.getIndexes(resolver, registry, ctx.agent);
        return s -> s.getFraction(indexes);
    }

    @Override
    public MeasureFunction<PopulationState> visitIfThenElseExpression(PopulationModelParser.IfThenElseExpressionContext ctx) {
        PopulationPredicateEvaluator predicateEvaluator = getPopulationPredicateEvaluator();
        Predicate<PopulationState> guard = ctx.guard.accept(predicateEvaluator);
        MeasureFunction<PopulationState> thenBranch = ctx.thenBranch.accept(this);
        MeasureFunction<PopulationState> elseBranch = ctx.elseBranch.accept(this);
        return s -> (guard.test(s)?thenBranch.apply(s):elseBranch.apply(s));
    }

    public PopulationPredicateEvaluator getPopulationPredicateEvaluator() {
        return new PopulationPredicateEvaluator(this);
    }

    @Override
    public MeasureFunction<PopulationState> visitRealValue(PopulationModelParser.RealValueContext ctx) {
        double val = Double.parseDouble(ctx.getText());
        return s -> val;
    }

    @Override
    public MeasureFunction<PopulationState> visitMulDivExpression(PopulationModelParser.MulDivExpressionContext ctx) {
        return evalBinaryExpression(ctx.left,ctx.op.getText(),ctx.right);
    }

    private MeasureFunction<PopulationState> evalBinaryExpression(PopulationModelParser.ExprContext left, String op, PopulationModelParser.ExprContext right) {
        return PopulationModelGenerator.combine(
                left.accept(this),
                PopulationModelGenerator.getOperator(op),
                right.accept(this)
        );
    }

    @Override
    public MeasureFunction<PopulationState> visitPopulationSizeExpression(PopulationModelParser.PopulationSizeExpressionContext ctx) {
        int[] indexes = PopulationModelGenerator.getIndexes(resolver, registry, ctx.agent);
        return s -> s.getOccupancy(indexes);
    }

    @Override
    public MeasureFunction<PopulationState> visitAddSubExpression(PopulationModelParser.AddSubExpressionContext ctx) {
        return evalBinaryExpression(ctx.left,ctx.op.getText(),ctx.right);
    }

    @Override
    public MeasureFunction<PopulationState> visitUnaryExpression(PopulationModelParser.UnaryExpressionContext ctx) {
        MeasureFunction<PopulationState> arg = ctx.arg.accept(this);
        if (ctx.op.getText().equals("-")) {
            return s -> -arg.apply(s);
        } else {
            return arg;
        }
    }
}
