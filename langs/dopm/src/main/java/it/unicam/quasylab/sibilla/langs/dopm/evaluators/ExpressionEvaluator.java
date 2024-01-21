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

package it.unicam.quasylab.sibilla.langs.dopm.evaluators;

import it.unicam.quasylab.sibilla.core.util.values.SibillaBoolean;
import it.unicam.quasylab.sibilla.core.util.values.SibillaDouble;
import it.unicam.quasylab.sibilla.core.util.values.SibillaInteger;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelBaseVisitor;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelParser;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This visitor is used to evaluate expressions as double. Whenever an expression of the wrong
 * type is considered, a Double.NaN is returned.
 */
public class ExpressionEvaluator extends DataOrientedPopulationModelBaseVisitor<BiFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, SibillaValue>> {


    public ExpressionEvaluator() {

    }


    @Override
    public BiFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, SibillaValue> visitExponentExpression(DataOrientedPopulationModelParser.ExponentExpressionContext ctx) {
        BiFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, SibillaValue> left = ctx.left.accept(this);
        BiFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, SibillaValue> right = ctx.right.accept(this);

        return (agent, sender) -> SibillaValue.eval(Math::pow, left.apply(agent,sender), right.apply(agent,sender));
    }

    @Override
    public BiFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, SibillaValue> visitReferenceExpression(DataOrientedPopulationModelParser.ReferenceExpressionContext ctx) {
        String name = ctx.reference.getText();
        return (agent, sender) -> agent.apply(name).orElse(SibillaValue.ERROR_VALUE);
    }

    @Override
    public BiFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, SibillaValue> visitSenderReferenceExpression(DataOrientedPopulationModelParser.SenderReferenceExpressionContext ctx) {
        String name = ctx.ID().getText();
        return (agent, sender) -> agent.apply(name).orElse(SibillaValue.ERROR_VALUE);
    }

    @Override
    public BiFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, SibillaValue> visitIntValue(DataOrientedPopulationModelParser.IntValueContext ctx) {
        int integer = Integer.parseInt(ctx.getText());
        return (agent, sender) -> new SibillaInteger(integer);
    }

    @Override
    public BiFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, SibillaValue> visitBracketExpression(DataOrientedPopulationModelParser.BracketExpressionContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public BiFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, SibillaValue> visitRealValue(DataOrientedPopulationModelParser.RealValueContext ctx) {
        double doublevalue = Double.parseDouble(ctx.getText());
        return (agent, sender) -> new SibillaDouble(doublevalue);
    }

    @Override
    public BiFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, SibillaValue> visitIfThenElseExpression(DataOrientedPopulationModelParser.IfThenElseExpressionContext ctx) {

        BiFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, SibillaValue> guard = ctx.guard.accept(this);
        BiFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, SibillaValue> thenBranch = ctx.thenBranch.accept(this);
        BiFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, SibillaValue> elseBranch = ctx.elseBranch.accept(this);

        return (agent, receiver) -> guard.apply(agent, receiver).booleanOf() ? thenBranch.apply(agent, receiver) : elseBranch.apply(agent, receiver);
    }

    @Override
    public BiFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, SibillaValue> visitNegationExpression(DataOrientedPopulationModelParser.NegationExpressionContext ctx) {
        BiFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, SibillaValue> expr = ctx.arg.accept(this);
        return (agent, receiver) -> SibillaValue.not(expr.apply(agent, receiver));
    }

    @Override
    public BiFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, SibillaValue> visitTrueValue(DataOrientedPopulationModelParser.TrueValueContext ctx) {
        return (agent, sender) -> SibillaBoolean.TRUE;
    }

    @Override
    public BiFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, SibillaValue> visitRelationExpression(DataOrientedPopulationModelParser.RelationExpressionContext ctx) {
        BiFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, SibillaValue> left = ctx.left.accept(this);
        BiFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, SibillaValue> right = ctx.right.accept(this);
        String operator = ctx.op.getText();

        return (agent, sender) -> SibillaBoolean.of(SibillaValue.getRelationOperator(operator).test(left.apply(agent,sender), right.apply(agent, sender)));
    }

    @Override
    public BiFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, SibillaValue> visitOrExpression(DataOrientedPopulationModelParser.OrExpressionContext ctx) {
        BiFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, SibillaValue> left = ctx.left.accept(this);
        BiFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, SibillaValue> right = ctx.right.accept(this);

        return (agent, sender) -> SibillaValue.or(left.apply(agent, sender), right.apply(agent, sender));
    }

    @Override
    public BiFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, SibillaValue> visitFalseValue(DataOrientedPopulationModelParser.FalseValueContext ctx) {
        return (agent, sender) -> SibillaBoolean.FALSE;
    }

    @Override
    public BiFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, SibillaValue> visitAndExpression(DataOrientedPopulationModelParser.AndExpressionContext ctx) {
        BiFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, SibillaValue> left = ctx.left.accept(this);
        BiFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, SibillaValue> right = ctx.right.accept(this);

        return (agent, sender) -> SibillaValue.and(left.apply(agent, sender), right.apply(agent, sender));
    }

    @Override
    protected BiFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, SibillaValue> defaultResult() {
        return (agent, sender) -> SibillaValue.ERROR_VALUE;
    }


    @Override
    public BiFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, SibillaValue> visitMulDivExpression(DataOrientedPopulationModelParser.MulDivExpressionContext ctx) {
        BiFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, SibillaValue> left = ctx.left.accept(this);
        BiFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, SibillaValue> right = ctx.right.accept(this);
        String operator = ctx.op.getText();

        return (agent, sender) -> SibillaValue.getOperator(operator).apply(left.apply(agent,sender),right.apply(agent, sender));
    }

    @Override
    public BiFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, SibillaValue>  visitAddSubExpression(DataOrientedPopulationModelParser.AddSubExpressionContext ctx) {
        BiFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, SibillaValue> left = ctx.left.accept(this);
        BiFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, SibillaValue> right = ctx.right.accept(this);
        String operator = ctx.op.getText();

        return (agent, sender) -> SibillaValue.getOperator(operator).apply(left.apply(agent,sender),right.apply(agent, sender));
    }

    @Override
    public BiFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, SibillaValue> visitUnaryExpression(DataOrientedPopulationModelParser.UnaryExpressionContext ctx) {
        if (ctx.op.getText().equals("-")) {
            BiFunction<Function<String, Optional<SibillaValue>>, Function<String, Optional<SibillaValue>>, SibillaValue> expr= ctx.arg.accept(this);
            return (agent, sender) -> SibillaValue.minus(expr.apply(agent, sender));
        } else {
            return ctx.arg.accept(this);
        }
    }

    /*public double evalDouble(DataOrientedPopulationModelParser.ExprContext expression) {
        return expression.accept(this).doubleOf();
    }

    public int evalInteger(DataOrientedPopulationModelParser.ExprContext expression) {
        return expression.accept(this).intOf();
    }*/

}
