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

import it.unicam.quasylab.sibilla.langs.util.ErrorCollector;
import it.unicam.quasylab.sibilla.langs.util.ParseError;

import java.util.Map;

/**
 * A visitor used to infer type of expressions
 */
public class TypeInferenceVisitor extends LIOModelBaseVisitor<LIOType> {

    private final SymbolTable table;
    private final ErrorCollector errors;
    private final Map<String, LIOType> localVariables;

    public TypeInferenceVisitor(SymbolTable table, Map<String, LIOType> localVariables, ErrorCollector errors) {
        this.table = table;
        this.errors = errors;
        this.localVariables = localVariables;
    }

    @Override
    public LIOType visitExpressionConjunction(LIOModelParser.ExpressionConjunctionContext ctx) {
        LIOType left = ctx.left.accept(this);
        LIOType right = ctx.right.accept(this);
        LIOType result = LIOType.LIO_BOOLEAN;
        if (!left.isABoolean()) {
            this.errors.record(new ParseError(ParseUtil.booleanValueExpected(left, ctx.left.start),ctx.left.start.getLine(), ctx.left.start.getCharPositionInLine()));
            result = LIOType.LIO_NONE;
        }
        if (!right.isABoolean()) {
            this.errors.record(new ParseError(ParseUtil.booleanValueExpected(right, ctx.left.start),ctx.right.start.getLine(), ctx.right.start.getCharPositionInLine()));
            result = LIOType.LIO_NONE;
        }
        return result;
    }

    @Override
    public LIOType visitExpressionReference(LIOModelParser.ExpressionReferenceContext ctx) {
        String name = ctx.reference.getText();
        if (localVariables.containsKey(name)) {
            return localVariables.get(name);
        }
        if (table.isParameter(name)||table.isConstant(name)) {
            LIOType typeOf = table.getTypeOf(name);
            if (typeOf == LIOType.LIO_NONE) {
                this.errors.record(new ParseError(ParseUtil.illegalUseOfUnboundNameError(ctx.reference),ctx.reference.getLine(), ctx.reference.getCharPositionInLine()));
            }
            return typeOf;
        }
        this.errors.record(new ParseError(ParseUtil.illegalUseOfNameError(ctx.reference),ctx.reference.getLine(), ctx.reference.getCharPositionInLine()));
        return LIOType.LIO_NONE;
    }

    @Override
    public LIOType visitExpressionSumDiff(LIOModelParser.ExpressionSumDiffContext ctx) {
        return inferNumerical(ctx.left, ctx.right);
    }

    private LIOType inferNumerical(LIOModelParser.ExprContext e1, LIOModelParser.ExprContext e2) {
        LIOType left = e1.accept(this);
        LIOType right = e2.accept(this);
        if (!left.isANumber()) {
            this.errors.record(new ParseError(ParseUtil.numericalValueExpected(left, e1.start),e1.start.getLine(), e1.start.getCharPositionInLine()));
            return LIOType.LIO_NONE;
        }
        if (!right.isANumber()) {
            this.errors.record(new ParseError(ParseUtil.numericalValueExpected(right, e2.start),e2.start.getLine(), e2.start.getCharPositionInLine()));
            return LIOType.LIO_NONE;
        }
        return LIOType.combine(left, right);
    }

    @Override
    public LIOType visitExpressionMulDiv(LIOModelParser.ExpressionMulDivContext ctx) {
        return inferNumerical(ctx.left, ctx.right);
    }

    @Override
    public LIOType visitExpressionUnary(LIOModelParser.ExpressionUnaryContext ctx) {
        LIOType type = ctx.arg.accept(this);
        if (type.isANumber()) {
            return type;
        } else {
            this.errors.record(new ParseError(ParseUtil.numericalValueExpected(type, ctx.arg.start),ctx.arg.start.getLine(), ctx.arg.start.getCharPositionInLine()));
            return LIOType.LIO_NONE;
        }
    }

