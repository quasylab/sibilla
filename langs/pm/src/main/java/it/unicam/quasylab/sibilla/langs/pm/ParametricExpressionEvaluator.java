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

import java.util.Map;
import java.util.function.DoubleBinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * This visitor is used to evaluate expressions as double. Whenever an expression of the wrong
 * type is considered, a Double.NaN is returned.
 */
public class ParametricExpressionEvaluator extends PopulationModelBaseVisitor<Function<double[],Double>> {

    private final Function<String,Double> nameResolver;
    private final Map<String,Integer> variableIndexes;

    public ParametricExpressionEvaluator(Function<String,Double> nameResolver, Map<String,Integer> variableIndexes) {
        this.nameResolver = nameResolver;
        this.variableIndexes = variableIndexes;
    }


    @Override
    public Function<double[],Double> visitExponentExpression(PopulationModelParser.ExponentExpressionContext ctx) {
        Function<double[],Double> left = ctx.left.accept(this);
        Function<double[],Double> right = ctx.right.accept(this);
        return d -> Math.pow(left.apply(d), right.apply(d));
    }

    @Override
    public Function<double[],Double> visitReferenceExpression(PopulationModelParser.ReferenceExpressionContext ctx) {
        String name = ctx.reference.getText();
        if (variableIndexes.containsKey(name)) {
            int idx = variableIndexes.get(name);
            return d -> d[idx];
        } else {
            double v = nameResolver.apply(name);
            return d -> v;
        }
    }

    @Override
    public Function<double[],Double> visitIntValue(PopulationModelParser.IntValueContext ctx) {
        double v = Double.parseDouble(ctx.getText());
        return d -> v;
    }

    @Override
    public Function<double[],Double> visitBracketExpression(PopulationModelParser.BracketExpressionContext ctx) {
        return ctx.expr().accept(this);
    }


    @Override
    public Function<double[],Double> visitIfThenElseExpression(PopulationModelParser.IfThenElseExpressionContext ctx) {
        Predicate<double[]> guard = ctx.guard.accept(getBooleanExpressionEvaluator());
        Function<double[], Double> thenBranch = ctx.thenBranch.accept(this);
        Function<double[], Double> elseBranch = ctx.elseBranch.accept(this);
        return d -> (guard.test(d)?thenBranch.apply(d):elseBranch.apply(d));
    }

    public ParametricBooleanExpressionEvaluator getBooleanExpressionEvaluator() {
        return new ParametricBooleanExpressionEvaluator(this);
    }

    @Override
    public Function<double[],Double> visitMulDivExpression(PopulationModelParser.MulDivExpressionContext ctx) {
        return evalBinaryExpression(ctx.left, ctx.op.getText(), ctx.right);
    }

    private Function<double[],Double> evalBinaryExpression(PopulationModelParser.ExprContext e1, String op, PopulationModelParser.ExprContext e2) {
        Function<double[], Double> left = e1.accept(this);
        Function<double[], Double> right = e2.accept(this);
        DoubleBinaryOperator doubleOperator = PopulationModelGenerator.getOperator(op);
        return d -> doubleOperator.applyAsDouble(left.apply(d),right.apply(d));
    }



    @Override
    public Function<double[],Double> visitAddSubExpression(PopulationModelParser.AddSubExpressionContext ctx) {
        return evalBinaryExpression(ctx.left, ctx.op.getText(), ctx.right);
    }


    @Override
    public Function<double[],Double> visitUnaryExpression(PopulationModelParser.UnaryExpressionContext ctx) {
        if (ctx.op.getText().equals("-")) {
            Function<double[], Double> arg = ctx.arg.accept(this);
            return d -> -arg.apply(d);
        } else {
            return ctx.arg.accept(this);
        }
    }

}
