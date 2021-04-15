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

import org.antlr.v4.runtime.Token;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

public class StringTemplateGenerator extends PopulationModelBaseVisitor<ST> {


    private static final String UNARY_EXPRESSION_TEMPLATE = "unaryExpression";
    private static final String POW_EXPRESSION_TEMPLATE = "powExpression";
    private static final String LITERAL_EXPRESSION_TEMPLATE = "literal";
    private static final String REFERENCE_EXPRESSION_TEMPLATE = "reference";
    private static final String BINARY_EXPRESSION_TEMPLATE = "binaryExpression";

    private final STGroup group = new STGroupFile(StringTemplateGenerator.class.getResource("/PopulationModel.st"));

    private final SymbolTable table;

    public StringTemplateGenerator(SymbolTable table) {
        this.table = table;
    }

    private ST unary(String op, ST arg) {
        ST template = group.getInstanceOf(UNARY_EXPRESSION_TEMPLATE);
        template.add("op",op);
        template.add("arg",arg);
        return template;
    }

    private ST binary(ST left, String op, ST right) {
        ST template = group.getInstanceOf(BINARY_EXPRESSION_TEMPLATE);
        template.add("left", left);
        template.add("op",op);
        template.add("right",right);
        return template;
    }

    private ST literal(String value) {
        ST template = group.getInstanceOf(LITERAL_EXPRESSION_TEMPLATE);
        template.add("value", value);
        return template;
    }

    @Override
    public ST visitSystem_declaration(PopulationModelParser.System_declarationContext ctx) {
        ST template = group.getInstanceOf("systemGeneration");
        template.add("name",ctx.name.getText());
        for( Token a: ctx.args) {
            addSystemParameter(template,a.getText());
        }
        for( PopulationModelParser.Species_pattern_elementContext s: ctx.species_pattern().species_pattern_element()) {
            addSpeciesInitialization(template,s.species_expression(),s.expr());
        }
        return template;
    }

    private void addSpeciesInitialization(ST template, PopulationModelParser.Species_expressionContext species_expression, PopulationModelParser.ExprContext expr) {
        ST assignment = group.getInstanceOf("speciesAssignment");
        assignment.add("index",species_expression.accept(this));
        assignment.add("size",expr.accept(this));
        template.add("assignments",assignment);
    }

    private void addSystemParameter(ST template, String name) {
        ST argTemplate = group.getInstanceOf("systemParameterDeclaration");
        argTemplate.add("name",name);
        template.add("args",argTemplate);
    }

    @Override
    public ST visitConst_declaration(PopulationModelParser.Const_declarationContext ctx) {
        ST template = group.getInstanceOf("constDeclaration");
        template.add("name",ctx.name.getText());
        template.add("type",table.getType(ctx.name.getText()).javaType());
        template.add("value",ctx.expr().accept(this));
        return template;
    }

    @Override
    public ST visitSpecies_declaration(PopulationModelParser.Species_declarationContext ctx) {
        return super.visitSpecies_declaration(ctx);
    }

    @Override
    public ST visitRange(PopulationModelParser.RangeContext ctx) {
        return super.visitRange(ctx);
    }

    @Override
    public ST visitRule_declaration(PopulationModelParser.Rule_declarationContext ctx) {
        return super.visitRule_declaration(ctx);
    }

    @Override
    public ST visitRulestatement(PopulationModelParser.RulestatementContext ctx) {
        return super.visitRulestatement(ctx);
    }

    @Override
    public ST visitFor_statement(PopulationModelParser.For_statementContext ctx) {
        return super.visitFor_statement(ctx);
    }

    @Override
    public ST visitWhen_statement(PopulationModelParser.When_statementContext ctx) {
        return super.visitWhen_statement(ctx);
    }

    @Override
    public ST visitRule_body(PopulationModelParser.Rule_bodyContext ctx) {
        return super.visitRule_body(ctx);
    }

    @Override
    public ST visitSpecies_pattern(PopulationModelParser.Species_patternContext ctx) {
        return super.visitSpecies_pattern(ctx);
    }

    @Override
    public ST visitSpecies_pattern_element(PopulationModelParser.Species_pattern_elementContext ctx) {
        return super.visitSpecies_pattern_element(ctx);
    }

    @Override
    public ST visitSpecies_expression(PopulationModelParser.Species_expressionContext ctx) {
        return super.visitSpecies_expression(ctx);
    }

    @Override
    public ST visitMeasure_declaration(PopulationModelParser.Measure_declarationContext ctx) {
        return super.visitMeasure_declaration(ctx);
    }

    @Override
    public ST visitParam_declaration(PopulationModelParser.Param_declarationContext ctx) {
        return super.visitParam_declaration(ctx);
    }

