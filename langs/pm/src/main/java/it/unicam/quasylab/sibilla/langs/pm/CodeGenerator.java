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

import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

public class CodeGenerator implements PopulationModelVisitor<String> {

    private static final String CLASS_PREAMBLE = "/* Code generated with Sibilla */";
    private static final String ELEMENT_SPACE = "\n\n\n";
    private static final String PACKAGE_DECLARATION = "package %s;";
    private static final String START_CLASS = "public class %s implements PopulationModelDefinition {";
    private static final String END_CLASS = "}";
    private final String className;
    private final String packageName;

    public CodeGenerator(String className, String packageName) {
        this.className = className;
        this.packageName = packageName;
    }


    @Override
    public String visitModel(PopulationModelParser.ModelContext ctx) {
        String toReturn = getPreamble();
        toReturn += getPackageDeclaration();
        toReturn += startClass();
        toReturn += constDefinition(ctx);
        toReturn += endClass();
        return toReturn;
    }

    private String constDefinition(PopulationModelParser.ModelContext ctx) {

        return "";
    }

    private String endClass() {
        return END_CLASS;
    }

    private String startClass() {
        return String.format(START_CLASS,className);
    }

    private String getPackageDeclaration() {
        return String.format(PACKAGE_DECLARATION,packageName)+ELEMENT_SPACE;
    }

    private String getPreamble() {
        return CLASS_PREAMBLE+ELEMENT_SPACE;
    }



    @Override
    public String visitElement(PopulationModelParser.ElementContext ctx) {
        return null;
    }

    @Override
    public String visitSystem_declaration(PopulationModelParser.System_declarationContext ctx) {
        return null;
    }

    @Override
    public String visitConst_declaration(PopulationModelParser.Const_declarationContext ctx) {
        return null;
    }

    @Override
    public String visitSpecies_declaration(PopulationModelParser.Species_declarationContext ctx) {
        return null;
    }

    @Override
    public String visitRange(PopulationModelParser.RangeContext ctx) {
        return null;
    }

    @Override
    public String visitRule_declaration(PopulationModelParser.Rule_declarationContext ctx) {
        return null;
    }

    @Override
    public String visitRulestatement(PopulationModelParser.RulestatementContext ctx) {
        return null;
    }

    @Override
    public String visitFor_statement(PopulationModelParser.For_statementContext ctx) {
        return null;
    }

    @Override
    public String visitWhen_statement(PopulationModelParser.When_statementContext ctx) {
        return null;
    }

    @Override
    public String visitRule_body(PopulationModelParser.Rule_bodyContext ctx) {
        return null;
    }

    @Override
    public String visitSpecies_pattern(PopulationModelParser.Species_patternContext ctx) {
        return null;
    }

    @Override
    public String visitSpecies_pattern_element(PopulationModelParser.Species_pattern_elementContext ctx) {
        return null;
    }

    @Override
    public String visitSpecies_expression(PopulationModelParser.Species_expressionContext ctx) {
        return null;
    }

    @Override
    public String visitMeasure_declaration(PopulationModelParser.Measure_declarationContext ctx) {
        return null;
    }

    @Override
    public String visitParam_declaration(PopulationModelParser.Param_declarationContext ctx) {
        return null;
    }

    @Override
    public String visitIntType(PopulationModelParser.IntTypeContext ctx) {
        return null;
    }

    @Override
    public String visitRealType(PopulationModelParser.RealTypeContext ctx) {
        return null;
    }

    @Override
    public String visitNegationExpression(PopulationModelParser.NegationExpressionContext ctx) {
        return null;
    }

    @Override
    public String visitExponentExpression(PopulationModelParser.ExponentExpressionContext ctx) {
        return null;
    }

    @Override
    public String visitReferenceExpression(PopulationModelParser.ReferenceExpressionContext ctx) {
        return null;
    }

    @Override
    public String visitIntValue(PopulationModelParser.IntValueContext ctx) {
        return null;
    }

    @Override
    public String visitTrueValue(PopulationModelParser.TrueValueContext ctx) {
        return null;
    }

    @Override
    public String visitRelationExpression(PopulationModelParser.RelationExpressionContext ctx) {
        return null;
    }

    @Override
    public String visitBracketExpression(PopulationModelParser.BracketExpressionContext ctx) {
        return null;
    }

    @Override
    public String visitPopulationFractionExpression(PopulationModelParser.PopulationFractionExpressionContext ctx) {
        return null;
    }

    @Override
    public String visitOrExpression(PopulationModelParser.OrExpressionContext ctx) {
        return null;
    }

    @Override
    public String visitIfThenElseExpression(PopulationModelParser.IfThenElseExpressionContext ctx) {
        return null;
    }

    @Override
    public String visitFalseValue(PopulationModelParser.FalseValueContext ctx) {
        return null;
    }

    @Override
    public String visitRealValue(PopulationModelParser.RealValueContext ctx) {
        return null;
    }

    @Override
    public String visitAndExpression(PopulationModelParser.AndExpressionContext ctx) {
        return null;
    }

    @Override
    public String visitMulDivExpression(PopulationModelParser.MulDivExpressionContext ctx) {
        return null;
    }

    @Override
    public String visitPopulationSizeExpression(PopulationModelParser.PopulationSizeExpressionContext ctx) {
        return null;
    }

    @Override
    public String visitAddSubExpression(PopulationModelParser.AddSubExpressionContext ctx) {
        return null;
    }

    @Override
    public String visitUnaryExpression(PopulationModelParser.UnaryExpressionContext ctx) {
        return null;
    }

//    @Override
//    public String visitNowExpression(PopulationModelParser.NowExpressionContext ctx) {
//        return null;
//    }

    @Override
    public String visit(ParseTree tree) {
        return null;
    }

    @Override
    public String visitChildren(RuleNode node) {
        return null;
    }

    @Override
    public String visitTerminal(TerminalNode node) {
        return null;
    }

    @Override
    public String visitErrorNode(ErrorNode node) {
        return null;
    }
}
