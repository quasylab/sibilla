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

package it.unicam.quasylab.sibilla.langs.lio;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import it.unicam.quasylab.sibilla.langs.util.ParseError;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.math3.analysis.function.Exp;

public class ExpressionChecker extends LIOModelBaseVisitor<Boolean> {

    private static final String TYPE_ERROR_MESSAGE = "Type error! Expected %s, is %s.";
    private static final String ILLEGAL_USE_OF_POPULATION_EXPRESSION =  "Population expressions cannot be used in this context!";
    private static final String UNKNOWN_STATE_ERROR =  "%s is not an agent state.";
    private static final String UNKNOWN_SYMBOL_ERROR =  "Symbol %s is unknown.";

    private final SymbolTable table;
    private final Set<String> parameters;

    public ExpressionChecker(ExpressionChecker expressionChecker, ExpressionType type) {
        this(expressionChecker.errors, expressionChecker.table, expressionChecker.parameters,type,expressionChecker.isPopulation);
    }

    public enum ExpressionType {
        BOOLEAN,
        INTEGER,
        REAL,
        NUMBER,
        STATE,
        ACTION,
        SYSTEM;

        boolean compatible(ExpressionType type) {
            if (this==type) {
                return true;
            }
            switch (this) {
                case BOOLEAN:
                case INTEGER:
                    return false;
                case REAL:
                    return (type == INTEGER)||(type==NUMBER);
                case NUMBER:
                    return (type==INTEGER)||(type==REAL);
            }
            return false;
        }

        public String getDescription() {
            switch (this) {
                case BOOLEAN: return "a boolean";
                case INTEGER: return "an integer";
                case REAL: return "a real";
                case NUMBER: return "a number";
                case STATE: return "a state identifier";
                case ACTION: return "an action";
                case SYSTEM: return "a system";
            }
            return null;
        }

        public boolean isANumber() {
            return (this==REAL)||(this==INTEGER)||(this==NUMBER);
        }
    }

    private final List<ParseError> errors;

    private final ExpressionType type;

    private final boolean isPopulation;


    public static boolean checkBooleanExpression(List<ParseError> errors, SymbolTable table, Set<String> parameters, ParseTree tree) {
        return check(errors,table, parameters, ExpressionType.BOOLEAN, false, tree);
    }

    public static boolean checkBooleanExpression(List<ParseError> errors, SymbolTable table, Set<String> parameters, boolean isPopulation, ParseTree tree) {
        return check(errors,table, parameters, ExpressionType.BOOLEAN, isPopulation, tree);
    }

    public static boolean checkIntegerExpression(List<ParseError> errors, SymbolTable table, Set<String> parameters, ParseTree tree) {
        return check(errors,table, parameters, ExpressionType.INTEGER, false, tree);
    }

    public static boolean checkIntegerExpression(List<ParseError> errors, SymbolTable table, Set<String> parameters, boolean isPopulation, ParseTree tree) {
        return check(errors,table, parameters, ExpressionType.INTEGER, isPopulation, tree);
    }

    public static boolean checkRealExpression(List<ParseError> errors, SymbolTable table, Set<String> parameters, ParseTree tree) {
        return check(errors,table, parameters, ExpressionType.REAL, false, tree);
    }

    public static boolean checkRealExpression(List<ParseError> errors, SymbolTable table, Set<String> parameters, boolean isPopulation, ParseTree tree) {
        return check(errors,table, parameters, ExpressionType.REAL, isPopulation, tree);
    }

    public static boolean check(List<ParseError> errors, SymbolTable table, Set<String> parameters, ExpressionType type, boolean isPopulation, ParseTree tree) {
        ExpressionChecker checker = new ExpressionChecker(errors, table, parameters, type, isPopulation);
        return tree.accept(checker);
    }

    public ExpressionChecker(List<ParseError> errors,
                             SymbolTable table,
                             Set<String> parameters,
                             ExpressionType type,
                             boolean isPopulation) {
        this.errors = errors;
        this.type = type;
        this.isPopulation = isPopulation;
        this.table = table;
        this.parameters = parameters;
    }

    public synchronized void recordError(String message, int line, int offset) {
        errors.add(new ParseError(message,line,offset));
    }

    public synchronized boolean withErrors() {
        return !errors.isEmpty();
    }

    public List<ParseError> getErrors() {
        return errors;
    }

    @Override
    public Boolean visitIntValue(LIOModelParser.IntValueContext ctx) {
        boolean flag = true;
        if (!type.compatible(ExpressionType.INTEGER)) {
            flag = false;
            recordTypeError(ExpressionType.INTEGER,ctx);
        }
        return flag;
    }

    private void recordTypeError(ExpressionType actual, ParserRuleContext ctx) {
        recordError(String.format(TYPE_ERROR_MESSAGE,type.getDescription(),actual.getDescription()),ctx.start.getLine(),ctx.start.getCharPositionInLine());
    }

    @Override
    public Boolean visitTrueValue(LIOModelParser.TrueValueContext ctx) {
        if (type != ExpressionType.BOOLEAN) {
            recordTypeError(ExpressionType.BOOLEAN,ctx);
            return false;
        }
        return true;
    }

    @Override
    public Boolean visitPopulationFractionExpression(LIOModelParser.PopulationFractionExpressionContext ctx) {
        if (!isPopulation) {
            recordIllegalUsePopulationExpression(ctx);
            return false;
        }
        if (type.compatible(typeOf(ctx.agent.getText()))) {
            recordNameIsNotAState(ctx.agent);
            return false;
        }
        return true;
    }

