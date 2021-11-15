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
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

public class StateExpressionEvaluator extends MarkovChainModelBaseVisitor<LazyValue<MappingState>> {

    private final Function<String, Double> resolver;
    private final Function<String, DataType> types;
    private final VariableTable table;


    public static Predicate<MappingState> evalStatePredicate(Function<String, DataType> types, Function<String, Double> resolver, VariableTable table, ParserRuleContext ctx) {
        return ctx.accept( new StateExpressionEvaluator(table, resolver, types)).getToBooleanFunction();
    }

    public static ToIntFunction<MappingState> evalToIntFunction(Function<String, DataType> types, Function<String, Double> resolver, VariableTable table, ParserRuleContext ctx) {
        return ctx.accept( new StateExpressionEvaluator(table, resolver, types)).getToIntegerFunction();
    }

    public static ToDoubleFunction<MappingState> evalToDoubleFunction(Function<String, DataType> types, Function<String, Double> resolver, VariableTable table, ParserRuleContext ctx) {
        return ctx.accept( new StateExpressionEvaluator(table, resolver, types)).getToDoubleFunction();
    }

    public StateExpressionEvaluator(VariableTable table, Function<String, Double> resolver, Function<String, DataType> types) {
        super();
        this.types = types;
        this.resolver = resolver;
        this.table = table;
    }

    @Override
    public LazyValue<MappingState> visitNegationExpression(MarkovChainModelParser.NegationExpressionContext ctx) {
        return ctx.arg.accept(this).not();
    }

    @Override
    public LazyValue<MappingState> visitExponentExpression(MarkovChainModelParser.ExponentExpressionContext ctx) {
        return ctx.left.accept(this).pow(ctx.right.accept(this));
    }

    @Override
    public LazyValue<MappingState> visitReferenceExpression(MarkovChainModelParser.ReferenceExpressionContext ctx) {
        String name = ctx.getText();
        DataType type = types.apply(name);
        if (table.contains(name)) {
            return getStateVariableValue(name, type);
        } else {
            return getValue(name, type);
        }
    }

    private LazyValue<MappingState> getValue(String name, DataType type) {
        int idx = table.indexOf(name);
        double value = resolver.apply(name);
        switch (type) {
            case BOOLEAN: return new LazyValue.LazyBoolean<>(s -> (value>0));
            case INTEGER: return new LazyValue.LazyInteger<>(s -> (int) value);
            case REAL: return new LazyValue.LazyReal<>(s -> value);
            default:
                return new LazyValue.NoneLazyValue<>();
        }
    }

    private LazyValue<MappingState> getStateVariableValue(String name, DataType type) {
        int idx = table.indexOf(name);
        switch (type) {
            case BOOLEAN: return new LazyValue.LazyBoolean<>(s -> s.getDoubleValue(idx)>0);
            case INTEGER: return new LazyValue.LazyInteger<>(s -> s.getIntValue(idx));
            case REAL: return new LazyValue.LazyReal<>(s -> s.getDoubleValue(idx));
            default:
                return new LazyValue.NoneLazyValue<>();
        }
    }

    @Override
    public LazyValue<MappingState> visitIntValue(MarkovChainModelParser.IntValueContext ctx) {
        int n = Integer.parseInt(ctx.getText());
        return new LazyValue.LazyInteger<>(s -> n);
    }

    @Override
    public LazyValue<MappingState> visitTrueValue(MarkovChainModelParser.TrueValueContext ctx) {
        return new LazyValue.LazyBoolean<>(s -> true);
    }

    @Override
    public LazyValue<MappingState> visitRelationExpression(MarkovChainModelParser.RelationExpressionContext ctx) {
        return LazyValue.evalRelation(ctx.left.accept(this), ctx.op.getText(), ctx.right.accept(this));
    }

    @Override
    public LazyValue<MappingState> visitBracketExpression(MarkovChainModelParser.BracketExpressionContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public LazyValue<MappingState> visitOrExpression(MarkovChainModelParser.OrExpressionContext ctx) {
        return ctx.left.accept(this).or(ctx.right.accept(this));
    }

    @Override
    public LazyValue<MappingState> visitIfThenElseExpression(MarkovChainModelParser.IfThenElseExpressionContext ctx) {
        LazyValue<MappingState> guard = ctx.guard.accept(this);
        LazyValue<MappingState> thenBranch = ctx.thenBranch.accept(this);
        LazyValue<MappingState> elseBranch = ctx.elseBranch.accept(this);
        return LazyValue.ifThenElse(guard, thenBranch, elseBranch);
    }

    @Override
    public LazyValue<MappingState> visitFalseValue(MarkovChainModelParser.FalseValueContext ctx) {
        return new LazyValue.LazyBoolean<>(s -> false);
    }

    @Override
    public LazyValue<MappingState> visitRealValue(MarkovChainModelParser.RealValueContext ctx) {
        double v = Double.parseDouble(ctx.getText());
        return new LazyValue.LazyReal<>(s -> v);
    }

    @Override
    public LazyValue<MappingState> visitAndExpression(MarkovChainModelParser.AndExpressionContext ctx) {
        return ctx.left.accept(this).and(ctx.right.accept(this));
    }

    @Override
    public LazyValue<MappingState> visitMulDivExpression(MarkovChainModelParser.MulDivExpressionContext ctx) {
        return  LazyValue.apply(ctx.left.accept(this), ctx.op.getText(), ctx.right.accept(this));
    }

    @Override
    public LazyValue<MappingState> visitAddSubExpression(MarkovChainModelParser.AddSubExpressionContext ctx) {
        return  LazyValue.apply(ctx.left.accept(this), ctx.op.getText(), ctx.right.accept(this));
    }

    @Override
    public LazyValue<MappingState> visitCastToIntExpression(MarkovChainModelParser.CastToIntExpressionContext ctx) {
        return ctx.arg.accept(this).cast(DataType.INTEGER);
    }

    @Override
    public LazyValue<MappingState> visitUnaryExpression(MarkovChainModelParser.UnaryExpressionContext ctx) {
        switch (ctx.op.getText()) {
            case "+": return ctx.arg.accept(this).plus();
            case "-": return ctx.arg.accept(this).minus();
            default:
                return null;
        }
    }
}
