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
import org.antlr.v4.runtime.Token;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PopulationExpressionChecker extends NumberExpressionChecker {

    public PopulationExpressionChecker(List<ModelBuildingError> errorList, SymbolTable table) {
        super(errorList, table, new HashSet<>());
    }

    public PopulationExpressionChecker(List<ModelBuildingError> errorList, SymbolTable table, Set<String> localVariables) {
        super(errorList, table, localVariables);
    }

    @Override
    public Boolean visitPopulationFractionExpression(PopulationModelParser.PopulationFractionExpressionContext ctx) {
        return ctx.species_expression().accept(this);
    }

    @Override
    public Boolean visitPopulationSizeExpression(PopulationModelParser.PopulationSizeExpressionContext ctx) {
        return ctx.species_expression().accept(this);
    }

    @Override
    public Boolean visitSpecies_expression(PopulationModelParser.Species_expressionContext ctx) {
        if (!checkSpeciesName(ctx.name)) { return false; }
        if (!checkSpeciesArity(ctx)) { return false; }
        Set<String> localVariables = checkSpeciesVariables(ctx.local_variables());
        if (localVariables == null) { return false; }
        NumberExpressionChecker checker = new NumberExpressionChecker(this.errorList, this.table,localVariables );
        if ((ctx.guard_expression()!=null)&&!ctx.guard_expression().accept(checker.getBooleanExpressionChecker())) {
            return false;
        }
        localVariables.addAll(this.localVariables);
        if ((ctx.guard_expression() != null)&&!ctx.guard_expression().accept(checker.getBooleanExpressionChecker())) {
            return false;
        }
        for(PopulationModelParser.ExprContext arg: ctx.expr()) {
            if (!arg.accept(checker)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkSpeciesArity(PopulationModelParser.Species_expressionContext ctx) {
        String species = ctx.name.getText();
        int actualArity = ctx.expr().size();
        int expected = this.table.getSpeciesContext(species).range().size();
        if (actualArity != expected) {
            this.errorList.add(ModelBuildingError.wrongNumberOfSpeciesParameters(expected,ctx));
            return false;
        } else {
            return true;
        }
    }

    private boolean checkSpeciesName(Token species) {
        if (!this.table.isASpecies(species.getText())) {
            this.errorList.add(ModelBuildingError.unknownAgent(species));
            return false;
        } else {
            return true;
        }
    }

    private Set<String> checkSpeciesVariables(PopulationModelParser.Local_variablesContext local_variables) {
        Set<String> localVariables = new HashSet<>();
        if (local_variables != null) {
            NumberExpressionChecker checker = new NumberExpressionChecker(this.errorList,this.table,localVariables);
            for(PopulationModelParser.Local_variableContext lv: local_variables.variables) {
                ParserRuleContext ctx = this.table.getContext(lv.name.getText());
                if (ctx != null) {
                    this.errorList.add(ModelBuildingError.duplicatedName(lv.name.getText(),ctx,lv));
                    return null;
                }
                if (!lv.range().min.accept(checker)||!lv.range().max.accept(checker)) {
                    return null;
                }
                localVariables.add(lv.name.getText());
            }
            localVariables.addAll(this.localVariables);
        }
        return localVariables;
    }

    @Override
    protected BooleanExpressionChecker getBooleanExpressionChecker() {
        return new PopulationPredicateChecker(this.errorList, this);
    }
}