    @Override
    public LIOType visitExpressionPower(LIOModelParser.ExpressionPowerContext ctx) {
        return inferNumerical(ctx.left, ctx.right);
    }

    @Override
    public LIOType visitExpressionBracket(LIOModelParser.ExpressionBracketContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public LIOType visitExpressionDisjunction(LIOModelParser.ExpressionDisjunctionContext ctx) {
        LIOType left = ctx.left.accept(this);
        LIOType right = ctx.right.accept(this);
        LIOType result = LIOType.LIO_BOOLEAN;
        if (!left.isABoolean()) {
            this.errors.record(new ParseError(ParseUtil.booleanValueExpected(left, ctx.left.start),ctx.left.start.getLine(), ctx.left.start.getCharPositionInLine()));
            result = LIOType.LIO_NONE;
        }
        if (!right.isABoolean()) {
            this.errors.record(new ParseError(ParseUtil.booleanValueExpected(right, ctx.left.start),ctx.right.start.getLine(), ctx.right.start.getCharPositionInLine()));
            result = LIOType.LIO_NONE;
        }
        return result;
    }

    @Override
    public LIOType visitExpressionIfThenElse(LIOModelParser.ExpressionIfThenElseContext ctx) {
        LIOType guardType = ctx.guard.accept(this);
        LIOType thenType = ctx.thenBranch.accept(this);
        LIOType elseType = ctx.elseBranch.accept(this);
        LIOType result = LIOType.combine(thenType, elseType);
        if (LIOType.LIO_NONE == result) {
            this.errors.record(new ParseError(ParseUtil.typeErrorMessage(thenType, elseType, ctx.elseBranch.start),ctx.elseBranch.start.getLine(), ctx.elseBranch.start.getCharPositionInLine()));
        }
        if (!guardType.isABoolean()) {
            this.errors.record(new ParseError(ParseUtil.booleanValueExpected(guardType, ctx.guard.start),ctx.guard.start.getLine(), ctx.guard.start.getCharPositionInLine()));
            result = LIOType.LIO_NONE;
        }
        if (guardType.isAFunction()) {
            result = result.toFunction();
        }
        return result;
    }

    @Override
    public LIOType visitExpressionRelation(LIOModelParser.ExpressionRelationContext ctx) {
        LIOType left = ctx.left.accept(this);
        LIOType right = ctx.right.accept(this);
        LIOType result = LIOType.LIO_BOOLEAN;
        if (!left.isANumber()) {
            this.errors.record(new ParseError(ParseUtil.numericalValueExpected(left, ctx.left.start),ctx.left.start.getLine(), ctx.left.start.getCharPositionInLine()));
            result = LIOType.LIO_NONE;
        }
        if (!right.isANumber()) {
            this.errors.record(new ParseError(ParseUtil.numericalValueExpected(right, ctx.right.start),ctx.right.start.getLine(), ctx.right.start.getCharPositionInLine()));
            result = LIOType.LIO_NONE;
        }
        if (LIOType.combine(left, right).isAFunction()) {
            result = result.toFunction();
        }
        return result;
    }

    @Override
    public LIOType visitExpressionTrue(LIOModelParser.ExpressionTrueContext ctx) {
        return LIOType.LIO_BOOLEAN;
    }

    @Override
    public LIOType visitExpressionNegation(LIOModelParser.ExpressionNegationContext ctx) {
        LIOType left = ctx.arg.accept(this);
        if (!left.isABoolean()) {
            this.errors.record(new ParseError(ParseUtil.booleanValueExpected(left, ctx.arg.start),ctx.arg.start.getLine(), ctx.arg.start.getCharPositionInLine()));
            return LIOType.LIO_NONE;
        } else {
            return LIOType.LIO_BOOLEAN;
        }
    }

    @Override
    public LIOType visitExpressionReal(LIOModelParser.ExpressionRealContext ctx) {
        return LIOType.LIO_REAL;
    }

    @Override
    public LIOType visitExpressionFalse(LIOModelParser.ExpressionFalseContext ctx) {
        return LIOType.LIO_BOOLEAN;
    }

    @Override
    public LIOType visitExpressionFractionOfAgents(LIOModelParser.ExpressionFractionOfAgentsContext ctx) {
        String name = ctx.agent.getText();
        if (table.isPredicate(name)||table.isState(name)) {
            return LIOType.LIO_MEASURE;
        } else {
            this.errors.record(new ParseError(ParseUtil.illegalUseOfNameError(ctx.agent),ctx.agent.getLine(), ctx.agent.getCharPositionInLine()));
            return LIOType.LIO_NONE;
        }
    }

    @Override
    public LIOType visitExpressionNumberOfAgents(LIOModelParser.ExpressionNumberOfAgentsContext ctx) {
        String name = ctx.agent.getText();
        if (table.isPredicate(name)||table.isState(name)) {
            return LIOType.LIO_MEASURE;
        } else {
            this.errors.record(new ParseError(ParseUtil.illegalUseOfNameError(ctx.agent),ctx.agent.getLine(), ctx.agent.getCharPositionInLine()));
            return LIOType.LIO_NONE;
        }
    }

    @Override
    public LIOType visitExpressionInteger(LIOModelParser.ExpressionIntegerContext ctx) {
        return LIOType.LIO_INTEGER;
    }

    /**
     * Returns the type associated with the given expression and using the given table to solve
     * types of symbols.
     *
     * @param table symbol table used to resolve used names in the expression
     * @param errors list used to store the errors found while inferring the expression type
     * @param expr an expression
     * @return the type associated with the given expression.
     */
    public static LIOType inferTypeOf(SymbolTable table, ErrorCollector errors, LIOModelParser.ExprContext expr) {
        return inferTypeOf(Map.of(), table, errors, expr);
    }

    /**
     * Returns the type associated with the given expression and using the given table to solve
     * types of symbols.
     *
     * @param localVariables local variables that can be used in the expression
     * @param table symbol table used to resolve used names in the expression
     * @param errors list used to store the errors found while inferring the expression type
     * @param expr an expression
     * @return the type associated with the given expression.
     */
    public static LIOType inferTypeOf(Map<String, LIOType> localVariables, SymbolTable table, ErrorCollector errors, LIOModelParser.ExprContext expr) {
        return expr.accept(new TypeInferenceVisitor(table, localVariables, errors));
    }

    /**
     * Checks if the given expression has a type compatible with the given expected type. The type of expression is
     * inferred by using the given symbol table. In errors are found while inferring the type these are stored in the
     * given list.
     *
     * @param table symbol table used to resolve used names in the expression
     * @param errors list used to store the errors found while inferring the expression type
     * @param expectedType expected type.
     * @param expr  expression to check.
     * @return true if the given expression has a type compatible with the given expected type.
     */
    public static boolean hasType(SymbolTable table, ErrorCollector errors, LIOType expectedType, LIOModelParser.ExprContext expr) {
        return hasType(Map.of(), table, errors, expectedType, expr);
    }

    /**
     * Checks if the given expression has a type compatible with the given expected type. The type of expression is
     * inferred by using the given symbol table. In errors are found while inferring the type these are stored in the
     * given list.
     *
     * @param localVariables local variables that can be used in the expression
     * @param table symbol table used to resolve used names in the expression
     * @param errors list used to store the errors found while inferring the expression type
     * @param expectedType expected type.
     * @param expr  expression to check.
     * @return true if the given expression has a type compatible with the given expected type.
     */
    public static boolean hasType(Map<String, LIOType> localVariables, SymbolTable table, ErrorCollector errors, LIOType expectedType, LIOModelParser.ExprContext expr) {
        LIOType actualType = inferTypeOf(localVariables, table, errors, expr);
        if (expectedType.compatibleWith(actualType)) {
            return true;
        } else {
            errors.record(new ParseError(ParseUtil.typeErrorMessage(expectedType, actualType, expr.start),expr.start.getLine(), expr.start.getCharPositionInLine()));
            return false;
        }
    }
}
