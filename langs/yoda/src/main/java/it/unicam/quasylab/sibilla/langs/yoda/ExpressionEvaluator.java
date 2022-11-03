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

package it.unicam.quasylab.sibilla.langs.yoda;

import java.util.function.Function;

public class ExpressionEvaluator extends YodaModelBaseVisitor<Value> {

    private final Function<String, Double> resolver;
    private final Function<String, DataType> types;

    public ExpressionEvaluator(Function<String, Double> resolver, Function<String, DataType> types) {
        super();
        this.resolver = resolver;
        this.types = types;
    }

    public static int evalInteger(Function<String, DataType> types, Function<String, Double> resolver, YodaModelParser.ExprContext exprContext){
        ExpressionEvaluator evaluator = new ExpressionEvaluator(resolver,types);
        return exprContext.accept(evaluator).getIntValue();
    }

    public static double evalDouble(Function<String, DataType> types, Function<String, Double> resolver, YodaModelParser.ExprContext exprContext){
        ExpressionEvaluator evaluator = new ExpressionEvaluator(resolver, types);
        return exprContext.accept(evaluator).getDoubleValue();
    }

    public static boolean evalBoolean(Function<String, DataType> types, Function<String, Double> resolver, YodaModelParser.ExprContext exprContext){
        ExpressionEvaluator evaluator = new ExpressionEvaluator(resolver, types);
        return exprContext.accept(evaluator).getBooleanValue();
    }

    @Override
    public Value visitNegationExpression(YodaModelParser.NegationExpressionContext ctx) {
        return ctx.argument.accept(this).not();
    }

    @Override
    public Value visitExprBrackets(YodaModelParser.ExprBracketsContext ctx) {
        return ctx.expr().accept(this);
    }

    //TODO
    @Override
    public Value visitWeightedRandomExpression(YodaModelParser.WeightedRandomExpressionContext ctx) {
        return null;
    }

    @Override
    public Value visitFalse(YodaModelParser.FalseContext ctx) {
        return Value.FALSE;
    }

    //TODO
    @Override
    public Value visitMinimumExpression(YodaModelParser.MinimumExpressionContext ctx) {
        return null;
    }

    //TODO
    @Override
    public Value visitMaximumExpression(YodaModelParser.MaximumExpressionContext ctx) {
        return null;
    }

    @Override
    public Value visitMultdivOperation(YodaModelParser.MultdivOperationContext ctx) {
        return Value.applyOperation(ctx.leftOp.accept(this), ctx.oper.getText(), ctx.rightOp.accept(this));
    }

    //TODO
    @Override
    public Value visitRelationExpression(YodaModelParser.RelationExpressionContext ctx) {
        return null;
    }

    //TODO
    @Override
    public Value visitReference(YodaModelParser.ReferenceContext ctx) {
        return null;
    }

    /*
    //TODO
    @Override
    public Value visitRecordExpression(YodaModelParser.RecordExpressionContext ctx) {
        return null;
    }
    */

    @Override
    public Value visitOrExpression(YodaModelParser.OrExpressionContext ctx) {
        return ctx.leftOp.accept(this).or(ctx.rightOp.accept(this));
    }

    //TODO
    @Override
    public Value visitExponentOperation(YodaModelParser.ExponentOperationContext ctx) {
        return null;
    }

    @Override
    public Value visitRealValue(YodaModelParser.RealValueContext ctx) {
        return new Value.RealValue(Double.parseDouble(ctx.getText()));
    }

    @Override
    public Value visitAndExpression(YodaModelParser.AndExpressionContext ctx) {
        return ctx.leftOp.accept(this).and(ctx.rightOp.accept(this));
    }

    //TODO
    @Override
    public Value visitAdditionalOperation(YodaModelParser.AdditionalOperationContext ctx) {
        return null;
    }

    //TODO
    @Override
    public Value visitForallExpression(YodaModelParser.ForallExpressionContext ctx) {
        return null;
    }

    //TODO
    @Override
    public Value visitExistsExpression(YodaModelParser.ExistsExpressionContext ctx) {
        return null;
    }

    @Override
    public Value visitTrue(YodaModelParser.TrueContext ctx) {
        return Value.TRUE;
    }

    @Override
    public Value visitAddsubOperation(YodaModelParser.AddsubOperationContext ctx) {
        return Value.applyOperation(ctx.leftOp.accept(this), ctx.oper.getText(), ctx.rightOp.accept(this));
    }

    @Override
    public Value visitIntegerValue(YodaModelParser.IntegerValueContext ctx) {
        return new Value.IntegerValue(Integer.parseInt(ctx.getText()));
    }

    @Override
    public Value visitIfthenelseExpression(YodaModelParser.IfthenelseExpressionContext ctx) {
        return (ctx.guardExpr.accept(this).getBooleanValue()?ctx.thenBranch.accept(this):ctx.elseBranch.accept(this));
    }

    @Override
    public Value visitUnaryExpression(YodaModelParser.UnaryExpressionContext ctx) {
        return Value.applySign(ctx.oper.getText(), ctx.arg.accept(this));
    }

    //TODO
    @Override
    public Value visitRandomExpression(YodaModelParser.RandomExpressionContext ctx) {
        return null;
    }

    //TODO
    @Override
    public Value visitItselfRef(YodaModelParser.ItselfRefContext ctx) {
        return null;
    }

    //TODO
    @Override
    public Value visitAttributeRef(YodaModelParser.AttributeRefContext ctx) {
        return null;
    }

}
