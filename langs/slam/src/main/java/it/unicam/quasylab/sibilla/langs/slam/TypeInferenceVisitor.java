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

import it.unicam.quasylab.sibilla.core.models.slam.data.SlamType;
import it.unicam.quasylab.sibilla.langs.util.ParseError;
import org.antlr.v4.runtime.Token;

import java.util.List;

/**
 * This visitor is used to infer types of expressions.
 */
public class TypeInferenceVisitor extends SlamModelBaseVisitor<SlamType> {

    private final TypeSolver typeResolver;
    private final List<ParseError> errors;
    private final ExpressionContext context;

    private final SymbolTable table;

    /**
     * Creates a new visitor that is used to infer the type of expressions occurring in the
     * given context.
     *
     * @param context expresssion context
     * @param typeResolver function used to resolve types
     * @param errors list where errors are stored.
     */
    public TypeInferenceVisitor(ExpressionContext context,  SymbolTable table, TypeSolver typeResolver, List<ParseError> errors) {
        this.context = context;
        this.typeResolver = typeResolver;
        this.errors = errors;
        this.table = table;
    }

    public boolean checkType(SlamType expected, SlamModelParser.ExprContext expr) {
        SlamType actual = expr.accept(this);
        if (!expected.equals(actual)) {
            errors.add(ParseUtil.typeError(expected, actual, expr.start));
            return false;
        } else {
            return true;
        }
    }

    private SlamType checkAndReturn(SlamType expected, SlamModelParser.ExprContext expr) {
        return checkAndReturn(expected, expected, expr);
    }

    private SlamType checkAndReturn(SlamType result, SlamType expected, SlamModelParser.ExprContext expr) {
        SlamType actual = expr.accept(this);
        if (!expected.equals(actual)) {
            errors.add(ParseUtil.typeError(SlamType.REAL_TYPE, actual, expr.start));
        }
        return result;
    }

    @Override
    public SlamType visitExpressionACos(SlamModelParser.ExpressionACosContext ctx) {
        return checkAndReturn(SlamType.REAL_TYPE, SlamType.REAL_TYPE, ctx.argument);
    }

    @Override
    public SlamType visitExpressionReference(SlamModelParser.ExpressionReferenceContext ctx) {
        SlamType referenceType = typeResolver.typeOf(ctx.reference.getText());
        if (referenceType == SlamType.NONE_TYPE) {
            this.errors.add(ParseUtil.unknownSymbolError(ctx.reference));
        }
        return referenceType;
    }

    @Override
    public SlamType visitExpressionLog(SlamModelParser.ExpressionLogContext ctx) {
        return checkAndReturn(SlamType.REAL_TYPE, SlamType.REAL_TYPE, ctx.argument);
    }

    @Override
    public SlamType visitExpressionCos(SlamModelParser.ExpressionCosContext ctx) {
        return checkAndReturn(SlamType.REAL_TYPE, SlamType.REAL_TYPE, ctx.argument);
    }


    private SlamType validateAgentExpression(Token start, SlamModelParser.ExprContext expr) {
        switch (context) {
            case PREDICATE:
            case MEASURE:
            case AGENT_VIEW:
                SlamType type = expr.accept(this);
                if (type.isNumericType()) {
                    return type;
                } else {
                    this.errors.add(ParseUtil.illegalTypeInArithmeticExpressionError(type,expr.start));
                    return SlamType.NONE_TYPE;
                }
            default:
                this.errors.add(ParseUtil.illegalAgentExpressionError(start));
                return SlamType.NONE_TYPE;
        }
    }

    @Override
    public SlamType visitExpressionMaxAgents(SlamModelParser.ExpressionMaxAgentsContext ctx) {
        return validateAgentExpression(ctx.start, ctx.expr());
    }

    @Override
    public SlamType visitExpressionFloor(SlamModelParser.ExpressionFloorContext ctx) {
        return checkAndReturn(SlamType.REAL_TYPE, SlamType.REAL_TYPE, ctx.argument);
    }

    @Override
    public SlamType visitExpressionIfThenElse(SlamModelParser.ExpressionIfThenElseContext ctx) {
        checkType(SlamType.BOOLEAN_TYPE, ctx.guard);
        return checkAndReturn(ctx.thenBranch.accept(this), ctx.elseBranch);
    }

    @Override
    public SlamType visitExpressionForAllAgents(SlamModelParser.ExpressionForAllAgentsContext ctx) {
        return SlamType.BOOLEAN_TYPE;
    }

    @Override
    public SlamType visitExpressionRelation(SlamModelParser.ExpressionRelationContext ctx) {
        SlamType leftType = ctx.left.accept(this);
        if (!leftType.isComparable()) {
            errors.add(ParseUtil.incomparableTypeError(leftType,ctx.left.start));
        } else {
            checkType(leftType, ctx.right);
        }
        return SlamType.BOOLEAN_TYPE;
    }

