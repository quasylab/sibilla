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

package it.unicam.quasylab.sibilla.langs.slam;

import it.unicam.quasylab.sibilla.core.models.EvaluationEnvironment;
import it.unicam.quasylab.sibilla.core.models.slam.SlamType;
import it.unicam.quasylab.sibilla.core.models.slam.SlamValue;

import java.util.HashMap;
import java.util.Map;

/**
 * This visitor is used to evaluate an expression as a scalar. In this context, only parameters and constants can be used.
 */
public class ScalarExpressionEvaluator extends SlamModelBaseVisitor<SlamValue> {

    private final EvaluationEnvironment environment;
    private final Map<String, SlamValue> constants;

    public ScalarExpressionEvaluator(EvaluationEnvironment environment) {
        this(environment, new HashMap<>());
    }

    public ScalarExpressionEvaluator(EvaluationEnvironment environment, Map<String, SlamValue> consts) {
        this.environment = environment;
        this.constants = consts;
    }

    @Override
    public SlamValue visitExpressionACos(SlamModelParser.ExpressionACosContext ctx) {
        return ctx.accept(this).acos();
    }

    @Override
    public SlamValue visitExpressionReference(SlamModelParser.ExpressionReferenceContext ctx) {
        String name = ctx.reference.getText();
        if (constants.containsKey(name)) {
            return constants.get(name);
        }
        if (environment.isDefined(name)) {
            return SlamValue.REAL_VALUE.apply(environment.get(ctx.reference.getText()));
        }
        return SlamValue.NONE;
    }

    @Override
    public SlamValue visitExpressionLog(SlamModelParser.ExpressionLogContext ctx) {
        return ctx.argument.accept(this).log();
    }

    @Override
    public SlamValue visitExpressionCos(SlamModelParser.ExpressionCosContext ctx) {
        return ctx.argument.accept(this).cos();
    }

    @Override
    public SlamValue visitExpressionMaxAgents(SlamModelParser.ExpressionMaxAgentsContext ctx) {
        return SlamValue.NONE;
    }

    @Override
    public SlamValue visitExpressionFloor(SlamModelParser.ExpressionFloorContext ctx) {
        return ctx.argument.accept(this).floor();
    }

    @Override
    public SlamValue visitExpressionIfThenElse(SlamModelParser.ExpressionIfThenElseContext ctx) {
        return ctx.guard.accept(this).ifThenElse(() -> ctx.thenBranch.accept(this), () -> ctx.elseBranch.accept(this));
    }

    @Override
    public SlamValue visitExpressionForAllAgents(SlamModelParser.ExpressionForAllAgentsContext ctx) {
        return SlamValue.NONE;
    }

    @Override
    public SlamValue visitExpressionRelation(SlamModelParser.ExpressionRelationContext ctx) {
        SlamValue left = ctx.left.accept(this);
        SlamValue right = ctx.right.accept(this);
        switch (ctx.op.getText()) {
            case "<":   return left.lessThan(right);
            case "=<":
            case "<=":  return left.lessOrEqualThan(right);
            case "==":  return left.equalTo(right);
            case "!=":  return left.notEqualTo(right);
            case ">":   return left.greaterThan(right);
            case "=>":
            case ">=":  return left.greaterOrEqualThan(right);
            default: return SlamValue.NONE;
        }
    }

    @Override
    public SlamValue visitExpressionSamplingNormal(SlamModelParser.ExpressionSamplingNormalContext ctx) {
        return SlamValue.NONE;
    }

    @Override
    public SlamValue visitExpressionMin(SlamModelParser.ExpressionMinContext ctx) {
        return ctx.firstArgument.accept(this).min(ctx.secondArgument.accept(this));
    }

    @Override
    public SlamValue visitExpressionAnd(SlamModelParser.ExpressionAndContext ctx) {
        return ctx.left.accept(this).conjunction(ctx.right.accept(this));
    }

    @Override
    public SlamValue visitExpressionCast(SlamModelParser.ExpressionCastContext ctx) {
        return ctx.expr().accept(this).cast(SlamType.getTypeOf(ctx.type.getText()));
    }

    @Override
    public SlamValue visitExpressionNow(SlamModelParser.ExpressionNowContext ctx) {
        return SlamValue.NONE;
    }

    @Override
    public SlamValue visitExpressionLog10(SlamModelParser.ExpressionLog10Context ctx) {
        return ctx.argument.accept(this).log10();
    }

    @Override
    public SlamValue visitExpressionCosh(SlamModelParser.ExpressionCoshContext ctx) {
        return ctx.argument.accept(this).cosh();
    }

    @Override
    public SlamValue visitExpressionInteger(SlamModelParser.ExpressionIntegerContext ctx) {
        return SlamValue.INT_VALUE.apply(Integer.parseInt(ctx.getText()));
    }

    @Override
    public SlamValue visitExpressionUnaryOperator(SlamModelParser.ExpressionUnaryOperatorContext ctx) {
        switch (ctx.op.getText()) {
            case "+": return ctx.arg.accept(this).plus();
            case "-": return ctx.arg.accept(this).minus();
            default: return SlamValue.NONE;
        }
    }

