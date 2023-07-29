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

package it.unicam.quasylab.sibilla.langs.lio;

import it.unicam.quasylab.sibilla.core.util.values.SibillaBoolean;
import it.unicam.quasylab.sibilla.core.util.values.SibillaDouble;
import it.unicam.quasylab.sibilla.core.util.values.SibillaInteger;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import it.unicam.quasylab.sibilla.langs.util.ErrorCollector;
import it.unicam.quasylab.sibilla.langs.util.ParseError;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

/**
 * This visitor is used to evaluate expressions that depends on a parameter.
 *
 * @param <T> type of evaluation function.
 */
public class ParametricExpressionEvaluator<T> extends LIOModelBaseVisitor<Function<T,SibillaValue>> {


    private final Map<String, SibillaValue> evaluationContext;

    private final ErrorCollector errors;


    private final Function<String, Function<T, SibillaValue>> variableSolver;

    /**
     * Creates a new evaluator.
     *
     * @param errors collector used to store errors
     * @param variableSolver function used to obtain the value of a symbol from the parameter of the expression
     *                       evaluation
     * @param evaluationContext a mapping used to resolve constants and parameters values.
     */
    public ParametricExpressionEvaluator(ErrorCollector errors, Function<String, Function<T, SibillaValue>> variableSolver, Map<String, SibillaValue> evaluationContext) {
        this.variableSolver = variableSolver;
        this.evaluationContext = evaluationContext;
        this.errors = errors;
    }

    public static ParametricExpressionEvaluator<SibillaValue[]> getAgentDependentExpressionEvaluator(ErrorCollector errors, Map<String, Integer> varIndexes, Map<String, SibillaValue> evaluationContext) {
        Function<String, Function<SibillaValue[], SibillaValue>> resolver = str -> {
            Integer index = varIndexes.get(str);
            if (index != null) {
                return args -> args[index];
            } else {
                return null;
            }
        };
        return new ParametricExpressionEvaluator<>(errors, resolver, evaluationContext);
    }

    public static ParametricExpressionEvaluator<Map<String, SibillaValue>> getParametricExpressionEvaluator(ErrorCollector errors, Set<String> variables, Map<String, SibillaValue> evaluationContext) {
        return new ParametricExpressionEvaluator<>(errors, str -> (variables.contains(str)?m -> m.get(str):null), evaluationContext);
    }


    @Override
    public Function<T,SibillaValue> visitExpressionConjunction(LIOModelParser.ExpressionConjunctionContext ctx) {
        Function<T,SibillaValue> leftFunction = ctx.left.accept(this);
        Function<T,SibillaValue> rightFunction = ctx.right.accept(this);
        return s -> ((SibillaBoolean) leftFunction.apply(s)).and((SibillaBoolean) (rightFunction.apply(s)));
    }

    @Override
    public Function<T,SibillaValue> visitExpressionReference(LIOModelParser.ExpressionReferenceContext ctx) {
        Function<T, SibillaValue> valueSolver = variableSolver.apply(ctx.reference.getText());
        if (valueSolver != null) {
            return valueSolver;
        }
        SibillaValue v = evaluationContext.get(ctx.reference.getText());
        if (v != null) {
            return s -> v;
        }
        errors.record(new ParseError(ParseUtil.unknownSymbolError(ctx.reference), ctx.reference.getLine(), ctx.reference.getCharPositionInLine()));
        return s -> SibillaValue.ERROR_VALUE;
    }

    @Override
    public Function<T,SibillaValue> visitExpressionSumDiff(LIOModelParser.ExpressionSumDiffContext ctx) {
        Function<T,SibillaValue> leftFunction = ctx.left.accept(this);
        Function<T,SibillaValue> rightFunction = ctx.right.accept(this);
        if (ctx.op.getText().equals("+")) {
            return s -> SibillaValue.sum(leftFunction.apply(s), rightFunction.apply(s));
        }
        if (ctx.op.getText().equals("-")) {
            return s -> SibillaValue.sub(leftFunction.apply(s), rightFunction.apply(s));
        }
        return s -> SibillaValue.mod(leftFunction.apply(s), rightFunction.apply(s));
    }

    @Override
    public Function<T,SibillaValue> visitExpressionMulDiv(LIOModelParser.ExpressionMulDivContext ctx) {
        Function<T,SibillaValue> leftFunction = ctx.left.accept(this);
        Function<T,SibillaValue> rightFunction = ctx.right.accept(this);
        if (ctx.op.getText().equals("*")) {
            return s -> SibillaValue.mul(leftFunction.apply(s), rightFunction.apply(s));
        }
        if (ctx.op.getText().equals("/")) {
            return s -> SibillaValue.div(leftFunction.apply(s), rightFunction.apply(s));
        }
        return s -> SibillaValue.zeroDiv(leftFunction.apply(s), rightFunction.apply(s));
    }

    @Override
    public Function<T,SibillaValue> visitExpressionUnary(LIOModelParser.ExpressionUnaryContext ctx) {
        Function<T,SibillaValue> argFunction = ctx.arg.accept(this);
        if (ctx.op.getText().equals("-")) {
            return s -> SibillaValue.minus(argFunction.apply(s));
        } else {
            return argFunction;
        }
    }

