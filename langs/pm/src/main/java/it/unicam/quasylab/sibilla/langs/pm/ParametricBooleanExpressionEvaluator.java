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

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class ParametricBooleanExpressionEvaluator extends PopulationModelBaseVisitor<Predicate<double[]>> {

    private final ParametricExpressionEvaluator expressionEvaluator;

    public ParametricBooleanExpressionEvaluator(ParametricExpressionEvaluator expressionEvaluator) {
        this.expressionEvaluator = expressionEvaluator;
    }

    @Override
    public Predicate<double[]> visitNegationExpression(PopulationModelParser.NegationExpressionContext ctx) {
        return ctx.arg.accept(this).negate();
    }

    @Override
    public Predicate<double[]> visitOrExpression(PopulationModelParser.OrExpressionContext ctx) {
        return ctx.left.accept(this).or(ctx.right.accept(this));
    }

    @Override
    public Predicate<double[]> visitIfThenElseExpression(PopulationModelParser.IfThenElseExpressionContext ctx) {
        Predicate<double[]> guard = ctx.guard.accept(this);
        Predicate<double[]> thenBranch = ctx.thenBranch.accept(this);
        Predicate<double[]> elseBranch = ctx.elseBranch.accept(this);
        return d -> (guard.test(d)?thenBranch.test(d):elseBranch.test(d));
    }

    @Override
    public Predicate<double[]> visitFalseValue(PopulationModelParser.FalseValueContext ctx) {
        return d -> false;
    }

    @Override
    public Predicate<double[]> visitAndExpression(PopulationModelParser.AndExpressionContext ctx) {
        return ctx.left.accept(this).and(ctx.right.accept(this));
    }

    @Override
    public Predicate<double[]> visitRelationExpression(PopulationModelParser.RelationExpressionContext ctx) {
        BiFunction<Double, Double, Boolean> op = PopulationModelGenerator.getRelationOperator(ctx.op.getText());
        Function<double[], Double> left = ctx.left.accept(expressionEvaluator);
        Function<double[], Double> right = ctx.right.accept(expressionEvaluator);
        return d -> op.apply(left.apply(d),right.apply(d));
    }

    @Override
    public Predicate<double[]> visitTrueValue(PopulationModelParser.TrueValueContext ctx) {
        return d -> true;
    }


}
