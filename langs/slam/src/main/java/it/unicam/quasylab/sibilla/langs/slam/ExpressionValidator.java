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

import it.unicam.quasylab.sibilla.core.models.slam.data.SlamType;
import it.unicam.quasylab.sibilla.langs.util.ParseError;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.RuleNode;

import java.util.List;
import java.util.function.Function;

/**
 * Instances of this class are used to validate expressions.
 */
public class ExpressionValidator extends SlamModelBaseVisitor<Boolean> {

    private final ExpressionContext context;

    private final SymbolTable table;

    private final Function<String, SlamType> typeSolver;

    private final List<ParseError> errors;

    /**
     * Creates a validator that can be used to validate an expression placed in the given context,
     * in a model where the available symbols are stored in the given table and by using the given type solver.
     *  @param context expression context
     * @param table table containing defined symbols
     * @param typeSolver function used to solve type names
     * @param errors list used to store validation errors
     */
    public ExpressionValidator(ExpressionContext context, SymbolTable table, Function<String, SlamType> typeSolver, List<ParseError> errors) {
        this.context = context;
        this.table = table;
        this.typeSolver = typeSolver;
        this.errors = errors;
    }

    @Override
    protected Boolean defaultResult() {
        return false;
    }

    @Override
    protected Boolean aggregateResult(Boolean aggregate, Boolean nextResult) {
        return aggregate&&nextResult;
    }

    @Override
    protected boolean shouldVisitNextChild(RuleNode node, Boolean currentResult) {
        return currentResult;
    }

    @Override
    public Boolean visitExpressionACos(SlamModelParser.ExpressionACosContext ctx) {
        return ctx.argument.accept(this);
    }

    @Override
    public Boolean visitExpressionReference(SlamModelParser.ExpressionReferenceContext ctx) {
        SlamType type = typeSolver.apply(ctx.reference.getText());
        if ((type == null)||(SlamType.NONE_TYPE.equals(type))) {
            this.errors.add(ParseUtil.unknownSymbolError(ctx.reference));
            return false;
        }
        return true;
    }

    @Override
    public Boolean visitExpressionAddSub(SlamModelParser.ExpressionAddSubContext ctx) {
        return ctx.left.accept(this) & ctx.right.accept(this);
    }

    @Override
    public Boolean visitExpressionLog(SlamModelParser.ExpressionLogContext ctx) {
        return ctx.argument.accept(this);
    }

    @Override
    public Boolean visitExpressionCos(SlamModelParser.ExpressionCosContext ctx) {
        return ctx.argument.accept(this);
    }

    @Override
    public Boolean visitExpressionMaxAgents(SlamModelParser.ExpressionMaxAgentsContext ctx) {
        return validateAgentExpression(ctx.start, ctx.expr(), ctx.agentPattern());
    }

    private Boolean validateAgentExpression(Token start, SlamModelParser.ExprContext expr, SlamModelParser.AgentPatternContext agentPattern) {
        if (context.agentExpressionAllowed()) {
            return expr.accept(this) & agentPattern.accept(this);
        } else {
            this.errors.add(ParseUtil.illegalAgentExpressionError(start));
            return false;
        }
    }

    @Override
    public Boolean visitExpressionFloor(SlamModelParser.ExpressionFloorContext ctx) {
        return ctx.argument.accept(this);
    }

    @Override
    public Boolean visitExpressionIfThenElse(SlamModelParser.ExpressionIfThenElseContext ctx) {
        return ctx.guard.accept(this) & ctx.thenBranch.accept(this) & ctx.elseBranch.accept(this);
    }

    @Override
    public Boolean visitExpressionForAllAgents(SlamModelParser.ExpressionForAllAgentsContext ctx) {
        if (context.agentExpressionAllowed()) {
            return ctx.agentPattern().accept(this);
        } else {
            this.errors.add(ParseUtil.illegalAgentExpressionError(ctx.start));
            return false;
        }
    }

    @Override
    public Boolean visitExpressionRelation(SlamModelParser.ExpressionRelationContext ctx) {
        return ctx.left.accept(this) & ctx.right.accept(this);
    }

    @Override
    public Boolean visitExpressionSamplingNormal(SlamModelParser.ExpressionSamplingNormalContext ctx) {
        if (context.randomExpressionAllowed()) {
            return ctx.mean.accept(this) & ctx.sigma.accept(this);
        } else {
            this.errors.add(ParseUtil.illegalUseOfRandomExpression(ctx.start));
            return false;
        }
    }

    @Override
    public Boolean visitExpressionMin(SlamModelParser.ExpressionMinContext ctx) {
        return ctx.firstArgument.accept(this) & ctx.secondArgument.accept(this);
    }

    @Override
    public Boolean visitExpressionAnd(SlamModelParser.ExpressionAndContext ctx) {
        return ctx.left.accept(this) & ctx.right.accept(this);
    }

