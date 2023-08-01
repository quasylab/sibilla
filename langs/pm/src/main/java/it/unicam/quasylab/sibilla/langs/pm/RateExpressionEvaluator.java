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
import it.unicam.quasylab.sibilla.core.models.pm.util.PopulationRegistry;
import it.unicam.quasylab.sibilla.core.util.values.SibillaBoolean;
import it.unicam.quasylab.sibilla.core.util.values.SibillaDouble;
import it.unicam.quasylab.sibilla.core.util.values.SibillaInteger;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class RateExpressionEvaluator extends PopulationModelBaseVisitor<RatePopulationFunction> {

    private final Function<String, Optional<SibillaValue>> resolver;
    private final PopulationRegistry registry;

    public RateExpressionEvaluator(Function<String, Optional<SibillaValue>> resolver, PopulationRegistry registry) {
        this.resolver = resolver;
        this.registry = registry;
    }

    @Override
    protected RatePopulationFunction defaultResult() {
        return (n,s) -> SibillaValue.ERROR_VALUE;
    }

    @Override
    public RatePopulationFunction visitReferenceExpression(PopulationModelParser.ReferenceExpressionContext ctx) {
        Optional<SibillaValue> v = resolver.apply(ctx.reference.getText());
        if (v.isPresent()) {
            SibillaValue val = v.get();
            return (n, s) -> val;
        } else {
            return (n, s) -> SibillaValue.ERROR_VALUE;
        }
    }

    @Override
    public RatePopulationFunction visitExponentExpression(PopulationModelParser.ExponentExpressionContext ctx) {
        RatePopulationFunction left = ctx.left.accept(this);
        RatePopulationFunction right = ctx.right.accept(this);
        return (n,s) -> SibillaValue.eval(Math::pow, left.apply(n,s),right.apply(n,s));
    }

    @Override
    public RatePopulationFunction visitIntValue(PopulationModelParser.IntValueContext ctx) {
        SibillaInteger v = new SibillaInteger(Integer.parseInt(ctx.getText()));
        return (n,s) -> v;
    }

    @Override
    public RatePopulationFunction visitBracketExpression(PopulationModelParser.BracketExpressionContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public RatePopulationFunction visitPopulationFractionExpression(PopulationModelParser.PopulationFractionExpressionContext ctx) {
        int[] indexes = PopulationModelGenerator.getIndexes(resolver, registry, ctx.agent);
        return (n,s) -> new SibillaDouble(s.getFraction(indexes));
    }

    @Override
    public RatePopulationFunction visitIfThenElseExpression(PopulationModelParser.IfThenElseExpressionContext ctx) {
        RatePopulationFunction guard = ctx.guard.accept(this);
        RatePopulationFunction thenBranch = ctx.thenBranch.accept(this);
        RatePopulationFunction elseBranch = ctx.elseBranch.accept(this);
        return (n,s) -> (guard.apply(n,s).booleanOf()?thenBranch.apply(n,s):elseBranch.apply(n,s));
    }


    @Override
    public RatePopulationFunction visitRealValue(PopulationModelParser.RealValueContext ctx) {
        SibillaDouble val = new SibillaDouble(Double.parseDouble(ctx.getText()));
        return (n,s) -> val;
    }

    @Override
    public RatePopulationFunction visitMulDivExpression(PopulationModelParser.MulDivExpressionContext ctx) {
        return evalBinaryExpression(ctx.left,ctx.op.getText(),ctx.right);
    }

    private RatePopulationFunction evalBinaryExpression(PopulationModelParser.ExprContext left, String op, PopulationModelParser.ExprContext right) {
        return PopulationModelGenerator.combine(left.accept(this),
                PopulationModelGenerator.getOperator(op),
                right.accept(this));
    }

    @Override
    public RatePopulationFunction visitPopulationSizeExpression(PopulationModelParser.PopulationSizeExpressionContext ctx) {
        int[] indexes = PopulationModelGenerator.getIndexes(resolver, registry, ctx.agent);
        return (n,s) -> new SibillaDouble(s.getOccupancy(indexes));
    }

    @Override
    public RatePopulationFunction visitAddSubExpression(PopulationModelParser.AddSubExpressionContext ctx) {
        return evalBinaryExpression(ctx.left,ctx.op.getText(),ctx.right);
    }

    @Override
    public RatePopulationFunction visitUnaryExpression(PopulationModelParser.UnaryExpressionContext ctx) {
        RatePopulationFunction arg = ctx.arg.accept(this);
        if (ctx.op.getText().equals("-")) {
            return (n,s) -> SibillaValue.minus(arg.apply(n,s));
        } else {
            return arg;
        }
    }

    @Override
    public RatePopulationFunction visitNegationExpression(PopulationModelParser.NegationExpressionContext ctx) {
        RatePopulationFunction argFunction = ctx.arg.accept(this);
        return (now, s) -> SibillaValue.not(argFunction.apply(now, s));
    }

    @Override
    public RatePopulationFunction visitTrueValue(PopulationModelParser.TrueValueContext ctx) {
        return (now, s) -> SibillaBoolean.TRUE;
    }

    @Override
    public RatePopulationFunction visitRelationExpression(PopulationModelParser.RelationExpressionContext ctx) {
        BiPredicate<SibillaValue, SibillaValue> relationPredicate = PopulationModelGenerator.getRelationOperator(ctx.op.getText());
        RatePopulationFunction leftFunction = ctx.left.accept(this);
        RatePopulationFunction rightFunction = ctx.right.accept(this);
        return (now, s) -> SibillaBoolean.of(relationPredicate.test(leftFunction.apply(now, s), rightFunction.apply(now, s)));
    }

    @Override
    public RatePopulationFunction visitOrExpression(PopulationModelParser.OrExpressionContext ctx) {
        RatePopulationFunction leftFunction = ctx.left.accept(this);
        RatePopulationFunction rightFunction = ctx.right.accept(this);
        return (now, s) -> SibillaValue.or(leftFunction.apply(now, s), rightFunction.apply(now, s));
    }

    @Override
    public RatePopulationFunction visitFalseValue(PopulationModelParser.FalseValueContext ctx) {
        return (now, s) -> SibillaBoolean.FALSE;
    }

    @Override
    public RatePopulationFunction visitAndExpression(PopulationModelParser.AndExpressionContext ctx) {
        RatePopulationFunction leftFunction = ctx.left.accept(this);
        RatePopulationFunction rightFunction = ctx.right.accept(this);
        return (now, s) -> SibillaValue.and(leftFunction.apply(now, s), rightFunction.apply(now, s));
    }
}
