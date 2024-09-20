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
import org.antlr.v4.runtime.Token;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This visitor class is used to infer type in expressions
 */
public class TypeInferenceVisitor extends YodaModelBaseVisitor<YodaType>{


    private final ErrorCollector errors;

    private final Function<String, Optional<YodaType>> typeFunction;
    private final YodaElementAttributeTable elementAttributeTable;
    private final Predicate<String> validAttributePredicate;
    private final Predicate<String> validItAttributePredicates;
    private final boolean randomnessAllowed;
    private final boolean groupExpressionsAllowed;

    private final Map<String, YodaType.RecordType> fieldsRecordType;


    public TypeInferenceVisitor(ErrorCollector errors,
                                Function<String, Optional<YodaType>> typeFunction,
                                YodaElementAttributeTable elementAttributeTable,
                                Map<String, YodaType.RecordType> fieldsRecordType,
                                Predicate<String> validAttributePredicate,
                                Predicate<String> validItAttributePredicates,
                                boolean randomnessAllowed,
                                boolean groupExpressionsAllowed) {
        this.errors = errors;
        this.typeFunction = typeFunction;
        this.elementAttributeTable = elementAttributeTable;
        this.validAttributePredicate = validAttributePredicate;
        this.validItAttributePredicates = validItAttributePredicates;
        this.randomnessAllowed = randomnessAllowed;
        this.groupExpressionsAllowed = groupExpressionsAllowed;
        this.fieldsRecordType = fieldsRecordType;
    }

    public TypeInferenceVisitor(ErrorCollector errors, Function<String, Optional<YodaType>> getTypeOfConstantsAndParameters, YodaElementAttributeTable elementAttributeTable, Map<String, YodaType.RecordType> fieldsRecordType) {
        this(errors, getTypeOfConstantsAndParameters, elementAttributeTable, fieldsRecordType, x -> false, x -> false, false, false);
    }

    public boolean withErrors() {
        return errors.withErrors();
    }

