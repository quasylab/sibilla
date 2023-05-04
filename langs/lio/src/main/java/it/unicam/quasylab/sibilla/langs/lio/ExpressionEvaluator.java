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

import it.unicam.quasylab.sibilla.core.models.lio.Agent;
import it.unicam.quasylab.sibilla.core.models.lio.LIOCollective;
import org.antlr.v4.runtime.tree.RuleNode;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

/**
 * This class provides utility methods for evaluating expressions in the different contexts.
 */
public class ExpressionEvaluator {


    public static double evalExpressionToDouble(ToDoubleFunction<String> context, LIOModelParser.ExprContext expr) {
        return expr.accept(new ToDoubleEvaluator(context));
    }


    public static class ToDoubleEvaluator extends LIOModelBaseVisitor<Double> {

        private final ToDoubleFunction<String> context;
        private final ToBooleanEvaluator booleanEvaluator;


        public ToDoubleEvaluator(ToDoubleFunction<String> context) {
            this.context = context;
            this.booleanEvaluator = new ToBooleanEvaluator(this, context);
        }

        @Override
        protected Double defaultResult() {
            return Double.NaN;
        }

        @Override
        protected Double aggregateResult(Double aggregate, Double nextResult) {
            return aggregate;
        }

        @Override
        protected boolean shouldVisitNextChild(RuleNode node, Double currentResult) {
            return false;
        }

        @Override
        public Double visitExpressionConjunction(LIOModelParser.ExpressionConjunctionContext ctx) {
            return Double.NaN;
        }

        @Override
        public Double visitExpressionReference(LIOModelParser.ExpressionReferenceContext ctx) {
            return context.applyAsDouble(ctx.reference.getText());
        }

        @Override
        public Double visitExpressionSumDiff(LIOModelParser.ExpressionSumDiffContext ctx) {
            double left = ctx.left.accept(this);
            double right = ctx.right.accept(this);
            switch (ctx.op.getText()) {
                case "+": return left+right;
                case "-": return left-right;
                case "%": return left%right;
                default:
                    return Double.NaN;
            }
        }

        @Override
        public Double visitExpressionMulDiv(LIOModelParser.ExpressionMulDivContext ctx) {
            double left = ctx.left.accept(this);
            double right = ctx.right.accept(this);
            switch (ctx.op.getText()) {
                case "*": return left*right;
                case "/": return left/right;
                case "//": return (right==0.0?0.0:left/right);
                default:
                    return Double.NaN;
            }
        }

        @Override
        public Double visitExpressionUnary(LIOModelParser.ExpressionUnaryContext ctx) {
            double arg = ctx.arg.accept(this);
            switch (ctx.op.getText()) {
                case "+": return +arg;
                case "-": return -arg;
                default:
                    return Double.NaN;
            }
        }

        @Override
        public Double visitExpressionPower(LIOModelParser.ExpressionPowerContext ctx) {
            double left = ctx.left.accept(this);
            double right = ctx.left.accept(this);
            return Math.pow(left, right);
        }

        @Override
        public Double visitExpressionBracket(LIOModelParser.ExpressionBracketContext ctx) {
            return ctx.expr().accept(this);
        }

        @Override
        public Double visitExpressionDisjunction(LIOModelParser.ExpressionDisjunctionContext ctx) {
            return Double.NaN;
        }

        @Override
        public Double visitExpressionIfThenElse(LIOModelParser.ExpressionIfThenElseContext ctx) {
            if (ctx.guard.accept(booleanEvaluator)) {
                return ctx.thenBranch.accept(this);
            } else {
                return ctx.elseBranch.accept(this);
            }
        }

        @Override
        public Double visitExpressionRelation(LIOModelParser.ExpressionRelationContext ctx) {
            return Double.NaN;
        }

        @Override
        public Double visitExpressionTrue(LIOModelParser.ExpressionTrueContext ctx) {
            return Double.NaN;
        }

