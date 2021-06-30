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

import org.antlr.v4.runtime.ParserRuleContext;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ModelValidator extends PopulationModelBaseVisitor<Boolean> {

    //TODO: Check circular dependencies
    //TODO: Parameters can only depends on constants
    private final List<ModelBuildingError> errors;
    private final SymbolTable table;

    public ModelValidator() {
        this(new LinkedList<>());
    }

    public ModelValidator(List<ModelBuildingError> errors) {
        this.errors = errors;
        this.table = new BasicSymbolTable();
    }

    @Override
    public Boolean visitModel(PopulationModelParser.ModelContext ctx) {
        boolean result = true;
        for (PopulationModelParser.ElementContext e: ctx.element()) {
            result &= e.accept(this);
        }
        return result;
    }

    @Override
    public Boolean visitLabel_declaration(PopulationModelParser.Label_declarationContext ctx) {
        try {
            this.table.addLabel(ctx.name.getText(),ctx);
            Set<String> localVariables = checkLocalVariables(ctx.local_variables());
            return (localVariables != null)
                    && checkGuardExpression(localVariables,ctx.guard_expression())
                    && checkSpeciesExpressionList(localVariables, ctx.species_expression());
        } catch (DuplicatedSymbolException e) {
            this.errors.add(new ModelBuildingError(e.getMessage()));
            return false;
        }
    }

    private boolean checkRangeList(Set<String> localVariables, List<PopulationModelParser.RangeContext> rangeContextList) {
        for (PopulationModelParser.RangeContext range: rangeContextList) {
            if (!checkNumericalExpression(new HashSet<>(), range.min)) { return false; }
            if (!checkNumericalExpression(new HashSet<>(), range.max)) { return false; }
        }
        return true;
    }

    private boolean checkGuardExpression(Set<String> localVariables, PopulationModelParser.Guard_expressionContext guard_expression) {
        if (guard_expression != null) {
            return checkBooleanExpression(localVariables, guard_expression.guard);
        }
        return true;
    }

    @Override
    protected Boolean defaultResult() {
        return true;
    }

    @Override
    public Boolean visitSystem_declaration(PopulationModelParser.System_declarationContext ctx) {
        try {
            this.table.addSystem(ctx.name.getText(), ctx);
            return checkSpeciesPattern(this.table.checkLocalVariables(ctx.args, ctx), ctx.species_pattern());
        } catch (DuplicatedSymbolException e) {
            this.errors.add(new ModelBuildingError(e.getMessage()));
            return false;
        }
    }


    @Override
    public Boolean visitConst_declaration(PopulationModelParser.Const_declarationContext ctx) {
        try {
            table.addConstant(ctx.name.getText(),ctx,SymbolType.NUMBER);
            return checkNumericalExpression(new HashSet<>(), ctx.expr());
        } catch (DuplicatedSymbolException e) {
            errors.add(new ModelBuildingError(e.getMessage()));
            return false;
        }
    }

    @Override
    public Boolean visitSpecies_declaration(PopulationModelParser.Species_declarationContext ctx) {
        try {
            table.addSpecies(ctx.name.getText(), ctx);
            return checkRangeList(new HashSet<>(), ctx.range());
        } catch (DuplicatedSymbolException e) {
            errors.add(new ModelBuildingError(e.getMessage()));
            return false;
        }
    }

    @Override
    public Boolean visitMeasure_declaration(PopulationModelParser.Measure_declarationContext ctx) {
        try {
            table.addMeasure(ctx.name.getText(),ctx);
            Set<String> localVariables = checkLocalVariables(ctx.local_variables());
            return (localVariables != null)
                    && checkGuardExpression(localVariables,ctx.guard_expression())
                    && checkPopulationExpression(localVariables, ctx.expr());
        } catch (DuplicatedSymbolException e) {
            errors.add(new ModelBuildingError(e.getMessage()));
            return false;
        }
    }

    @Override
    public Boolean visitRule_declaration(PopulationModelParser.Rule_declarationContext ctx) {
        try {
            this.table.addRule(ctx.name.getText(),ctx);
            Set<String> localVariables = checkLocalVariables(ctx.local_variables());
            return  (localVariables!=null)
                    && checkGuardExpression(localVariables,ctx.guard_expression())
                    && checkRuleBody(localVariables, ctx.rule_body());
        } catch (DuplicatedSymbolException e) {
            errors.add(new ModelBuildingError(e.getMessage()));
            return false;
        }
    }

    private Set<String> checkLocalVariables(PopulationModelParser.Local_variablesContext localVariables) {
        if (localVariables == null) {
            return new HashSet<>();
        }
        try {
            Set<String> variableNames = this.table.checkLocalVariables(localVariables);
            if (checkRangeList(new HashSet<>(),
                    localVariables.variables.stream().map(PopulationModelParser.Local_variableContext::range).collect(Collectors.toList()))) {
                return variableNames;
            }
        } catch (DuplicatedSymbolException e) {
            errors.add(new ModelBuildingError(e.getMessage()));
        }
        return null;
    }

    private boolean checkRuleBody(Set<String> localVariables, PopulationModelParser.Rule_bodyContext rule_body) {
        return checkPopulationPredicate(localVariables, rule_body.guard) &&
                checkSpeciesPattern(localVariables, rule_body.pre) &&
                checkPopulationExpression(localVariables, rule_body.rate) &&
                checkSpeciesPattern(localVariables, rule_body.post);
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

    public SymbolTable getSymbolTable() {
        return table;
    }

    public int getNumberOfValidationErrors() {
        return errors.size();
    }

    public List<ModelBuildingError> getErrors() {
        return errors;
    }


    public boolean checkBooleanExpression(Set<String> localVariables, PopulationModelParser.ExprContext expr) {
        NumberExpressionChecker checker = new NumberExpressionChecker(this.errors, this.table, localVariables);
        return expr.accept( checker.getBooleanExpressionChecker() );
    }

    public boolean checkNumericalExpression(Set<String> localVariables, PopulationModelParser.ExprContext expr) {
        NumberExpressionChecker checker = new NumberExpressionChecker(this.errors, this.table, localVariables);
        return expr.accept(checker);
    }

    public boolean checkSpeciesExpressionList(Set<String> localVariables, List<PopulationModelParser.Species_expressionContext> speciesExpressions) {
        for (PopulationModelParser.Species_expressionContext se: speciesExpressions) {
            if (!checkSpeciesExpression(localVariables, se)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkSpeciesExpression(Set<String> localVariables, PopulationModelParser.Species_expressionContext speciesExpression) {
        PopulationExpressionChecker checker = new PopulationExpressionChecker(this.errors, this.table, localVariables);
        return speciesExpression.accept(checker);
    }

    private Boolean checkSpeciesPattern(Set<String> checkLocalVariables, PopulationModelParser.Species_patternContext pattern) {
        for (PopulationModelParser.Species_pattern_elementContext element: pattern.species_pattern_element()) {
            if (!checkSpeciesPatternElement(checkLocalVariables, element)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkSpeciesPatternElement(Set<String> localVariables, PopulationModelParser.Species_pattern_elementContext element) {
        return checkSpeciesExpression(localVariables, element.species_expression()) &&
                ((element.size == null)||checkNumericalExpression(localVariables, element.size));
    }

    private boolean checkPopulationExpression(Set<String> localVariables, PopulationModelParser.ExprContext species_expression) {
        return species_expression.accept(new PopulationExpressionChecker(this.errors, this.table, localVariables));
    }

    private boolean checkPopulationPredicate(Set<String> localVariables, PopulationModelParser.ExprContext species_expression) {
        return (species_expression == null)||species_expression.accept(new PopulationExpressionChecker(this.errors, this.table, localVariables).getBooleanExpressionChecker());
    }

}