    @Override
    public SlamType visitExpressionSamplingNormal(SlamModelParser.ExpressionSamplingNormalContext ctx) {
        checkType(SlamType.REAL_TYPE, ctx.mean);
        checkType(SlamType.REAL_TYPE, ctx.sigma);
        return SlamType.REAL_TYPE;
    }

    @Override
    public SlamType visitExpressionMin(SlamModelParser.ExpressionMinContext ctx) {
        SlamType typeOfFirstArgument = ctx.firstArgument.accept(this);
        if (typeOfFirstArgument.isComparable()) {
            errors.add(ParseUtil.incomparableTypeError(typeOfFirstArgument,ctx.firstArgument.start));
        }
        return checkAndReturn(typeOfFirstArgument, ctx.secondArgument);
    }

    @Override
    public SlamType visitExpressionAnd(SlamModelParser.ExpressionAndContext ctx) {
        return checkAndReturn(checkAndReturn(SlamType.BOOLEAN_TYPE, ctx.left), ctx.right);
    }

    @Override
    public SlamType visitExpressionCast(SlamModelParser.ExpressionCastContext ctx) {
        SlamType argumentType = ctx.expr().accept(this);
        SlamType resultType = SlamType.getTypeOf(ctx.type.getText());
        if (!argumentType.canCastTo(resultType)) {
            errors.add(ParseUtil.illegalCastError(argumentType, resultType, ctx.expr().start));
        }
        return resultType;
    }

    @Override
    public SlamType visitExpressionNow(SlamModelParser.ExpressionNowContext ctx) {
        if (context.timedExpressionAllowed()) {
            return SlamType.REAL_TYPE;
        } else {
            errors.add(ParseUtil.illegalUseOfTimedExpression(ctx.start));
            return SlamType.NONE_TYPE;
        }
    }

    @Override
    public SlamType visitExpressionLog10(SlamModelParser.ExpressionLog10Context ctx) {
        return checkAndReturn(SlamType.REAL_TYPE, ctx.argument);
    }

    @Override
    public SlamType visitExpressionCosh(SlamModelParser.ExpressionCoshContext ctx) {
        return checkAndReturn(SlamType.REAL_TYPE, ctx.argument);
    }

    @Override
    public SlamType visitExpressionInteger(SlamModelParser.ExpressionIntegerContext ctx) {
        return SlamType.INTEGER_TYPE;
    }

    @Override
    public SlamType visitExpressionUnaryOperator(SlamModelParser.ExpressionUnaryOperatorContext ctx) {
        SlamType argumentType = ctx.arg.accept(this);
        if (!argumentType.isNumericType()) {
            errors.add(ParseUtil.illegalTypeInArithmeticExpressionError(argumentType, ctx.arg.start));
            return SlamType.NONE_TYPE;
        } else {
            return argumentType;
        }
    }

    @Override
    public SlamType visitExpressionExistsAgent(SlamModelParser.ExpressionExistsAgentContext ctx) {
        return SlamType.BOOLEAN_TYPE;
    }

    @Override
    public SlamType visitExpressionCeil(SlamModelParser.ExpressionCeilContext ctx) {
        return checkAndReturn(SlamType.REAL_TYPE, ctx.argument);
    }

    @Override
    public SlamType visitExpressionMinAgents(SlamModelParser.ExpressionMinAgentsContext ctx) {
        return validateAgentExpression(ctx.start, ctx.expr());
    }

    @Override
    public SlamType visitExpressionMeanAgents(SlamModelParser.ExpressionMeanAgentsContext ctx) {
        return validateAgentExpression(ctx.start, ctx.expr());
    }

    @Override
    public SlamType visitExpressionTan(SlamModelParser.ExpressionTanContext ctx) {
        return checkAndReturn(SlamType.REAL_TYPE, ctx.argument);
    }

    @Override
    public SlamType visitExpressionMulDiv(SlamModelParser.ExpressionMulDivContext ctx) {
        SlamType leftType = ctx.left.accept(this);
        if (!leftType.isNumericType()) {
            errors.add(ParseUtil.illegalTypeInArithmeticExpressionError(leftType, ctx.left.start));
            return SlamType.NONE_TYPE;
        } else {
            return checkAndReturn(leftType, ctx.right);
        }
    }

    @Override
    public SlamType visitExpressionMax(SlamModelParser.ExpressionMaxContext ctx) {
        SlamType leftType = ctx.firstArgument.accept(this);
        if (!leftType.isNumericType()) {
            errors.add(ParseUtil.illegalTypeInArithmeticExpressionError(leftType, ctx.firstArgument.start));
            return SlamType.NONE_TYPE;
        } else {
            return checkAndReturn(leftType, ctx.secondArgument);
        }
    }

