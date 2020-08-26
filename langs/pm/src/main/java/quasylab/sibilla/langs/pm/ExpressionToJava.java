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

package quasylab.sibilla.langs.pm;

public class ExpressionToJava extends PopulationModelBaseVisitor<String> {
    @Override
    public String visitNegationExpression(PopulationModelParser.NegationExpressionContext ctx) {
        return "!("+ctx.accept(this)+")";
    }

    @Override
    public String visitExponentExpression(PopulationModelParser.ExponentExpressionContext ctx) {
        return String.format("Math.pow(%s,%s)",ctx.left.accept(this),ctx.right.accept(this));
    }

    @Override
    public String visitReferenceExpression(PopulationModelParser.ReferenceExpressionContext ctx) {
        return ParseUtil.getSymbolName(ctx.reference.getText());
    }

    @Override
    public String visitIntValue(PopulationModelParser.IntValueContext ctx) {
        return ctx.INTEGER().getText();
    }

    @Override
    public String visitTrueValue(PopulationModelParser.TrueValueContext ctx) {
        return "true";
    }

    @Override
    public String visitRelationExpression(PopulationModelParser.RelationExpressionContext ctx) {
        return String.format("(%s %s %s)",ctx.left.accept(this),ctx.op.getText(),ctx.right.accept(this));
    }

    @Override
    public String visitBracketExpression(PopulationModelParser.BracketExpressionContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public String visitPopulationFractionExpression(PopulationModelParser.PopulationFractionExpressionContext ctx) {
        String name = ctx.species_expression().name.getText();
        String[] args = ctx.species_expression().expr().stream().map(e -> e.accept(this)).toArray(i -> new String[i]);
        return ParseUtil.populationFractionExpression(name,args);
    }

    @Override
    public String visitOrExpression(PopulationModelParser.OrExpressionContext ctx) {
        return buildExpression(ctx.left.accept(this),"||",ctx.right.accept(this));
    }

    private String buildExpression(String arg1, String op, String arg2) {
        return String.format("(%s %s %s)",arg1,op,arg2);
    }

    @Override
    public String visitIfThenElseExpression(PopulationModelParser.IfThenElseExpressionContext ctx) {
        return String.format("(%s?%s:%s)",ctx.guard.accept(this),ctx.thenBranch.accept(this),ctx.elseBranch.accept(this));
    }

    @Override
    public String visitFalseValue(PopulationModelParser.FalseValueContext ctx) {
        return "false";
    }

    @Override
    public String visitRealValue(PopulationModelParser.RealValueContext ctx) {
        return ctx.getText();
    }

    @Override
    public String visitAndExpression(PopulationModelParser.AndExpressionContext ctx) {
        return buildExpression(ctx.left.accept(this),"&&",ctx.right.accept(this));
    }

    @Override
    public String visitMulDivExpression(PopulationModelParser.MulDivExpressionContext ctx) {
        if (ctx.op.getText().equals("\\\\")) {
            return String.format("PopulationModelDefinition.fraction(%s,%s)",ctx.left.accept(this),ctx.right.accept(this));
        } else {
            return buildExpression(ctx.left.accept(this),ctx.op.getText(),ctx.right.accept(this));
        }
    }

    @Override
    public String visitPopulationSizeExpression(PopulationModelParser.PopulationSizeExpressionContext ctx) {
        String name = ctx.species_expression().name.getText();
        String[] args = ctx.species_expression().expr().stream().map(e -> e.accept(this)).toArray(i -> new String[i]);
        return ParseUtil.populationSizeExpression(name,args);
    }

    @Override
    public String visitAddSubExpression(PopulationModelParser.AddSubExpressionContext ctx) {
        return buildExpression(ctx.left.accept(this),ctx.op.getText(),ctx.right.accept(this));
    }

    @Override
    public String visitUnaryExpression(PopulationModelParser.UnaryExpressionContext ctx) {
        return String.format("(%s%s)",ctx.op.getText(),ctx.arg.accept(this));
    }

    @Override
    public String visitNowExpression(PopulationModelParser.NowExpressionContext ctx) {
        return ParseUtil.NOW;
    }
}
