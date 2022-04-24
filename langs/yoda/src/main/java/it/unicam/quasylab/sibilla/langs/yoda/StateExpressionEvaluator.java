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

import it.unicam.quasylab.sibilla.core.models.util.MappingState;
import it.unicam.quasylab.sibilla.core.models.util.VariableTable;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

public class StateExpressionEvaluator extends YodaModelBaseVisitor<LazyValue<MappingState>>{
    private final Function<String, DataType> types;
    private final Function<String, Double> resolver;
    private final VariableTable table;

    public StateExpressionEvaluator(Function<String, DataType> types, Function<String, Double> resolver, VariableTable table){
        super();
        this.types=types;
        this.resolver=resolver;
        this.table=table;
    }

    public static Predicate<MappingState> evalStatePredicate(Function<String,DataType> types, Function<String, Double> resolver, VariableTable table, ParserRuleContext ctx){
        return ctx.accept(new StateExpressionEvaluator(types, resolver, table)).getToBooleanFunction();
    }

    public static ToIntFunction<MappingState> evalToIntFunction(Function<String,DataType> types, Function<String, Double> resolver, VariableTable table, ParserRuleContext ctx){
        return ctx.accept(new StateExpressionEvaluator(types,resolver,table)).getToIntegerFunction();
    }

    public static ToDoubleFunction<MappingState> evalToDoubleFunction(Function<String,DataType> types, Function<String, Double> resolver, VariableTable table, ParserRuleContext ctx){
        return ctx.accept(new StateExpressionEvaluator(types,resolver,table)).getToDoubleFunction();
    }

    private LazyValue<MappingState> getStateVariableValue(String name, DataType type) {
        int index = table.indexOf(name);
        switch (type){
            case BOOLEAN: return new LazyValue.LazyBoolean<>(s -> s.getDoubleValue(index)>0);
            case REAL: return new LazyValue.LazyReal<>(s -> s.getDoubleValue(index));
            case INTEGER: return new LazyValue.LazyInteger<>(s -> s.getIntValue(index));
            default: return new LazyValue.NoneLazyValue<>();
        }
    }

    private LazyValue<MappingState> getValue(String name, DataType type) {
        //int index = table.indexOf(name);
        double value = resolver.apply(name);
        switch (type){
            case INTEGER: return new LazyValue.LazyInteger<>(s -> (int) value);
            case REAL: return new LazyValue.LazyReal<>(s -> value);
            case BOOLEAN: return new LazyValue.LazyBoolean<>(s -> (value>0));
            default: return new LazyValue.NoneLazyValue<>();
        }
    }

    @Override
    public LazyValue<MappingState> visitIntegerValue(YodaModelParser.IntegerValueContext ctx) {
        int n = Integer.parseInt(ctx.getText());
        return new LazyValue.LazyInteger<>(s -> n);
    }

    @Override
    public LazyValue<MappingState> visitRealValue(YodaModelParser.RealValueContext ctx) {
        double n = Double.parseDouble(ctx.getText());
        return new LazyValue.LazyReal<>(s -> n);
    }

    @Override
    public LazyValue<MappingState> visitFalse(YodaModelParser.FalseContext ctx) {
        return new LazyValue.LazyBoolean<>(s -> false);
    }

    @Override
    public LazyValue<MappingState> visitTrue(YodaModelParser.TrueContext ctx) {
        return new LazyValue.LazyBoolean<>(s -> true);
    }

    @Override
    public LazyValue<MappingState> visitReference(YodaModelParser.ReferenceContext ctx) {
        String name = ctx.getText();
        DataType type = types.apply(name);
        if (table.contains(name)){
            return getStateVariableValue(name, type);
        }else {
            return getValue(name, type);
        }
    }

