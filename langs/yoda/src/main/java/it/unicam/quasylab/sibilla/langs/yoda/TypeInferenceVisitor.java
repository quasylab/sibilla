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

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * This visitor class is used to infer type in expressions
 */
public class TypeInferenceVisitor extends YodaModelBaseVisitor<YodaType>{


    private final ErrorCollector errors;
    //private final ExpressionContext context;
    private final Function<String, YodaType> typeFunction;

    private final BiFunction<String, String, YodaType> agentAttributeTypes;

    private final Function<String, Function<String, YodaType>> agentsEnvironment;

    public TypeInferenceVisitor(ErrorCollector errors, Function<String, YodaType> typeFunction, BiFunction<String, String, YodaType> agetAttributeTypes) {
        this(errors, typeFunction, agetAttributeTypes, g -> (a -> YodaType.NONE_TYPE));
    }

    public TypeInferenceVisitor(ErrorCollector errors, Function<String, YodaType> typeFunction, BiFunction<String, String, YodaType> agetAttributeTypes, Function<String, Function<String, YodaType>> agentsEnvironment) {
        //this.context = context;
        this.errors = errors;
        this.typeFunction = typeFunction;
        this.agentAttributeTypes = agetAttributeTypes;
        this.agentsEnvironment = agentsEnvironment;
    }

    public TypeInferenceVisitor(ErrorCollector errors, Function<String, YodaType> typeFunction) {
        this(errors, typeFunction, (ag, attr) -> YodaType.NONE_TYPE);
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
        YodaType actual = ctx.accept(this);
        if (actual.canBeAssignedTo(expected)) {
            return expected;
        } else {
            errors.record(ParseUtil.wrongTypeError(expected, actual, ctx ));
            return YodaType.NONE_TYPE;
        }
    }

