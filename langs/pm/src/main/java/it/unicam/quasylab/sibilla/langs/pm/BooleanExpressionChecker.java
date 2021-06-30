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

package it.unicam.quasylab.sibilla.langs.pm;

import java.util.List;

public class BooleanExpressionChecker extends PopulationModelBaseVisitor<Boolean> {

    private final NumberExpressionChecker numberExpressionChecker;
    protected final List<ModelBuildingError> errorList;

    public BooleanExpressionChecker(List<ModelBuildingError> errorList, NumberExpressionChecker numberExpressionChecker) {
        this.numberExpressionChecker = numberExpressionChecker;
        this.errorList = errorList;
    }

    @Override
    public Boolean visitNegationExpression(PopulationModelParser.NegationExpressionContext ctx) {
        return ctx.arg.accept(this);
    }

    @Override
    public Boolean visitExponentExpression(PopulationModelParser.ExponentExpressionContext ctx) {
        this.errorList.add(ModelBuildingError.expectedBoolean(SymbolType.NUMBER,ctx));
        return false;
    }

    @Override
    public Boolean visitReferenceExpression(PopulationModelParser.ReferenceExpressionContext ctx) {
        this.errorList.add(ModelBuildingError.expectedBoolean(SymbolType.NUMBER,ctx));
        return false;
    }

    @Override
    public Boolean visitIntValue(PopulationModelParser.IntValueContext ctx) {
        this.errorList.add(ModelBuildingError.expectedBoolean(SymbolType.NUMBER,ctx));
        return false;
    }

    @Override
    public Boolean visitTrueValue(PopulationModelParser.TrueValueContext ctx) {
        return true;
    }

    @Override
    public Boolean visitRelationExpression(PopulationModelParser.RelationExpressionContext ctx) {
        return ctx.left.accept(numberExpressionChecker)&ctx.right.accept(numberExpressionChecker);
    }

    @Override
    public Boolean visitBracketExpression(PopulationModelParser.BracketExpressionContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public Boolean visitPopulationFractionExpression(PopulationModelParser.PopulationFractionExpressionContext ctx) {
        this.errorList.add(ModelBuildingError.illegalPopulationExpression(ctx));
        return false;
    }

    @Override
    public Boolean visitOrExpression(PopulationModelParser.OrExpressionContext ctx) {
        return ctx.left.accept(this) & ctx.right.accept(this);
    }

    @Override
    public Boolean visitIfThenElseExpression(PopulationModelParser.IfThenElseExpressionContext ctx) {
        return ctx.guard.accept(this) &
                ctx.thenBranch.accept(numberExpressionChecker) &
                ctx.elseBranch.accept(numberExpressionChecker);
    }

    @Override
    public Boolean visitFalseValue(PopulationModelParser.FalseValueContext ctx) {
        return true;
    }

    @Override
    public Boolean visitRealValue(PopulationModelParser.RealValueContext ctx) {
        this.errorList.add(ModelBuildingError.expectedBoolean(SymbolType.NUMBER,ctx));
        return false;
    }

    @Override
    public Boolean visitAndExpression(PopulationModelParser.AndExpressionContext ctx) {
        return ctx.left.accept(this) & ctx.right.accept(this);
    }

    @Override
    public Boolean visitMulDivExpression(PopulationModelParser.MulDivExpressionContext ctx) {
        this.errorList.add(ModelBuildingError.expectedBoolean(SymbolType.NUMBER,ctx));
        return false;
    }

    @Override
    public Boolean visitPopulationSizeExpression(PopulationModelParser.PopulationSizeExpressionContext ctx) {
        this.errorList.add(ModelBuildingError.illegalPopulationExpression(ctx));
        return false;
    }

    @Override
    public Boolean visitAddSubExpression(PopulationModelParser.AddSubExpressionContext ctx) {
        this.errorList.add(ModelBuildingError.expectedBoolean(SymbolType.NUMBER,ctx));
        return false;
    }

    @Override
    public Boolean visitUnaryExpression(PopulationModelParser.UnaryExpressionContext ctx) {
        this.errorList.add(ModelBuildingError.expectedBoolean(SymbolType.NUMBER,ctx));
        return false;
    }

}
