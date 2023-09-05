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

package it.unicam.quasylab.sibilla.langs.slam;

import it.unicam.quasylab.sibilla.core.models.slam.SlamInternalRuntimeException;
import it.unicam.quasylab.sibilla.core.models.slam.agents.SlamAgent;
import it.unicam.quasylab.sibilla.core.models.slam.agents.SlamAgentDefinitions;
import it.unicam.quasylab.sibilla.core.models.slam.data.AgentStore;
import it.unicam.quasylab.sibilla.core.models.slam.data.AgentVariable;
import it.unicam.quasylab.sibilla.core.models.slam.data.VariableRegistry;
import it.unicam.quasylab.sibilla.core.util.values.SibillaBoolean;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;

/**
 * This visitor is used to evaluate an expression as a scalar. In this context, only parameters and constants can be used.
 */
public class SlamExpressionEvaluator extends SlamModelBaseVisitor<Function<SlamExpressionEvaluationParameters,SibillaValue>> {

    private final Function<String, Optional<SibillaValue>> globalAssignments;

    private final VariableRegistry registry;

    private final ExpressionContext context;
    private final SlamAgentDefinitions agentDefinitions;


    public SlamExpressionEvaluator(ExpressionContext context, Function<String, Optional<SibillaValue>> globalAssignments, VariableRegistry registry, SlamAgentDefinitions agentDefinitions) {
        this.globalAssignments = globalAssignments;
        this.registry = registry;
        this.context = context;
        this.agentDefinitions = agentDefinitions;
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionACos(SlamModelParser.ExpressionACosContext ctx) {
        Function<SlamExpressionEvaluationParameters,SibillaValue> argumentEvaluation = ctx.argument.accept(this);
        return SibillaValue.apply(Math::acos, argumentEvaluation);
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionReference(SlamModelParser.ExpressionReferenceContext ctx) {
        Optional<SibillaValue> optionalValue = globalAssignments.apply(ctx.reference.getText());
        if (optionalValue.isPresent()) {
            SibillaValue value = optionalValue.get();
            return arg -> value;
        }
        if (context.accessToAttributesAllowed()) {
            Optional<AgentVariable> optionalAgentVariable = registry.getVariable(ctx.reference.getText());
            if (optionalAgentVariable.isPresent()) {
                AgentVariable variable = optionalAgentVariable.get();
                return arg -> arg.get(variable);
            }
        }
        throw new SlamInternalRuntimeException(ParseUtil.unknownSymbolMessage(ctx.reference));
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionLog(SlamModelParser.ExpressionLogContext ctx) {
        return SibillaValue.apply(Math::log, ctx.argument.accept(this));
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionCos(SlamModelParser.ExpressionCosContext ctx) {
        return SibillaValue.apply(Math::cos, ctx.argument.accept(this));
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionMaxAgents(SlamModelParser.ExpressionMaxAgentsContext ctx) {
        if (context.agentExpressionAllowed()) {
            Function<SlamExpressionEvaluationParameters, SibillaValue> expr = ctx.expr().accept(this);
            BiPredicate<AgentStore, SlamAgent> filter = ctx.agentPattern().accept(new AgentPatternGenerator(globalAssignments, registry, agentDefinitions));
            return arg -> arg.getMaxOf(expr, filter);
        }
        throw new SlamInternalRuntimeException(ParseUtil.illegalAgentExpressionMessage(ctx.start));
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionFloor(SlamModelParser.ExpressionFloorContext ctx) {
        return SibillaValue.apply(Math::floor, ctx.argument.accept(this));
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionIfThenElse(SlamModelParser.ExpressionIfThenElseContext ctx) {
        Function<SlamExpressionEvaluationParameters,SibillaValue> guardEvaluation = ctx.guard.accept(this);
        Function<SlamExpressionEvaluationParameters,SibillaValue> thenEvaluation = ctx.thenBranch.accept(this);
        Function<SlamExpressionEvaluationParameters,SibillaValue> elseEvaluation = ctx.elseBranch.accept(this);
        return (arg) -> {
            if (guardEvaluation.apply(arg).booleanOf()) {
                return thenEvaluation.apply(arg);
            } else {
                return elseEvaluation.apply(arg);
            }
        };
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionForAllAgents(SlamModelParser.ExpressionForAllAgentsContext ctx) {
        if (context.agentExpressionAllowed()) {
            BiPredicate<AgentStore, SlamAgent> predicate = ctx.agentPattern().accept(new AgentPatternGenerator(globalAssignments, registry, agentDefinitions));
            return arg -> arg.forAll(predicate);
        }
        throw new SlamInternalRuntimeException(ParseUtil.illegalAgentExpressionMessage(ctx.start));
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionRelation(SlamModelParser.ExpressionRelationContext ctx) {
        Function<SlamExpressionEvaluationParameters,SibillaValue> left = ctx.left.accept(this);
        Function<SlamExpressionEvaluationParameters,SibillaValue> right = ctx.right.accept(this);
        BiPredicate<SibillaValue, SibillaValue> relationOperator = SibillaValue.getRelationOperator(ctx.op.getText());
        return arg -> SibillaValue.of(relationOperator.test(left.apply(arg), right.apply(arg)));
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionSamplingNormal(SlamModelParser.ExpressionSamplingNormalContext ctx) {
        if (context.randomExpressionAllowed()) {
            Function<SlamExpressionEvaluationParameters, SibillaValue> mean = ctx.mean.accept(this);
            Function<SlamExpressionEvaluationParameters, SibillaValue> sigma = ctx.sigma.accept(this);
            return arg -> arg.sampleGaussian(mean.apply(arg), sigma.apply(arg));
        }
        throw new SlamInternalRuntimeException(ParseUtil.illegalUseOfRandomExpressionMessage(ctx.start));
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionMin(SlamModelParser.ExpressionMinContext ctx) {
        Function<SlamExpressionEvaluationParameters,SibillaValue> firstArgument = ctx.firstArgument.accept(this);
        Function<SlamExpressionEvaluationParameters,SibillaValue> secondArgument = ctx.secondArgument.accept(this);
        return arg -> SibillaValue.min(firstArgument.apply(arg), secondArgument.apply(arg));
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionAnd(SlamModelParser.ExpressionAndContext ctx) {
        Function<SlamExpressionEvaluationParameters,SibillaValue> left = ctx.left.accept(this);
        Function<SlamExpressionEvaluationParameters,SibillaValue> right = ctx.right.accept(this);
        return (arg) -> SibillaValue.and(left.apply(arg), right.apply(arg));
    }


    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionNow(SlamModelParser.ExpressionNowContext ctx) {
        if (context.timedExpressionAllowed()) {
            return SlamExpressionEvaluationParameters::now;
        }
        throw new SlamInternalRuntimeException(ParseUtil.illegalUseOfTimedExpressionMessage(ctx.start));
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionLog10(SlamModelParser.ExpressionLog10Context ctx) {
        return SibillaValue.apply(Math::log10, ctx.argument.accept(this));
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionCosh(SlamModelParser.ExpressionCoshContext ctx) {
        return SibillaValue.apply(Math::cosh, ctx.argument.accept(this));
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionInteger(SlamModelParser.ExpressionIntegerContext ctx) {
        SibillaValue value = SibillaValue.of(Integer.parseInt(ctx.getText()));
        return arg -> value;
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionUnaryOperator(SlamModelParser.ExpressionUnaryOperatorContext ctx) {
        Function<SlamExpressionEvaluationParameters,SibillaValue> argumentEvaluation = ctx.arg.accept(this);
        if (ctx.op.getText().equals("-")) {
            return arg -> SibillaValue.minus(argumentEvaluation.apply(arg));
        }
        return argumentEvaluation;
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionPatternReference(SlamModelParser.ExpressionPatternReferenceContext ctx) {
        if (context.thisPatternReferenceAllowed()) {
            Optional<AgentVariable> optionalAgentVariable = registry.getVariable(ctx.reference.getText());
            if (optionalAgentVariable.isPresent()) {
                AgentVariable variable = optionalAgentVariable.get();
                return arg -> arg.getFromPatternElement(variable);
            }
            throw new SlamInternalRuntimeException(ParseUtil.unknownSymbolMessage(ctx.reference));
        }
        throw new SlamInternalRuntimeException(ParseUtil.itIsNotAllowedHereMessage(ctx.start));
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionMeanAgents(SlamModelParser.ExpressionMeanAgentsContext ctx) {
        if (context.agentExpressionAllowed()) {
            Function<SlamExpressionEvaluationParameters, SibillaValue> expr = ctx.expr().accept(this);
            BiPredicate<AgentStore, SlamAgent> filter = ctx.agentPattern().accept(new AgentPatternGenerator(globalAssignments, registry, agentDefinitions));
            return arg -> arg.getMeanOf(expr, filter);
        }
        throw new SlamInternalRuntimeException(ParseUtil.illegalAgentExpressionMessage(ctx.start));
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionCastToInteger(SlamModelParser.ExpressionCastToIntegerContext ctx) {
        Function<SlamExpressionEvaluationParameters,SibillaValue> argumentEvaluation = ctx.expr().accept(this);
        return arg -> SibillaValue.of(argumentEvaluation.apply(arg).intOf());
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionExistsAgent(SlamModelParser.ExpressionExistsAgentContext ctx) {
        if (context.agentExpressionAllowed()) {
            BiPredicate<AgentStore, SlamAgent> predicate = ctx.agentPattern().accept(new AgentPatternGenerator(globalAssignments, registry, agentDefinitions));
            return arg -> arg.exists(predicate);
        }
        throw new SlamInternalRuntimeException(ParseUtil.illegalAgentExpressionMessage(ctx.start));
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionCeil(SlamModelParser.ExpressionCeilContext ctx) {
        return SibillaValue.apply(Math::ceil, ctx.argument.accept(this));
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionMinAgents(SlamModelParser.ExpressionMinAgentsContext ctx) {
        if (context.agentExpressionAllowed()) {
            Function<SlamExpressionEvaluationParameters, SibillaValue> expr = ctx.expr().accept(this);
            BiPredicate<AgentStore, SlamAgent> filter = ctx.agentPattern().accept(new AgentPatternGenerator(globalAssignments, registry, agentDefinitions));
            return arg -> arg.getMinOf(expr, filter);
        }
        throw new SlamInternalRuntimeException(ParseUtil.illegalAgentExpressionMessage(ctx.start));
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionTan(SlamModelParser.ExpressionTanContext ctx) {
        return SibillaValue.apply(Math::tan, ctx.argument.accept(this));
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionMulDiv(SlamModelParser.ExpressionMulDivContext ctx) {
        Function<SlamExpressionEvaluationParameters,SibillaValue> left = ctx.left.accept(this);
        Function<SlamExpressionEvaluationParameters,SibillaValue> right = ctx.right.accept(this);
        return SibillaValue.apply(SibillaValue.getOperator(ctx.op.getText()), left, right);
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionMax(SlamModelParser.ExpressionMaxContext ctx) {
        return SibillaValue.apply(SibillaValue::max,ctx.firstArgument.accept(this), (ctx.secondArgument.accept(this)));
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionSamplingUniform(SlamModelParser.ExpressionSamplingUniformContext ctx) {
        if (context.randomExpressionAllowed()) {
            Function<SlamExpressionEvaluationParameters, SibillaValue> from = ctx.from.accept(this);
            Function<SlamExpressionEvaluationParameters, SibillaValue> to = ctx.to.accept(this);
            return arg -> arg.sampleUniform(from.apply(arg), to.apply(arg));
        }
        throw new SlamInternalRuntimeException(ParseUtil.illegalUseOfRandomExpressionMessage(ctx.start));
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionATan(SlamModelParser.ExpressionATanContext ctx) {
        return SibillaValue.apply(Math::atan, ctx.argument.accept(this));
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionBracket(SlamModelParser.ExpressionBracketContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionRandomValue(SlamModelParser.ExpressionRandomValueContext ctx) {
        if (context.randomExpressionAllowed()) {
            return SlamExpressionEvaluationParameters::nextRandomValue;
        }
        throw new SlamInternalRuntimeException(ParseUtil.illegalUseOfRandomExpressionMessage(ctx.start));
    }

    @Override
    public Function<SlamExpressionEvaluationParameters, SibillaValue> visitExpressionCountAgents(SlamModelParser.ExpressionCountAgentsContext ctx) {
        if (context.agentExpressionAllowed()) {
            BiPredicate<AgentStore, SlamAgent> filter = ctx.agentPattern().accept(new AgentPatternGenerator(globalAssignments, registry, agentDefinitions));
            return arg -> arg.count(filter);
        }
        throw new SlamInternalRuntimeException(ParseUtil.illegalAgentExpressionMessage(ctx.start));
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionSin(SlamModelParser.ExpressionSinContext ctx) {
        return SibillaValue.apply(Math::sin, ctx.argument.accept(this));
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionPow(SlamModelParser.ExpressionPowContext ctx) {
        return SibillaValue.apply(Math::pow, ctx.left.accept(this), ctx.right.accept(this));
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionSumAgents(SlamModelParser.ExpressionSumAgentsContext ctx) {
        if (context.agentExpressionAllowed()) {
            Function<SlamExpressionEvaluationParameters, SibillaValue> expr = ctx.expr().accept(this);
            BiPredicate<AgentStore, SlamAgent> filter = ctx.agentPattern().accept(new AgentPatternGenerator(globalAssignments, registry, agentDefinitions));
            return arg -> arg.getSumOf(expr, filter);
        }
        throw new SlamInternalRuntimeException(ParseUtil.illegalAgentExpressionMessage(ctx.start));
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionExp(SlamModelParser.ExpressionExpContext ctx) {
        return SibillaValue.apply(Math::exp, ctx.argument.accept(this));
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionTrue(SlamModelParser.ExpressionTrueContext ctx) {
        return arg -> SibillaBoolean.TRUE;
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionSinh(SlamModelParser.ExpressionSinhContext ctx) {
        return SibillaValue.apply(Math::sinh, ctx.argument.accept(this));
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionASin(SlamModelParser.ExpressionASinContext ctx) {
        return SibillaValue.apply(Math::asin, ctx.argument.accept(this));
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionNegation(SlamModelParser.ExpressionNegationContext ctx) {
        return SibillaValue.apply(SibillaValue::not, ctx.arg.accept(this));
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionReal(SlamModelParser.ExpressionRealContext ctx) {
       SibillaValue value =  SibillaValue.of(Double.parseDouble(ctx.getText()));
       return arg -> value;
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionATan2(SlamModelParser.ExpressionATan2Context ctx) {
        return SibillaValue.apply(Math::atan2, ctx.firstArgument.accept(this), ctx.secondArgument.accept(this));
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionFalse(SlamModelParser.ExpressionFalseContext ctx) {
        return arg -> SibillaBoolean.FALSE;
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionAbs(SlamModelParser.ExpressionAbsContext ctx) {
        DoubleUnaryOperator op = Math::abs;
        return SibillaValue.apply(op, ctx.argument.accept(this));
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionTanh(SlamModelParser.ExpressionTanhContext ctx) {
        return SibillaValue.apply(Math::tanh, ctx.argument.accept(this));
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionOr(SlamModelParser.ExpressionOrContext ctx) {
        return SibillaValue.apply(SibillaValue::or, ctx.left.accept(this), ctx.right.accept(this));
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionAddSub(SlamModelParser.ExpressionAddSubContext ctx) {
        return SibillaValue.apply(SibillaValue.getOperator(ctx.op.getText()), ctx.left.accept(this), ctx.right.accept(this));
    }

    @Override
    public Function<SlamExpressionEvaluationParameters,SibillaValue> visitExpressionDt(SlamModelParser.ExpressionDtContext ctx) {
        if (context.deltaTimeAllowed()) {
            return SlamExpressionEvaluationParameters::dt;
        }
        throw new SlamInternalRuntimeException(ParseUtil.illegalUseOfTimedExpressionMessage(ctx.start));
    }
}
