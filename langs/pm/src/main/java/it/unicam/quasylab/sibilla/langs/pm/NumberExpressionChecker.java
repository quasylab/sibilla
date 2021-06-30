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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NumberExpressionChecker extends PopulationModelBaseVisitor<Boolean> {

    protected final List<ModelBuildingError> errorList;
    protected final SymbolTable table;
    protected final Set<String> localVariables;

    public NumberExpressionChecker(List<ModelBuildingError> errorList, SymbolTable table) {
        this(errorList, table, new HashSet<>());
    }

    public NumberExpressionChecker(List<ModelBuildingError> errorList, SymbolTable table, Set<String> localVariables) {
        this.errorList = errorList;
        this.table = table;
        this.localVariables = localVariables;
    }

    @Override
    public Boolean visitNegationExpression(PopulationModelParser.NegationExpressionContext ctx) {
        this.errorList.add(ModelBuildingError.expectedNumber(SymbolType.BOOLEAN,ctx));
        return false;
    }

    @Override
    public Boolean visitExponentExpression(PopulationModelParser.ExponentExpressionContext ctx) {
        return ctx.left.accept(this)&ctx.right.accept(this);
    }

    @Override
    public Boolean visitReferenceExpression(PopulationModelParser.ReferenceExpressionContext ctx) {
        String name = ctx.reference.getText();
        if (localVariables.contains(name)||table.isAConst(name)||table.isAParameter(name)) {
            return true;
        }
        if (table.isASpecies(name)||table.isALabel(name)||table.isAMeasure(name)||table.isARule(name)) {
            this.errorList.add(ModelBuildingError.illegalUseOfName(name,ctx));
        } else {
            this.errorList.add(ModelBuildingError.unknownSymbol(name,ctx.start.getLine(), ctx.start.getCharPositionInLine()));
        }
        return false;
    }

    @Override
    public Boolean visitIntValue(PopulationModelParser.IntValueContext ctx) {
        return true;
    }

    @Override
    public Boolean visitTrueValue(PopulationModelParser.TrueValueContext ctx) {
        this.errorList.add(ModelBuildingError.expectedNumber(SymbolType.BOOLEAN,ctx));
        return false;
    }

    @Override
    public Boolean visitRelationExpression(PopulationModelParser.RelationExpressionContext ctx) {
        this.errorList.add(ModelBuildingError.expectedNumber(SymbolType.BOOLEAN,ctx));
        return false;
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
        this.errorList.add(ModelBuildingError.expectedNumber(SymbolType.BOOLEAN,ctx));
        return false;
    }

    @Override
    public Boolean visitIfThenElseExpression(PopulationModelParser.IfThenElseExpressionContext ctx) {
        BooleanExpressionChecker checker = getBooleanExpressionChecker();
        return ctx.guard.accept(checker) & ctx.thenBranch.accept(this) & ctx.elseBranch.accept(this);
    }

    protected BooleanExpressionChecker getBooleanExpressionChecker() {
        return new BooleanExpressionChecker(this.errorList,this);
    }

    @Override
    public Boolean visitFalseValue(PopulationModelParser.FalseValueContext ctx) {
        this.errorList.add(ModelBuildingError.expectedNumber(SymbolType.BOOLEAN,ctx));
        return false;
    }

    @Override
    public Boolean visitRealValue(PopulationModelParser.RealValueContext ctx) {
        return true;
    }

    @Override
    public Boolean visitAndExpression(PopulationModelParser.AndExpressionContext ctx) {
        this.errorList.add(ModelBuildingError.expectedNumber(SymbolType.BOOLEAN,ctx));
        return false;
    }

    @Override
    public Boolean visitMulDivExpression(PopulationModelParser.MulDivExpressionContext ctx) {
        return ctx.left.accept(this) & ctx.right.accept(this);
    }

    @Override
    public Boolean visitPopulationSizeExpression(PopulationModelParser.PopulationSizeExpressionContext ctx) {
        this.errorList.add(ModelBuildingError.illegalPopulationExpression(ctx));
        return false;
    }

    @Override
    public Boolean visitAddSubExpression(PopulationModelParser.AddSubExpressionContext ctx) {
        return ctx.left.accept(this) & ctx.right.accept(this);
    }

    @Override
    public Boolean visitUnaryExpression(PopulationModelParser.UnaryExpressionContext ctx) {
        return ctx.arg.accept(this);
    }
}
