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

import it.unicam.quasylab.sibilla.core.models.util.MappingState;
import it.unicam.quasylab.sibilla.core.models.util.VariableTable;
import it.unicam.quasylab.sibilla.core.util.values.SibillaBoolean;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import org.antlr.v4.runtime.ParserRuleContext;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.function.*;

public class StateExpressionEvaluator extends MarkovChainModelBaseVisitor<Function<MappingState, SibillaValue>> {

    private final Function<String, Optional<SibillaValue>> resolver;
    private final VariableTable table;


    public static Predicate<MappingState> evalStatePredicate(Function<String, Optional<SibillaValue>> resolver, VariableTable table, ParserRuleContext ctx) {
        Function<MappingState, SibillaValue> f = ctx.accept( new StateExpressionEvaluator(table, resolver));
        return s -> f.apply(s).booleanOf();
    }

    public static ToIntFunction<MappingState> evalToIntFunction(Function<String, Optional<SibillaValue>> resolver, VariableTable table, ParserRuleContext ctx) {
        Function<MappingState, SibillaValue> f = ctx.accept( new StateExpressionEvaluator(table, resolver));
        return s -> f.apply(s).intOf();
    }

    public static ToDoubleFunction<MappingState> evalToDoubleFunction(Function<String, Optional<SibillaValue>> resolver, VariableTable table, ParserRuleContext ctx) {
        Function<MappingState, SibillaValue> f = ctx.accept( new StateExpressionEvaluator(table, resolver));
        return s -> f.apply(s).doubleOf();
    }

    public StateExpressionEvaluator(VariableTable table, Function<String, Optional<SibillaValue>> resolver) {
        super();
        this.resolver = resolver;
        this.table = table;
    }

    @Override
    public Function<MappingState, SibillaValue> visitNegationExpression(MarkovChainModelParser.NegationExpressionContext ctx) {
        Function<MappingState, SibillaValue> fun = ctx.arg.accept(this);
        return s -> SibillaValue.not(fun.apply(s));
    }

    @Override
    public Function<MappingState, SibillaValue> visitExponentExpression(MarkovChainModelParser.ExponentExpressionContext ctx) {
        Function<MappingState, SibillaValue> left = ctx.left.accept(this);
        Function<MappingState, SibillaValue> right = ctx.right.accept(this);
        return s -> SibillaValue.eval(Math::pow, left.apply(s), right.apply(s));
    }

    @Override
    public Function<MappingState, SibillaValue> visitReferenceExpression(MarkovChainModelParser.ReferenceExpressionContext ctx) {
        String name = ctx.getText();
        if (table.contains(name)) {
            return getStateVariableValue(name);
        } else {
            return getValue(name);
        }
    }

    private Function<MappingState, SibillaValue> getValue(String name) {
        int idx = table.indexOf(name);
        return s -> s.get(idx);
    }

    private Function<MappingState, SibillaValue> getStateVariableValue(String name) {
        int idx = table.indexOf(name);
        return s -> s.get(idx);
    }

    @Override
    public Function<MappingState, SibillaValue> visitIntValue(MarkovChainModelParser.IntValueContext ctx) {
        SibillaValue n = SibillaValue.of(Integer.parseInt(ctx.getText()));
        return s -> n;
    }

    @Override
    public Function<MappingState, SibillaValue> visitTrueValue(MarkovChainModelParser.TrueValueContext ctx) {
        return s -> SibillaBoolean.TRUE;
    }

    @Override
    public Function<MappingState, SibillaValue> visitRelationExpression(MarkovChainModelParser.RelationExpressionContext ctx) {
        Function<MappingState, SibillaValue> left = ctx.left.accept(this);
        Function<MappingState, SibillaValue> right = ctx.right.accept(this);
        BiPredicate<SibillaValue, SibillaValue> op = SibillaValue.getRelationOperator(ctx.op.getText());
        return s -> SibillaValue.of(op.test(left.apply(s), right.apply(s)));
    }

    @Override
    public Function<MappingState, SibillaValue> visitBracketExpression(MarkovChainModelParser.BracketExpressionContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public Function<MappingState, SibillaValue> visitOrExpression(MarkovChainModelParser.OrExpressionContext ctx) {
        Function<MappingState, SibillaValue> left = ctx.left.accept(this);
        Function<MappingState, SibillaValue> right = ctx.right.accept(this);
        return s -> SibillaValue.or(left.apply(s), right.apply(s));
    }

    @Override
    public Function<MappingState, SibillaValue> visitIfThenElseExpression(MarkovChainModelParser.IfThenElseExpressionContext ctx) {
        Function<MappingState, SibillaValue> guard = ctx.guard.accept(this);
        Function<MappingState, SibillaValue> thenBranch = ctx.thenBranch.accept(this);
        Function<MappingState, SibillaValue> elseBranch = ctx.elseBranch.accept(this);
        return s -> (guard.apply(s).booleanOf()?thenBranch.apply(s):elseBranch.apply(s));
    }

    @Override
    public Function<MappingState, SibillaValue> visitFalseValue(MarkovChainModelParser.FalseValueContext ctx) {
        return s -> SibillaBoolean.FALSE;
    }

    @Override
    public Function<MappingState, SibillaValue> visitRealValue(MarkovChainModelParser.RealValueContext ctx) {
        SibillaValue v = SibillaValue.of(Double.parseDouble(ctx.getText()));
        return s -> v;
    }

    @Override
    public Function<MappingState, SibillaValue> visitAndExpression(MarkovChainModelParser.AndExpressionContext ctx) {
        Function<MappingState, SibillaValue> left = ctx.left.accept(this);
        Function<MappingState, SibillaValue> right = ctx.right.accept(this);
        return s -> SibillaValue.and(left.apply(s), right.apply(s));
    }

    @Override
    public Function<MappingState, SibillaValue> visitMulDivExpression(MarkovChainModelParser.MulDivExpressionContext ctx) {
        Function<MappingState, SibillaValue> left = ctx.left.accept(this);
        Function<MappingState, SibillaValue> right = ctx.right.accept(this);
        BinaryOperator<SibillaValue> op = SibillaValue.getOperator(ctx.op.getText());
        return s -> op.apply(left.apply(s), right.apply(s));
    }

    @Override
    public Function<MappingState, SibillaValue> visitAddSubExpression(MarkovChainModelParser.AddSubExpressionContext ctx) {
        Function<MappingState, SibillaValue> left = ctx.left.accept(this);
        Function<MappingState, SibillaValue> right = ctx.right.accept(this);
        BinaryOperator<SibillaValue> op = SibillaValue.getOperator(ctx.op.getText());
        return s -> op.apply(left.apply(s), right.apply(s));
    }

    @Override
    public Function<MappingState, SibillaValue> visitCastToIntExpression(MarkovChainModelParser.CastToIntExpressionContext ctx) {
        Function<MappingState, SibillaValue> arg = ctx.arg.accept(this);
        return s -> SibillaValue.of(arg.apply(s).intOf());
    }

    @Override
    public Function<MappingState, SibillaValue> visitUnaryExpression(MarkovChainModelParser.UnaryExpressionContext ctx) {
        if (ctx.op.equals("-")) {
            Function<MappingState, SibillaValue> arg = ctx.arg.accept(this);
            return s -> SibillaValue.minus(arg.apply(s));
        } else {
            return ctx.arg.accept(this);
        }
    }
}