    @Override
    public SlamValue visitExpressionExistsAgent(SlamModelParser.ExpressionExistsAgentContext ctx) {
        return SlamValue.NONE;
    }

    @Override
    public SlamValue visitExpressionCeil(SlamModelParser.ExpressionCeilContext ctx) {
        return ctx.argument.accept(this).ceil();
    }

    @Override
    public SlamValue visitExpressionMinAgents(SlamModelParser.ExpressionMinAgentsContext ctx) {
        return SlamValue.NONE;
    }

    @Override
    public SlamValue visitExpressionTan(SlamModelParser.ExpressionTanContext ctx) {
        return ctx.argument.accept(this).tan();
    }

    @Override
    public SlamValue visitExpressionMulDiv(SlamModelParser.ExpressionMulDivContext ctx) {
        SlamValue left = ctx.left.accept(this);
        SlamValue right = ctx.right.accept(this);
        switch (ctx.op.getText()) {
            case "*":   return left.mul(right);
            case "/":   return left.div(right);
            case "//":  return left.zeroDiv(right);
            default:    return SlamValue.NONE;
        }
    }

    @Override
    public SlamValue visitExpressionMax(SlamModelParser.ExpressionMaxContext ctx) {
        return ctx.firstArgument.accept(this).max(ctx.secondArgument.accept(this));
    }

    @Override
    public SlamValue visitExpressionSamplingUniform(SlamModelParser.ExpressionSamplingUniformContext ctx) {
        return SlamValue.NONE;
    }

    @Override
    public SlamValue visitExpressionATan(SlamModelParser.ExpressionATanContext ctx) {
        return ctx.argument.accept(this).atan();
    }

    @Override
    public SlamValue visitExpressionBracket(SlamModelParser.ExpressionBracketContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public SlamValue visitExpressionRandomValue(SlamModelParser.ExpressionRandomValueContext ctx) {
        return SlamValue.NONE;
    }

    @Override
    public SlamValue visitExpressionSin(SlamModelParser.ExpressionSinContext ctx) {
        return ctx.argument.accept(this).sin();
    }

    @Override
    public SlamValue visitExpressionPow(SlamModelParser.ExpressionPowContext ctx) {
        return ctx.left.accept(this).pow(ctx.right.accept(this));
    }

    @Override
    public SlamValue visitExpressionSumAgents(SlamModelParser.ExpressionSumAgentsContext ctx) {
        return SlamValue.NONE;
    }

    @Override
    public SlamValue visitExpressionExp(SlamModelParser.ExpressionExpContext ctx) {
        return ctx.argument.accept(this).exp();
    }

    @Override
    public SlamValue visitExpressionTrue(SlamModelParser.ExpressionTrueContext ctx) {
        return SlamValue.TRUE;
    }

    @Override
    public SlamValue visitExpressionSinh(SlamModelParser.ExpressionSinhContext ctx) {
        return ctx.argument.accept(this).sinh();
    }

    @Override
    public SlamValue visitExpressionASin(SlamModelParser.ExpressionASinContext ctx) {
        return ctx.argument.accept(this).asin();
    }

    @Override
    public SlamValue visitExpressionNegation(SlamModelParser.ExpressionNegationContext ctx) {
        return ctx.arg.accept(this).negation();
    }

    @Override
    public SlamValue visitExpressionReal(SlamModelParser.ExpressionRealContext ctx) {
        return SlamValue.REAL_VALUE.apply(Double.parseDouble(ctx.getText()));
    }

    @Override
    public SlamValue visitExpressionATan2(SlamModelParser.ExpressionATan2Context ctx) {
        return ctx.firstArgument.accept(this).atan2(ctx.secondArgument.accept(this));
    }

    @Override
    public SlamValue visitExpressionFalse(SlamModelParser.ExpressionFalseContext ctx) {
        return SlamValue.FALSE;
    }

    @Override
    public SlamValue visitExpressionAbs(SlamModelParser.ExpressionAbsContext ctx) {
        return ctx.argument.accept(this).abs();
    }

    @Override
    public SlamValue visitExpressionTanh(SlamModelParser.ExpressionTanhContext ctx) {
        return ctx.argument.accept(this).tanh();
    }

    @Override
    public SlamValue visitExpressionOr(SlamModelParser.ExpressionOrContext ctx) {
        return ctx.left.accept(this).disjunction(ctx.right.accept(this));
    }

    @Override
    public SlamValue visitExpressionAddSub(SlamModelParser.ExpressionAddSubContext ctx) {
        SlamValue left = ctx.left.accept(this);
        SlamValue right = ctx.right.accept(this);
        switch (ctx.op.getText()) {
            case "+": return left.sum(right);
            case "-": return left.sub(right);
            case "%": return left.mod(right);
            default: return SlamValue.NONE;
        }
    }

    @Override
    public SlamValue visitExpressionDt(SlamModelParser.ExpressionDtContext ctx) {
        return SlamValue.NONE;
    }
}