        @Override
        public Double visitExpressionNegation(LIOModelParser.ExpressionNegationContext ctx) {
            return Double.NaN;
        }

        @Override
        public Double visitExpressionReal(LIOModelParser.ExpressionRealContext ctx) {
            return Double.parseDouble(ctx.getText());
        }

        @Override
        public Double visitExpressionFalse(LIOModelParser.ExpressionFalseContext ctx) {
            return Double.NaN;
        }

        @Override
        public Double visitExpressionFractionOfAgents(LIOModelParser.ExpressionFractionOfAgentsContext ctx) {
            return Double.NaN;
        }

        @Override
        public Double visitExpressionNumberOfAgents(LIOModelParser.ExpressionNumberOfAgentsContext ctx) {
            return Double.NaN;
        }

        @Override
        public Double visitExpressionInteger(LIOModelParser.ExpressionIntegerContext ctx) {
            return (double) Integer.parseInt(ctx.getText());
        }
    }


    public static class ToBooleanEvaluator extends LIOModelBaseVisitor<Boolean> {

        private final ToDoubleEvaluator toDoubleEvaluator;
        private final ToDoubleFunction<String> context;

        public ToBooleanEvaluator(ToDoubleEvaluator toDoubleEvaluator, ToDoubleFunction<String> context) {
            this.toDoubleEvaluator = toDoubleEvaluator;
            this.context = context;
        }

        @Override
        public Boolean visitExpressionConjunction(LIOModelParser.ExpressionConjunctionContext ctx) {
            return ctx.left.accept(this)&&ctx.right.accept(this);
        }

        @Override
        public Boolean visitExpressionReference(LIOModelParser.ExpressionReferenceContext ctx) {
            return false;
        }

        @Override
        public Boolean visitExpressionSumDiff(LIOModelParser.ExpressionSumDiffContext ctx) {
            return false;
        }

        @Override
        public Boolean visitExpressionMulDiv(LIOModelParser.ExpressionMulDivContext ctx) {
            return false;
        }

        @Override
        public Boolean visitExpressionUnary(LIOModelParser.ExpressionUnaryContext ctx) {
            return false;
        }

        @Override
        public Boolean visitExpressionPower(LIOModelParser.ExpressionPowerContext ctx) {
            return false;
        }

        @Override
        public Boolean visitExpressionBracket(LIOModelParser.ExpressionBracketContext ctx) {
            return ctx.expr().accept(this);
        }

        @Override
        public Boolean visitExpressionDisjunction(LIOModelParser.ExpressionDisjunctionContext ctx) {
            return ctx.left.accept(this)||ctx.right.accept(this);
        }

        @Override
        public Boolean visitExpressionIfThenElse(LIOModelParser.ExpressionIfThenElseContext ctx) {
            if (ctx.guard.accept(this)) {
                return ctx.thenBranch.accept(this);
            } else {
                return ctx.elseBranch.accept(this);
            }
        }

        @Override
        public Boolean visitExpressionRelation(LIOModelParser.ExpressionRelationContext ctx) {
            double left = ctx.left.accept(toDoubleEvaluator);
            double right = ctx.right.accept(toDoubleEvaluator);
            switch (ctx.op.getText()) {
                case "<": return left<right;
                case "<=":
                case "=<":  return left<=right;
                case "==":  return left==right;
                case "!=":  return left!=right;
                case ">=":
                case "=>":  return left >= right;
                case ">":   return left > right;
                default:
                    return false;
            }
        }

        @Override
        public Boolean visitExpressionTrue(LIOModelParser.ExpressionTrueContext ctx) {
            return true;
        }

        @Override
        public Boolean visitExpressionNegation(LIOModelParser.ExpressionNegationContext ctx) {
            return !ctx.arg.accept(this);
        }

        @Override
        public Boolean visitExpressionReal(LIOModelParser.ExpressionRealContext ctx) {
            return false;
        }

        @Override
        public Boolean visitExpressionFalse(LIOModelParser.ExpressionFalseContext ctx) {
            return false;
        }

