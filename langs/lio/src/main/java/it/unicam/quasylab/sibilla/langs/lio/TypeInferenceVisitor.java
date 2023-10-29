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

import it.unicam.quasylab.sibilla.langs.util.ErrorCollector;
import it.unicam.quasylab.sibilla.langs.util.ParseError;
import org.antlr.v4.runtime.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A visitor used to infer type of expressions
 */
public class TypeInferenceVisitor extends LIOModelBaseVisitor<LIOType> {


    private final ErrorCollector errors;
    private final LIOTypeResolver localVariables;

    private final boolean stateExpressionsAllowed;

    private final Map<String, LIOType[]> agentPrototype;
    private boolean withError = false;


    /**
     * Creates a visitor to infer type of expressions.
     *
     * @param agentPrototype          map associating each declared state with its parameter types
     * @param localVariables          type environment for declared symbols
     * @param errors                  error collector
     * @param stateExpressionsAllowed flag indicating if expressions of states are allowed or not
     */
    public TypeInferenceVisitor(Map<String, LIOType[]> agentPrototype, LIOTypeResolver localVariables, ErrorCollector errors, boolean stateExpressionsAllowed) {
        this.agentPrototype = agentPrototype;
        this.errors = errors;
        this.localVariables = localVariables;
        this.stateExpressionsAllowed = stateExpressionsAllowed;
    }

    public TypeInferenceVisitor(LIOTypeResolver resolver, ErrorCollector errors) {
        this(Map.of(), resolver, errors, false);
    }

    public TypeInferenceVisitor(Map<String, LIOType[]> agentPrototype, LIOTypeResolver resolver, ErrorCollector errors) {
        this(agentPrototype, resolver, errors, true);
    }


    public boolean checkBooleanType(LIOModelParser.ExprContext expr) {
        LIOType actual = expr.accept(this);
        if (!actual.isABoolean()) {
            recordError(new ParseError(ParseUtil.booleanValueExpected(actual, expr.start),expr.start.getLine(), expr.start.getCharPositionInLine()));
            return false;
        }
        return true;
    }

    @Override
    public LIOType visitExpressionConjunction(LIOModelParser.ExpressionConjunctionContext ctx) {
        if (checkBooleanType(ctx.left)&&checkBooleanType(ctx.right)) {
            return LIOType.LIO_BOOLEAN;
        }
        return LIOType.LIO_NONE;
    }

    @Override
    public LIOType visitExpressionReference(LIOModelParser.ExpressionReferenceContext ctx) {
        String name = ctx.reference.getText();
        Optional<LIOType> oType = localVariables.get(name);
        if (oType.isPresent()) {
            return oType.get();
        }
        recordError(new ParseError(ParseUtil.unknownSymbolError(ctx.reference), ctx.reference.getLine(), ctx.reference.getCharPositionInLine()));
        return LIOType.LIO_NONE;
    }

    @Override
    public LIOType visitExpressionSumDiff(LIOModelParser.ExpressionSumDiffContext ctx) {
        return inferNumerical(ctx.left, ctx.right);
    }

    private LIOType inferNumerical(LIOModelParser.ExprContext e1, LIOModelParser.ExprContext e2) {
        LIOType left = e1.accept(this);
        LIOType right = e2.accept(this);
        if (!left.isANumber()) {
            recordError(new ParseError(ParseUtil.numericalValueExpected(left, e1.start),e1.start.getLine(), e1.start.getCharPositionInLine()));
            return LIOType.LIO_NONE;
        }
        if (!right.isANumber()) {
            recordError(new ParseError(ParseUtil.numericalValueExpected(right, e2.start),e2.start.getLine(), e2.start.getCharPositionInLine()));
            return LIOType.LIO_NONE;
        }
        return LIOType.combine(left, right);
    }

    @Override
    public LIOType visitExpressionMulDiv(LIOModelParser.ExpressionMulDivContext ctx) {
        return inferNumerical(ctx.left, ctx.right);
    }

    @Override
    public LIOType visitExpressionUnary(LIOModelParser.ExpressionUnaryContext ctx) {
        LIOType type = ctx.arg.accept(this);
        if (type.isANumber()) {
            return type;
        } else {
            recordError(new ParseError(ParseUtil.numericalValueExpected(type, ctx.arg.start),ctx.arg.start.getLine(), ctx.arg.start.getCharPositionInLine()));
            return LIOType.LIO_NONE;
        }
    }

    @Override
    public LIOType visitExpressionPower(LIOModelParser.ExpressionPowerContext ctx) {
        return inferNumerical(ctx.left, ctx.right);
    }

