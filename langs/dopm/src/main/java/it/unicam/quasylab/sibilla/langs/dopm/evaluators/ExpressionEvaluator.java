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

package it.unicam.quasylab.sibilla.langs.dopm.evaluators;

import it.unicam.quasylab.sibilla.core.util.values.SibillaBoolean;
import it.unicam.quasylab.sibilla.core.util.values.SibillaDouble;
import it.unicam.quasylab.sibilla.core.util.values.SibillaInteger;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelBaseVisitor;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelParser;

import java.util.Optional;
import java.util.function.Function;

/**
 * This visitor is used to evaluate expressions as double. Whenever an expression of the wrong
 * type is considered, a Double.NaN is returned.
 */
public class ExpressionEvaluator extends DataOrientedPopulationModelBaseVisitor<SibillaValue> {

    private final Function<String, Optional<SibillaValue>> nameResolver;

    public ExpressionEvaluator(Function<String, Optional<SibillaValue>> nameResolver) {
        this.nameResolver = nameResolver;
    }


    @Override
    public SibillaValue visitExponentExpression(DataOrientedPopulationModelParser.ExponentExpressionContext ctx) {
        return SibillaValue.eval(Math::pow, ctx.left.accept(this), ctx.right.accept(this));
    }

    @Override
    public SibillaValue visitReferenceExpression(DataOrientedPopulationModelParser.ReferenceExpressionContext ctx) {
        return nameResolver.apply(ctx.reference.getText()).orElse(SibillaValue.ERROR_VALUE);
    }

    @Override
    public SibillaValue visitSenderReferenceExpression(DataOrientedPopulationModelParser.SenderReferenceExpressionContext ctx) {
        return nameResolver.apply("sender."+ctx.reference.getText()).orElse(SibillaValue.ERROR_VALUE);
    }

    @Override
    public SibillaValue visitIntValue(DataOrientedPopulationModelParser.IntValueContext ctx) {
        return new SibillaInteger(Integer.parseInt(ctx.getText()));
    }

    @Override
    public SibillaValue visitBracketExpression(DataOrientedPopulationModelParser.BracketExpressionContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public SibillaValue visitRealValue(DataOrientedPopulationModelParser.RealValueContext ctx) {
        return new SibillaDouble(Double.parseDouble(ctx.getText()));
    }

    @Override
    public SibillaValue visitIfThenElseExpression(DataOrientedPopulationModelParser.IfThenElseExpressionContext ctx) {
        if (ctx.guard.accept(this).booleanOf()) {
            return ctx.thenBranch.accept(this);
        } else {
            return ctx.elseBranch.accept(this);
        }
    }

    @Override
    public SibillaValue visitNegationExpression(DataOrientedPopulationModelParser.NegationExpressionContext ctx) {
        return SibillaValue.not(ctx.arg.accept(this));
    }

    @Override
    public SibillaValue visitTrueValue(DataOrientedPopulationModelParser.TrueValueContext ctx) {
        return SibillaBoolean.TRUE;
    }

    @Override
    public SibillaValue visitRelationExpression(DataOrientedPopulationModelParser.RelationExpressionContext ctx) {
        return SibillaBoolean.of(SibillaValue.getRelationOperator(ctx.op.getText()).test(ctx.left.accept(this), ctx.right.accept(this)));
    }

    @Override
    public SibillaValue visitOrExpression(DataOrientedPopulationModelParser.OrExpressionContext ctx) {
        return SibillaValue.or(ctx.left.accept(this), ctx.right.accept(this));
    }

    @Override
    public SibillaValue visitFalseValue(DataOrientedPopulationModelParser.FalseValueContext ctx) {
        return SibillaBoolean.FALSE;
    }

    @Override
    public SibillaValue visitAndExpression(DataOrientedPopulationModelParser.AndExpressionContext ctx) {
        return SibillaValue.and(ctx.left.accept(this), ctx.right.accept(this));
    }

    @Override
    protected SibillaValue defaultResult() {
        return SibillaValue.ERROR_VALUE;
    }


    @Override
    public SibillaValue visitMulDivExpression(DataOrientedPopulationModelParser.MulDivExpressionContext ctx) {
        return SibillaValue.getOperator(ctx.op.getText()).apply(ctx.left.accept(this),ctx.right.accept(this));
    }

    @Override
    public SibillaValue visitAddSubExpression(DataOrientedPopulationModelParser.AddSubExpressionContext ctx) {
        return SibillaValue.getOperator(ctx.op.getText()).apply(ctx.left.accept(this),ctx.right.accept(this));
    }

    @Override
    public SibillaValue visitUnaryExpression(DataOrientedPopulationModelParser.UnaryExpressionContext ctx) {
        if (ctx.op.getText().equals("-")) {
            return SibillaValue.minus(ctx.arg.accept(this));
        } else {
            return ctx.arg.accept(this);
        }
    }

    public double evalDouble(DataOrientedPopulationModelParser.ExprContext expression) {
        return expression.accept(this).doubleOf();
    }

    public int evalInteger(DataOrientedPopulationModelParser.ExprContext expression) {
        return expression.accept(this).intOf();
    }

}
