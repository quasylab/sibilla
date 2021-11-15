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

package it.unicam.quasylab.sibilla.langs.markov;

import it.unicam.quasylab.sibilla.langs.util.ErrorCollector;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Map;
import java.util.function.Function;

public class TypeVisitor extends MarkovChainModelBaseVisitor<DataType> {

    private final Function<String, DataType> table;
    private final ErrorCollector errors;

    public TypeVisitor(Function<String, DataType> table, ErrorCollector errors) {
        this.table = table;
        this.errors = errors;
    }

    public static DataType getTypeOf(ErrorCollector errorList, ParserRuleContext ctx) {
        return getTypeOf(errorList, (v -> DataType.NONE), ctx);
    }

    public static DataType getTypeOf(ErrorCollector errorList, Function<String, DataType> types, ParserRuleContext ctx) {
        return ctx.accept(new TypeVisitor(types, errorList));
    }


    @Override
    public DataType visitNegationExpression(MarkovChainModelParser.NegationExpressionContext ctx) {
        checkType(DataType.BOOLEAN, ctx.arg);
        return DataType.BOOLEAN;
    }

    public boolean checkType(DataType expected, MarkovChainModelParser.ExprContext arg) {
        DataType actual = arg.accept(this);
        if (!actual.isSubtypeOf(expected)) {
            errors.record(ParseUtil.wrongTypeError(expected, actual, arg));
            return false;
        } else {
            return true;
        }
    }

    @Override
    public DataType visitReferenceExpression(MarkovChainModelParser.ReferenceExpressionContext ctx) {
        String name = ctx.reference.getText();
        DataType result = table.apply(name);
        if (result == null) {
            errors.record(ParseUtil.unknownSymbol(name, ctx));
        }
        return result;
    }

    //TODO: Override castToIntExpression case!

    @Override
    public DataType visitIntValue(MarkovChainModelParser.IntValueContext ctx) {
        return DataType.INTEGER;
    }

    @Override
    public DataType visitTrueValue(MarkovChainModelParser.TrueValueContext ctx) {
        return DataType.BOOLEAN;
    }

    @Override
    public DataType visitRelationExpression(MarkovChainModelParser.RelationExpressionContext ctx) {
        checkNumber(ctx.left);
        checkNumber(ctx.right);
        return DataType.BOOLEAN;
    }

    public DataType checkNumber(MarkovChainModelParser.ExprContext expr) {
        DataType t = expr.accept(this);
        if (!t.isANumber()) {
            errors.record(ParseUtil.expectedNumberError(t, expr));
            return DataType.INTEGER;
        }
        return t;
    }

    @Override
    public DataType visitOrExpression(MarkovChainModelParser.OrExpressionContext ctx) {
        checkType(DataType.BOOLEAN, ctx.left);
        checkType(DataType.BOOLEAN, ctx.right);
        return DataType.BOOLEAN;
    }

    @Override
    public DataType visitIfThenElseExpression(MarkovChainModelParser.IfThenElseExpressionContext ctx) {
        checkType(DataType.BOOLEAN, ctx.guard);
        return DataType.merge(ctx.thenBranch.accept(this), ctx.elseBranch.accept(this));
    }

    @Override
    public DataType visitFalseValue(MarkovChainModelParser.FalseValueContext ctx) {
        return DataType.BOOLEAN;
    }

    @Override
    public DataType visitRealValue(MarkovChainModelParser.RealValueContext ctx) {
        return DataType.REAL;
    }

    @Override
    public DataType visitAndExpression(MarkovChainModelParser.AndExpressionContext ctx) {
        checkType(DataType.BOOLEAN, ctx.left);
        checkType(DataType.BOOLEAN, ctx.right);
        return DataType.BOOLEAN;
    }

    @Override
    public DataType visitMulDivExpression(MarkovChainModelParser.MulDivExpressionContext ctx) {
        DataType t1 = checkNumber(ctx.left);
        DataType t2 = checkNumber(ctx.right);
        return DataType.merge(t1, t2);
    }

    @Override
    public DataType visitAddSubExpression(MarkovChainModelParser.AddSubExpressionContext ctx) {
        DataType t1 = checkNumber(ctx.left);
        DataType t2 = checkNumber(ctx.right);
        return DataType.merge(t1, t2);
    }

    @Override
    public DataType visitUnaryExpression(MarkovChainModelParser.UnaryExpressionContext ctx) {
        return checkNumber(ctx.arg);
    }

    @Override
    public DataType visitBracketExpression(MarkovChainModelParser.BracketExpressionContext ctx) {
        return ctx.expr().accept(this);
    }


}
