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

import java.util.function.Function;

public class ExpressionEvaluator extends MarkovChainModelBaseVisitor<Value> {

    private final Function<String, Double> resolver;
    private final Function<String, DataType> types;

    public ExpressionEvaluator(Function<String, Double> resolver, Function<String, DataType> types) {
        super();
        this.resolver = resolver;
        this.types = types;
    }

    public static int evalInteger(Function<String, DataType> types, Function<String, Double> resolver, MarkovChainModelParser.ExprContext expr) {
        ExpressionEvaluator evaluator = new ExpressionEvaluator(resolver, types);
        return expr.accept(evaluator).getIntValue();
    }

    public static double evalDouble(Function<String, DataType> types, Function<String, Double> resolver, MarkovChainModelParser.ExprContext expr) {
        ExpressionEvaluator evaluator = new ExpressionEvaluator(resolver, types);
        return expr.accept(evaluator).getDoubleValue();
    }

    public static boolean evalBoolean(Function<String, DataType> types, Function<String, Double> resolver, MarkovChainModelParser.ExprContext expr) {
        ExpressionEvaluator evaluator = new ExpressionEvaluator(resolver, types);
        return expr.accept(evaluator).getBooleanValue();
    }

    @Override
    public Value visitExponentExpression(MarkovChainModelParser.ExponentExpressionContext ctx) {
        Value v1 = ctx.left.accept(this);
        Value v2 = ctx.right.accept(this);
        return v1.pow(v2);
    }

    @Override
    public Value visitNegationExpression(MarkovChainModelParser.NegationExpressionContext ctx) {
        return ctx.arg.accept(this).not();
    }

    @Override
    public Value visitReferenceExpression(MarkovChainModelParser.ReferenceExpressionContext ctx) {
        return Value.getValue(types.apply(ctx.getText()), resolver.apply(ctx.getText()));
    }

    @Override
    public Value visitIntValue(MarkovChainModelParser.IntValueContext ctx) {
        return new Value.IntegerValue(Integer.parseInt(ctx.getText()));
    }

    @Override
    public Value visitTrueValue(MarkovChainModelParser.TrueValueContext ctx) {
        return Value.TRUE;
    }

    @Override
    public Value visitRelationExpression(MarkovChainModelParser.RelationExpressionContext ctx) {
        return Value.evalRelation(ctx.left.accept(this), ctx.op.getText(), ctx.right.accept(this));
    }

    @Override
    public Value visitBracketExpression(MarkovChainModelParser.BracketExpressionContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public Value visitOrExpression(MarkovChainModelParser.OrExpressionContext ctx) {
        return ctx.left.accept(this).or(ctx.right.accept(this));
    }

    @Override
    public Value visitIfThenElseExpression(MarkovChainModelParser.IfThenElseExpressionContext ctx) {
        return (ctx.guard.accept(this).getBooleanValue()?ctx.thenBranch.accept(this):ctx.elseBranch.accept(this));
    }

    @Override
    public Value visitFalseValue(MarkovChainModelParser.FalseValueContext ctx) {
        return Value.FALSE;
    }

    @Override
    public Value visitRealValue(MarkovChainModelParser.RealValueContext ctx) {
        return new Value.RealValue(Double.parseDouble(ctx.getText()));
    }

    @Override
    public Value visitAndExpression(MarkovChainModelParser.AndExpressionContext ctx) {
        return ctx.left.accept(this).and(ctx.right.accept(this));
    }

    @Override
    public Value visitMulDivExpression(MarkovChainModelParser.MulDivExpressionContext ctx) {
        return Value.apply(ctx.left.accept(this), ctx.op.getText(), ctx.right.accept(this));
    }

    @Override
    public Value visitAddSubExpression(MarkovChainModelParser.AddSubExpressionContext ctx) {
        return Value.apply(ctx.left.accept(this), ctx.op.getText(), ctx.right.accept(this));
    }

    @Override
    public Value visitCastToIntExpression(MarkovChainModelParser.CastToIntExpressionContext ctx) {
        return ctx.arg.accept(this).cast(DataType.INTEGER);
    }

    @Override
    public Value visitUnaryExpression(MarkovChainModelParser.UnaryExpressionContext ctx) {
        return Value.apply(ctx.op.getText(), ctx.arg.accept(this));
    }
}
