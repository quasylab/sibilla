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

package it.unicam.quasylab.sibilla.langs.yoda;

import it.unicam.quasylab.sibilla.core.models.yoda.YodaType;
import it.unicam.quasylab.sibilla.langs.util.ErrorCollector;

import java.util.function.Function;

/**
 * This visitor class is used to infer type in expressions
 */
public class TypeInferenceVisitor extends YodaModelBaseVisitor<YodaType>{


    private final ErrorCollector errors;
    //private final ExpressionContext context;
    private final Function<String, YodaType> typeFunction;

    public TypeInferenceVisitor(ErrorCollector errors, Function<String, YodaType> typeFunction) {
        //this.context = context;
        this.errors = errors;
        this.typeFunction = typeFunction;
    }


    public boolean withErrors() {
        return errors.withErrors();
    }

    private YodaType checkType(YodaType expected, YodaModelParser.ExprContext ctx) {
        YodaType actual = ctx.accept(this);
        if (!expected.equals(actual)) {
            errors.record(ParseUtil.wrongTypeError(expected, actual, ctx));
        }
        return expected;
    }

    private YodaType checkAndReturn(YodaType expected, YodaModelParser.ExprContext ctx) {
        return checkAndReturn(expected, expected, ctx);
    }

    private YodaType checkAndReturn(YodaType result, YodaType expected, YodaModelParser.ExprContext ctx) {
        YodaType actual = ctx.accept(this);
        if (!expected.equals(actual)) {
            errors.record(ParseUtil.wrongTypeError(expected, actual, ctx ));
        }
        return result;
    }

    @Override
    public YodaType visitExpressionInteger(YodaModelParser.ExpressionIntegerContext ctx) {
        return YodaType.INTEGER_TYPE;
    }

    @Override
    public YodaType visitExpressionReal(YodaModelParser.ExpressionRealContext ctx) {
        return YodaType.REAL_TYPE;
    }

    @Override
    public YodaType visitExpressionFalse(YodaModelParser.ExpressionFalseContext ctx) {
        return YodaType.BOOLEAN_TYPE;
    }

    @Override
    public YodaType visitExpressionTrue(YodaModelParser.ExpressionTrueContext ctx) {
        return YodaType.BOOLEAN_TYPE;
    }

    @Override
    public YodaType visitExpressionReference(YodaModelParser.ExpressionReferenceContext ctx) {
        return ctx.ID().accept(this);
    }

