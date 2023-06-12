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

import it.unicam.quasylab.sibilla.core.models.yoda.YodaValue;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ScalarExpressionEvaluator extends YodaModelBaseVisitor<YodaValue> {

    private final Function<String, YodaValue> nameResolver;


    public ScalarExpressionEvaluator(Function<String, YodaValue> nameResolver) {
        this.nameResolver = nameResolver;
    }

    @Override
    public YodaValue visitExpressionInteger(YodaModelParser.ExpressionIntegerContext ctx) {
        return YodaValue.INTEGER_VALUE.apply(Integer.parseInt(ctx.getText()));
    }

    @Override
    public YodaValue visitExpressionReal(YodaModelParser.ExpressionRealContext ctx) {
        return YodaValue.REAL_VALUE.apply(Double.parseDouble(ctx.getText()));
    }

    @Override
    public YodaValue visitExpressionFalse(YodaModelParser.ExpressionFalseContext ctx) {
        return YodaValue.FALSE;
    }

    @Override
    public YodaValue visitExpressionTrue(YodaModelParser.ExpressionTrueContext ctx) {
        return YodaValue.TRUE;
    }

    @Override
    public YodaValue visitExpressionReference(YodaModelParser.ExpressionReferenceContext ctx) {
        return nameResolver.apply(ctx.getText());
    }

    @Override
    public YodaValue visitExpressionBrackets(YodaModelParser.ExpressionBracketsContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public YodaValue visitExpressionAttributeRef(YodaModelParser.ExpressionAttributeRefContext ctx) {
        //TODO: In the type inference one has to check that the used field is defined in the corresponding type.
        YodaValue value = nameResolver.apply(ctx.parent.getText());
        if (value.isRecord()) {
            return ((YodaValue.RecordValue) value).get(ctx.son.getText());
        } else {
            return YodaValue.NONE;
        }
    }

    @Override
    public YodaValue visitExpressionUnary(YodaModelParser.ExpressionUnaryContext ctx) {
        switch (ctx.oper.getText()) {
            case "+" : return ctx.arg.accept(this).plus();
            case "-" : return ctx.arg.accept(this).minus();
            default  : return YodaValue.NONE;
        }
    }

    @Override
    public YodaValue visitExpressionAddSubOperation(YodaModelParser.ExpressionAddSubOperationContext ctx) {
        switch (ctx.oper.getText()) {
            case "+" : return ctx.leftOp.accept(this).sum(ctx.rightOp.accept(this));
            case "-" : return ctx.leftOp.accept(this).subtract(ctx.rightOp.accept(this));
            default  : return YodaValue.NONE;
        }
    }

    @Override
    public YodaValue visitExpressionMultDivOperation(YodaModelParser.ExpressionMultDivOperationContext ctx) {
        switch (ctx.oper.getText()) {
            case "*" : return ctx.leftOp.accept(this).multiply(ctx.rightOp.accept(this));
            case "/" : return ctx.leftOp.accept(this).divide(ctx.rightOp.accept(this));
            default  : return YodaValue.NONE;
        }
    }

    @Override
    public YodaValue visitExpressionAdditionalOperation(YodaModelParser.ExpressionAdditionalOperationContext ctx) {
        switch (ctx.oper.getText()) {
            case "%": return ctx.leftOp.accept(this).module(ctx.rightOp.accept(this));
            case "//": {
                YodaValue rightValue = ctx.rightOp.accept(this);
                if (rightValue.isZero()) {
                    return rightValue;
                } else {
                    return ctx.leftOp.accept(this).divide(rightValue);
                }
            }
            default: return YodaValue.NONE;
        }
    }

    @Override
    public YodaValue visitExpressionExponentOperation(YodaModelParser.ExpressionExponentOperationContext ctx) {
        return ctx.leftOp.accept(this).pow(ctx.rightOp.accept(this));
    }

    @Override
    public YodaValue visitExpressionNegation(YodaModelParser.ExpressionNegationContext ctx) {
        return ctx.argument.accept(this).negation();
    }

    @Override
    public YodaValue visitExpressionSquareRoot(YodaModelParser.ExpressionSquareRootContext ctx) {
        return ctx.argument.accept(this).sqrt();
    }

    @Override
    public YodaValue visitExpressionAnd(YodaModelParser.ExpressionAndContext ctx) {
        return ctx.leftOp.accept(this).conjunction(ctx.rightOp.accept(this));
    }

    @Override
    public YodaValue visitExpressionOr(YodaModelParser.ExpressionOrContext ctx) {
        return ctx.leftOp.accept(this).disjunction(ctx.rightOp.accept(this));
    }

    @Override
    public YodaValue visitExpressionRelation(YodaModelParser.ExpressionRelationContext ctx) {
        YodaValue leftValue = ctx.leftOp.accept(this);
        YodaValue rightValue = ctx.rightOp.accept(this);
        switch (ctx.oper.getText()) {
            case "<" : return leftValue.lessThan(rightValue);
            case "<=": return leftValue.lessOrEqualThan(rightValue);
            case "==": return leftValue.equalTo(rightValue);
            case "!=": return leftValue.notEqualTo(rightValue);
            case ">=": return leftValue.greaterOrEqualTo(rightValue);
            case ">" : return leftValue.greaterThan(rightValue);
            default  : return YodaValue.NONE;
        }
    }

    @Override
    public YodaValue visitExpressionIfThenElse(YodaModelParser.ExpressionIfThenElseContext ctx) {
        return ctx.guardExpr.accept(this).ifThenElse(() -> ctx.thenBranch.accept(this),() -> ctx.elseBranch.accept(this));
    }

    @Override
    public YodaValue visitExpressionRecord(YodaModelParser.ExpressionRecordContext ctx) {
        //TODO: Checks in the expressions that the fields occurring in this expression are all part of the same type!
        Map<String, YodaValue> fields = ctx.fieldAssignment().stream().collect(Collectors.toMap(f -> f.name.getText(), f -> f.value.accept(this)));
        return super.visitExpressionRecord(ctx);
    }

    @Override
    public YodaValue visitExpressionSin(YodaModelParser.ExpressionSinContext ctx) {
        return ctx.argument.accept(this).sin();
    }

    @Override
    public YodaValue visitExpressionSinh(YodaModelParser.ExpressionSinhContext ctx) {
        return ctx.argument.accept(this).sinh();
    }

    @Override
    public YodaValue visitExpressionAsin(YodaModelParser.ExpressionAsinContext ctx) {
        return ctx.argument.accept(this).asin();
    }

    @Override
    public YodaValue visitExpressionCos(YodaModelParser.ExpressionCosContext ctx) {
        return ctx.argument.accept(this).cos();
    }

    @Override
    public YodaValue visitExpressionCosh(YodaModelParser.ExpressionCoshContext ctx) {
        return ctx.argument.accept(this).cosh();
    }

    @Override
    public YodaValue visitExpressionAcos(YodaModelParser.ExpressionAcosContext ctx) {
        return ctx.argument.accept(this).acos();
    }

    @Override
    public YodaValue visitExpressionTan(YodaModelParser.ExpressionTanContext ctx) {
        return ctx.argument.accept(this).tan();
    }

    @Override
    public YodaValue visitExpressionTanh(YodaModelParser.ExpressionTanhContext ctx) {
        return ctx.argument.accept(this).tanh();
    }

    @Override
    public YodaValue visitExpressionAtan(YodaModelParser.ExpressionAtanContext ctx) {
        return ctx.argument.accept(this).atan();
    }

    @Override
    protected YodaValue defaultResult() {
        return YodaValue.NONE;
    }




}
