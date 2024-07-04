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

package it.unicam.quasylab.sibilla.tools.stl;

import it.unicam.quasylab.sibilla.langs.slam.StlModelBaseVisitor;
import it.unicam.quasylab.sibilla.langs.slam.StlModelParser;

import java.util.Map;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

public class StlMonitorExpressionEvaluator<S> extends StlModelBaseVisitor<Function<Map<String,Double>,ToDoubleFunction<S>>> {

    private final Map<String, Double> constants;

    private final Map<String, ToDoubleFunction<S>> measures;

    public StlMonitorExpressionEvaluator(Map<String, Double> constants, Map<String, ToDoubleFunction<S>> measures) {
        this.constants = constants;
        this.measures = measures;
    }

    public StlMonitorExpressionEvaluator(Map<String, ToDoubleFunction<S>> measures) {
        this(Map.of(), measures);
    }


    private Function<Map<String,Double>,ToDoubleFunction<S>> apply(DoubleUnaryOperator op, Function<Map<String,Double>,ToDoubleFunction<S>> evaluationFunction) {
        return m -> {
            ToDoubleFunction<S> f = evaluationFunction.apply(m);
            return s -> op.applyAsDouble(f.applyAsDouble(s));
        };
    }

    private Function<Map<String,Double>,ToDoubleFunction<S>> apply(DoubleBinaryOperator op, Function<Map<String,Double>,ToDoubleFunction<S>> f, Function<Map<String,Double>,ToDoubleFunction<S>> g) {
        return m -> {
            ToDoubleFunction<S> f1 = f.apply(m);
            ToDoubleFunction<S> g1 = g.apply(m);
            return s -> op.applyAsDouble(f1.applyAsDouble(s), g1.applyAsDouble(s));
        };
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
    public Function<Map<String,Double>,ToDoubleFunction<S>> visitExpressionACos(StlModelParser.ExpressionACosContext ctx) {
        return apply(Math::acos, ctx.argument.accept(this));
    }


    @Override
    public Function<Map<String,Double>,ToDoubleFunction<S>> visitExpressionReference(StlModelParser.ExpressionReferenceContext ctx) {
        String name = ctx.reference.getText();
        if (constants.containsKey(name)) {
            double v = constants.get(name);
            return m -> (s -> v);
        }
        if (measures.containsKey(name)) {
            ToDoubleFunction<S> sToDoubleFunction = measures.get(name);
            return m -> sToDoubleFunction;
        }
        return m -> {
            double v = m.getOrDefault(name, Double.NaN);
            return s -> v;
        };
    }

    @Override
    public Function<Map<String,Double>,ToDoubleFunction<S>> visitExpressionAddSub(StlModelParser.ExpressionAddSubContext ctx) {
        return apply(getBinaryOperator(ctx.op.getText()), ctx.left.accept(this), ctx.right.accept(this));
    }

    @Override
    public Function<Map<String,Double>,ToDoubleFunction<S>> visitExpressionLog(StlModelParser.ExpressionLogContext ctx) {
        return apply(Math::log, ctx.argument.accept(this));
    }

    @Override
    public Function<Map<String,Double>,ToDoubleFunction<S>> visitExpressionCos(StlModelParser.ExpressionCosContext ctx) {
        return apply(Math::cos, ctx.argument.accept(this));
    }

    @Override
    public Function<Map<String,Double>,ToDoubleFunction<S>> visitExpressionFloor(StlModelParser.ExpressionFloorContext ctx) {
        return apply(Math::floor, ctx.argument.accept(this));
    }

    @Override
    public Function<Map<String,Double>,ToDoubleFunction<S>> visitExpressionMin(StlModelParser.ExpressionMinContext ctx) {
        return apply(Math::min, ctx.firstArgument.accept(this), ctx.secondArgument.accept(this));
    }

    @Override
    public Function<Map<String,Double>,ToDoubleFunction<S>> visitExpressionLog10(StlModelParser.ExpressionLog10Context ctx) {
        return apply(Math::log10, ctx.argument.accept(this));
    }

    @Override
    public Function<Map<String,Double>,ToDoubleFunction<S>> visitExpressionCosh(StlModelParser.ExpressionCoshContext ctx) {
        return apply(Math::cosh, ctx.argument.accept(this));
    }

    @Override
    public Function<Map<String,Double>,ToDoubleFunction<S>> visitExpressionInteger(StlModelParser.ExpressionIntegerContext ctx) {
        int i = Integer.parseInt(ctx.getText());
        return m -> (s -> i);
    }

    @Override
    public Function<Map<String,Double>,ToDoubleFunction<S>> visitExpressionUnaryOperator(StlModelParser.ExpressionUnaryOperatorContext ctx) {
        return apply(getUnaryOperator(ctx.op.getText()), ctx.arg.accept(this));
    }

    @Override
    public Function<Map<String,Double>,ToDoubleFunction<S>> visitExpressionCeil(StlModelParser.ExpressionCeilContext ctx) {
        return apply(Math::ceil, ctx.argument.accept(this));
    }

    @Override
    public Function<Map<String,Double>,ToDoubleFunction<S>> visitExpressionTan(StlModelParser.ExpressionTanContext ctx) {
        return apply(Math::tan, ctx.argument.accept(this));
    }

    @Override
    public Function<Map<String,Double>,ToDoubleFunction<S>> visitExpressionMulDiv(StlModelParser.ExpressionMulDivContext ctx) {
        return apply(getBinaryOperator(ctx.op.getText()), ctx.left.accept(this), ctx.right.accept(this));
    }

    @Override
    public Function<Map<String,Double>,ToDoubleFunction<S>> visitExpressionMax(StlModelParser.ExpressionMaxContext ctx) {
        return apply(Math::max, ctx.firstArgument.accept(this), ctx.secondArgument.accept(this));
    }

    @Override
    public Function<Map<String,Double>,ToDoubleFunction<S>> visitExpressionATan(StlModelParser.ExpressionATanContext ctx) {
        return apply(Math::atan, ctx.argument.accept(this));
    }

    @Override
    public Function<Map<String,Double>,ToDoubleFunction<S>> visitExpressionBracket(StlModelParser.ExpressionBracketContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public Function<Map<String,Double>,ToDoubleFunction<S>> visitExpressionSin(StlModelParser.ExpressionSinContext ctx) {
        return apply(Math::sin, ctx.argument.accept(this));
    }

    @Override
    public Function<Map<String,Double>,ToDoubleFunction<S>> visitExpressionPow(StlModelParser.ExpressionPowContext ctx) {
        return apply(Math::pow, ctx.left.accept(this), ctx.right.accept(this));
    }

    @Override
    public Function<Map<String,Double>,ToDoubleFunction<S>> visitExpressionExp(StlModelParser.ExpressionExpContext ctx) {
        return apply(Math::exp, ctx.argument.accept(this));
    }

    @Override
    public Function<Map<String,Double>,ToDoubleFunction<S>> visitExpressionSinh(StlModelParser.ExpressionSinhContext ctx) {
        return apply(Math::sinh, ctx.argument.accept(this));
    }

    @Override
    public Function<Map<String,Double>,ToDoubleFunction<S>> visitExpressionASin(StlModelParser.ExpressionASinContext ctx) {
        return apply(Math::asin, ctx.argument.accept(this));
    }

    @Override
    public Function<Map<String,Double>,ToDoubleFunction<S>> visitExpressionReal(StlModelParser.ExpressionRealContext ctx) {
        double v = Double.parseDouble(ctx.getText());
        return m -> (s -> v);
    }

    @Override
    public Function<Map<String,Double>,ToDoubleFunction<S>> visitExpressionATan2(StlModelParser.ExpressionATan2Context ctx) {
        return apply(Math::atan2, ctx.firstArgument.accept(this), ctx.secondArgument.accept(this));
    }

    @Override
    public Function<Map<String,Double>,ToDoubleFunction<S>> visitExpressionAbs(StlModelParser.ExpressionAbsContext ctx) {
        return apply(Math::abs, ctx.argument.accept(this));
    }

    @Override
    public Function<Map<String,Double>,ToDoubleFunction<S>> visitExpressionTanh(StlModelParser.ExpressionTanhContext ctx) {
        return apply(Math::tanh, ctx.argument.accept(this));
    }
}