    @Override
    public LIOType visitExpressionBracket(LIOModelParser.ExpressionBracketContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public LIOType visitExpressionDisjunction(LIOModelParser.ExpressionDisjunctionContext ctx) {
        if (checkBooleanType(ctx.left)&&checkBooleanType(ctx.right)) {
            return LIOType.LIO_BOOLEAN;
        }
        return LIOType.LIO_NONE;
    }

    @Override
    public LIOType visitExpressionIfThenElse(LIOModelParser.ExpressionIfThenElseContext ctx) {
        checkBooleanType(ctx.guard);
        LIOType thenType = ctx.thenBranch.accept(this);
        LIOType elseType = ctx.elseBranch.accept(this);
        if (thenType.compatibleWith(elseType)||elseType.compatibleWith(thenType)) {
            return LIOType.combine(thenType, elseType);
        } else {
            recordError(new ParseError(ParseUtil.typeErrorMessage(thenType, elseType, ctx.elseBranch.start), ctx.elseBranch.start.getLine(), ctx.elseBranch.start.getCharPositionInLine()));
            return LIOType.LIO_NONE;
        }
    }

    @Override
    public LIOType visitExpressionRelation(LIOModelParser.ExpressionRelationContext ctx) {
        LIOType left = ctx.left.accept(this);
        LIOType right = ctx.right.accept(this);
        if (!left.isANumber()) {
            recordError(new ParseError(ParseUtil.numericalValueExpected(left, ctx.left.start),ctx.left.start.getLine(), ctx.left.start.getCharPositionInLine()));
        }
        if (!right.isANumber()) {
            recordError(new ParseError(ParseUtil.numericalValueExpected(right, ctx.right.start),ctx.right.start.getLine(), ctx.right.start.getCharPositionInLine()));
        }
        return LIOType.LIO_BOOLEAN;
    }

    @Override
    public LIOType visitExpressionTrue(LIOModelParser.ExpressionTrueContext ctx) {
        return LIOType.LIO_BOOLEAN;
    }

    @Override
    public LIOType visitExpressionNegation(LIOModelParser.ExpressionNegationContext ctx) {
        if (checkBooleanType(ctx.arg)) {
            return LIOType.LIO_NONE;
        } else {
            return LIOType.LIO_BOOLEAN;
        }
    }

    @Override
    public LIOType visitExpressionReal(LIOModelParser.ExpressionRealContext ctx) {
        return LIOType.LIO_REAL;
    }

    @Override
    public LIOType visitExpressionFalse(LIOModelParser.ExpressionFalseContext ctx) {
        return LIOType.LIO_BOOLEAN;
    }

    @Override
    public LIOType visitExpressionFractionOfAgents(LIOModelParser.ExpressionFractionOfAgentsContext ctx) {
        if (stateExpressionsAllowed) {
            checkAgentPatter(ctx.agentPattern());
        } else {
            recordError(new ParseError(ParseUtil.illegalUseOfStateExpression(ctx.start), ctx.start.getLine(), ctx.start.getCharPositionInLine()));
        }
        return LIOType.LIO_REAL;
    }

    private void recordError(ParseError parseError) {
        errors.record(parseError);
        withError = true;
    }

    private void checkAgentPatter(LIOModelParser.AgentPatternContext ctx) {
        LIOType[] argsType = agentPrototype.get(ctx.name.getText());
        if (ctx.patternElements.size() != argsType.length) {
            recordError(new ParseError(ParseUtil.wrongNumberOfAgentParameters(ctx.name, argsType.length, ctx.patternElements.size()), ctx.name.getLine(), ctx.name.getCharPositionInLine()));
            return ;
        }
        if (ctx.guard != null) {
            LIOTypeResolver patternVariables = LIOTypeResolver.resolverOf(getPatternVariableType(ctx.patternElements, argsType)).orElse(localVariables);
            withError = withError & (new TypeInferenceVisitor(agentPrototype, patternVariables, errors, false).checkBooleanType(ctx.guard));
        }
    }

    private Map<String, LIOType> getPatternVariableType(List<LIOModelParser.PatternElementContext> patternElements, LIOType[] argsType) {
        Map<String, LIOType> map = new HashMap<>();
        Map<String, Token> declared = new HashMap<>();
        int counter = 0;
        for (LIOModelParser.PatternElementContext patternElement: patternElements) {
            if (patternElement instanceof LIOModelParser.PatternElementVariableContext localVariable) {
                if (declared.containsKey(localVariable.name.getText())) {
                    recordError(new ParseError(ParseUtil.duplicatedNameErrorMessage(localVariable.name, declared.get(localVariable.name.getText())),localVariable.name.getLine(), localVariable.name.getCharPositionInLine()));
                } else {
                    map.put(localVariable.name.getText(), argsType[counter]);
                    declared.put(localVariable.name.getText(), localVariable.name);
                }
            }
            counter++;
        }
        return map;
    }


    @Override
    public LIOType visitExpressionInteger(LIOModelParser.ExpressionIntegerContext ctx) {
        return LIOType.LIO_INTEGER;
    }

    public boolean withError() {
        return withError;
    }
}
