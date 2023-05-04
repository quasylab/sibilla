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

import java.util.function.BiFunction;

public class BooleanExpressionEvaluator extends PopulationModelBaseVisitor<Boolean> {

    private final ExpressionEvaluator expressionEvaluator;

    public BooleanExpressionEvaluator(ExpressionEvaluator expressionEvaluator) {
        this.expressionEvaluator = expressionEvaluator;
    }

    @Override
    public Boolean visitNegationExpression(PopulationModelParser.NegationExpressionContext ctx) {
        return !ctx.arg.accept(this);
    }

    @Override
    public Boolean visitOrExpression(PopulationModelParser.OrExpressionContext ctx) {
        return ctx.left.accept(this)||ctx.right.accept(this);
    }

    @Override
    public Boolean visitIfThenElseExpression(PopulationModelParser.IfThenElseExpressionContext ctx) {
        return (ctx.guard.accept(this)?ctx.thenBranch.accept(this):ctx.elseBranch.accept(this));
    }

    @Override
    public Boolean visitFalseValue(PopulationModelParser.FalseValueContext ctx) {
        return false;
    }

    @Override
    public Boolean visitAndExpression(PopulationModelParser.AndExpressionContext ctx) {
        return ctx.left.accept(this)&&ctx.right.accept(this);
    }

    @Override
    public Boolean visitRelationExpression(PopulationModelParser.RelationExpressionContext ctx) {
        return PopulationModelGenerator.getRelationOperator(ctx.op.getText()).apply(ctx.left.accept(expressionEvaluator),ctx.right.accept(expressionEvaluator));
    }

    @Override
    public Boolean visitTrueValue(PopulationModelParser.TrueValueContext ctx) {
        return true;
    }

    @Override
    public Boolean visitBracketExpression(PopulationModelParser.BracketExpressionContext ctx) {
        return ctx.expr().accept(this);
    }
}