        @Override
        public Boolean visitExpressionFractionOfAgents(LIOModelParser.ExpressionFractionOfAgentsContext ctx) {
            return false;
        }

        @Override
        public Boolean visitExpressionNumberOfAgents(LIOModelParser.ExpressionNumberOfAgentsContext ctx) {
            return false;
        }

        @Override
        public Boolean visitExpressionInteger(LIOModelParser.ExpressionIntegerContext ctx) {
            return false;
        }

        @Override
        protected Boolean defaultResult() {
            return false;
        }

        @Override
        protected Boolean aggregateResult(Boolean aggregate, Boolean nextResult) {
            return aggregate;
        }

        @Override
        protected boolean shouldVisitNextChild(RuleNode node, Boolean currentResult) {
            return false;
        }
    }

    public static class ToMeasureEvaluator<S extends LIOCollective> extends LIOModelBaseVisitor<ToDoubleFunction<S>> {

        private final ToDoubleFunction<String> context;
        private final ToPredicateEvaluator<S> predicateEvaluator;
        private final Function<String, Predicate<Agent>> atomicPropositionSolver;

        public ToMeasureEvaluator(ToDoubleFunction<String> context, Function<String, Predicate<Agent>> atomicPropositionSolver) {
            this.context = context;
            this.predicateEvaluator = new ToPredicateEvaluator(context, this);
            this.atomicPropositionSolver = atomicPropositionSolver;
        }

        @Override
        public ToDoubleFunction<S> visitExpressionConjunction(LIOModelParser.ExpressionConjunctionContext ctx) {
            return s -> Double.NaN;
        }

        @Override
        public ToDoubleFunction<S> visitExpressionReference(LIOModelParser.ExpressionReferenceContext ctx) {
            double value = context.applyAsDouble(ctx.getText());
            return s -> value;
        }

        @Override
        public ToDoubleFunction<S> visitExpressionSumDiff(LIOModelParser.ExpressionSumDiffContext ctx) {
            ToDoubleFunction<S> left = ctx.left.accept(this);
            ToDoubleFunction<S> right = ctx.right.accept(this);
            switch (ctx.op.getText()) {
                case "+": return s -> (left.applyAsDouble(s)+right.applyAsDouble(s));
                case "-": return s -> (left.applyAsDouble(s)-right.applyAsDouble(s));
                case "%": return s -> (left.applyAsDouble(s)%right.applyAsDouble(s));
                default:
                    return s -> Double.NaN;
            }
        }

        @Override
        public ToDoubleFunction<S> visitExpressionMulDiv(LIOModelParser.ExpressionMulDivContext ctx) {
            ToDoubleFunction<S> left = ctx.left.accept(this);
            ToDoubleFunction<S> right = ctx.right.accept(this);
            switch (ctx.op.getText()) {
                case "*": return s -> (left.applyAsDouble(s)*right.applyAsDouble(s));
                case "/": return s -> (left.applyAsDouble(s)/right.applyAsDouble(s));
                case "//": return s -> {
                    double v2 = right.applyAsDouble(s);
                    if (v2 == 0.0) {
                        return 0.0;
                    } else {
                        return left.applyAsDouble(s)/v2;
                    }
                };
                default:
                    return s -> Double.NaN;
            }
        }

        @Override
        public ToDoubleFunction<S> visitExpressionUnary(LIOModelParser.ExpressionUnaryContext ctx) {
            ToDoubleFunction<S> arg = ctx.arg.accept(this);
            switch (ctx.op.getText()) {
                case "+": return arg;
                case "-": return s -> -(arg.applyAsDouble(s));
                default: return s -> Double.NaN;
            }
        }

        @Override
        public ToDoubleFunction<S> visitExpressionPower(LIOModelParser.ExpressionPowerContext ctx) {
            ToDoubleFunction<S> left = ctx.left.accept(this);
            ToDoubleFunction<S> right = ctx.right.accept(this);
            return s -> Math.pow(left.applyAsDouble(s), right.applyAsDouble(s));
        }