    @Override
    public YodaType visitExpressionBrackets(YodaModelParser.ExpressionBracketsContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public YodaType visitExpressionUnary(YodaModelParser.ExpressionUnaryContext ctx) {
        YodaType actual = ctx.arg.accept(this);
        if (!actual.isNumericType()) {
            errors.record(ParseUtil.illegalTypeError(actual, ctx));
            return YodaType.NONE_TYPE;
        }
        return actual;
    }

    @Override
    public YodaType visitExpressionAddSubOperation(YodaModelParser.ExpressionAddSubOperationContext ctx) {
        YodaType leftType = ctx.leftOp.accept(this);
        if (!leftType.isNumericType()) {
            errors.record(ParseUtil.illegalTypeError(leftType, ctx));
            return YodaType.NONE_TYPE;
        } else {
            return checkAndReturn(leftType, ctx.rightOp);
        }
    }

    @Override
    public YodaType visitExpressionMultDivOperation(YodaModelParser.ExpressionMultDivOperationContext ctx) {
        YodaType leftType = ctx.leftOp.accept(this);
        if (!leftType.isNumericType()) {
            errors.record(ParseUtil.illegalTypeError(leftType, ctx));
            return YodaType.NONE_TYPE;
        } else {
            return checkAndReturn(leftType, ctx.rightOp);
        }
    }

    @Override
    public YodaType visitExpressionAdditionalOperation(YodaModelParser.ExpressionAdditionalOperationContext ctx) {
        YodaType leftType = ctx.leftOp.accept(this);
        if (!leftType.isNumericType()) {
            errors.record(ParseUtil.illegalTypeError(leftType, ctx));
            return YodaType.NONE_TYPE;
        } else {
            return checkAndReturn(leftType, ctx.rightOp);
        }
    }

    @Override
    public YodaType visitExpressionExponentOperation(YodaModelParser.ExpressionExponentOperationContext ctx) {
        YodaType leftType = ctx.leftOp.accept(this);
        if (!leftType.isNumericType()) {
            errors.record(ParseUtil.illegalTypeError(leftType, ctx));
            return YodaType.NONE_TYPE;
        } else {
            return YodaType.REAL_TYPE;
        }
    }

    @Override
    public YodaType visitExpressionNegation(YodaModelParser.ExpressionNegationContext ctx) {
        return checkAndReturn(YodaType.BOOLEAN_TYPE, ctx.argument);
    }

    @Override
    public YodaType visitExpressionSquareRoot(YodaModelParser.ExpressionSquareRootContext ctx) {
        YodaType argType = ctx.argument.accept(this);
        if (!argType.isNumericType()) {
            errors.record(ParseUtil.illegalTypeError(argType, ctx));
            return YodaType.NONE_TYPE;
        } else {
            return YodaType.REAL_TYPE;
        }
    }

    @Override
    public YodaType visitExpressionAnd(YodaModelParser.ExpressionAndContext ctx) {
        YodaType leftType = checkAndReturn(YodaType.BOOLEAN_TYPE, ctx.leftOp);
        return checkAndReturn(leftType, ctx.rightOp);
    }

    @Override
    public YodaType visitExpressionOr(YodaModelParser.ExpressionOrContext ctx) {
        YodaType leftType = checkAndReturn(YodaType.BOOLEAN_TYPE, ctx.leftOp);
        return checkAndReturn(leftType, ctx.rightOp);
    }

    //TODO
    @Override
    public YodaType visitExpressionRelation(YodaModelParser.ExpressionRelationContext ctx) {
        return super.visitExpressionRelation(ctx);
    }

    @Override
    public YodaType visitExpressionIfThenElse(YodaModelParser.ExpressionIfThenElseContext ctx) {
        checkType(YodaType.BOOLEAN_TYPE, ctx.guardExpr);
        return checkAndReturn(ctx.thenBranch.accept(this), ctx.elseBranch);
    }

    @Override
    public YodaType visitExpressionWeightedRandom(YodaModelParser.ExpressionWeightedRandomContext ctx) {
        YodaType minType = ctx.min.accept(this);
        if (!minType.isNumericType()) {
            errors.record(ParseUtil.illegalTypeError(minType,ctx));
            return YodaType.NONE_TYPE;
        } else {
            return checkAndReturn(minType, ctx.max);
        }    }

    @Override
    public YodaType visitExpressionRandom(YodaModelParser.ExpressionRandomContext ctx) {
        return YodaType.REAL_TYPE;
    }

    //TODO
    @Override
    public YodaType visitExpressionAttributeRef(YodaModelParser.ExpressionAttributeRefContext ctx) {
        return super.visitExpressionAttributeRef(ctx);
    }

    //TODO
    @Override
    public YodaType visitExpressionMinimum(YodaModelParser.ExpressionMinimumContext ctx) {
        return super.visitExpressionMinimum(ctx);
    }

    //TODO
    @Override
    public YodaType visitExpressionMaximum(YodaModelParser.ExpressionMaximumContext ctx) {
        return super.visitExpressionMaximum(ctx);
    }

    @Override
    public YodaType visitExpressionForAll(YodaModelParser.ExpressionForAllContext ctx) {
        return YodaType.BOOLEAN_TYPE;
    }

    @Override
    public YodaType visitExpressionExists(YodaModelParser.ExpressionExistsContext ctx) {
        return YodaType.BOOLEAN_TYPE;
    }

    @Override
    public YodaType visitExpressionItselfRef(YodaModelParser.ExpressionItselfRefContext ctx) {
        return ctx.ID().accept(this);
    }

    //TODO
    @Override
    public YodaType visitExpressionSin(YodaModelParser.ExpressionSinContext ctx) {
        return super.visitExpressionSin(ctx);
    }

    //TODO
    @Override
    public YodaType visitExpressionSinh(YodaModelParser.ExpressionSinhContext ctx) {
        return super.visitExpressionSinh(ctx);
    }

    //TODO
    @Override
    public YodaType visitExpressionAsin(YodaModelParser.ExpressionAsinContext ctx) {
        return super.visitExpressionAsin(ctx);
    }

    //TODO
    @Override
    public YodaType visitExpressionCos(YodaModelParser.ExpressionCosContext ctx) {
        return super.visitExpressionCos(ctx);
    }

    //TODO
    @Override
    public YodaType visitExpressionCosh(YodaModelParser.ExpressionCoshContext ctx) {
        return super.visitExpressionCosh(ctx);
    }

    //TODO
    @Override
    public YodaType visitExpressionAcos(YodaModelParser.ExpressionAcosContext ctx) {
        return super.visitExpressionAcos(ctx);
    }

    //TODO
    @Override
    public YodaType visitExpressionTan(YodaModelParser.ExpressionTanContext ctx) {
        return super.visitExpressionTan(ctx);
    }

    //TODO
    @Override
    public YodaType visitExpressionTanh(YodaModelParser.ExpressionTanhContext ctx) {
        return super.visitExpressionTanh(ctx);
    }

    //TODO
    @Override
    public YodaType visitExpressionAtan(YodaModelParser.ExpressionAtanContext ctx) {
        return super.visitExpressionAtan(ctx);
    }

    //TODO
    @Override
    public YodaType visitExpressionCeiling(YodaModelParser.ExpressionCeilingContext ctx) {
        return super.visitExpressionCeiling(ctx);
    }

    //TODO
    @Override
    public YodaType visitExpressionFloor(YodaModelParser.ExpressionFloorContext ctx) {
        return super.visitExpressionFloor(ctx);
    }

}
