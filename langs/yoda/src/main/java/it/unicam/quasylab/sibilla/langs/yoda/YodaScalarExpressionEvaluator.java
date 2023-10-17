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

import it.unicam.quasylab.sibilla.core.util.values.SibillaRecord;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class YodaScalarExpressionEvaluator extends YodaModelBaseVisitor<SibillaValue> {

    private final Function<String, Optional<SibillaValue>> nameResolver;


    public YodaScalarExpressionEvaluator(Function<String, Optional<SibillaValue>> nameResolver) {
        this.nameResolver = nameResolver;
    }

    @Override
    public SibillaValue visitExpressionInteger(YodaModelParser.ExpressionIntegerContext ctx) {
        return SibillaValue.of(Integer.parseInt(ctx.getText()));
    }

    @Override
    public SibillaValue visitExpressionReal(YodaModelParser.ExpressionRealContext ctx) {
        return SibillaValue.of(Double.parseDouble(ctx.getText()));
    }

    @Override
    public SibillaValue visitExpressionFalse(YodaModelParser.ExpressionFalseContext ctx) {
        return SibillaValue.of(false);
    }

    @Override
    public SibillaValue visitExpressionTrue(YodaModelParser.ExpressionTrueContext ctx) {
        return SibillaValue.of(true);
    }

    @Override
    public SibillaValue visitExpressionReference(YodaModelParser.ExpressionReferenceContext ctx) {
        return nameResolver.apply(ctx.getText()).orElse(SibillaValue.ERROR_VALUE);
    }

    @Override
    public SibillaValue visitExpressionBrackets(YodaModelParser.ExpressionBracketsContext ctx) {
        return ctx.expr().accept(this);
    }


    @Override
    public SibillaValue visitExpressionUnary(YodaModelParser.ExpressionUnaryContext ctx) {
        switch (ctx.oper.getText()) {
            case "+" : return ctx.arg.accept(this);
            case "-" : return SibillaValue.minus(ctx.arg.accept(this));
            default  : return SibillaValue.ERROR_VALUE;
        }
    }

    @Override
    public SibillaValue visitExpressionAddSubOperation(YodaModelParser.ExpressionAddSubOperationContext ctx) {
        return SibillaValue.apply(SibillaValue.getOperator(ctx.oper.getText()), ctx.leftOp.accept(this), ctx.rightOp.accept(this));
    }

    @Override
    public SibillaValue visitExpressionMultDivOperation(YodaModelParser.ExpressionMultDivOperationContext ctx) {
        return SibillaValue.apply(SibillaValue.getOperator(ctx.oper.getText()), ctx.leftOp.accept(this), ctx.rightOp.accept(this));
    }

    @Override
    public SibillaValue visitExpressionAdditionalOperation(YodaModelParser.ExpressionAdditionalOperationContext ctx) {
        return SibillaValue.apply(SibillaValue.getOperator(ctx.oper.getText()), ctx.leftOp.accept(this), ctx.rightOp.accept(this));
    }

    @Override
    public SibillaValue visitExpressionPowOperation(YodaModelParser.ExpressionPowOperationContext ctx) {
        return SibillaValue.apply(Math::pow, ctx.leftOp.accept(this), ctx.rightOp.accept(this));
    }

    @Override
    public SibillaValue visitExpressionNegation(YodaModelParser.ExpressionNegationContext ctx) {
        return SibillaValue.not(ctx.argument.accept(this));
    }

    @Override
    public SibillaValue visitExpressionSquareRoot(YodaModelParser.ExpressionSquareRootContext ctx) {
        return SibillaValue.apply(Math::sqrt, ctx.argument.accept(this));
    }

    @Override
    public SibillaValue visitExpressionAnd(YodaModelParser.ExpressionAndContext ctx) {
        return SibillaValue.and(ctx.leftOp.accept(this), ctx.rightOp.accept(this));
    }

    @Override
    public SibillaValue visitExpressionOr(YodaModelParser.ExpressionOrContext ctx) {
        return SibillaValue.or(ctx.leftOp.accept(this), ctx.rightOp.accept(this));
    }

    @Override
    public SibillaValue visitExpressionRelation(YodaModelParser.ExpressionRelationContext ctx) {
        return SibillaValue.apply(SibillaValue.getOperator(ctx.oper.getText()), ctx.leftOp.accept(this), ctx.rightOp.accept(this));
    }

    @Override
    public SibillaValue visitExpressionIfThenElse(YodaModelParser.ExpressionIfThenElseContext ctx) {
        if (ctx.guardExpr.accept(this).booleanOf()) {
            return ctx.thenBranch.accept(this);
        } else {
            return ctx.elseBranch.accept(this);
        }
    }

    @Override
    public SibillaValue visitExpressionRecord(YodaModelParser.ExpressionRecordContext ctx) {
        return new SibillaRecord(ctx.fieldAssignment().stream().collect(Collectors.toMap(fa -> fa.name.getText(), fa -> fa.value.accept(this))));
    }

    @Override
    public SibillaValue visitExpressionSin(YodaModelParser.ExpressionSinContext ctx) {
        return SibillaValue.apply(Math::sin, ctx.argument.accept(this));
    }

    @Override
    public SibillaValue visitExpressionSinh(YodaModelParser.ExpressionSinhContext ctx) {
        return SibillaValue.apply(Math::sinh, ctx.argument.accept(this));
    }

    @Override
    public SibillaValue visitExpressionAsin(YodaModelParser.ExpressionAsinContext ctx) {
        return SibillaValue.apply(Math::asin, ctx.argument.accept(this));
    }

    @Override
    public SibillaValue visitExpressionCos(YodaModelParser.ExpressionCosContext ctx) {
        return SibillaValue.apply(Math::cos, ctx.argument.accept(this));
    }

    @Override
    public SibillaValue visitExpressionCosh(YodaModelParser.ExpressionCoshContext ctx) {
        return SibillaValue.apply(Math::cosh, ctx.argument.accept(this));
    }

    @Override
    public SibillaValue visitExpressionAcos(YodaModelParser.ExpressionAcosContext ctx) {
        return SibillaValue.apply(Math::acos, ctx.argument.accept(this));
    }

    @Override
    public SibillaValue visitExpressionTan(YodaModelParser.ExpressionTanContext ctx) {
        return SibillaValue.apply(Math::sin, ctx.argument.accept(this));
    }

    @Override
    public SibillaValue visitExpressionTanh(YodaModelParser.ExpressionTanhContext ctx) {
        return SibillaValue.apply(Math::tanh, ctx.argument.accept(this));
    }

    @Override
    public SibillaValue visitExpressionAtan(YodaModelParser.ExpressionAtanContext ctx) {
        return SibillaValue.apply(Math::atan, ctx.argument.accept(this));
    }

    @Override
    protected SibillaValue defaultResult() {
        return SibillaValue.ERROR_VALUE;
    }


    @Override
    public SibillaValue visitExpressionCeiling(YodaModelParser.ExpressionCeilingContext ctx) {
        return SibillaValue.apply(Math::ceil, ctx.argument.accept(this));
    }

    @Override
    public SibillaValue visitExpressionImplication(YodaModelParser.ExpressionImplicationContext ctx) {
        return SibillaValue.of(!ctx.leftOp.accept(this).booleanOf()||ctx.rightOp.accept(this).booleanOf());
    }

    @Override
    public SibillaValue visitExpressionFloor(YodaModelParser.ExpressionFloorContext ctx) {
        return SibillaValue.apply(Math::floor, ctx.argument.accept(this));
    }

    @Override
    public SibillaValue visitExpressionRecordAccess(YodaModelParser.ExpressionRecordAccessContext ctx) {
        return SibillaValue.access(ctx.record.accept(this), ctx.fieldName.getText());
    }

    @Override
    public SibillaValue visitExpressionAbsolute(YodaModelParser.ExpressionAbsoluteContext ctx) {
        return SibillaValue.apply(Math::abs, ctx.argument.accept(this));
    }
}
