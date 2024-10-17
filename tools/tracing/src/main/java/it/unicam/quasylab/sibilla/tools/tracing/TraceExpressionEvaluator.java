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

package it.unicam.quasylab.sibilla.tools.tracing;

import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

import java.util.Iterator;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TraceExpressionEvaluator extends TracingSpecificationBaseVisitor<Function<Function<String, SibillaValue>,SibillaValue>> {

    @Override
    public Function<Function<String, SibillaValue>, SibillaValue> visitExpressionACos(TracingSpecificationParser.ExpressionACosContext ctx) {
        Function<Function<String, SibillaValue>, SibillaValue> arg = ctx.argument.accept(this);
        return f -> SibillaValue.apply(Math::acos, arg.apply(f));
    }

    @Override
    public Function<Function<String, SibillaValue>, SibillaValue> visitExpressionReference(TracingSpecificationParser.ExpressionReferenceContext ctx) {
        if (TracingConstants.isShape(ctx.reference.getText().toUpperCase())) {
            throw new RuntimeException(String.format("Illegal use of shape %s at line %d char %d!", ctx.reference.getText(), ctx.reference.getLine(), ctx.reference.getCharPositionInLine()));
        }
        if (TracingConstants.isColour(ctx.reference.getText().toUpperCase())) {
            throw new RuntimeException(String.format("Illegal use of colour %s at line %d char %d!", ctx.reference.getText(), ctx.reference.getLine(), ctx.reference.getCharPositionInLine()));
        }
        String name = ctx.reference.getText();
        return f -> f.apply(name);
    }

    @Override
    public Function<Function<String, SibillaValue>, SibillaValue> visitExpressionAddSub(TracingSpecificationParser.ExpressionAddSubContext ctx) {
        Function<Function<String, SibillaValue>, SibillaValue> left = ctx.left.accept(this);
        Function<Function<String, SibillaValue>, SibillaValue> right = ctx.right.accept(this);
        return f -> SibillaValue.apply(SibillaValue.getOperator(ctx.op.getText()), left.apply(f), right.apply(f));
    }

    @Override
    public Function<Function<String, SibillaValue>, SibillaValue> visitExpressionLog(TracingSpecificationParser.ExpressionLogContext ctx) {
        Function<Function<String, SibillaValue>, SibillaValue> arg = ctx.argument.accept(this);
        return f -> SibillaValue.apply(Math::log, arg.apply(f));
    }

    @Override
    public Function<Function<String, SibillaValue>, SibillaValue> visitExpressionRelop(TracingSpecificationParser.ExpressionRelopContext ctx) {
        Function<Function<String, SibillaValue>, SibillaValue> left = ctx.left.accept(this);
        Function<Function<String, SibillaValue>, SibillaValue> right = ctx.right.accept(this);
        BiPredicate<SibillaValue, SibillaValue> op = SibillaValue.getRelationOperator(ctx.op.getText());
        return f -> SibillaValue.of(op.test(left.apply(f), right.apply(f)));
    }



    @Override
    public Function<Function<String, SibillaValue>, SibillaValue> visitExpressionCos(TracingSpecificationParser.ExpressionCosContext ctx) {
        Function<Function<String, SibillaValue>, SibillaValue> arg = ctx.argument.accept(this);
        return f -> SibillaValue.apply(Math::cos, arg.apply(f));
    }

    @Override
    public Function<Function<String, SibillaValue>, SibillaValue> visitExpressionFloor(TracingSpecificationParser.ExpressionFloorContext ctx) {
        Function<Function<String, SibillaValue>, SibillaValue> arg = ctx.argument.accept(this);
        return f -> SibillaValue.apply(Math::floor, arg.apply(f));
    }

    @Override
    public Function<Function<String, SibillaValue>, SibillaValue> visitExpressionMin(TracingSpecificationParser.ExpressionMinContext ctx) {
        Function<Function<String, SibillaValue>, SibillaValue> firstArgument = ctx.firstArgument.accept(this);
        Function<Function<String, SibillaValue>, SibillaValue> secondArgument = ctx.secondArgument.accept(this);
        return f -> SibillaValue.min(firstArgument.apply(f), secondArgument.apply(f));
    }

    @Override
    public Function<Function<String, SibillaValue>, SibillaValue> visitExpressionLog10(TracingSpecificationParser.ExpressionLog10Context ctx) {
        Function<Function<String, SibillaValue>, SibillaValue> arg = ctx.argument.accept(this);
        return f -> SibillaValue.apply(Math::log10, arg.apply(f));
    }

    @Override
    public Function<Function<String, SibillaValue>, SibillaValue> visitExpressionCosh(TracingSpecificationParser.ExpressionCoshContext ctx) {
        Function<Function<String, SibillaValue>, SibillaValue> arg = ctx.argument.accept(this);
        return f -> SibillaValue.apply(Math::cosh, arg.apply(f));
    }

    @Override
    public Function<Function<String, SibillaValue>, SibillaValue> visitExpressionInteger(TracingSpecificationParser.ExpressionIntegerContext ctx) {
        SibillaValue value = SibillaValue.of(Integer.parseInt(ctx.getText()));
        return f -> value;
    }

    @Override
    public Function<Function<String, SibillaValue>, SibillaValue> visitExpressionUnaryOperator(TracingSpecificationParser.ExpressionUnaryOperatorContext ctx) {
        Function<Function<String, SibillaValue>, SibillaValue> arg = ctx.arg.accept(this);
        if (ctx.op.getText().equals("+")) {
            return arg;
        } else {
            return f -> SibillaValue.minus(arg.apply(f));
        }
    }

    @Override
    public Function<Function<String, SibillaValue>, SibillaValue> visitExpressionItReferemce(TracingSpecificationParser.ExpressionItReferemceContext ctx) {
        String name = ctx.reference.getText();
        return f -> f.apply(name);
    }

    @Override
    public Function<Function<String, SibillaValue>, SibillaValue> visitExpressionCeil(TracingSpecificationParser.ExpressionCeilContext ctx) {
        Function<Function<String, SibillaValue>, SibillaValue> arg = ctx.argument.accept(this);
        return f -> SibillaValue.apply(Math::ceil, arg.apply(f));
    }

    @Override
    public Function<Function<String, SibillaValue>, SibillaValue> visitExpressionTan(TracingSpecificationParser.ExpressionTanContext ctx) {
        Function<Function<String, SibillaValue>, SibillaValue> arg = ctx.argument.accept(this);
        return f -> SibillaValue.apply(Math::tan, arg.apply(f));
    }

    @Override
    public Function<Function<String, SibillaValue>, SibillaValue> visitExpressionMulDiv(TracingSpecificationParser.ExpressionMulDivContext ctx) {
        Function<Function<String, SibillaValue>, SibillaValue> left = ctx.left.accept(this);
        Function<Function<String, SibillaValue>, SibillaValue> right = ctx.right.accept(this);
        return f -> SibillaValue.apply(SibillaValue.getOperator(ctx.op.getText()), left.apply(f), right.apply(f));
    }

    @Override
    public Function<Function<String, SibillaValue>, SibillaValue> visitExpressionMax(TracingSpecificationParser.ExpressionMaxContext ctx) {
        Function<Function<String, SibillaValue>, SibillaValue> firstArgument = ctx.firstArgument.accept(this);
        Function<Function<String, SibillaValue>, SibillaValue> secondArgument = ctx.secondArgument.accept(this);
        return f -> SibillaValue.max(firstArgument.apply(f), secondArgument.apply(f));
    }

    @Override
    public Function<Function<String, SibillaValue>, SibillaValue> visitExpressionATan(TracingSpecificationParser.ExpressionATanContext ctx) {
        Function<Function<String, SibillaValue>, SibillaValue> arg = ctx.argument.accept(this);
        return f -> SibillaValue.apply(Math::atan, arg.apply(f));
    }

    @Override
    public Function<Function<String, SibillaValue>, SibillaValue> visitExpressionBracket(TracingSpecificationParser.ExpressionBracketContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public Function<Function<String, SibillaValue>, SibillaValue> visitExpressionSin(TracingSpecificationParser.ExpressionSinContext ctx) {
        Function<Function<String, SibillaValue>, SibillaValue> arg = ctx.argument.accept(this);
        return f -> SibillaValue.apply(Math::sin, arg.apply(f));
    }

    @Override
    public Function<Function<String, SibillaValue>, SibillaValue> visitExpressionPow(TracingSpecificationParser.ExpressionPowContext ctx) {
        Function<Function<String, SibillaValue>, SibillaValue> left = ctx.left.accept(this);
        Function<Function<String, SibillaValue>, SibillaValue> right = ctx.right.accept(this);
        return f -> SibillaValue.apply(Math::pow, left.apply(f), right.apply(f));
    }

    @Override
    public Function<Function<String, SibillaValue>, SibillaValue> visitExpressionExp(TracingSpecificationParser.ExpressionExpContext ctx) {
        Function<Function<String, SibillaValue>, SibillaValue> arg = ctx.argument.accept(this);
        return f -> SibillaValue.apply(Math::exp, arg.apply(f));
    }

    @Override
    public Function<Function<String, SibillaValue>, SibillaValue> visitExpressionSinh(TracingSpecificationParser.ExpressionSinhContext ctx) {
        Function<Function<String, SibillaValue>, SibillaValue> arg = ctx.argument.accept(this);
        return f -> SibillaValue.apply(Math::sinh, arg.apply(f));
    }

    @Override
    public Function<Function<String, SibillaValue>, SibillaValue> visitExpressionASin(TracingSpecificationParser.ExpressionASinContext ctx) {
        Function<Function<String, SibillaValue>, SibillaValue> arg = ctx.argument.accept(this);
        return f -> SibillaValue.apply(Math::asin, arg.apply(f));
    }

    @Override
    public Function<Function<String, SibillaValue>, SibillaValue> visitExpressionReal(TracingSpecificationParser.ExpressionRealContext ctx) {
        SibillaValue value = SibillaValue.of(Double.parseDouble(ctx.getText()));
        return f -> value;
    }

    @Override
    public Function<Function<String, SibillaValue>, SibillaValue> visitExpressionATan2(TracingSpecificationParser.ExpressionATan2Context ctx) {
        Function<Function<String, SibillaValue>, SibillaValue> left = ctx.firstArgument.accept(this);
        Function<Function<String, SibillaValue>, SibillaValue> right = ctx.secondArgument.accept(this);
        return f -> SibillaValue.apply(Math::atan2, left.apply(f), right.apply(f));
    }

    @Override
    public Function<Function<String, SibillaValue>, SibillaValue> visitExpressionAbs(TracingSpecificationParser.ExpressionAbsContext ctx) {
        Function<Function<String, SibillaValue>, SibillaValue> arg = ctx.argument.accept(this);
        return f -> SibillaValue.apply(Math::abs, arg.apply(f));
    }

    @Override
    public Function<Function<String, SibillaValue>, SibillaValue> visitExpressionTanh(TracingSpecificationParser.ExpressionTanhContext ctx) {
        Function<Function<String, SibillaValue>, SibillaValue> arg = ctx.argument.accept(this);
        return f -> SibillaValue.apply(Math::tanh, arg.apply(f));
    }

    @Override
    public Function<Function<String, SibillaValue>, SibillaValue> visitExpressionPi(TracingSpecificationParser.ExpressionPiContext ctx) {
        SibillaValue pi = SibillaValue.of(Math.PI);
        return f -> pi;
    }

    @Override
    public Function<Function<String, SibillaValue>, SibillaValue> visitWhenBlock(TracingSpecificationParser.WhenBlockContext ctx) {
        List<Function<Function<String, SibillaValue>, SibillaValue>> values = ctx.values.stream().map(e -> e.accept(this)).toList();
        List<Function<Function<String, SibillaValue>, SibillaValue>> guards = ctx.guards.stream().map(g -> g.accept(this)).toList();
        Function<Function<String, SibillaValue>, SibillaValue> defaultValue =  ctx.default_.accept(this);
        return f -> {
            Iterator<Function<Function<String, SibillaValue>, SibillaValue>> valuesIterator = values.iterator();
            for (Function<Function<String, SibillaValue>, SibillaValue> guard : guards) {
                Function<Function<String, SibillaValue>, SibillaValue> value = valuesIterator.next();
                if (guard.apply(f).booleanOf()) {
                    return value.apply(f);
                }
            }
            return defaultValue.apply(f);
        };
    }
}
