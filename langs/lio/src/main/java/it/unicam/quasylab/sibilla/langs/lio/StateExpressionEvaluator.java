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

import it.unicam.quasylab.sibilla.core.models.lio.LIOAgent;
import it.unicam.quasylab.sibilla.core.models.lio.LIOAgentDefinitions;
import it.unicam.quasylab.sibilla.core.models.lio.LIOAgentName;
import it.unicam.quasylab.sibilla.core.util.values.SibillaBoolean;
import it.unicam.quasylab.sibilla.core.util.values.SibillaDouble;
import it.unicam.quasylab.sibilla.core.util.values.SibillaInteger;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import it.unicam.quasylab.sibilla.langs.util.ErrorCollector;
import it.unicam.quasylab.sibilla.langs.util.ParseError;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * This class provides utility methods for evaluating expressions in the different contexts.
 */
public class StateExpressionEvaluator extends LIOModelBaseVisitor<CollectiveExpressionEvaluationFunction> {

    private final LIOAgentDefinitions definition;

    private final Map<String, SibillaValue> evaluationContext;

    private final ErrorCollector errors;

    public StateExpressionEvaluator(ErrorCollector errors, LIOAgentDefinitions definition, Map<String, SibillaValue> evaluationContext) {
        this.definition = definition;
        this.evaluationContext = evaluationContext;
        this.errors = errors;
    }


    @Override
    public CollectiveExpressionEvaluationFunction visitExpressionConjunction(LIOModelParser.ExpressionConjunctionContext ctx) {
        CollectiveExpressionEvaluationFunction leftFunction = ctx.left.accept(this);
        CollectiveExpressionEvaluationFunction rightFunction = ctx.right.accept(this);
        return s -> ((SibillaBoolean) leftFunction.eval(s)).and((SibillaBoolean) (rightFunction.eval(s)));
    }

    @Override
    public CollectiveExpressionEvaluationFunction visitExpressionReference(LIOModelParser.ExpressionReferenceContext ctx) {
        SibillaValue v = evaluationContext.get(ctx.reference.getText());
        if (v == null) {
            errors.record(new ParseError(ParseUtil.unknownSymbolError(ctx.reference), ctx.reference.getLine(), ctx.reference.getCharPositionInLine()));
            return s -> SibillaValue.ERROR_VALUE;
        }
        return s -> v;
    }

    @Override
    public CollectiveExpressionEvaluationFunction visitExpressionSumDiff(LIOModelParser.ExpressionSumDiffContext ctx) {
        CollectiveExpressionEvaluationFunction leftFunction = ctx.left.accept(this);
        CollectiveExpressionEvaluationFunction rightFunction = ctx.right.accept(this);
        if (ctx.op.getText().equals("+")) {
            return s -> SibillaValue.sum(leftFunction.eval(s), rightFunction.eval(s));
        }
        if (ctx.op.getText().equals("-")) {
            return s -> SibillaValue.sub(leftFunction.eval(s), rightFunction.eval(s));
        }
        return s -> SibillaValue.mod(leftFunction.eval(s), rightFunction.eval(s));
    }

    @Override
    public CollectiveExpressionEvaluationFunction visitExpressionMulDiv(LIOModelParser.ExpressionMulDivContext ctx) {
        CollectiveExpressionEvaluationFunction leftFunction = ctx.left.accept(this);
        CollectiveExpressionEvaluationFunction rightFunction = ctx.right.accept(this);
        if (ctx.op.getText().equals("*")) {
            return s -> SibillaValue.mul(leftFunction.eval(s), rightFunction.eval(s));
        }
        if (ctx.op.getText().equals("/")) {
            return s -> SibillaValue.div(leftFunction.eval(s), rightFunction.eval(s));
        }
        return s -> SibillaValue.zeroDiv(leftFunction.eval(s), rightFunction.eval(s));
    }

    @Override
    public CollectiveExpressionEvaluationFunction visitExpressionUnary(LIOModelParser.ExpressionUnaryContext ctx) {
        CollectiveExpressionEvaluationFunction argFunction = ctx.arg.accept(this);
        if (ctx.op.getText().equals("-")) {
            return s -> SibillaValue.minus(argFunction.eval(s));
        } else {
            return argFunction;
        }
    }

    @Override
    public CollectiveExpressionEvaluationFunction visitExpressionPower(LIOModelParser.ExpressionPowerContext ctx) {
        CollectiveExpressionEvaluationFunction leftFunction = ctx.left.accept(this);
        CollectiveExpressionEvaluationFunction rightFunction = ctx.right.accept(this);
        return s -> new SibillaDouble(Math.pow(leftFunction.eval(s).doubleOf(), rightFunction.eval(s).doubleOf()));
    }

