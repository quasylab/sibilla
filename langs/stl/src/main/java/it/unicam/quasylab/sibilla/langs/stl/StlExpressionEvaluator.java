/*
 *  Sibilla:  a Java framework designed to support analysis of Collective
 *  Adaptive Systems.
 *
 *              Copyright (C) ${YEAR}.
 *
 *  See the NOTICE file distributed with this work for additional information
 *  regarding copyright ownership.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *    or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package it.unicam.quasylab.sibilla.langs.stl;

import it.unicam.quasylab.sibilla.langs.slam.StlModelBaseVisitor;
import it.unicam.quasylab.sibilla.langs.slam.StlModelParser;

import java.util.Map;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.function.ToDoubleFunction;

public class StlExpressionEvaluator extends StlModelBaseVisitor<ToDoubleFunction<Map<String,Double>>> {

    private final Map<String, Double> constants;

    public StlExpressionEvaluator(Map<String, Double> constants) {
        this.constants = constants;
    }

    public StlExpressionEvaluator() {
        this(Map.of());
    }


    private ToDoubleFunction<Map<String, Double>> apply(DoubleUnaryOperator op, ToDoubleFunction<Map<String, Double>> evaluationFunction) {
        return m -> op.applyAsDouble(evaluationFunction.applyAsDouble(m));
    }

    private ToDoubleFunction<Map<String, Double>> apply(DoubleBinaryOperator op, ToDoubleFunction<Map<String, Double>> f, ToDoubleFunction<Map<String, Double>> g) {
        return m -> op.applyAsDouble(f.applyAsDouble(m), g.applyAsDouble(m));
    }

    private DoubleBinaryOperator getBinaryOperator(String op) {
        return switch (op) {
            case "+" -> Double::sum;
            case "-" -> (x, y) -> x - y;
            case "*" -> (x, y) -> x * y;
            case "/" -> (x, y) -> x / y;
            case "%" -> (x, y) -> x % y;
            case "^" -> Math::pow;
            default -> (x, y) -> Double.NaN;
        };
    }

    private DoubleUnaryOperator getUnaryOperator(String op) {
        return switch (op) {
            case "+" -> x-> x;
            case "-" -> x -> -x;
            default -> x -> Double.NaN;
        };
    }


    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionACos(StlModelParser.ExpressionACosContext ctx) {
        return apply(Math::acos, ctx.argument.accept(this));
    }


    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionReference(StlModelParser.ExpressionReferenceContext ctx) {
        String name = ctx.reference.getText();
        if (constants.containsKey(name)) {
            double v = constants.get(name);
            return m -> v;
        }
        return m -> m.getOrDefault(name, Double.NaN);
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionAddSub(StlModelParser.ExpressionAddSubContext ctx) {
        return apply(getBinaryOperator(ctx.op.getText()), ctx.left.accept(this), ctx.right.accept(this));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionLog(StlModelParser.ExpressionLogContext ctx) {
        return apply(Math::log, ctx.argument.accept(this));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionCos(StlModelParser.ExpressionCosContext ctx) {
        return apply(Math::cos, ctx.argument.accept(this));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionFloor(StlModelParser.ExpressionFloorContext ctx) {
        return apply(Math::floor, ctx.argument.accept(this));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionMin(StlModelParser.ExpressionMinContext ctx) {
        return apply(Math::min, ctx.firstArgument.accept(this), ctx.secondArgument.accept(this));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionLog10(StlModelParser.ExpressionLog10Context ctx) {
        return apply(Math::log10, ctx.argument.accept(this));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionCosh(StlModelParser.ExpressionCoshContext ctx) {
        return apply(Math::cosh, ctx.argument.accept(this));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionInteger(StlModelParser.ExpressionIntegerContext ctx) {
        int i = Integer.parseInt(ctx.getText());
        return m -> i;
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionUnaryOperator(StlModelParser.ExpressionUnaryOperatorContext ctx) {
        return apply(getUnaryOperator(ctx.op.getText()), ctx.arg.accept(this));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionCeil(StlModelParser.ExpressionCeilContext ctx) {
        return apply(Math::ceil, ctx.argument.accept(this));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionTan(StlModelParser.ExpressionTanContext ctx) {
        return apply(Math::tan, ctx.argument.accept(this));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionMulDiv(StlModelParser.ExpressionMulDivContext ctx) {
        return apply(getBinaryOperator(ctx.op.getText()), ctx.left.accept(this), ctx.right.accept(this));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionMax(StlModelParser.ExpressionMaxContext ctx) {
        return apply(Math::max, ctx.firstArgument.accept(this), ctx.secondArgument.accept(this));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionATan(StlModelParser.ExpressionATanContext ctx) {
        return apply(Math::atan, ctx.argument.accept(this));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionBracket(StlModelParser.ExpressionBracketContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionSin(StlModelParser.ExpressionSinContext ctx) {
        return apply(Math::sin, ctx.argument.accept(this));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionPow(StlModelParser.ExpressionPowContext ctx) {
        return apply(Math::pow, ctx.left.accept(this), ctx.right.accept(this));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionExp(StlModelParser.ExpressionExpContext ctx) {
        return apply(Math::exp, ctx.argument.accept(this));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionSinh(StlModelParser.ExpressionSinhContext ctx) {
        return apply(Math::sinh, ctx.argument.accept(this));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionASin(StlModelParser.ExpressionASinContext ctx) {
        return apply(Math::asin, ctx.argument.accept(this));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionReal(StlModelParser.ExpressionRealContext ctx) {
        double v = Double.parseDouble(ctx.getText());
        return m -> v;
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionATan2(StlModelParser.ExpressionATan2Context ctx) {
        return apply(Math::atan2, ctx.firstArgument.accept(this), ctx.secondArgument.accept(this));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionAbs(StlModelParser.ExpressionAbsContext ctx) {
        return apply(Math::abs, ctx.argument.accept(this));
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> visitExpressionTanh(StlModelParser.ExpressionTanhContext ctx) {
        return apply(Math::tanh, ctx.argument.accept(this));
    }
}
