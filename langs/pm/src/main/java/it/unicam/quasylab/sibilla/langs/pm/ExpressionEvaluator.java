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

import java.util.function.Function;

/**
 * This visitor is used to evaluate expressions as double. Whenever an expression of the wrong
 * type is considered, a Double.NaN is returned.
 */
public class ExpressionEvaluator extends PopulationModelBaseVisitor<Double> {

    private final Function<String,Double> nameResolver;

    public ExpressionEvaluator(Function<String,Double> nameResolver) {
        this.nameResolver = nameResolver;
    }


    @Override
    public Double visitExponentExpression(PopulationModelParser.ExponentExpressionContext ctx) {
        return Math.pow(ctx.left.accept(this), ctx.right.accept(this));
    }

    @Override
    public Double visitReferenceExpression(PopulationModelParser.ReferenceExpressionContext ctx) {
        return nameResolver.apply(ctx.reference.getText());
    }

    @Override
    public Double visitIntValue(PopulationModelParser.IntValueContext ctx) {
        return Double.parseDouble(ctx.getText());
    }

    @Override
    public Double visitBracketExpression(PopulationModelParser.BracketExpressionContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public Double visitRealValue(PopulationModelParser.RealValueContext ctx) {
        return Double.parseDouble(ctx.getText());
    }

    @Override
    public Double visitIfThenElseExpression(PopulationModelParser.IfThenElseExpressionContext ctx) {
        return (ctx.guard.accept(getBooleanExpressionEvaluator())?ctx.thenBranch.accept(this):ctx.elseBranch.accept(this));
    }

    public BooleanExpressionEvaluator getBooleanExpressionEvaluator() {
        return new BooleanExpressionEvaluator(this);
    }

    @Override
    public Double visitMulDivExpression(PopulationModelParser.MulDivExpressionContext ctx) {
        return PopulationModelGenerator.getOperator(ctx.op.getText()).applyAsDouble(ctx.left.accept(this),ctx.right.accept(this));
    }


    @Override
    public Double visitAddSubExpression(PopulationModelParser.AddSubExpressionContext ctx) {
        return PopulationModelGenerator.getOperator(ctx.op.getText()).applyAsDouble(ctx.left.accept(this),ctx.right.accept(this));
    }

    @Override
    public Double visitUnaryExpression(PopulationModelParser.UnaryExpressionContext ctx) {
        if (ctx.op.getText().equals("-")) {
            return -ctx.arg.accept(this);
        } else {
            return ctx.arg.accept(this);
        }
    }

    public double evalDouble(PopulationModelParser.ExprContext expression) {
        return expression.accept(this);
    }

    public int evalInteger(PopulationModelParser.ExprContext expression) {
        return (int) evalDouble(expression);
    }

}