    @Override
    public Function<T,SibillaValue> visitExpressionPower(LIOModelParser.ExpressionPowerContext ctx) {
        Function<T,SibillaValue> leftFunction = ctx.left.accept(this);
        Function<T,SibillaValue> rightFunction = ctx.right.accept(this);
        return s -> new SibillaDouble(Math.pow(leftFunction.apply(s).doubleOf(), rightFunction.apply(s).doubleOf()));
    }

    @Override
    public Function<T,SibillaValue> visitExpressionBracket(LIOModelParser.ExpressionBracketContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public Function<T,SibillaValue> visitExpressionDisjunction(LIOModelParser.ExpressionDisjunctionContext ctx) {
        Function<T,SibillaValue> leftFunction = ctx.left.accept(this);
        Function<T,SibillaValue> rightFunction = ctx.right.accept(this);
        return s -> ((SibillaBoolean) leftFunction.apply(s)).or((SibillaBoolean) (rightFunction.apply(s)));
    }

    @Override
    public Function<T,SibillaValue> visitExpressionIfThenElse(LIOModelParser.ExpressionIfThenElseContext ctx) {
        Function<T,SibillaValue> guardFunction = ctx.guard.accept(this);
        Function<T,SibillaValue> thenFunction = ctx.thenBranch.accept(this);
        Function<T,SibillaValue> elseFunction = ctx.elseBranch.accept(this);
        return s -> ((SibillaBoolean) guardFunction.apply(s)).ifThenElse(() -> thenFunction.apply(s), () -> elseFunction.apply(s));
    }

    @Override
    public Function<T,SibillaValue> visitExpressionRelation(LIOModelParser.ExpressionRelationContext ctx) {
        Function<T,SibillaValue> left = ctx.left.accept(this);
        Function<T,SibillaValue> right = ctx.right.accept(this);
        if (ctx.op.getText().equals(">")) {
            return s -> SibillaBoolean.of(left.apply(s).doubleOf()>right.apply(s).doubleOf());
        }
        if (ctx.op.getText().equals(">=")) {
            return s -> SibillaBoolean.of(left.apply(s).doubleOf()>=right.apply(s).doubleOf());
        }
        if (ctx.op.getText().equals("==")) {
            return s -> SibillaBoolean.of(left.apply(s).doubleOf()==right.apply(s).doubleOf());
        }
        if (ctx.op.getText().equals("!=")) {
            return s -> SibillaBoolean.of(left.apply(s).doubleOf()!=right.apply(s).doubleOf());
        }
        if (ctx.op.getText().equals("<")) {
            return s -> SibillaBoolean.of(left.apply(s).doubleOf()<right.apply(s).doubleOf());
        }
        return s -> SibillaBoolean.of(left.apply(s).doubleOf()<=right.apply(s).doubleOf());
    }


    @Override
    public Function<T,SibillaValue> visitExpressionTrue(LIOModelParser.ExpressionTrueContext ctx) {
        return s -> SibillaBoolean.TRUE;
    }

    @Override
    public Function<T,SibillaValue> visitExpressionNegation(LIOModelParser.ExpressionNegationContext ctx) {
        Function<T,SibillaValue> argumentFunction = ctx.arg.accept(this);
        return s -> ((SibillaBoolean) argumentFunction.apply(s)).not();
    }

    @Override
    public Function<T,SibillaValue> visitExpressionReal(LIOModelParser.ExpressionRealContext ctx) {
        SibillaDouble val = new SibillaDouble(Double.parseDouble(ctx.getText()));
        return s -> val;
    }

    @Override
    public Function<T,SibillaValue> visitExpressionFalse(LIOModelParser.ExpressionFalseContext ctx) {
        return s -> SibillaBoolean.FALSE;
    }

    @Override
    public Function<T,SibillaValue> visitExpressionFractionOfAgents(LIOModelParser.ExpressionFractionOfAgentsContext ctx) {
        errors.record(new ParseError(ParseUtil.illegalUseOfStateExpression(ctx.start),ctx.start.getLine(), ctx.start.getCharPositionInLine()));
        return s -> SibillaValue.ERROR_VALUE;
    }



    @Override
    public Function<T,SibillaValue> visitExpressionInteger(LIOModelParser.ExpressionIntegerContext ctx) {
        SibillaInteger val = new SibillaInteger(Integer.parseInt(ctx.getText()));
        return s -> val;
    }

    /**
     * Returns the predicate obtained by the evaluation of the given (boolean) expression.
     *
     * @param expr a boolean expression.
     * @return the predicate obtained by the evaluation of the given (boolean) expression.
     */
    public Predicate<T> predicateOf(LIOModelParser.ExprContext expr) {
        Function<T,SibillaValue> evalFunction = expr.accept(this);
        return s -> evalFunction.apply(s).booleanOf();
    }

    /**
     * Returns the double evaluation of the given expression.
     *
     * @param expr the expression to evaluate.
     * @return the double evaluation of the given expression.
     */
    public ToDoubleFunction<T> doubleFunctionOf(LIOModelParser.ExprContext expr) {
        Function<T,SibillaValue> evalFunction = expr.accept(this);
        return s -> evalFunction.apply(s).doubleOf();
    }

}
