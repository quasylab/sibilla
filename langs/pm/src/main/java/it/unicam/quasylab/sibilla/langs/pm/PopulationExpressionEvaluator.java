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
import it.unicam.quasylab.sibilla.core.util.values.SibillaBoolean;
import it.unicam.quasylab.sibilla.core.util.values.SibillaDouble;
import it.unicam.quasylab.sibilla.core.util.values.SibillaInteger;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;

public class PopulationExpressionEvaluator extends PopulationModelBaseVisitor<Function<PopulationState, SibillaValue>> {

    private final Function<String, Optional<SibillaValue>> resolver;
    private final PopulationRegistry registry;

    public PopulationExpressionEvaluator(Function<String, Optional<SibillaValue>> resolver, PopulationRegistry registry) {
        this.resolver = resolver;
        this.registry = registry;
    }

    @Override
    protected Function<PopulationState, SibillaValue> defaultResult() {
        return s -> SibillaValue.ERROR_VALUE;
    }

    @Override
    public Function<PopulationState, SibillaValue> visitExponentExpression(PopulationModelParser.ExponentExpressionContext ctx) {
        Function<PopulationState, SibillaValue> left = ctx.left.accept(this);
        Function<PopulationState, SibillaValue> right = ctx.right.accept(this);
        return s -> SibillaValue.eval(Math::pow, left.apply(s),right.apply(s));
    }

    @Override
    public Function<PopulationState, SibillaValue> visitIntValue(PopulationModelParser.IntValueContext ctx) {
        SibillaValue v = new SibillaInteger(Integer.parseInt(ctx.getText()));
        return s-> v;
    }

    @Override
    public Function<PopulationState, SibillaValue> visitBracketExpression(PopulationModelParser.BracketExpressionContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public Function<PopulationState, SibillaValue> visitPopulationFractionExpression(PopulationModelParser.PopulationFractionExpressionContext ctx) {
        int[] indexes = PopulationModelGenerator.getIndexes(resolver, registry, ctx.agent);
        return s -> new SibillaDouble(s.getFraction(indexes));
    }

    @Override
    public Function<PopulationState, SibillaValue> visitIfThenElseExpression(PopulationModelParser.IfThenElseExpressionContext ctx) {
        PopulationPredicateEvaluator predicateEvaluator = getPopulationPredicateEvaluator();
        Predicate<PopulationState> guard = ctx.guard.accept(predicateEvaluator);
        Function<PopulationState, SibillaValue> thenBranch = ctx.thenBranch.accept(this);
        Function<PopulationState, SibillaValue> elseBranch = ctx.elseBranch.accept(this);
        return s -> (guard.test(s)?thenBranch.apply(s):elseBranch.apply(s));
    }

    public PopulationPredicateEvaluator getPopulationPredicateEvaluator() {
        return new PopulationPredicateEvaluator(this);
    }

    @Override
    public Function<PopulationState, SibillaValue> visitRealValue(PopulationModelParser.RealValueContext ctx) {
        SibillaDouble val = new SibillaDouble(Double.parseDouble(ctx.getText()));
        return s -> val;
    }

    @Override
    public Function<PopulationState, SibillaValue> visitMulDivExpression(PopulationModelParser.MulDivExpressionContext ctx) {
        return evalBinaryExpression(ctx.left,ctx.op.getText(),ctx.right);
    }

    private Function<PopulationState, SibillaValue> evalBinaryExpression(PopulationModelParser.ExprContext left, String op, PopulationModelParser.ExprContext right) {
        Function<PopulationState, SibillaValue> leftEvaluationFunciton = left.accept(this);
        Function<PopulationState, SibillaValue> rightEvaluationFunction = right.accept(this);
        BinaryOperator<SibillaValue> semanticFunction = PopulationModelGenerator.getOperator(op);
        return s -> semanticFunction.apply(leftEvaluationFunciton.apply(s), rightEvaluationFunction.apply(s));
    }

    @Override
    public Function<PopulationState, SibillaValue> visitPopulationSizeExpression(PopulationModelParser.PopulationSizeExpressionContext ctx) {
        int[] indexes = PopulationModelGenerator.getIndexes(resolver, registry, ctx.agent);
        return s -> new SibillaDouble(s.getOccupancy(indexes));
    }

    @Override
    public Function<PopulationState, SibillaValue> visitAddSubExpression(PopulationModelParser.AddSubExpressionContext ctx) {
        return evalBinaryExpression(ctx.left,ctx.op.getText(),ctx.right);
    }

    @Override
    public Function<PopulationState, SibillaValue> visitUnaryExpression(PopulationModelParser.UnaryExpressionContext ctx) {
        Function<PopulationState, SibillaValue> arg = ctx.arg.accept(this);

        if (ctx.op.getText().equals("-")) {
            return s -> SibillaValue.minus(arg.apply(s));
        } else {
            return arg;
        }
    }
}