    @Override
    public CollectiveExpressionEvaluationFunction visitExpressionBracket(LIOModelParser.ExpressionBracketContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public CollectiveExpressionEvaluationFunction visitExpressionDisjunction(LIOModelParser.ExpressionDisjunctionContext ctx) {
        CollectiveExpressionEvaluationFunction leftFunction = ctx.left.accept(this);
        CollectiveExpressionEvaluationFunction rightFunction = ctx.right.accept(this);
        return s -> ((SibillaBoolean) leftFunction.eval(s)).or((SibillaBoolean) (rightFunction.eval(s)));
    }

    @Override
    public CollectiveExpressionEvaluationFunction visitExpressionIfThenElse(LIOModelParser.ExpressionIfThenElseContext ctx) {
        CollectiveExpressionEvaluationFunction guardFunction = ctx.guard.accept(this);
        CollectiveExpressionEvaluationFunction thenFunction = ctx.thenBranch.accept(this);
        CollectiveExpressionEvaluationFunction elseFunction = ctx.elseBranch.accept(this);
        return s -> ((SibillaBoolean) guardFunction.eval(s)).ifThenElse(() -> thenFunction.eval(s), () -> elseFunction.eval(s));
    }

    @Override
    public CollectiveExpressionEvaluationFunction visitExpressionRelation(LIOModelParser.ExpressionRelationContext ctx) {
        CollectiveExpressionEvaluationFunction left = ctx.left.accept(this);
        CollectiveExpressionEvaluationFunction right = ctx.right.accept(this);
        if (ctx.op.getText().equals(">")) {
            return s -> SibillaBoolean.of(left.eval(s).doubleOf()>right.eval(s).doubleOf());
        }
        if (ctx.op.getText().equals(">=")) {
            return s -> SibillaBoolean.of(left.eval(s).doubleOf()>=right.eval(s).doubleOf());
        }
        if (ctx.op.getText().equals("==")) {
            return s -> SibillaBoolean.of(left.eval(s).doubleOf()==right.eval(s).doubleOf());
        }
        if (ctx.op.getText().equals("!=")) {
            return s -> SibillaBoolean.of(left.eval(s).doubleOf()!=right.eval(s).doubleOf());
        }
        if (ctx.op.getText().equals("<")) {
            return s -> SibillaBoolean.of(left.eval(s).doubleOf()<right.eval(s).doubleOf());
        }
        return s -> SibillaBoolean.of(left.eval(s).doubleOf()<=right.eval(s).doubleOf());
    }


    @Override
    public CollectiveExpressionEvaluationFunction visitExpressionTrue(LIOModelParser.ExpressionTrueContext ctx) {
        return s -> SibillaBoolean.TRUE;
    }

    @Override
    public CollectiveExpressionEvaluationFunction visitExpressionNegation(LIOModelParser.ExpressionNegationContext ctx) {
        CollectiveExpressionEvaluationFunction argumentFunction = ctx.arg.accept(this);
        return s -> ((SibillaBoolean) argumentFunction.eval(s)).not();
    }

    @Override
    public CollectiveExpressionEvaluationFunction visitExpressionReal(LIOModelParser.ExpressionRealContext ctx) {
        SibillaDouble val = new SibillaDouble(Double.parseDouble(ctx.getText()));
        return s -> val;
    }

    @Override
    public CollectiveExpressionEvaluationFunction visitExpressionFalse(LIOModelParser.ExpressionFalseContext ctx) {
        return s -> SibillaBoolean.FALSE;
    }

    @Override
    public CollectiveExpressionEvaluationFunction visitExpressionFractionOfAgents(LIOModelParser.ExpressionFractionOfAgentsContext ctx) {
        if (this.definition == null) {
            return  s -> SibillaValue.ERROR_VALUE;
        }
        Predicate<LIOAgentName> predicate = getAgentNamePredicate(ctx.agentPattern());
        Set<LIOAgent> agents = definition.getAgents(predicate);
        if (agents.size()==1) {
            LIOAgent a = agents.stream().findAny().get();
            return s -> new SibillaDouble(s.fractionOf(a));
        } else {
            return s -> new SibillaDouble(s.fractionOf(agents));
        }
    }

    private Predicate<LIOAgentName> getAgentNamePredicate(LIOModelParser.AgentPatternContext agentPattern) {
        String name = agentPattern.name.getText();
        Map<String, Integer> localVariables = getLocalVariables(agentPattern);
        if (agentPattern.guard != null) {
            Predicate<SibillaValue[]> indexPredicate = ParametricExpressionEvaluator.getAgentDependentExpressionEvaluator(this.errors, localVariables, this.evaluationContext).predicateOf(agentPattern.guard);
            return n -> n.getName().equals(name)&&indexPredicate.test(n.getIndexes());
        } else {
            return n -> n.getName().equals(name);
        }
    }

    private Map<String, Integer> getLocalVariables(LIOModelParser.AgentPatternContext agentPattern) {
        int counter = 0;
        Map<String, Integer> index = new HashMap<>();
        for (LIOModelParser.PatternElementContext element: agentPattern.patternElements) {
            if (element instanceof LIOModelParser.PatternElementVariableContext) {
                index.put(((LIOModelParser.PatternElementVariableContext) element).name.getText(),counter);
            }
            counter++;
        }
        return index;
    }


    @Override
    public CollectiveExpressionEvaluationFunction visitExpressionInteger(LIOModelParser.ExpressionIntegerContext ctx) {
        SibillaInteger val = new SibillaInteger(Integer.parseInt(ctx.getText()));
        return s -> val;
    }


}
