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

import it.unicam.quasylab.sibilla.core.models.lio.Agent;
import it.unicam.quasylab.sibilla.core.models.lio.AgentName;
import it.unicam.quasylab.sibilla.core.models.lio.AgentsDefinition;
import it.unicam.quasylab.sibilla.core.util.values.SibillaBoolean;
import it.unicam.quasylab.sibilla.core.util.values.SibillaDouble;
import it.unicam.quasylab.sibilla.core.util.values.SibillaInteger;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import it.unicam.quasylab.sibilla.langs.util.ErrorCollector;
import it.unicam.quasylab.sibilla.langs.util.ParseError;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * This class provides utility methods for evaluating expressions in the different contexts.
 */
public class GlobalExpressionEvaluator extends LIOModelBaseVisitor<SibillaValue> {


    private final Map<String, SibillaValue> evaluationContext;

    private final ErrorCollector errors;

    public GlobalExpressionEvaluator(ErrorCollector errors, Map<String, SibillaValue> evaluationContext) {
        this.evaluationContext = evaluationContext;
        this.errors = errors;
    }


    @Override
    public SibillaValue visitExpressionConjunction(LIOModelParser.ExpressionConjunctionContext ctx) {
        SibillaValue leftValue = ctx.left.accept(this);
        SibillaValue rightValue = ctx.right.accept(this);
        return SibillaValue.and(leftValue, rightValue);
    }

    @Override
    public SibillaValue visitExpressionReference(LIOModelParser.ExpressionReferenceContext ctx) {
        SibillaValue v = evaluationContext.get(ctx.reference.getText());
        if (v == null) {
            errors.record(new ParseError(ParseUtil.unknownSymbolError(ctx.reference), ctx.reference.getLine(), ctx.reference.getCharPositionInLine()));
            return SibillaValue.ERROR_VALUE;
        }
        return v;
    }

    @Override
    public SibillaValue visitExpressionSumDiff(LIOModelParser.ExpressionSumDiffContext ctx) {
        SibillaValue leftValue = ctx.left.accept(this);
        SibillaValue rightValue = ctx.right.accept(this);
        if (ctx.op.getText().equals("+")) {
            return SibillaValue.sum(leftValue, rightValue);
        }
        if (ctx.op.getText().equals("-")) {
            return SibillaValue.sub(leftValue, rightValue);
        }
        return SibillaValue.mod(leftValue, rightValue);
    }

    @Override
    public SibillaValue visitExpressionMulDiv(LIOModelParser.ExpressionMulDivContext ctx) {
        SibillaValue leftValue = ctx.left.accept(this);
        SibillaValue rightValue = ctx.right.accept(this);
        if (ctx.op.getText().equals("*")) {
            return SibillaValue.mul(leftValue, rightValue);
        }
        if (ctx.op.getText().equals("/")) {
            return SibillaValue.div(leftValue, rightValue);
        }
        return SibillaValue.zeroDiv(leftValue, rightValue);
    }

    @Override
    public SibillaValue visitExpressionUnary(LIOModelParser.ExpressionUnaryContext ctx) {
        SibillaValue value = ctx.arg.accept(this);
        if (ctx.op.getText().equals("-")) {
            return SibillaValue.minus(value);
        } else {
            return value;
        }
    }

    @Override
    public SibillaValue visitExpressionPower(LIOModelParser.ExpressionPowerContext ctx) {
        SibillaValue leftValue = ctx.left.accept(this);
        SibillaValue rightValue = ctx.right.accept(this);
        return new SibillaDouble(Math.pow(leftValue.doubleOf(), rightValue.doubleOf()));
    }

    @Override
    public SibillaValue visitExpressionBracket(LIOModelParser.ExpressionBracketContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public SibillaValue visitExpressionDisjunction(LIOModelParser.ExpressionDisjunctionContext ctx) {
        SibillaValue leftValue = ctx.left.accept(this);
        SibillaValue rightValue = ctx.right.accept(this);
        return SibillaValue.or(leftValue, rightValue);
    }

    @Override
    public SibillaValue visitExpressionIfThenElse(LIOModelParser.ExpressionIfThenElseContext ctx) {
        SibillaValue guardValue = ctx.guard.accept(this);
        if (guardValue.booleanOf()) {
            return ctx.thenBranch.accept(this);
        } else {
            return ctx.elseBranch.accept(this);
        }
    }

    @Override
    public SibillaValue visitExpressionRelation(LIOModelParser.ExpressionRelationContext ctx) {
        SibillaValue left = ctx.left.accept(this);
        SibillaValue right = ctx.right.accept(this);
        if (ctx.op.getText().equals(">")) {
            return SibillaBoolean.of(left.doubleOf()>right.doubleOf());
        }
        if (ctx.op.getText().equals(">=")) {
            return SibillaBoolean.of(left.doubleOf()>=right.doubleOf());
        }
        if (ctx.op.getText().equals("==")) {
            return SibillaBoolean.of(left.doubleOf()==right.doubleOf());
        }
        if (ctx.op.getText().equals("!=")) {
            return SibillaBoolean.of(left.doubleOf()!=right.doubleOf());
        }
        if (ctx.op.getText().equals("<")) {
            return SibillaBoolean.of(left.doubleOf()<right.doubleOf());
        }
        return SibillaBoolean.of(left.doubleOf()<=right.doubleOf());
    }


    @Override
    public SibillaValue visitExpressionTrue(LIOModelParser.ExpressionTrueContext ctx) {
        return SibillaBoolean.TRUE;
    }

    @Override
    public SibillaValue visitExpressionNegation(LIOModelParser.ExpressionNegationContext ctx) {
        return SibillaValue.not(ctx.arg.accept(this));
    }

    @Override
    public SibillaValue visitExpressionReal(LIOModelParser.ExpressionRealContext ctx) {
        return new SibillaDouble(Double.parseDouble(ctx.getText()));
    }

    @Override
    public SibillaValue visitExpressionFalse(LIOModelParser.ExpressionFalseContext ctx) {
        return SibillaBoolean.FALSE;
    }

    @Override
    public SibillaValue visitExpressionFractionOfAgents(LIOModelParser.ExpressionFractionOfAgentsContext ctx) {
        errors.record(new ParseError(ParseUtil.illegalUseOfStateExpression(ctx.start), ctx.start.getLine(), ctx.start.getCharPositionInLine()));
        return SibillaValue.ERROR_VALUE;
    }


    @Override
    public SibillaValue visitExpressionInteger(LIOModelParser.ExpressionIntegerContext ctx) {
        return new SibillaInteger(Integer.parseInt(ctx.getText()));
    }


}