    @Override
    public ST visitIntType(PopulationModelParser.IntTypeContext ctx) {
        return super.visitIntType(ctx);
    }

    @Override
    public ST visitRealType(PopulationModelParser.RealTypeContext ctx) {
        return super.visitRealType(ctx);
    }

    @Override
    public ST visitNegationExpression(PopulationModelParser.NegationExpressionContext ctx) {
        return unary("!",ctx.arg.accept(this));
    }


    @Override
    public ST visitExponentExpression(PopulationModelParser.ExponentExpressionContext ctx) {
        ST template = group.getInstanceOf(POW_EXPRESSION_TEMPLATE);
        template.add("base", ctx.left.accept(this));
        template.add("exponent", ctx.right.accept(this));
        return template;
    }

    @Override
    public ST visitReferenceExpression(PopulationModelParser.ReferenceExpressionContext ctx) {
        ST template = group.getInstanceOf(REFERENCE_EXPRESSION_TEMPLATE);
        template.add("name", ctx.reference.getText());
        return template;
    }

    @Override
    public ST visitIntValue(PopulationModelParser.IntValueContext ctx) {
        return literal(ctx.getText());
    }

    @Override
    public ST visitTrueValue(PopulationModelParser.TrueValueContext ctx) {
        return literal(ctx.getText());
    }

    @Override
    public ST visitRelationExpression(PopulationModelParser.RelationExpressionContext ctx) {
        return binary(ctx.left.accept(this),ctx.op.getText(),ctx.right.accept(this));
    }

    @Override
    public ST visitBracketExpression(PopulationModelParser.BracketExpressionContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public ST visitPopulationFractionExpression(PopulationModelParser.PopulationFractionExpressionContext ctx) {
        ST template = group.getInstanceOf("populationFractionExpression");
        template.add("name",ctx.species_expression().name.getText());
        if (ctx.species_expression().expr().size()>0) {
            ctx.species_expression().expr().forEach(e -> template.add("args", e.accept(this)));
        } else {
            template.add("args",null);
        }
        return template;
    }


    @Override
    public ST visitOrExpression(PopulationModelParser.OrExpressionContext ctx) {
        return binary(ctx.left.accept(this),"||",ctx.right.accept(this));
    }

    @Override
    public ST visitIfThenElseExpression(PopulationModelParser.IfThenElseExpressionContext ctx) {
        ST template = group.getInstanceOf("ifThenElseExpression");
        template.add("guard", ctx.guard.accept(this));
        template.add("thenBranch", ctx.thenBranch.accept(this));
        template.add("elseBranch", ctx.elseBranch.accept(this));
        return template;
    }

    @Override
    public ST visitFalseValue(PopulationModelParser.FalseValueContext ctx) {
        return literal("false");
    }

    @Override
    public ST visitRealValue(PopulationModelParser.RealValueContext ctx) {
        return literal(ctx.getText());
    }

    @Override
    public ST visitAndExpression(PopulationModelParser.AndExpressionContext ctx) {
        return binary(ctx.left.accept(this),"&&",ctx.right.accept(this));
    }

    @Override
    public ST visitMulDivExpression(PopulationModelParser.MulDivExpressionContext ctx) {
        if (ctx.op.getText().equals("//")) {
            ST template = group.getInstanceOf("safeDivExpression");
            template.add("left", ctx.left.accept(this));
            template.add("right", ctx.right.accept(this));
            return template;
        } else {
            return binary(ctx.left.accept(this), ctx.op.getText() ,ctx.right.accept(this));
        }
    }

    @Override
    public ST visitPopulationSizeExpression(PopulationModelParser.PopulationSizeExpressionContext ctx) {
        ST template = group.getInstanceOf("populationOccupancyExpression");
        template.add("name",ctx.species_expression().name.getText());
        if (ctx.species_expression().expr().size()>0) {
            ctx.species_expression().expr().forEach(e -> template.add("args", e.accept(this)));
        } else {
            template.add("args",null);
        }
        return template;
    }

    @Override
    public ST visitAddSubExpression(PopulationModelParser.AddSubExpressionContext ctx) {
        return binary(ctx.left.accept(this),ctx.op.getText(),ctx.right.accept(this));
    }

    @Override
    public ST visitUnaryExpression(PopulationModelParser.UnaryExpressionContext ctx) {
        return unary(ctx.op.getText(),ctx.arg.accept(this));
    }

//    @Override
//    public ST visitNowExpression(PopulationModelParser.NowExpressionContext ctx) {
//        ST template = group.getInstanceOf("nowValue");
//        return template;
//    }
}
