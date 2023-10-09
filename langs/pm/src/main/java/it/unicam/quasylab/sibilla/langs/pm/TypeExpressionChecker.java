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
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.LinkedList;
import java.util.List;

public class TypeExpressionChecker extends PopulationModelBaseVisitor<SymbolType> {

    private final SymbolTable table;
    private final boolean populationAllowed;
    private final boolean timeDependent;
    private final List<ModelBuildingError> errors;

    public TypeExpressionChecker(SymbolTable table) {
        this(table, new LinkedList<>());
    }

    public TypeExpressionChecker(SymbolTable table, List<ModelBuildingError> errors) {
        this(table,errors,false,false);
    }

    public TypeExpressionChecker(SymbolTable table, List<ModelBuildingError> errors, boolean populationAllowed, boolean timeDependent) {
        this.table = table;
        this.errors = errors;
        this.populationAllowed = populationAllowed;
        this.timeDependent = timeDependent;
    }

    public SymbolType checkNumber(ParserRuleContext ctx) {
        SymbolType t = ctx.accept(this);
        if (!t.isANumber()) {
            errors.add(ModelBuildingError.expectedNumber(t,ctx));
        }
        return t;
    }

    public boolean isANumber(ParserRuleContext ctx) {
        SymbolType t = ctx.accept(this);
        if (!t.isANumber()) {
            errors.add(ModelBuildingError.expectedNumber(t,ctx));
            return false;
        }
        return true;
    }

    public boolean isABoolean(ParserRuleContext ctx) {
        if (ctx == null) {
            return true;
        }
        SymbolType t = ctx.accept(this);
        if (t.isABoolean()) {
            return true;
        } else {
            errors.add(ModelBuildingError.expectedBoolean(t,ctx));
            return false;
        }
    }

    @Override
    public SymbolType visitNegationExpression(PopulationModelParser.NegationExpressionContext ctx) {
        isABoolean(ctx.arg);
        return SymbolType.BOOLEAN;
    }

    @Override
    public SymbolType visitExponentExpression(PopulationModelParser.ExponentExpressionContext ctx) {
        checkNumber(ctx.left);
        checkNumber(ctx.right);
        return SymbolType.NUMBER;
    }


    @Override
    public SymbolType visitReferenceExpression(PopulationModelParser.ReferenceExpressionContext ctx) {
        String name = ctx.reference.getText();
        if (table.isAReference(name)) {
            return table.getType(name);
        } else {
            if (table.isASpecies(name)) {
                errors.add(ModelBuildingError.illegalUseOfAgentIdentifier(ctx.reference));
            } else {
                errors.add(ModelBuildingError.unknownSymbol(name,ctx.reference.getLine(),ctx.reference.getCharPositionInLine()));
            }
            return SymbolType.ERROR;
        }
    }

    @Override
    public SymbolType visitIfThenElseExpression(PopulationModelParser.IfThenElseExpressionContext ctx) {
        isABoolean(ctx.guard);
        SymbolType t1 = ctx.thenBranch.accept(this);
        SymbolType t2 = ctx.elseBranch.accept(this);
        if (t1 == SymbolType.ERROR) {
            return t2;
        }
        if (t2 == SymbolType.ERROR) {
            return t1;
        }
        if (t1!=t2) {
            errors.add(ModelBuildingError.typeError(t1,t2,ctx.thenBranch));
        }
        return t1;
    }



    /*
        forStatement: 'for' '(' assignment=expr ';' condition=expr ';' update=expr ')' expr;
     */
//    public SymbolType visitForStatementExpression(PopulationModelParser.forStatment ctx){
//        int assignment  = visit(ctx.assignment);
//        Predicate condition  = visit(ctx.codition);
//
//
//        while (evaluateCondition(ctx.condition)) {
//            visit(ctx.expr);
//
//        }
//    }

    @Override
    public SymbolType visitIntValue(PopulationModelParser.IntValueContext ctx) {
        return SymbolType.NUMBER;
    }

    @Override
    public SymbolType visitTrueValue(PopulationModelParser.TrueValueContext ctx) {
        return SymbolType.BOOLEAN;
    }

    @Override
    public SymbolType visitRelationExpression(PopulationModelParser.RelationExpressionContext ctx) {
        return SymbolType.BOOLEAN;
    }

    @Override
    public SymbolType visitBracketExpression(PopulationModelParser.BracketExpressionContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public SymbolType visitPopulationFractionExpression(PopulationModelParser.PopulationFractionExpressionContext ctx) {
        if (!populationAllowed) {
            errors.add(ModelBuildingError.illegalPopulationExpression(ctx));
        }
        return SymbolType.NUMBER;
    }

    @Override
    public SymbolType visitOrExpression(PopulationModelParser.OrExpressionContext ctx) {
        isABoolean(ctx.left);
        isABoolean(ctx.right);
        return SymbolType.BOOLEAN;
    }

    @Override
    public SymbolType visitFalseValue(PopulationModelParser.FalseValueContext ctx) {
        return SymbolType.BOOLEAN;
    }

    @Override
    public SymbolType visitRealValue(PopulationModelParser.RealValueContext ctx) {
        return SymbolType.NUMBER;
    }

    @Override
    public SymbolType visitAndExpression(PopulationModelParser.AndExpressionContext ctx) {
        isABoolean(ctx.left);
        isABoolean(ctx.right);
        return SymbolType.BOOLEAN;
    }

    @Override
    public SymbolType visitMulDivExpression(PopulationModelParser.MulDivExpressionContext ctx) {
        return checkBinaryNumberOperation(ctx.left,ctx.right);
    }

    private SymbolType checkBinaryNumberOperation(PopulationModelParser.ExprContext left, PopulationModelParser.ExprContext right) {
        SymbolType t1 = checkNumber(left);
        SymbolType t2 = checkNumber(right);
        if (!t1.isANumber()) {
            errors.add(ModelBuildingError.typeError(SymbolType.NUMBER,t1,left));
        }
        if (!t2.isANumber()) {
            errors.add(ModelBuildingError.typeError(SymbolType.NUMBER,t2,right));
        }
        return SymbolType.NUMBER;

    }

    @Override
    public SymbolType visitPopulationSizeExpression(PopulationModelParser.PopulationSizeExpressionContext ctx) {
        if (!populationAllowed) {
            errors.add(ModelBuildingError.illegalPopulationExpression(ctx));
        }
        return SymbolType.NUMBER;
    }

    @Override
    public SymbolType visitAddSubExpression(PopulationModelParser.AddSubExpressionContext ctx) {
        return checkBinaryNumberOperation(ctx.left,ctx.right);
    }

    @Override
    public SymbolType visitUnaryExpression(PopulationModelParser.UnaryExpressionContext ctx) {
        return checkNumber(ctx.arg);
    }

//    @Override
//    public SymbolType visitNowExpression(PopulationModelParser.NowExpressionContext ctx) {
//        if (!timeDependent) {
//            errors.add(ModelBuildingError.illegalUseOfNow(ctx.start.getLine(),ctx.start.getCharPositionInLine()));
//        }
//        return SymbolType.REAL;
//    }

}
