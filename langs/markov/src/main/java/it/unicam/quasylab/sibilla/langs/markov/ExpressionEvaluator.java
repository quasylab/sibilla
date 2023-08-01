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

package it.unicam.quasylab.sibilla.langs.markov;

import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

import java.util.Optional;
import java.util.function.Function;

public class ExpressionEvaluator extends MarkovChainModelBaseVisitor<SibillaValue> {

    private final Function<String, Optional<SibillaValue>> resolver;

    public ExpressionEvaluator(Function<String, Optional<SibillaValue>> resolver) {
        super();
        this.resolver = resolver;
    }

    public static int evalInteger(Function<String, Optional<SibillaValue>> resolver, MarkovChainModelParser.ExprContext expr) {
        ExpressionEvaluator evaluator = new ExpressionEvaluator(resolver);
        return expr.accept(evaluator).intOf();
    }

    public static double evalDouble(Function<String, Optional<SibillaValue>> resolver, MarkovChainModelParser.ExprContext expr) {
        ExpressionEvaluator evaluator = new ExpressionEvaluator(resolver);
        return expr.accept(evaluator).doubleOf();
    }

    public static boolean evalBoolean(Function<String, Optional<SibillaValue>> resolver, MarkovChainModelParser.ExprContext expr) {
        ExpressionEvaluator evaluator = new ExpressionEvaluator(resolver);
        return expr.accept(evaluator).booleanOf();
    }

    @Override
    public SibillaValue visitExponentExpression(MarkovChainModelParser.ExponentExpressionContext ctx) {
        SibillaValue v1 = ctx.left.accept(this);
        SibillaValue v2 = ctx.right.accept(this);

        return SibillaValue.eval(Math::pow, v1, v2);
    }

    @Override
    public SibillaValue visitNegationExpression(MarkovChainModelParser.NegationExpressionContext ctx) {
        return SibillaValue.not(ctx.arg.accept(this));
    }

    @Override
    public SibillaValue visitReferenceExpression(MarkovChainModelParser.ReferenceExpressionContext ctx) {
        return resolver.apply(ctx.getText()).orElse(SibillaValue.ERROR_VALUE);
    }

    @Override
    public SibillaValue visitIntValue(MarkovChainModelParser.IntValueContext ctx) {
        return SibillaValue.of(Integer.parseInt(ctx.getText()));
    }

    @Override
    public SibillaValue visitTrueValue(MarkovChainModelParser.TrueValueContext ctx) {
        return SibillaValue.of(true);
    }

    @Override
    public SibillaValue visitRelationExpression(MarkovChainModelParser.RelationExpressionContext ctx) {
        if (ctx.op.equals("<")) return SibillaValue.of(ctx.left.accept(this).doubleOf()<ctx.right.accept(this).doubleOf());
        if (ctx.op.equals("<=")) return SibillaValue.of(ctx.left.accept(this).doubleOf()<=ctx.right.accept(this).doubleOf());
        if (ctx.op.equals("==")) return SibillaValue.of(ctx.left.accept(this).doubleOf()==ctx.right.accept(this).doubleOf());
        if (ctx.op.equals("!=")) return SibillaValue.of(ctx.left.accept(this).doubleOf()!=ctx.right.accept(this).doubleOf());
        if (ctx.op.equals(">")) return SibillaValue.of(ctx.left.accept(this).doubleOf()>ctx.right.accept(this).doubleOf());
        if (ctx.op.equals(">=")) return SibillaValue.of(ctx.left.accept(this).doubleOf()>=ctx.right.accept(this).doubleOf());
        return SibillaValue.ERROR_VALUE;
    }

    @Override
    public SibillaValue visitBracketExpression(MarkovChainModelParser.BracketExpressionContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public SibillaValue visitOrExpression(MarkovChainModelParser.OrExpressionContext ctx) {
        return SibillaValue.or(ctx.left.accept(this), ctx.right.accept(this));
    }

    @Override
    public SibillaValue visitIfThenElseExpression(MarkovChainModelParser.IfThenElseExpressionContext ctx) {
        return (ctx.guard.accept(this).booleanOf()?ctx.thenBranch.accept(this):ctx.elseBranch.accept(this));
    }

    @Override
    public SibillaValue visitFalseValue(MarkovChainModelParser.FalseValueContext ctx) {
        return SibillaValue.of(false);
    }

    @Override
    public SibillaValue visitRealValue(MarkovChainModelParser.RealValueContext ctx) {
        return SibillaValue.of(Double.parseDouble(ctx.getText()));
    }

    @Override
    public SibillaValue visitAndExpression(MarkovChainModelParser.AndExpressionContext ctx) {
        return SibillaValue.and(ctx.left.accept(this), ctx.right.accept(this));
    }

    @Override
    public SibillaValue visitMulDivExpression(MarkovChainModelParser.MulDivExpressionContext ctx) {
        if (ctx.op.getText().equals("*")) return SibillaValue.mul(ctx.left.accept(this), ctx.right.accept(this));
        if (ctx.op.getText().equals("/")) return SibillaValue.div(ctx.left.accept(this), ctx.right.accept(this));
        if (ctx.op.getText().equals("//")) return SibillaValue.zeroDiv(ctx.left.accept(this), ctx.right.accept(this));
        return SibillaValue.ERROR_VALUE;
    }

    @Override
    public SibillaValue visitAddSubExpression(MarkovChainModelParser.AddSubExpressionContext ctx) {
        if (ctx.op.getText().equals("+")) return SibillaValue.sum(ctx.left.accept(this), ctx.right.accept(this));
        if (ctx.op.getText().equals("-")) return SibillaValue.sub(ctx.left.accept(this), ctx.right.accept(this));
        if (ctx.op.getText().equals("%")) return SibillaValue.mod(ctx.left.accept(this), ctx.right.accept(this));
        return SibillaValue.ERROR_VALUE;
    }

    @Override
    public SibillaValue visitCastToIntExpression(MarkovChainModelParser.CastToIntExpressionContext ctx) {
        return SibillaValue.of(ctx.arg.accept(this).intOf());
    }

    @Override
    public SibillaValue visitUnaryExpression(MarkovChainModelParser.UnaryExpressionContext ctx) {
        if (ctx.op.getText().equals("-")) {
            return SibillaValue.minus(ctx.arg.accept(this));
        } else {
            return ctx.arg.accept(this);
        }
    }
}