    private void recordNameIsNotAState(Token agent) {
        recordError(String.format(UNKNOWN_STATE_ERROR,agent.getText()),agent.getLine(),agent.getCharPositionInLine());
    }

    private void recordIllegalUsePopulationExpression(ParserRuleContext ctx) {
        recordError(ILLEGAL_USE_OF_POPULATION_EXPRESSION,ctx.start.getLine(),ctx.start.getCharPositionInLine());
    }

    @Override
    public Boolean visitFalseValue(LIOModelParser.FalseValueContext ctx) {
        if (type != ExpressionType.BOOLEAN) {
            recordTypeError(ExpressionType.BOOLEAN,ctx);
            return false;
        }
        return true;
    }

    @Override
    public Boolean visitRealValue(LIOModelParser.RealValueContext ctx) {
        if (!type.compatible(ExpressionType.REAL)) {
            recordTypeError(ExpressionType.REAL,ctx);
            return false;
        }
        return true;
    }

    @Override
    public Boolean visitPopulationSizeExpression(LIOModelParser.PopulationSizeExpressionContext ctx) {
        if (!isPopulation) {
            recordIllegalUsePopulationExpression(ctx);
            return false;
        }
        if (type.compatible(typeOf(ctx.agent.getText()))) {
            recordNameIsNotAState(ctx.agent);
            return false;
        }
        return true;
    }


    @Override
    public Boolean visitNegationExpression(LIOModelParser.NegationExpressionContext ctx) {
        boolean flag = true;
        if (type != ExpressionType.BOOLEAN) {
            recordTypeError(ExpressionType.BOOLEAN,ctx);
            return false;
        } else {
            return ctx.arg.accept(this);
        }
    }

    @Override
    public Boolean visitExponentExpression(LIOModelParser.ExponentExpressionContext ctx) {
        boolean flag = true;
        if (!type.compatible(ExpressionType.REAL)) {
            recordTypeError(ExpressionType.REAL,ctx);
            return false;
        }
        return ctx.left.accept(this) & ctx.right.accept(this);
    }

    @Override
    public Boolean visitReferenceExpression(LIOModelParser.ReferenceExpressionContext ctx) {
        if (!table.isDeclared(ctx.reference.getText())&&!parameters.contains(ctx.reference.getText())) {
            recordError(String.format(UNKNOWN_SYMBOL_ERROR,ctx.reference.getText()),ctx.reference.getLine(),ctx.reference.getCharPositionInLine());
        }
        ExpressionType actual = typeOf(ctx.reference.getText());
        if (!type.compatible(actual)) {
            recordTypeError(actual,ctx);
            return false;
        }
        return true;
    }

    @Override
    public Boolean visitRelationExpression(LIOModelParser.RelationExpressionContext ctx) {
        if (type != ExpressionType.BOOLEAN) {
            recordTypeError(ExpressionType.BOOLEAN,ctx);
            return false;
        }
        ExpressionChecker checker = new ExpressionChecker(this,ExpressionType.NUMBER);
        return ctx.left.accept(checker) & ctx.right.accept(checker);
    }

    @Override
    public Boolean visitOrExpression(LIOModelParser.OrExpressionContext ctx) {
        if (type != ExpressionType.BOOLEAN) {
            recordTypeError(ExpressionType.BOOLEAN,ctx);
            return false;
        }
        return ctx.left.accept(this) & ctx.right.accept(this);
    }

    @Override
    public Boolean visitIfThenElseExpression(LIOModelParser.IfThenElseExpressionContext ctx) {
        boolean flag = true;
        if (type == ExpressionType.BOOLEAN) {
            flag = ctx.guard.accept(this);
        } else {
            ExpressionChecker checker = new ExpressionChecker(this, ExpressionType.BOOLEAN);
            flag = ctx.guard.accept(checker);
        }
        return flag & ctx.thenBranch.accept(this) & ctx.elseBranch.accept(this);
    }

    @Override
    public Boolean visitAndExpression(LIOModelParser.AndExpressionContext ctx) {
        if (type != ExpressionType.BOOLEAN) {
            recordTypeError(ExpressionType.BOOLEAN,ctx);
            return false;
        }
        return ctx.left.accept(this) & ctx.right.accept(this);
    }

    @Override
    public Boolean visitMulDivExpression(LIOModelParser.MulDivExpressionContext ctx) {
        if (!type.isANumber()) {
            recordTypeError(ExpressionType.NUMBER,ctx);
            return false;
        }
        return ctx.left.accept(this) & ctx.right.accept(this);
    }

    @Override
    public Boolean visitAddSubExpression(LIOModelParser.AddSubExpressionContext ctx) {
        if (!type.isANumber()) {
            recordTypeError(ExpressionType.NUMBER,ctx);
            return false;
        }
        return ctx.left.accept(this) & ctx.right.accept(this);
    }

    @Override
    public Boolean visitUnaryExpression(LIOModelParser.UnaryExpressionContext ctx) {
        if (!type.isANumber()) {
            recordTypeError(ExpressionType.NUMBER,ctx);
            return false;
        }
        return ctx.arg.accept(this);
    }

    @Override
    protected Boolean defaultResult() {
        return false;
    }

    public ExpressionType typeOf(String name) {
        if (parameters.contains(name)) {
            return ExpressionType.INTEGER;
        }
        if (table.isAConstant(name)||table.isAParameter(name)) {
            return ExpressionType.REAL;
        }
        if (table.isAState(name)) {
            return ExpressionType.STATE;
        }
        if (table.isAnAction(name)) {
            return ExpressionType.ACTION;
        }
        if (table.isASystem(name)) {
            return ExpressionType.SYSTEM;
        }
        return null;
    }


}


