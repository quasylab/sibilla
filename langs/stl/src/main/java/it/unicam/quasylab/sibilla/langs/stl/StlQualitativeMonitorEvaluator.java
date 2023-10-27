/*
 *  Sibilla:  a Java framework designed to support analysis of Collective
 *  Adaptive Systems.
 *
 *              Copyright (C) ${YEAR}.
 *
 *  See the NOTICE file distributed with this work for additional information
 *  regarding copyright ownership.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *    or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package it.unicam.quasylab.sibilla.langs.stl;

import it.unicam.quasylab.sibilla.core.tools.stl.QualitativeMonitor;
import it.unicam.quasylab.sibilla.core.util.Interval;
import it.unicam.quasylab.sibilla.langs.slam.StlModelBaseVisitor;
import it.unicam.quasylab.sibilla.langs.slam.StlModelParser;

import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

public class StlQualitativeMonitorEvaluator<S> extends StlModelBaseVisitor<Function<Map<String, Double>,QualitativeMonitor<S>>> {

    private final Map<String, Double> constants;

    private final Map<String, ToDoubleFunction<S>> measures;

    public StlQualitativeMonitorEvaluator(Map<String, Double> constants, Map<String, ToDoubleFunction<S>> measures) {
        this.constants = constants;
        this.measures = measures;
    }

    public StlQualitativeMonitorEvaluator(Map<String, ToDoubleFunction<S>> measures) {
        this(Map.of(), measures);
    }


    private Function<Map<String, Double>, Interval> getInterval(StlModelParser.IntervalContext interval) {
        StlExpressionEvaluator expressionEvaluator = new StlExpressionEvaluator(constants);
        ToDoubleFunction<Map<String, Double>> fromEvaluator = interval.from.accept(expressionEvaluator);
        ToDoubleFunction<Map<String, Double>> toEvaluator = interval.to.accept(expressionEvaluator);
        return m -> new Interval(fromEvaluator.applyAsDouble(m), toEvaluator.applyAsDouble(m));
    }

    @Override
    public Function<Map<String, Double>,QualitativeMonitor<S>> visitStlFomulaGlobally(StlModelParser.StlFomulaGloballyContext ctx) {
        Function<Map<String, Double>, QualitativeMonitor<S>> argumentEvaluationFunction = ctx.arg.accept(this);
        Function<Map<String, Double>, Interval> intervalEvaluationFunction = getInterval(ctx.interval());
        return m -> QualitativeMonitor.monitorGlobally(intervalEvaluationFunction.apply(m), argumentEvaluationFunction.apply(m));
    }


    @Override
    public Function<Map<String, Double>,QualitativeMonitor<S>> visitStlFormulaEventually(StlModelParser.StlFormulaEventuallyContext ctx) {
        Function<Map<String, Double>, QualitativeMonitor<S>> argumentEvaluationFunction = ctx.arg.accept(this);
        Function<Map<String, Double>, Interval> intervalEvaluationFunction = getInterval(ctx.interval());
        return m -> QualitativeMonitor.monitorFinally(intervalEvaluationFunction.apply(m), argumentEvaluationFunction.apply(m));
    }

    @Override
    public Function<Map<String, Double>,QualitativeMonitor<S>> visitStlFormulaNot(StlModelParser.StlFormulaNotContext ctx) {
        Function<Map<String, Double>, QualitativeMonitor<S>> argumentEvaluationFunction = ctx.argument.accept(this);
        return m -> QualitativeMonitor.monitorNegation(argumentEvaluationFunction.apply(m));
    }

    @Override
    public Function<Map<String, Double>,QualitativeMonitor<S>> visitStlFormulaOr(StlModelParser.StlFormulaOrContext ctx) {
        Function<Map<String, Double>, QualitativeMonitor<S>> leftArgumentEvaluation = ctx.left.accept(this);
        Function<Map<String, Double>, QualitativeMonitor<S>> rightArgumentEvaluation = ctx.right.accept(this);
        return m -> QualitativeMonitor.monitorDisjuction(leftArgumentEvaluation.apply(m), rightArgumentEvaluation.apply(m));
    }

    @Override
    public Function<Map<String, Double>,QualitativeMonitor<S>> visitStlFormulaAnd(StlModelParser.StlFormulaAndContext ctx) {
        Function<Map<String, Double>, QualitativeMonitor<S>> leftArgumentEvaluation = ctx.left.accept(this);
        Function<Map<String, Double>, QualitativeMonitor<S>> rightArgumentEvaluation = ctx.right.accept(this);
        return m -> QualitativeMonitor.monitorConjunction(leftArgumentEvaluation.apply(m), rightArgumentEvaluation.apply(m));
    }

    @Override
    public Function<Map<String, Double>,QualitativeMonitor<S>> visitStlFormulaAtomic(StlModelParser.StlFormulaAtomicContext ctx) {
        StlMonitorExpressionEvaluator<S> expressionEvaluator = new StlMonitorExpressionEvaluator<>(constants, measures);
        Function<Map<String, Double>, ToDoubleFunction<S>> leftEvaluation = ctx.left.accept(expressionEvaluator);
        Function<Map<String, Double>, ToDoubleFunction<S>> rightEvaluation = ctx.right.accept(expressionEvaluator);
        BiPredicate<Double, Double> op = getRelationOperator(ctx.op.getText());
        return m -> {
            ToDoubleFunction<S> leftExpression = leftEvaluation.apply(m);
            ToDoubleFunction<S> rightExpression = rightEvaluation.apply(m);
            return QualitativeMonitor.atomicMonitor(s -> op.test(leftExpression.applyAsDouble(s), rightExpression.applyAsDouble(s)));
        };
    }

    private BiPredicate<Double, Double> getRelationOperator(String op) {
        return switch (op) {
            case "<" -> (x, y) -> x < y;
            case "<=" -> (x, y) -> x <= y;
            case "==" -> (x, y) -> x.doubleValue() == y.doubleValue();
            case "!=" -> (x, y) -> x.doubleValue() != y.doubleValue();
            case ">=" -> (x, y) -> x >= y;
            case ">" -> (x, y) -> x > y;
            default -> (x, y) -> false;
        };
    }

    @Override
    public Function<Map<String, Double>,QualitativeMonitor<S>> visitStlFormulaTrue(StlModelParser.StlFormulaTrueContext ctx) {
        return m -> QualitativeMonitor.monitorTrue();
    }

    @Override
    public Function<Map<String, Double>,QualitativeMonitor<S>> visitStlFormulaFalse(StlModelParser.StlFormulaFalseContext ctx) {
        return m -> QualitativeMonitor.monitorFalse();
    }

    @Override
    public Function<Map<String, Double>,QualitativeMonitor<S>> visitStlFormulaImply(StlModelParser.StlFormulaImplyContext ctx) {
        Function<Map<String, Double>, QualitativeMonitor<S>> leftArgumentEvaluation = ctx.left.accept(this);
        Function<Map<String, Double>, QualitativeMonitor<S>> rightArgumentEvaluation = ctx.right.accept(this);
        return m -> QualitativeMonitor.monitorImplication(leftArgumentEvaluation.apply(m), rightArgumentEvaluation.apply(m));
    }

    @Override
    public Function<Map<String, Double>,QualitativeMonitor<S>> visitStlFormulaBracket(StlModelParser.StlFormulaBracketContext ctx) {
        return ctx.stlFormula().accept(this);
    }

    @Override
    public Function<Map<String, Double>,QualitativeMonitor<S>> visitStlFormulaIfAndOnlyIf(StlModelParser.StlFormulaIfAndOnlyIfContext ctx) {
        Function<Map<String, Double>, QualitativeMonitor<S>> leftArgumentEvaluation = ctx.left.accept(this);
        Function<Map<String, Double>, QualitativeMonitor<S>> rightArgumentEvaluation = ctx.right.accept(this);
        return m -> QualitativeMonitor.monitorIfAndOnlyIf(leftArgumentEvaluation.apply(m), rightArgumentEvaluation.apply(m));
    }

    @Override
    public Function<Map<String, Double>,QualitativeMonitor<S>> visitStlFormulaUntil(StlModelParser.StlFormulaUntilContext ctx) {
        Function<Map<String, Double>, QualitativeMonitor<S>> leftArgumentEvaluation = ctx.left.accept(this);
        Function<Map<String, Double>, QualitativeMonitor<S>> rightArgumentEvaluation = ctx.right.accept(this);
        Function<Map<String, Double>, Interval> intervalEvaluation = getInterval(ctx.interval());
        return m -> QualitativeMonitor.monitorUntil(intervalEvaluation.apply(m), leftArgumentEvaluation.apply(m), rightArgumentEvaluation.apply(m));
    }
}