    private YodaType checkAndMerge(YodaType aType, YodaModelParser.ExprContext ctx) {
        YodaType otherType = ctx.accept(this);
        if (YodaType.areCompatible(aType, otherType)) {
            return YodaType.merge(aType, otherType);
        } else {
            errors.record(ParseUtil.wrongTypeError(aType, otherType, ctx ));
            return YodaType.NONE_TYPE;
        }
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
        YodaType type = typeFunction.apply(ctx.getText());
        if (type == YodaType.NONE_TYPE) {
            this.errors.record(ParseUtil.unknownSymbolError(ctx.getText(), ctx));
        }
        return type;
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

    private YodaType combineNumericBinaryOperators(YodaModelParser.ExprContext left, YodaModelParser.ExprContext right) {
        YodaType leftType = left.accept(this);
        if (!leftType.isNumericType()) {
            errors.record(ParseUtil.illegalTypeError(leftType, left));
            return YodaType.NONE_TYPE;
        } else {
            return checkAndMerge(leftType, right);
        }

    }

    @Override
    public YodaType visitExpressionAddSubOperation(YodaModelParser.ExpressionAddSubOperationContext ctx) {
        return combineNumericBinaryOperators(ctx.leftOp, ctx.rightOp);
    }

    @Override
    public YodaType visitExpressionMultDivOperation(YodaModelParser.ExpressionMultDivOperationContext ctx) {
        return combineNumericBinaryOperators(ctx.leftOp, ctx.rightOp);
    }

    @Override
    public YodaType visitExpressionAdditionalOperation(YodaModelParser.ExpressionAdditionalOperationContext ctx) {
        return combineNumericBinaryOperators(ctx.leftOp, ctx.rightOp);
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

    @Override
    public YodaType visitExpressionRelation(YodaModelParser.ExpressionRelationContext ctx) {
        if (checkNumericType(ctx.leftOp) & checkNumericType(ctx.rightOp)) {
            return YodaType.BOOLEAN_TYPE;
        }
        return YodaType.NONE_TYPE;
    }

    private boolean checkNumericType(YodaModelParser.ExprContext expr) {
        YodaType type = expr.accept(this);
        if (!type.isNumericType()) {
            errors.record(ParseUtil.expectedNumberError(type, expr));
            return false;
        }
        return true;
    }

    @Override
    public YodaType visitExpressionIfThenElse(YodaModelParser.ExpressionIfThenElseContext ctx) {
        checkType(YodaType.BOOLEAN_TYPE, ctx.guardExpr);
        return checkAndReturn(ctx.thenBranch.accept(this), ctx.elseBranch);
    }

    @Override
    public YodaType visitExpressionWeightedRandom(YodaModelParser.ExpressionWeightedRandomContext ctx) {
        return combineNumericBinaryOperators(ctx.min, ctx.max);
    }

    @Override
    public YodaType visitExpressionRandom(YodaModelParser.ExpressionRandomContext ctx) {
        return YodaType.REAL_TYPE;
    }

    @Override
    public YodaType visitExpressionAttributeRef(YodaModelParser.ExpressionAttributeRefContext ctx) {
        YodaType type = agentAttributeTypes.apply(ctx.parent.getText(), ctx.son.getText());
        if (type == YodaType.NONE_TYPE) {
            this.errors.record(ParseUtil.unknownSymbolError(ctx.getText(), ctx));
        }
        return type;
    }

    //TODO
    @Override
    public YodaType visitExpressionMinimum(YodaModelParser.ExpressionMinimumContext ctx) {
        return ctx.groupExpression().accept(this);
    }

    @Override
    public YodaType visitGroupExpression(YodaModelParser.GroupExpressionContext ctx) {
        Function<String, YodaType> groupAttributes = agentsEnvironment.apply(ctx.groupName.getText());
        if (groupAttributes == null) {
            errors.record(ParseUtil.unknownSymbolError(ctx.groupName.getText(), ctx));
            return YodaType.NONE_TYPE;
        }
        String varName = ctx.name.getText();
        TypeInferenceVisitor tiv = new TypeInferenceVisitor(errors, typeFunction, (n, a) -> (varName.equals(n)?groupAttributes.apply(a):agentAttributeTypes.apply(n, a)), agentsEnvironment);
        boolean flag = true;
        if (ctx.guard != null) {
            flag = (tiv.checkAndReturn(YodaType.BOOLEAN_TYPE, ctx.guard) == YodaType.BOOLEAN_TYPE);
        }
        YodaType valueType = ctx.value.accept(tiv);
        if (!valueType.isNumericType()) {
            errors.record(ParseUtil.illegalTypeError(valueType, ctx.value));
            flag = false;
        }
        if (flag) {
            return valueType;
        } else {
            return YodaType.NONE_TYPE;
        }
    }

    //TODO
    @Override
    public YodaType visitExpressionMaximum(YodaModelParser.ExpressionMaximumContext ctx) {
        return ctx.groupExpression().accept(this);
    }

    @Override
    public YodaType visitExpressionForAll(YodaModelParser.ExpressionForAllContext ctx) {
        YodaModelParser.GroupExpressionContext groupExpression = ctx.groupExpression();
        Function<String, YodaType> groupAttributes = agentsEnvironment.apply(groupExpression.groupName.getText());
        if (groupAttributes == null) {
            errors.record(ParseUtil.unknownSymbolError(groupExpression.groupName.getText(), ctx));
            return YodaType.NONE_TYPE;
        }
        String varName = groupExpression.name.getText();
        TypeInferenceVisitor tiv = new TypeInferenceVisitor(errors, typeFunction, (n, a) -> (varName.equals(n)?groupAttributes.apply(a):agentAttributeTypes.apply(n, a)), agentsEnvironment);
        boolean flag = true;
        if (groupExpression.guard != null) {
            flag = (tiv.checkAndReturn(YodaType.BOOLEAN_TYPE, groupExpression.guard) == YodaType.BOOLEAN_TYPE);
        }
        flag &= (tiv.checkAndReturn(YodaType.BOOLEAN_TYPE, groupExpression.value) == YodaType.BOOLEAN_TYPE);
        if (flag) {
            return YodaType.BOOLEAN_TYPE;
        } else {
            return YodaType.NONE_TYPE;
        }
    }

    @Override
    public YodaType visitExpressionExists(YodaModelParser.ExpressionExistsContext ctx) {
        YodaModelParser.GroupExpressionContext groupExpression = ctx.groupExpression();
        Function<String, YodaType> groupAttributes = agentsEnvironment.apply(groupExpression.groupName.getText());
        if (groupAttributes == null) {
            errors.record(ParseUtil.unknownSymbolError(groupExpression.groupName.getText(), ctx));
            return YodaType.NONE_TYPE;
        }
        String varName = groupExpression.name.getText();
        TypeInferenceVisitor tiv = new TypeInferenceVisitor(errors, typeFunction, (n, a) -> (varName.equals(n)?groupAttributes.apply(a):agentAttributeTypes.apply(n, a)), agentsEnvironment);
        boolean flag = true;
        if (groupExpression.guard != null) {
            flag = (tiv.checkAndReturn(YodaType.BOOLEAN_TYPE, groupExpression.guard) == YodaType.BOOLEAN_TYPE);
        }
        flag &= (tiv.checkAndReturn(YodaType.BOOLEAN_TYPE, groupExpression.value) == YodaType.BOOLEAN_TYPE);
        if (flag) {
            return YodaType.BOOLEAN_TYPE;
        } else {
            return YodaType.NONE_TYPE;
        }
    }

    //TODO
    @Override
    public YodaType visitExpressionItselfRef(YodaModelParser.ExpressionItselfRefContext ctx) {
        return agentAttributeTypes.apply("it", ctx.ref.getText());
    }

    @Override
    public YodaType visitExpressionSin(YodaModelParser.ExpressionSinContext ctx) {
        YodaType argType = ctx.argument.accept(this);
        if (!argType.isNumericType()) {
            errors.record(ParseUtil.illegalTypeError(argType, ctx));
            return YodaType.NONE_TYPE;
        }
        return YodaType.REAL_TYPE;
    }

    //TODO
    @Override
    public YodaType visitExpressionSinh(YodaModelParser.ExpressionSinhContext ctx) {
        YodaType argType = ctx.argument.accept(this);
        if (!argType.isNumericType()) {
            errors.record(ParseUtil.illegalTypeError(argType, ctx));
            return YodaType.NONE_TYPE;
        }
        return YodaType.REAL_TYPE;
    }

    //TODO
    @Override
    public YodaType visitExpressionAsin(YodaModelParser.ExpressionAsinContext ctx) {
        YodaType argType = ctx.argument.accept(this);
        if (!argType.isNumericType()) {
            errors.record(ParseUtil.illegalTypeError(argType, ctx));
            return YodaType.NONE_TYPE;
        }
        return YodaType.REAL_TYPE;
    }

    //TODO
    @Override
    public YodaType visitExpressionCos(YodaModelParser.ExpressionCosContext ctx) {
        YodaType argType = ctx.argument.accept(this);
        if (!argType.isNumericType()) {
            errors.record(ParseUtil.illegalTypeError(argType, ctx));
            return YodaType.NONE_TYPE;
        }
        return YodaType.REAL_TYPE;
    }

    //TODO
    @Override
    public YodaType visitExpressionCosh(YodaModelParser.ExpressionCoshContext ctx) {
        YodaType argType = ctx.argument.accept(this);
        if (!argType.isNumericType()) {
            errors.record(ParseUtil.illegalTypeError(argType, ctx));
            return YodaType.NONE_TYPE;
        }
        return YodaType.REAL_TYPE;
    }

    //TODO
    @Override
    public YodaType visitExpressionAcos(YodaModelParser.ExpressionAcosContext ctx) {
        YodaType argType = ctx.argument.accept(this);
        if (!argType.isNumericType()) {
            errors.record(ParseUtil.illegalTypeError(argType, ctx));
            return YodaType.NONE_TYPE;
        }
        return YodaType.REAL_TYPE;
    }

    //TODO
    @Override
    public YodaType visitExpressionTan(YodaModelParser.ExpressionTanContext ctx) {
        YodaType argType = ctx.argument.accept(this);
        if (!argType.isNumericType()) {
            errors.record(ParseUtil.illegalTypeError(argType, ctx));
            return YodaType.NONE_TYPE;
        }
        return YodaType.REAL_TYPE;
    }

    //TODO
    @Override
    public YodaType visitExpressionTanh(YodaModelParser.ExpressionTanhContext ctx) {
        YodaType argType = ctx.argument.accept(this);
        if (!argType.isNumericType()) {
            errors.record(ParseUtil.illegalTypeError(argType, ctx));
            return YodaType.NONE_TYPE;
        }
        return YodaType.REAL_TYPE;
    }

    //TODO
    @Override
    public YodaType visitExpressionAtan(YodaModelParser.ExpressionAtanContext ctx) {
        YodaType argType = ctx.argument.accept(this);
        if (!argType.isNumericType()) {
            errors.record(ParseUtil.illegalTypeError(argType, ctx));
            return YodaType.NONE_TYPE;
        }
        return YodaType.REAL_TYPE;
    }

    //TODO
    @Override
    public YodaType visitExpressionCeiling(YodaModelParser.ExpressionCeilingContext ctx) {
        YodaType argType = ctx.argument.accept(this);
        if (!argType.isNumericType()) {
            errors.record(ParseUtil.illegalTypeError(argType, ctx));
            return YodaType.NONE_TYPE;
        }
        return YodaType.REAL_TYPE;
    }

    //TODO
    @Override
    public YodaType visitExpressionFloor(YodaModelParser.ExpressionFloorContext ctx) {
        YodaType argType = ctx.argument.accept(this);
        if (!argType.isNumericType()) {
            errors.record(ParseUtil.illegalTypeError(argType, ctx));
            return YodaType.NONE_TYPE;
        }
        return YodaType.REAL_TYPE;
    }

    //TODO
    @Override
    public YodaType visitExpressionRecord(YodaModelParser.ExpressionRecordContext ctx) {
        return super.visitExpressionRecord(ctx);
    }
}
