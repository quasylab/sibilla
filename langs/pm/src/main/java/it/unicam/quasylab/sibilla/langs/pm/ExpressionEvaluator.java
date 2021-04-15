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

package it.unicam.quasylab.sibilla.langs.pm;

import it.unicam.quasylab.sibilla.core.models.EvaluationEnvironment;
import it.unicam.quasylab.sibilla.core.models.MeasureFunction;
import it.unicam.quasylab.sibilla.core.models.pm.Population;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.models.pm.util.PopulationRegistry;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class ExpressionEvaluator extends PopulationModelBaseVisitor<Double> {

    private final SymbolTable table;
    private Map<String,Double> context;
    private final EvaluationEnvironment environment;
    private final HashMap<String,Double> doubleCache;
    private final BooleanExpressionEvaluator booleanEvaluator;
    private final DoubleExpressionEvaluator doubleEvaluator;
    private final StatePredicateVisitor statePredicateVisitor;
    private final StateExpressionVisitor stateExpressionVisitor;
    private PopulationRegistry registry;


    public ExpressionEvaluator(SymbolTable table, Map<String, Double> context, EvaluationEnvironment environment) {
        this.table = table;
        this.context = context;
        this.environment = environment;
        this.doubleCache = new HashMap<>();
        this.booleanEvaluator = new BooleanExpressionEvaluator();
        this.doubleEvaluator = new DoubleExpressionEvaluator();
        this.stateExpressionVisitor = new StateExpressionVisitor();
        this.statePredicateVisitor = new StatePredicateVisitor();
    }

    public double evalDouble(ParseTree tree) {
        return doubleEvaluator.visit(tree);
    }

    public int evalInt(ParseTree tree) {
        return doubleEvaluator.visit(tree).intValue();
    }

    public boolean evalBool(ParseTree tree) {
        return booleanEvaluator.visit(tree);
    }

    public void setPopulationRegistry(PopulationRegistry registry) {
        this.registry = registry;
    }

    public Predicate<PopulationState> evalStatePredicate(ParseTree tree) {
        if (tree == null) {
            return s->true;
        }
        return statePredicateVisitor.visit(tree);
    }

    public MeasureFunction<PopulationState> evalStateFunction(ParseTree tree) {
        return stateExpressionVisitor.visit(tree);
    }

    public Population evalPopulation(PopulationModelParser.Species_pattern_elementContext patternElement) {
        int idx = evalSpeciesIndex(patternElement.species_expression());
        int size = (patternElement.size==null?1:evalInt(patternElement.size));
        return new Population(idx,size);
    }

    public Population[] evalPopulationPattern(PopulationModelParser.Species_patternContext pattern) {
        return pattern.species_pattern_element().stream().map(this::evalPopulation).toArray(i -> new Population[i]);
    }

    private int evalSpeciesIndex(PopulationModelParser.Species_expressionContext species_expression) {
        String name = species_expression.name.getText();
        Object[] args = species_expression.expr().stream().map(this::evalInt).toArray();
        return registry.indexOf(name,args);
    }

    public PopulationRegistry getPopulationRegistry() {
        return this.registry;
    }

    public PopulationState getState(double[] args, Map<String, Double> context, PopulationModelParser.Species_patternContext species_pattern) {
        this.context = context;
        Population[] populations = evalPopulationPattern(species_pattern);
        this.context = null;
        return registry.createPopulationState(populations);
    }

    private class BooleanExpressionEvaluator extends PopulationModelBaseVisitor<Boolean> {
        @Override
        protected Boolean defaultResult() {
            return false;
        }

        @Override
        public Boolean visitNegationExpression(PopulationModelParser.NegationExpressionContext ctx) {
            return !visit(ctx.arg);
        }

        @Override
        public Boolean visitTrueValue(PopulationModelParser.TrueValueContext ctx) {
            return false;
        }

        @Override
        public Boolean visitRelationExpression(PopulationModelParser.RelationExpressionContext ctx) {
            double left = doubleEvaluator.visit(ctx.left);
            double right = doubleEvaluator.visit(ctx.right);
            if (ctx.op.equals("<"))  { return left<right; }
            if (ctx.op.equals("<=")) { return left<=right; }
            if (ctx.op.equals("==")) { return left == right; }
            if (ctx.op.equals("!=")) { return left != right; }
            if (ctx.op.equals(">"))  { return left > right; }
            if (ctx.op.equals(">=")) { return left >= right; }
            return false;
        }

        @Override
        public Boolean visitOrExpression(PopulationModelParser.OrExpressionContext ctx) {
            return visit(ctx.left)||visit(ctx.right);
        }

        @Override
        public Boolean visitIfThenElseExpression(PopulationModelParser.IfThenElseExpressionContext ctx) {
            return (visit(ctx.guard)?visit(ctx.thenBranch):visit(ctx.elseBranch));
        }

        @Override
        public Boolean visitFalseValue(PopulationModelParser.FalseValueContext ctx) {
            return false;
        }

        @Override
        public Boolean visitAndExpression(PopulationModelParser.AndExpressionContext ctx) {
            return visit(ctx.left)&&visit(ctx.right);
        }
    }

    private class DoubleExpressionEvaluator extends PopulationModelBaseVisitor<Double> {
        @Override
        public Double visitReferenceExpression(PopulationModelParser.ReferenceExpressionContext ctx) {
            String id = ctx.reference.getText();
            if (table.isAConst(id)) {
                return getValueOfConstants(id);
            }
            if (table.isAParameter(id)) {
                return (double) environment.get(id);
            }
            return context.getOrDefault(id,Double.NaN);
        }

        private Double getValueOfConstants(String id) {
            Double value;
            if (!doubleCache.containsKey(id)) {
                value = this.visit(table.getContext(id));
                doubleCache.put(id,value);
            } else {
                value = doubleCache.get(id);
            }
            return value;
        }

        @Override
        public Double visitIfThenElseExpression(PopulationModelParser.IfThenElseExpressionContext ctx) {
            if (booleanEvaluator.visit(ctx.guard)) {
                return this.visit(ctx.thenBranch);
            } else {
                return this.visit(ctx.elseBranch);
            }
        }

        @Override
        public Double visitMulDivExpression(PopulationModelParser.MulDivExpressionContext ctx) {
            double left = visit(ctx.left);
            double right = visit(ctx.right);
            if (ctx.op.getText().equals("*")) {
                return visit(ctx.left)*visit(ctx.right);
            }
            if (ctx.op.getText().equals("/")) {
                return left/right;
            } else {
                return (right==0.0?0.0:left/right);
            }
        }

        @Override
        public Double visitAddSubExpression(PopulationModelParser.AddSubExpressionContext ctx) {
            double left = visit(ctx.left);
            double right = visit(ctx.right);
            if (ctx.op.getText().equals("+")) {
                return left+right;
            }
            if (ctx.op.getText().equals("-")) {
                return left-right;
            } else {
                return left%right;
            }
        }

        @Override
        public Double visitUnaryExpression(PopulationModelParser.UnaryExpressionContext ctx) {
            if (ctx.op.getText().equals("-")) {
                return -visit(ctx.arg);
            } else {
                return visit(ctx.arg);
            }
        }

        @Override
        public Double visitIntValue(PopulationModelParser.IntValueContext ctx) {
            return Double.parseDouble(ctx.INTEGER().getText());
        }

        @Override
        protected Double defaultResult() {
            return Double.NaN;
        }

        @Override
        public Double visitExponentExpression(PopulationModelParser.ExponentExpressionContext ctx) {
            return Math.pow(visit(ctx.left),visit(ctx.right));
        }

        @Override
        public Double visitRealValue(PopulationModelParser.RealValueContext ctx) {
            return Double.parseDouble(ctx.REAL().getText());
        }
    }

    public class StateExpressionVisitor extends PopulationModelBaseVisitor<MeasureFunction<PopulationState>> {
        @Override
        public MeasureFunction<PopulationState> visitExponentExpression(PopulationModelParser.ExponentExpressionContext ctx) {
            MeasureFunction<PopulationState> base = visit(ctx.left);
            MeasureFunction<PopulationState> exp = visit(ctx.right);
            return s -> Math.pow(base.apply(s),exp.apply(s));
        }

        @Override
        public MeasureFunction<PopulationState> visitIntValue(PopulationModelParser.IntValueContext ctx) {
            int value = evalInt(ctx);
            return s -> (double) value;
        }

        @Override
        public MeasureFunction<PopulationState> visitPopulationFractionExpression(PopulationModelParser.PopulationFractionExpressionContext ctx) {
            int idx = evalSpeciesIndex(ctx.species_expression());
            return s -> s.getFraction(idx);
        }

        @Override
        public MeasureFunction<PopulationState> visitRealValue(PopulationModelParser.RealValueContext ctx) {
            double value = evalDouble(ctx);
            return s -> value;
        }

        @Override
        public MeasureFunction<PopulationState> visitMulDivExpression(PopulationModelParser.MulDivExpressionContext ctx) {
            return applyBinary(visit(ctx.left),getOperator(ctx.op.getText()),visit(ctx.right));
        }

        @Override
        public MeasureFunction<PopulationState> visitPopulationSizeExpression(PopulationModelParser.PopulationSizeExpressionContext ctx) {
            int idx = evalSpeciesIndex(ctx.species_expression());
            return s -> s.getOccupancy(idx);
        }

        @Override
        public MeasureFunction<PopulationState> visitAddSubExpression(PopulationModelParser.AddSubExpressionContext ctx) {
            return applyBinary(visit(ctx.left),getOperator(ctx.op.getText()),visit(ctx.right));
        }

        @Override
        public MeasureFunction<PopulationState> visitUnaryExpression(PopulationModelParser.UnaryExpressionContext ctx) {
            MeasureFunction<PopulationState> arg = visit(ctx.arg);
            if (ctx.op.equals("-")) {
                return s -> -arg.apply(s);
            } else {
                return arg;
            }
        }
    }

    private MeasureFunction<PopulationState> applyBinary(MeasureFunction<PopulationState> left, BiFunction<Double, Double, Double> op, MeasureFunction<PopulationState> right) {
        return s -> op.apply(left.apply(s),right.apply(s));
    }

    private BiFunction<Double, Double, Double> getOperator(String op) {
        if (op.equals("+")) {
            return (x,y) -> x+y;
        }
        if (op.equals("-")) {
            return (x,y) -> x-y;
        }
        if (op.equals("%")) {
            return (x,y) -> x%y;
        }
        if (op.equals("*")) {
            return (x,y) -> x*y;
        }
        if (op.equals("/")) {
            return (x,y) -> x/y;
        }
        if (op.equals("//")) {
            return (x,y) -> (y==0.0?0.0:x/y);
        }
        return null;
     }

    public class StatePredicateVisitor extends PopulationModelBaseVisitor<Predicate<PopulationState>> {
        @Override
        public Predicate<PopulationState> visitNegationExpression(PopulationModelParser.NegationExpressionContext ctx) {
            return Predicate.not(visit(ctx.arg));
        }

        @Override
        public Predicate<PopulationState> visitTrueValue(PopulationModelParser.TrueValueContext ctx) {
            return s -> true;
        }

        @Override
        public Predicate<PopulationState> visitOrExpression(PopulationModelParser.OrExpressionContext ctx) {
            return visit(ctx.left).or(visit(ctx.right));
        }

        @Override
        public Predicate<PopulationState> visitIfThenElseExpression(PopulationModelParser.IfThenElseExpressionContext ctx) {
            Predicate<PopulationState> guard = visit(ctx.guard);
            Predicate<PopulationState> thenBranch = visit(ctx.thenBranch);
            Predicate<PopulationState> elseBranch = visit(ctx.elseBranch);
            return s -> (guard.test(s)?thenBranch.test(s):elseBranch.test(s));
        }

        @Override
        public Predicate<PopulationState> visitFalseValue(PopulationModelParser.FalseValueContext ctx) {
            return s -> false;
        }

        @Override
        public Predicate<PopulationState> visitAndExpression(PopulationModelParser.AndExpressionContext ctx) {
            return visit(ctx.left).and(visit(ctx.right));
        }


    }
}