    @Override
    public LazyValue<MappingState> visitExprBrackets(YodaModelParser.ExprBracketsContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public LazyValue<MappingState> visitAddsubOperation(YodaModelParser.AddsubOperationContext ctx) {
        return LazyValue.apply(ctx.leftOp.accept(this), ctx.oper.getText(), ctx.rightOp.accept(this));
    }

    @Override
    public LazyValue<MappingState> visitMultdivOperation(YodaModelParser.MultdivOperationContext ctx) {
        return LazyValue.apply(ctx.leftOp.accept(this), ctx.oper.getText(), ctx.rightOp.accept(this));
    }

    @Override
    public LazyValue<MappingState> visitAdditionalOperation(YodaModelParser.AdditionalOperationContext ctx) {
        return LazyValue.apply(ctx.leftOp.accept(this), ctx.oper.getText(), ctx.rightOp.accept(this));
    }

    @Override
    public LazyValue<MappingState> visitUnaryExpression(YodaModelParser.UnaryExpressionContext ctx) {
        switch (ctx.oper.getText()){
            case "+": ctx.arg.accept(this).plus();
            case "-": ctx.arg.accept(this).minus();
            default: return null;
        }
    }

    @Override
    public LazyValue<MappingState> visitExponentOperation(YodaModelParser.ExponentOperationContext ctx) {
        return ctx.leftOp.accept(this).pow(ctx.rightOp.accept(this));
    }

    @Override
    public LazyValue<MappingState> visitNegationExpression(YodaModelParser.NegationExpressionContext ctx) {
        return ctx.argument.accept(this).not();
    }

    @Override
    public LazyValue<MappingState> visitAndExpression(YodaModelParser.AndExpressionContext ctx) {
        return ctx.leftOp.accept(this).and(ctx.rightOp.accept(this));
    }

    @Override
    public LazyValue<MappingState> visitOrExpression(YodaModelParser.OrExpressionContext ctx) {
        return ctx.leftOp.accept(this).or(ctx.rightOp.accept(this));
    }

    @Override
    public LazyValue<MappingState> visitRelationExpression(YodaModelParser.RelationExpressionContext ctx) {
        return LazyValue.evalRelation(ctx.leftOp.accept(this), ctx.oper.getText(), ctx.rightOp.accept(this));
    }

    @Override
    public LazyValue<MappingState> visitIfthenelseExpression(YodaModelParser.IfthenelseExpressionContext ctx) {
        LazyValue<MappingState> guard = ctx.guardExpr.accept(this);
        LazyValue<MappingState> thenBranch = ctx.thenBranch.accept(this);
        LazyValue<MappingState> elseBranch = ctx.elseBranch.accept(this);
        return LazyValue.IfThenElse(guard, thenBranch, elseBranch);
    }

    //TODO
    @Override
    public LazyValue<MappingState> visitRecordExpression(YodaModelParser.RecordExpressionContext ctx) {
        return null;
    }

    //TODO
    @Override
    public LazyValue<MappingState> visitWeightedRandomExpression(YodaModelParser.WeightedRandomExpressionContext ctx) {
        return null;
    }

    //TODO
    @Override
    public LazyValue<MappingState> visitRandomExpression(YodaModelParser.RandomExpressionContext ctx) {
        return null;
    }

    //TODO
    @Override
    public LazyValue<MappingState> visitAttributeRef(YodaModelParser.AttributeRefContext ctx) {
        return null;
    }

    //TODO
    @Override
    public LazyValue<MappingState> visitForallExpression(YodaModelParser.ForallExpressionContext ctx) {
        return null;
    }

    //TODO
    @Override
    public LazyValue<MappingState> visitExistsExpression(YodaModelParser.ExistsExpressionContext ctx) {
        return null;
    }

    //TODO
    @Override
    public LazyValue<MappingState> visitMinimumExpression(YodaModelParser.MinimumExpressionContext ctx) {
        return null;
    }

    //TODO
    @Override
    public LazyValue<MappingState> visitMaximumExpression(YodaModelParser.MaximumExpressionContext ctx) {
        return null;
    }

    //TODO
    @Override
    public LazyValue<MappingState> visitItselfRef(YodaModelParser.ItselfRefContext ctx) {
        return null;
    }
}