    public boolean checkType(YodaType expected, YodaModelParser.ExprContext ctx) {
        YodaType actual = ctx.accept(this);
        if (!actual.canBeAssignedTo(expected)) {
            errors.record(ParseUtil.wrongTypeError(expected, actual, ctx));
            return false;
        } else {
            return true;
        }
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
        String name = ctx.reference.getText();
        if (elementAttributeTable.isAttribute(name)) {
            if (!validAttributePredicate.test(name)) {
                this.errors.record(ParseUtil.illegalSymbolError(ctx.reference.getText(), ctx));
                return YodaType.NONE_TYPE;
            }
            return elementAttributeTable.getTypeOf(name);
        }
        Optional<YodaType> type = typeFunction.apply(ctx.reference.getText());
        if (type.isEmpty()) {
            this.errors.record(ParseUtil.unknownSymbolError(ctx.getText(), ctx));
        }
        return type.orElse(YodaType.NONE_TYPE);
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
    public YodaType visitExpressionPowOperation(YodaModelParser.ExpressionPowOperationContext ctx) {
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
        if (!randomnessAllowed) {
            errors.record(ParseUtil.illegalRandomExpression(ctx));
        }
        return combineNumericBinaryOperators(ctx.min, ctx.max);
    }

    @Override
    public YodaType visitExpressionRandom(YodaModelParser.ExpressionRandomContext ctx) {
        if (!randomnessAllowed) {
            errors.record(ParseUtil.illegalRandomExpression(ctx));
        }
        return YodaType.REAL_TYPE;
    }


    @Override
    public YodaType visitExpressionMinimum(YodaModelParser.ExpressionMinimumContext ctx) {
        if (groupExpressionsAllowed) {
            return checkGroupExpression(ctx, ctx.groupName, ctx.value, ctx.guard);
        }
        return YodaType.NONE_TYPE;
    }

    private YodaType checkGroupExpression(YodaModelParser.ExprContext ctx, Token groupName, YodaModelParser.ExprContext value, YodaModelParser.ExprContext guard) {
        if (groupExpressionsAllowed) {
            Predicate<String> validAttributePredicate = getValidGroupPredicate(groupName);
            TypeInferenceVisitor visitor = new TypeInferenceVisitor(this.errors, typeFunction, elementAttributeTable, fieldsRecordType, validAttributePredicate, validItAttributePredicates, false, false);
            if (visitor.checkNumericType(value)&&((guard == null)||(visitor.checkType(YodaType.BOOLEAN_TYPE, guard)))) {
                return YodaType.REAL_TYPE;
            }
        }
        return YodaType.NONE_TYPE;
    }

    private YodaType checkGroupExpression(YodaModelParser.ExprContext ctx, Token groupName, YodaModelParser.ExprContext guard){
        if (groupExpressionsAllowed) {
            Predicate<String> validAttributePredicate = getValidGroupPredicate(groupName);
            TypeInferenceVisitor visitor = new TypeInferenceVisitor(this.errors,typeFunction, elementAttributeTable, fieldsRecordType, validAttributePredicate, validItAttributePredicates, false, false);
            if (guard==null|| visitor.checkType(YodaType.BOOLEAN_TYPE,guard)) {
                return YodaType.REAL_TYPE;
            }
        }
        return YodaType.NONE_TYPE;
    }

    @Override
    public YodaType visitExpressionCount(YodaModelParser.ExpressionCountContext ctx) {
        if (groupExpressionsAllowed){
            return checkGroupExpression(ctx, ctx.groupName, ctx.guard);
        }
        return YodaType.NONE_TYPE;
    }

    @Override
    public YodaType visitExpressionFraction(YodaModelParser.ExpressionFractionContext ctx) {
        if (groupExpressionsAllowed){
            return checkGroupExpression(ctx, ctx.groupName, ctx.guard);
        }
        return YodaType.NONE_TYPE;
    }

    private Predicate<String> getValidGroupPredicate(Token groupName) {
        Predicate<String> validAttributePredicate = elementAttributeTable.getGroupExpressionValidAttributePredicate();
        if (groupName != null) {
            if (elementAttributeTable.isGroupOrElement(groupName.getText())) {
                validAttributePredicate = elementAttributeTable.getSensingAttributePredicateOf(groupName.getText());
            } else {
                this.errors.record(ParseUtil.unknownSymbolError(groupName));
            }
        }
        return validAttributePredicate;
    }

    @Override
    public YodaType visitExpressionMaximum(YodaModelParser.ExpressionMaximumContext ctx) {
        if (groupExpressionsAllowed) {
            return checkGroupExpression(ctx, ctx.groupName, ctx.value, ctx.guard);
        }
        return YodaType.NONE_TYPE;
    }

    @Override
    public YodaType visitExpressionMean(YodaModelParser.ExpressionMeanContext ctx) {
        if (groupExpressionsAllowed) {
            return checkGroupExpression(ctx, ctx.groupName, ctx.value, ctx.guard);
        }
        return YodaType.NONE_TYPE;
    }



    @Override
    public YodaType visitExpressionImplication(YodaModelParser.ExpressionImplicationContext ctx) {
        if (this.checkType(YodaType.BOOLEAN_TYPE, ctx.leftOp)&&this.checkType(YodaType.BOOLEAN_TYPE, ctx.rightOp)) {
            return YodaType.BOOLEAN_TYPE;
        } else {
            return YodaType.NONE_TYPE;
        }
    }

    @Override
    public YodaType visitExpressionForAll(YodaModelParser.ExpressionForAllContext ctx) {
        if (groupExpressionsAllowed) {
            return checkGroupPredicateExpression(ctx, ctx.groupName, ctx.expr());
        }
        return YodaType.NONE_TYPE;
    }



    private YodaType checkGroupPredicateExpression(YodaModelParser.ExprContext ctx, Token groupName, YodaModelParser.ExprContext expr) {
        Predicate<String> validAttributePredicate = getValidGroupPredicate(groupName);
        TypeInferenceVisitor visitor = new TypeInferenceVisitor(this.errors, typeFunction, elementAttributeTable, fieldsRecordType, validAttributePredicate, validItAttributePredicates, false, false);
        return visitor.checkAndReturn(YodaType.BOOLEAN_TYPE, expr);
    }

    @Override
    public YodaType visitExpressionExists(YodaModelParser.ExpressionExistsContext ctx) {
        if (groupExpressionsAllowed) {
            return checkGroupPredicateExpression(ctx, ctx.groupName, ctx.expr());
        }
        return YodaType.NONE_TYPE;
    }

    @Override
    public YodaType visitExpressionItselfRef(YodaModelParser.ExpressionItselfRefContext ctx) {
        if (validItAttributePredicates.test(ctx.ref.getText())) {
            return elementAttributeTable.getTypeOf(ctx.ref.getText());
        } else {
            errors.record(ParseUtil.illegalSymbolError(ctx.ref.getText(), ctx));
            return YodaType.NONE_TYPE;
        }
    }

    @Override
    public YodaType visitExpressionRecordAccess(YodaModelParser.ExpressionRecordAccessContext ctx) {
        YodaType type = ctx.record.accept(this);
        if (type instanceof YodaType.RecordType recordType) {
            if (!recordType.hasField(ctx.fieldName.getText())) {
                return recordType.getType(ctx.fieldName.getText());
            } else {
                this.errors.record(ParseUtil.unknownFieldName(type, ctx.fieldName.getText(), ctx));
                return YodaType.NONE_TYPE;
            }
        } else {
            this.errors.record(ParseUtil.recordTypeExpected(type, ctx));
            return YodaType.NONE_TYPE;
        }
    }

    @Override
    public YodaType visitExpressionPi(YodaModelParser.ExpressionPiContext ctx) {
        return YodaType.REAL_TYPE;
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

    @Override
    public YodaType visitExpressionSinh(YodaModelParser.ExpressionSinhContext ctx) {
        YodaType argType = ctx.argument.accept(this);
        if (!argType.isNumericType()) {
            errors.record(ParseUtil.illegalTypeError(argType, ctx));
            return YodaType.NONE_TYPE;
        }
        return YodaType.REAL_TYPE;
    }

    @Override
    public YodaType visitExpressionAsin(YodaModelParser.ExpressionAsinContext ctx) {
        YodaType argType = ctx.argument.accept(this);
        if (!argType.isNumericType()) {
            errors.record(ParseUtil.illegalTypeError(argType, ctx));
            return YodaType.NONE_TYPE;
        }
        return YodaType.REAL_TYPE;
    }

    @Override
    public YodaType visitExpressionCos(YodaModelParser.ExpressionCosContext ctx) {
        YodaType argType = ctx.argument.accept(this);
        if (!argType.isNumericType()) {
            errors.record(ParseUtil.illegalTypeError(argType, ctx));
            return YodaType.NONE_TYPE;
        }
        return YodaType.REAL_TYPE;
    }

    @Override
    public YodaType visitExpressionCosh(YodaModelParser.ExpressionCoshContext ctx) {
        YodaType argType = ctx.argument.accept(this);
        if (!argType.isNumericType()) {
            errors.record(ParseUtil.illegalTypeError(argType, ctx));
            return YodaType.NONE_TYPE;
        }
        return YodaType.REAL_TYPE;
    }

    @Override
    public YodaType visitExpressionAcos(YodaModelParser.ExpressionAcosContext ctx) {
        YodaType argType = ctx.argument.accept(this);
        if (!argType.isNumericType()) {
            errors.record(ParseUtil.illegalTypeError(argType, ctx));
            return YodaType.NONE_TYPE;
        }
        return YodaType.REAL_TYPE;
    }

    @Override
    public YodaType visitExpressionTan(YodaModelParser.ExpressionTanContext ctx) {
        YodaType argType = ctx.argument.accept(this);
        if (!argType.isNumericType()) {
            errors.record(ParseUtil.illegalTypeError(argType, ctx));
            return YodaType.NONE_TYPE;
        }
        return YodaType.REAL_TYPE;
    }

    @Override
    public YodaType visitExpressionTanh(YodaModelParser.ExpressionTanhContext ctx) {
        YodaType argType = ctx.argument.accept(this);
        if (!argType.isNumericType()) {
            errors.record(ParseUtil.illegalTypeError(argType, ctx));
            return YodaType.NONE_TYPE;
        }
        return YodaType.REAL_TYPE;
    }

    @Override
    public YodaType visitExpressionAtan(YodaModelParser.ExpressionAtanContext ctx) {
        YodaType argType = ctx.argument.accept(this);
        if (!argType.isNumericType()) {
            errors.record(ParseUtil.illegalTypeError(argType, ctx));
            return YodaType.NONE_TYPE;
        }
        return YodaType.REAL_TYPE;
    }

    @Override
    public YodaType visitExpressionCeiling(YodaModelParser.ExpressionCeilingContext ctx) {
        YodaType argType = ctx.argument.accept(this);
        if (!argType.isNumericType()) {
            errors.record(ParseUtil.illegalTypeError(argType, ctx));
            return YodaType.NONE_TYPE;
        }
        return YodaType.REAL_TYPE;
    }

    @Override
    public YodaType visitExpressionFloor(YodaModelParser.ExpressionFloorContext ctx) {
        YodaType argType = ctx.argument.accept(this);
        if (!argType.isNumericType()) {
            errors.record(ParseUtil.illegalTypeError(argType, ctx));
            return YodaType.NONE_TYPE;
        }
        return YodaType.REAL_TYPE;
    }

    @Override
    public YodaType visitExpressionAbsolute(YodaModelParser.ExpressionAbsoluteContext ctx) {
        YodaType argType = ctx.argument.accept(this);
        if (!argType.isNumericType()){
            errors.record(ParseUtil.illegalTypeError(argType, ctx));
            return YodaType.NONE_TYPE;
        }
        return argType;
    }

    @Override
    public YodaType visitExpressionRecord(YodaModelParser.ExpressionRecordContext ctx) {
        boolean allFieldsAreCorrect = true;
        YodaType.RecordType recordType = null;
        for (YodaModelParser.FieldAssignmentContext fa: ctx.fieldAssignment()) {
            Optional<YodaType.RecordType> optionalRecordType = checkFieldAssignment(recordType, fa);
            if (optionalRecordType.isPresent()) {
                recordType = optionalRecordType.get();
            } else {
                allFieldsAreCorrect = false;
            }
        }
        if (recordType == null) return YodaType.NONE_TYPE;
        if (allFieldsAreCorrect) {
            checkAllFieldsAreAssigned(recordType, ctx);
        }
        return recordType;
    }

    @Override
    public YodaType visitExpressionDistance(YodaModelParser.ExpressionDistanceContext ctx) {
        YodaType a1Type = ctx.a1.accept(this);
        YodaType a2Type = ctx.a2.accept(this);
        YodaType a3Type = ctx.a3.accept(this);
        YodaType a4Type = ctx.a4.accept(this);
        YodaType[] args = new YodaType[]{a1Type, a2Type, a3Type, a4Type};
        for (YodaType arg : args) {
            if (!arg.isNumericType()) {
                errors.record(ParseUtil.illegalTypeError(arg, ctx));
                return YodaType.NONE_TYPE;
            }
        }
        return YodaType.REAL_TYPE;
    }

    @Override
    public YodaType visitExpressionAngleOf(YodaModelParser.ExpressionAngleOfContext ctx) {
        YodaType a1Type = ctx.a1.accept(this);
        YodaType a2Type = ctx.a2.accept(this);
        YodaType a3Type = ctx.a3.accept(this);
        YodaType a4Type = ctx.a4.accept(this);
        YodaType[] args = new YodaType[]{a1Type, a2Type, a3Type, a4Type};
        for (YodaType arg : args) {
            if (!arg.isNumericType()) {
                errors.record(ParseUtil.illegalTypeError(arg, ctx));
                return YodaType.NONE_TYPE;
            }
        }
        return YodaType.REAL_TYPE;
    }

    private void checkAllFieldsAreAssigned(YodaType.RecordType recordType, YodaModelParser.ExpressionRecordContext ctx) {
        Set<String> fields = recordType.getFields();
        if (fields.size() != ctx.fieldAssignment().size()) {
            fields.retainAll(ctx.fieldAssignment().stream().map(fa -> fa.name.getText()).collect(Collectors.toList()));
            errors.record(ParseUtil.missingFieldAssignment(fields, recordType, ctx));
        }
    }

    public Optional<YodaType.RecordType> checkFieldAssignment(YodaType.RecordType expected, YodaModelParser.FieldAssignmentContext ctx) {
        YodaType.RecordType recordType = fieldsRecordType.get(ctx.name.getText());
        if (recordType == null) {
            errors.record(ParseUtil.unknownAssignedFieldName(ctx.name));
            return Optional.empty();
        }
        checkAndReturn(recordType.getType(ctx.name.getText()), ctx.value);
        if ((expected != null)&&(!expected.equals(recordType))) {
            errors.record(ParseUtil.inconsistentFieldAssignment(expected, recordType, ctx.name));
            return Optional.empty();
        }
        return Optional.of(recordType);
    }
}