        @Override
        public ToDoubleFunction<S> visitExpressionBracket(LIOModelParser.ExpressionBracketContext ctx) {
            return ctx.expr().accept(this);
        }

        @Override
        public ToDoubleFunction<S> visitExpressionDisjunction(LIOModelParser.ExpressionDisjunctionContext ctx) {
            return s -> Double.NaN;
        }

        @Override
        public ToDoubleFunction<S> visitExpressionIfThenElse(LIOModelParser.ExpressionIfThenElseContext ctx) {
            Predicate<S> guard = ctx.guard.accept(predicateEvaluator);
            ToDoubleFunction<S> thenBranch = ctx.thenBranch.accept(this);
            ToDoubleFunction<S> elseBranch = ctx.elseBranch.accept(this);
            return s -> (guard.test(s)?thenBranch.applyAsDouble(s):elseBranch.applyAsDouble(s));
        }

        @Override
        public ToDoubleFunction<S> visitExpressionRelation(LIOModelParser.ExpressionRelationContext ctx) {
            return s -> Double.NaN;
        }

        @Override
        public ToDoubleFunction<S> visitExpressionTrue(LIOModelParser.ExpressionTrueContext ctx) {
            return s -> Double.NaN;
        }

        @Override
        public ToDoubleFunction<S> visitExpressionNegation(LIOModelParser.ExpressionNegationContext ctx) {
            return s -> Double.NaN;
        }

        @Override
        public ToDoubleFunction<S> visitExpressionReal(LIOModelParser.ExpressionRealContext ctx) {
            double value = Double.parseDouble(ctx.getText());
            return s -> value;
        }

        @Override
        public ToDoubleFunction<S> visitExpressionFalse(LIOModelParser.ExpressionFalseContext ctx) {
            return s -> Double.NaN;
        }

        @Override
        public ToDoubleFunction<S> visitExpressionFractionOfAgents(LIOModelParser.ExpressionFractionOfAgentsContext ctx) {
            Predicate<Agent> predicate = atomicPropositionSolver.apply(ctx.agent.getText());
            return s -> s.fractionOf(predicate);
        }

        @Override
        public ToDoubleFunction<S> visitExpressionNumberOfAgents(LIOModelParser.ExpressionNumberOfAgentsContext ctx) {
            Predicate<Agent> predicate = atomicPropositionSolver.apply(ctx.agent.getText());
            return s -> s.numberOf(predicate);
        }

        @Override
        public ToDoubleFunction<S> visitExpressionInteger(LIOModelParser.ExpressionIntegerContext ctx) {
            int value = Integer.parseInt(ctx.getText());
            return s -> value;
        }

        @Override
        protected ToDoubleFunction<S> defaultResult() {
            return s -> Double.NaN;
        }

        @Override
        protected ToDoubleFunction<S> aggregateResult(ToDoubleFunction<S> aggregate, ToDoubleFunction<S> nextResult) {
            return aggregate;
        }

        @Override
        protected boolean shouldVisitNextChild(RuleNode node, ToDoubleFunction<S> currentResult) {
            return false;
        }
    }

    public static class ToPredicateEvaluator<S extends LIOCollective> extends LIOModelBaseVisitor<Predicate<S>> {

        private final ToDoubleFunction<String> context;
        private final ToMeasureEvaluator<S> toMeasureEvaluator;

        public ToPredicateEvaluator(ToDoubleFunction<String> context, ToMeasureEvaluator<S> toMeasureEvaluator) {
            this.context = context;
            this.toMeasureEvaluator = toMeasureEvaluator;
        }

        @Override
        public Predicate<S> visitExpressionConjunction(LIOModelParser.ExpressionConjunctionContext ctx) {
            Predicate<S> left = ctx.left.accept(this);
            Predicate<S> right = ctx.right.accept(this);
            return s -> left.test(s)&&right.test(s);
        }

