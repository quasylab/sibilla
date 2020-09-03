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

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ModelValidator extends PopulationModelBaseVisitor<Boolean> {

    private final List<ModelBuildingError> errors;
    private SymbolTable table;

    public ModelValidator() {
        this.errors = new LinkedList<>();
        this.table = new BasicSymbolTable();
    }

    @Override
    public Boolean visitSystem_declaration(PopulationModelParser.System_declarationContext ctx) {
        boolean flag = true;
        for(Token t: ctx.args) {
            flag &= recordVariable(t.getText(),ctx,SymbolType.INT);
        }
        flag &= ctx.species_pattern().accept(this);
        for(Token t: ctx.args) {
            this.table.deleteVariable(t.getText());
        }
        return flag;
    }

    private boolean recordVariable(String name, ParserRuleContext ctx, SymbolType type) {
        try {
            this.table.addVariable(name,ctx,type);
            return true;
        } catch (DuplicatedSymbolException e) {
            errors.add(new ModelBuildingError(e.getMessage()));
            return false;
        }
    }

    @Override
    public Boolean visitConst_declaration(PopulationModelParser.Const_declarationContext ctx) {
        TypeExpressionChecker checker = new TypeExpressionChecker(table,errors);
        try {
            table.addConstant(ctx.name.getText(),ctx,checker.visit(ctx.expr()));
            return true;
        } catch (DuplicatedSymbolException e) {
            errors.add(new ModelBuildingError(e.getMessage()));
            return false;
        }
    }

    @Override
    public Boolean visitSpecies_declaration(PopulationModelParser.Species_declarationContext ctx) {
        try {
            table.addSpecies(ctx.name.getText(), ctx);
            return true;
        } catch (DuplicatedSymbolException e) {
            errors.add(new ModelBuildingError(e.getMessage()));
            return false;
        }
    }

    @Override
    public Boolean visitRule_declaration(PopulationModelParser.Rule_declarationContext ctx) {
        try {
            this.table.addRule(ctx.name.getText(),ctx);
            return ctx.rulestatement().accept(this);
        } catch (DuplicatedSymbolException e) {
            errors.add(new ModelBuildingError(e.getMessage()));
            return false;
        }
    }

    @Override
    public Boolean visitMeasure_declaration(PopulationModelParser.Measure_declarationContext ctx) {
        return super.visitMeasure_declaration(ctx);
    }

    @Override
    public Boolean visitFor_statement(PopulationModelParser.For_statementContext ctx) {
        boolean flag = recordVariable(ctx.name.getText(),ctx,SymbolType.INT);
        TypeExpressionChecker checker = new TypeExpressionChecker(table,errors);
        flag &= checker.isAnInteger(ctx.range().min)
                & checker.isAnInteger(ctx.range().max)
                & ctx.next.accept(this);
        return flag;
    }

    @Override
    public Boolean visitWhen_statement(PopulationModelParser.When_statementContext ctx) {
        TypeExpressionChecker checker = new TypeExpressionChecker(table,errors);
        return checker.isABoolean(ctx.guard) & (ctx.arg.accept(this));
    }

    @Override
    public Boolean visitRule_body(PopulationModelParser.Rule_bodyContext ctx) {
        TypeExpressionChecker checker = new TypeExpressionChecker(table,errors);
        return checker.isABoolean(ctx.guard)&checker.isANumber(ctx.rate)&ctx.pre.accept(this)&ctx.post.accept(this);
    }

    @Override
    public Boolean visitSpecies_pattern(PopulationModelParser.Species_patternContext ctx) {
        TypeExpressionChecker checker = new TypeExpressionChecker(table, errors);
        boolean flag = true;
        for (PopulationModelParser.Species_pattern_elementContext species: ctx.species_pattern_element()) {
            flag &= validateSpeciesPatter(species.species_expression());
            flag &= checker.isAnInteger(species.size);
        }
        return flag;
    }

    private boolean validateSpeciesPatter(PopulationModelParser.Species_expressionContext species) {
        boolean flag = this.table.isASpecies(species.name.getText());
        if (flag) {
            checkInstanceArity(species);
        }
        TypeExpressionChecker checker = new TypeExpressionChecker(table, errors);
        for (PopulationModelParser.ExprContext e: species.expr()) {
            flag &= checker.isAnInteger(e);
        }
        return flag;
    }

    private void checkInstanceArity(PopulationModelParser.Species_expressionContext species) {
        int expected = this.table.arity(species.name.getText());
        int actual = species.expr().size();
        if (expected != actual) {
            errors.add(ModelBuildingError.wrongNumberOfSpeciesParameters(expected,species));
        }
    }

    @Override
    public Boolean visitParam_declaration(PopulationModelParser.Param_declarationContext ctx) {
        TypeExpressionChecker checker = new TypeExpressionChecker(table,errors);
        try {
            table.addParameter(ctx.name.getText(),ctx,checker.visit(ctx.expr()));
            return true;
        } catch (DuplicatedSymbolException e) {
            errors.add(new ModelBuildingError(e.getMessage()));
            return false;
        }
    }
}