    @Override
    public SlamType visitExpressionSamplingUniform(SlamModelParser.ExpressionSamplingUniformContext ctx) {
        SlamType leftType = ctx.from.accept(this);
        if (!leftType.isNumericType()) {
            errors.add(ParseUtil.illegalTypeInArithmeticExpressionError(leftType, ctx.from.start));
            return SlamType.NONE_TYPE;
        } else {
            return checkAndReturn(leftType, ctx.to);
        }
    }

    @Override
    public SlamType visitExpressionATan(SlamModelParser.ExpressionATanContext ctx) {
        return checkAndReturn(SlamType.REAL_TYPE, ctx.argument);
    }

    @Override
    public SlamType visitExpressionBracket(SlamModelParser.ExpressionBracketContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public SlamType visitExpressionRandomValue(SlamModelParser.ExpressionRandomValueContext ctx) {
        return SlamType.REAL_TYPE;
    }

    @Override
    public SlamType visitExpressionDt(SlamModelParser.ExpressionDtContext ctx) {
        if (context == ExpressionContext.AGENT_TIME_UPDATE) {
            return SlamType.REAL_TYPE;
        } else {
            this.errors.add(ParseUtil.illegalExpressionError(ctx.start));
            return SlamType.NONE_TYPE;
        }
    }

    @Override
    public SlamType visitExpressionSin(SlamModelParser.ExpressionSinContext ctx) {
        return checkAndReturn(SlamType.REAL_TYPE, ctx.argument);
    }

    @Override
    public SlamType visitExpressionPow(SlamModelParser.ExpressionPowContext ctx) {
        return checkAndReturn(checkAndReturn(SlamType.REAL_TYPE, ctx.left), ctx.right);
    }

    @Override
    public SlamType visitExpressionSumAgents(SlamModelParser.ExpressionSumAgentsContext ctx) {
        return checkAndReturn(SlamType.REAL_TYPE, ctx.expr());
    }

    @Override
    public SlamType visitExpressionExp(SlamModelParser.ExpressionExpContext ctx) {
        return checkAndReturn(SlamType.REAL_TYPE, ctx.argument);
    }

    @Override
    public SlamType visitExpressionTrue(SlamModelParser.ExpressionTrueContext ctx) {
        return SlamType.BOOLEAN_TYPE;
    }

    @Override
    public SlamType visitExpressionSinh(SlamModelParser.ExpressionSinhContext ctx) {
        return checkAndReturn(SlamType.REAL_TYPE, ctx.argument);
    }

    @Override
    public SlamType visitExpressionASin(SlamModelParser.ExpressionASinContext ctx) {
        return checkAndReturn(SlamType.REAL_TYPE, ctx.argument);
    }

    @Override
    public SlamType visitExpressionNegation(SlamModelParser.ExpressionNegationContext ctx) {
        return checkAndReturn(SlamType.BOOLEAN_TYPE, ctx.arg);
    }

    @Override
    public SlamType visitExpressionReal(SlamModelParser.ExpressionRealContext ctx) {
        return SlamType.REAL_TYPE;
    }

    @Override
    public SlamType visitExpressionATan2(SlamModelParser.ExpressionATan2Context ctx) {
        return checkAndReturn(checkAndReturn(SlamType.REAL_TYPE, ctx.firstArgument), ctx.secondArgument);
    }

    @Override
    public SlamType visitExpressionFalse(SlamModelParser.ExpressionFalseContext ctx) {
        return SlamType.BOOLEAN_TYPE;
    }

    @Override
    public SlamType visitExpressionAbs(SlamModelParser.ExpressionAbsContext ctx) {
        SlamType argumentType = ctx.argument.accept(this);
        if (!argumentType.isNumericType()) {
            errors.add(ParseUtil.illegalTypeInArithmeticExpressionError(argumentType, ctx.argument.start));
            return SlamType.NONE_TYPE;
        } else {
            return argumentType;
        }
    }

    @Override
    public SlamType visitExpressionTanh(SlamModelParser.ExpressionTanhContext ctx) {
        return checkAndReturn(SlamType.REAL_TYPE, ctx.argument);
    }

    @Override
    public SlamType visitExpressionOr(SlamModelParser.ExpressionOrContext ctx) {
        return checkAndReturn(checkAndReturn(SlamType.BOOLEAN_TYPE, ctx.left), ctx.right);
    }

    @Override
    public SlamType visitExpressionAddSub(SlamModelParser.ExpressionAddSubContext ctx) {
        return checkAndReturn(checkAndReturn(SlamType.REAL_TYPE, ctx.left), ctx.right);
    }

}