        @Override
        public Predicate<S> visitExpressionReference(LIOModelParser.ExpressionReferenceContext ctx) {
            return s -> false;
        }

        @Override
        public Predicate<S> visitExpressionSumDiff(LIOModelParser.ExpressionSumDiffContext ctx) {
            return s -> false;
        }

        @Override
        public Predicate<S> visitExpressionMulDiv(LIOModelParser.ExpressionMulDivContext ctx) {
            return s -> false;
        }

        @Override
        public Predicate<S> visitExpressionUnary(LIOModelParser.ExpressionUnaryContext ctx) {
            return s -> false;
        }

        @Override
        public Predicate<S> visitExpressionPower(LIOModelParser.ExpressionPowerContext ctx) {
            return s -> false;
        }

        @Override
        public Predicate<S> visitExpressionBracket(LIOModelParser.ExpressionBracketContext ctx) {
            return ctx.expr().accept(this);
        }

        @Override
        public Predicate<S> visitExpressionDisjunction(LIOModelParser.ExpressionDisjunctionContext ctx) {
            Predicate<S> left = ctx.left.accept(this);
            Predicate<S> right = ctx.right.accept(this);
            return s -> left.test(s)||right.test(s);
        }

        @Override
        public Predicate<S> visitExpressionIfThenElse(LIOModelParser.ExpressionIfThenElseContext ctx) {
            Predicate<S> guard = ctx.guard.accept(this);
            Predicate<S> thenBranch = ctx.thenBranch.accept(this);
            Predicate<S> elseBranch = ctx.elseBranch.accept(this);
            return s -> (guard.test(s)?thenBranch.test(s):elseBranch.test(s));

        }

        @Override
        public Predicate<S> visitExpressionRelation(LIOModelParser.ExpressionRelationContext ctx) {
            ToDoubleFunction<S> v1 = ctx.left.accept(toMeasureEvaluator);
            ToDoubleFunction<S> v2 = ctx.right.accept(toMeasureEvaluator);
            switch (ctx.op.getText()) {
                case "<": return s -> v1.applyAsDouble(s)<v2.applyAsDouble(s);
                case "<=":
                case "=<":  return s -> v1.applyAsDouble(s)<=v2.applyAsDouble(s);
                case "==":  return s -> v1.applyAsDouble(s)==v2.applyAsDouble(s);
                case "!=":  return s -> v1.applyAsDouble(s)!=v2.applyAsDouble(s);
                case ">=":
                case "=>":  return s -> v1.applyAsDouble(s)>=v2.applyAsDouble(s);
                case ">":   return s -> v1.applyAsDouble(s)>v2.applyAsDouble(s);
                default:
                    return s -> false;
            }
        }

        @Override
        public Predicate<S> visitExpressionTrue(LIOModelParser.ExpressionTrueContext ctx) {
            return s -> true;
        }

        @Override
        public Predicate<S> visitExpressionNegation(LIOModelParser.ExpressionNegationContext ctx) {
            Predicate<S> arg = ctx.arg.accept(this);
            return arg.negate();
        }

        @Override
        public Predicate<S> visitExpressionReal(LIOModelParser.ExpressionRealContext ctx) {
            return s -> false;
        }

        @Override
        public Predicate<S> visitExpressionFalse(LIOModelParser.ExpressionFalseContext ctx) {
            return s -> false;
        }

        @Override
        public Predicate<S> visitExpressionFractionOfAgents(LIOModelParser.ExpressionFractionOfAgentsContext ctx) {
            return s -> false;
        }

        @Override
        public Predicate<S> visitExpressionNumberOfAgents(LIOModelParser.ExpressionNumberOfAgentsContext ctx) {
            return s -> false;
        }

        @Override
        public Predicate<S> visitExpressionInteger(LIOModelParser.ExpressionIntegerContext ctx) {
            return s -> false;
        }
    }


}
