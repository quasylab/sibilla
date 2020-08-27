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

import java.util.Locale;

public class ElementDeclarationGenerator extends PopulationModelBaseVisitor<String> {

    private static final String CONSTANT_DECLARATION_CODE = "public final static %s %s = %s";
    private static final String MEASURE_DECLARATION_CODE = "public double %s( PopulationState "+ParseUtil.getStateName()+" ) { return %s; }";
    private static final String RULE_DECLARATION_CODE = "public void %s(PopulationModel model) {\n%s\n}";
    private static final String FOR_RULE_DECLARATION_CODE = "for(int %1$s = %2$s;%1$s<%3$s; %1$s++) {\n%4$s\n}";
    private static final String WHEN_RULE_DECLARATION_CODE = "if (%s) {\n%s\n}";
    private static final String BODY_RULE_DECLARATION_CODE = "model.addRule(";



    private final ExpressionToJava expressionToJava;
    private final SymbolTable symbolTable;

    public ElementDeclarationGenerator(SymbolTable symbolTable, ExpressionToJava expressionToJava) {
        this.symbolTable = symbolTable;
        this.expressionToJava = expressionToJava;
    }

    @Override
    public String visitSystem_declaration(PopulationModelParser.System_declarationContext ctx) {
        return "";
    }

    @Override
    public String visitConst_declaration(PopulationModelParser.Const_declarationContext ctx) {
        return String.format(CONSTANT_DECLARATION_CODE,symbolTable.getType(ctx.name.getText()).javaType(),ParseUtil.getSymbolName(ctx.name.getText()),ctx.expr().accept(expressionToJava));
    }

    @Override
    public String visitSpecies_declaration(PopulationModelParser.Species_declarationContext ctx) {
        return "";
    }

    @Override
    public String visitRule_declaration(PopulationModelParser.Rule_declarationContext ctx) {
        return "";
    }

    @Override
    public String visitFor_statement(PopulationModelParser.For_statementContext ctx) {
        return super.visitFor_statement(ctx);
    }

    @Override
    public String visitWhen_statement(PopulationModelParser.When_statementContext ctx) {
        return super.visitWhen_statement(ctx);
    }

    @Override
    public String visitRule_body(PopulationModelParser.Rule_bodyContext ctx) {
        return super.visitRule_body(ctx);
    }

    @Override
    public String visitMeasure_declaration(PopulationModelParser.Measure_declarationContext ctx) {
        return String.format(MEASURE_DECLARATION_CODE,ParseUtil.getMeasureName(ctx.name.getText()),ctx.expr().accept(expressionToJava));
    }

    @Override
    public String visitParam_declaration(PopulationModelParser.Param_declarationContext ctx) {
        return "";
    }
}