    @Override
    public Boolean visitExpressionCast(SlamModelParser.ExpressionCastContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public Boolean visitExpressionNow(SlamModelParser.ExpressionNowContext ctx) {
        if (this.context.timedExpressionAllowed()) {
            return true;
        } else {
            this.errors.add(ParseUtil.illegalUseOfTimedExpression(ctx.start));
            return false;
        }
    }

    @Override
    public Boolean visitExpressionLog10(SlamModelParser.ExpressionLog10Context ctx) {
        return ctx.argument.accept(this);
    }

    @Override
    public Boolean visitExpressionCosh(SlamModelParser.ExpressionCoshContext ctx) {
        return ctx.argument.accept(this);
    }

    @Override
    public Boolean visitExpressionInteger(SlamModelParser.ExpressionIntegerContext ctx) {
        return true;
    }

    @Override
    public Boolean visitExpressionUnaryOperator(SlamModelParser.ExpressionUnaryOperatorContext ctx) {
        return ctx.arg.accept(this);
    }

    @Override
    public Boolean visitExpressionExistsAgent(SlamModelParser.ExpressionExistsAgentContext ctx) {
        if (context.agentExpressionAllowed()) {
            return ctx.agentPattern().accept(this);
        } else {
            this.errors.add(ParseUtil.illegalAgentExpressionError(ctx.start));
            return false;
        }
    }

    @Override
    public Boolean visitExpressionCeil(SlamModelParser.ExpressionCeilContext ctx) {
        return ctx.argument.accept(this);
    }

    @Override
    public Boolean visitExpressionMinAgents(SlamModelParser.ExpressionMinAgentsContext ctx) {
        return validateAgentExpression(ctx.start, ctx.expr(), ctx.agentPattern());
    }

    @Override
    public Boolean visitExpressionTan(SlamModelParser.ExpressionTanContext ctx) {
        return ctx.argument.accept(this);
    }

    @Override
    public Boolean visitExpressionMulDiv(SlamModelParser.ExpressionMulDivContext ctx) {
        return ctx.left.accept(this) & ctx.right.accept(this);
    }

    @Override
    public Boolean visitExpressionMax(SlamModelParser.ExpressionMaxContext ctx) {
        return ctx.firstArgument.accept(this) & ctx.secondArgument.accept(this);
    }

    @Override
    public Boolean visitExpressionSamplingUniform(SlamModelParser.ExpressionSamplingUniformContext ctx) {
        if (!context.randomExpressionAllowed()) {
            this.errors.add(ParseUtil.illegalUseOfRandomExpression(ctx.start));
            return false;
        } else {
            return ctx.from.accept(this) & ctx.to.accept(this);
        }
    }

    @Override
    public Boolean visitExpressionATan(SlamModelParser.ExpressionATanContext ctx) {
        return ctx.argument.accept(this);
    }

    @Override
    public Boolean visitExpressionBracket(SlamModelParser.ExpressionBracketContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public Boolean visitExpressionRandomValue(SlamModelParser.ExpressionRandomValueContext ctx) {
        if (context.randomExpressionAllowed()) {
            return true;
        } else {
            this.errors.add(ParseUtil.illegalUseOfRandomExpression(ctx.start));
            return false;
        }
    }

    @Override
    public Boolean visitExpressionDt(SlamModelParser.ExpressionDtContext ctx) {
        if (this.context.timedExpressionAllowed()) {
            return true;
        } else {
            this.errors.add(ParseUtil.illegalUseOfTimedExpression(ctx.start));
            return false;
        }
    }

    @Override
    public Boolean visitExpressionSin(SlamModelParser.ExpressionSinContext ctx) {
        return ctx.argument.accept(this);
    }

    @Override
    public Boolean visitExpressionPow(SlamModelParser.ExpressionPowContext ctx) {
        return ctx.left.accept(this) & ctx.right.accept(this);
    }

    @Override
    public Boolean visitExpressionSumAgents(SlamModelParser.ExpressionSumAgentsContext ctx) {
        return validateAgentExpression(ctx.start, ctx.expr(), ctx.agentPattern());
    }

    @Override
    public Boolean visitExpressionExp(SlamModelParser.ExpressionExpContext ctx) {
        return ctx.argument.accept(this);
    }

    @Override
    public Boolean visitExpressionTrue(SlamModelParser.ExpressionTrueContext ctx) {
        return true;
    }

    @Override
    public Boolean visitExpressionSinh(SlamModelParser.ExpressionSinhContext ctx) {
        return ctx.argument.accept(this);
    }

    @Override
    public Boolean visitExpressionASin(SlamModelParser.ExpressionASinContext ctx) {
        return ctx.argument.accept(this);
    }

    @Override
    public Boolean visitExpressionNegation(SlamModelParser.ExpressionNegationContext ctx) {
        return ctx.arg.accept(this);
    }

    @Override
    public Boolean visitExpressionReal(SlamModelParser.ExpressionRealContext ctx) {
        return true;
    }

    @Override
    public Boolean visitExpressionATan2(SlamModelParser.ExpressionATan2Context ctx) {
        return ctx.firstArgument.accept(this) & ctx.secondArgument.accept(this);
    }

    @Override
    public Boolean visitExpressionFalse(SlamModelParser.ExpressionFalseContext ctx) {
        return true;
    }

    @Override
    public Boolean visitExpressionAbs(SlamModelParser.ExpressionAbsContext ctx) {
        return ctx.argument.accept(this);
    }

    @Override
    public Boolean visitExpressionTanh(SlamModelParser.ExpressionTanhContext ctx) {
        return ctx.argument.accept(this);
    }

    @Override
    public Boolean visitExpressionOr(SlamModelParser.ExpressionOrContext ctx) {
        return ctx.left.accept(this) & ctx.right.accept(this);
    }
}
