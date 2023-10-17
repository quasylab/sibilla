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

import it.unicam.quasylab.sibilla.core.models.yoda.YodaElementName;
import it.unicam.quasylab.sibilla.core.models.yoda.YodaVariable;
import it.unicam.quasylab.sibilla.core.models.yoda.YodaVariableRegistry;
import it.unicam.quasylab.sibilla.core.util.values.SibillaBoolean;
import it.unicam.quasylab.sibilla.core.util.values.SibillaRecord;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.Math.abs;

/**
 * This visitor is used to build a function that represents the parametric evaluation of an expression.
 */
public class YodaFunctionalExpressionEvaluator<T> extends YodaModelBaseVisitor<Function<T, SibillaValue>> {

    private final Function<String, Optional<SibillaValue>> constantsAndParameters;

    private final YodaExpressionEvaluationContext<T> expressionEvaluationContext;

    private final YodaVariableRegistry registry;
    private final Function<String, Set<YodaElementName>> groupSolver;

    public YodaFunctionalExpressionEvaluator(Function<String, Optional<SibillaValue>> constantsAndParameters, YodaExpressionEvaluationContext<T> expressionEvaluationContext, YodaVariableRegistry registry, Function<String, Set<YodaElementName>> groupSolver) {
        this.constantsAndParameters = constantsAndParameters;
        this.expressionEvaluationContext = expressionEvaluationContext;
        this.registry = registry;
        this.groupSolver = groupSolver;
    }

    public YodaFunctionalExpressionEvaluator(Function<String, Optional<SibillaValue>> constantsAndParameters, YodaExpressionEvaluationContext<T> evaluationContext, YodaVariableRegistry variableRegistry) {
        this(constantsAndParameters, evaluationContext, variableRegistry, s -> Set.of());
    }

    @Override
    public Function<T, SibillaValue> visitExpressionCeiling(YodaModelParser.ExpressionCeilingContext ctx) {
        return SibillaValue.apply(Math::ceil, ctx.argument.accept(this));
    }

    @Override
    public Function<T, SibillaValue> visitExpressionReference(YodaModelParser.ExpressionReferenceContext ctx) {
        String name = ctx.reference.getText();
        Optional<SibillaValue> oValue = constantsAndParameters.apply(name);
        if (oValue.isPresent()) {
            SibillaValue v = oValue.get();
            return arg -> v;
        } else {
            YodaVariable var = registry.get(name);
            return arg -> expressionEvaluationContext.get(arg, var);
        }
    }

    @Override
    public Function<T, SibillaValue> visitExpressionImplication(YodaModelParser.ExpressionImplicationContext ctx) {
        Function<T, SibillaValue> firstArgumentEvaluation = ctx.leftOp.accept(this);
        Function<T, SibillaValue> secondArgumentEvaluation = ctx.rightOp.accept(this);
        return arg -> SibillaValue.of(!firstArgumentEvaluation.apply(arg).booleanOf()||secondArgumentEvaluation.apply(arg).booleanOf());
    }

    @Override
    public Function<T, SibillaValue> visitExpressionUnary(YodaModelParser.ExpressionUnaryContext ctx) {
        Function<T, SibillaValue> argumentEvaluation = ctx.arg.accept(this);
        if (ctx.oper.getText().equals("-")) {
            return arg -> SibillaValue.minus(argumentEvaluation.apply(arg));
        } else {
            return argumentEvaluation;
        }
    }

    @Override
    public Function<T, SibillaValue> visitExpressionCos(YodaModelParser.ExpressionCosContext ctx) {
        return SibillaValue.apply(Math::cos, ctx.argument.accept(this));
    }

    @Override
    public Function<T, SibillaValue> visitExpressionPowOperation(YodaModelParser.ExpressionPowOperationContext ctx) {
        return SibillaValue.apply(Math::pow, ctx.leftOp.accept(this), ctx.rightOp.accept(this));
    }

    @Override
    public Function<T, SibillaValue> visitExpressionAtan(YodaModelParser.ExpressionAtanContext ctx) {
        return SibillaValue.apply(Math::atan, ctx.argument.accept(this));
    }

    @Override
    public Function<T, SibillaValue> visitExpressionFloor(YodaModelParser.ExpressionFloorContext ctx) {
        return SibillaValue.apply(Math::floor, ctx.argument.accept(this));
    }

    @Override
    public Function<T, SibillaValue> visitExpressionAddSubOperation(YodaModelParser.ExpressionAddSubOperationContext ctx) {
        Function<T, SibillaValue> firstArgumentEvaluation = ctx.leftOp.accept(this);
        Function<T, SibillaValue> secondArgumentEvaluation = ctx.rightOp.accept(this);
        BinaryOperator<SibillaValue> op = SibillaValue.getOperator(ctx.oper.getText());
        return SibillaValue.apply(op, firstArgumentEvaluation, secondArgumentEvaluation);
    }

    @Override
    public Function<T, SibillaValue> visitExpressionRandom(YodaModelParser.ExpressionRandomContext ctx) {
        return expressionEvaluationContext::rnd;
    }

    @Override
    public Function<T, SibillaValue> visitExpressionIfThenElse(YodaModelParser.ExpressionIfThenElseContext ctx) {
        Function<T, SibillaValue> guardEvaluationFunction = ctx.guardExpr.accept(this);
        Function<T, SibillaValue> thenEvalutaionFunction = ctx.thenBranch.accept(this);
        Function<T, SibillaValue> elseEvaluationFunction = ctx.elseBranch.accept(this);
        return arg -> (guardEvaluationFunction.apply(arg).booleanOf()?thenEvalutaionFunction.apply(arg):elseEvaluationFunction.apply(arg));
    }

    @Override
    public Function<T, SibillaValue> visitExpressionMinimum(YodaModelParser.ExpressionMinimumContext ctx) {
        Function<YodaGroupExpressionEvaluationParameters, SibillaValue> groupExpression = ctx.value.accept(
                new YodaFunctionalExpressionEvaluator<>(this.constantsAndParameters, YodaGroupExpressionEvaluationParameters.EXPRESSION_EVALUATION_CONTEXT, registry, groupSolver)
        );
        if (ctx.guard != null) {
            Function<YodaGroupExpressionEvaluationParameters, SibillaValue> groupGuard = ctx.value.accept(
                    new YodaFunctionalExpressionEvaluator<>(this.constantsAndParameters, YodaGroupExpressionEvaluationParameters.EXPRESSION_EVALUATION_CONTEXT, registry, groupSolver)
            );
            if (ctx.groupName != null) {
                return arg -> expressionEvaluationContext.min(arg, groupSolver.apply(ctx.groupName.getText()), groupGuard, groupExpression);
            } else {
                return arg -> expressionEvaluationContext.min(arg, groupGuard, groupExpression);
            }
        } else {
            if (ctx.groupName != null) {
                return arg -> expressionEvaluationContext.min(arg, groupSolver.apply(ctx.groupName.getText()), groupExpression);
            } else {
                return arg -> expressionEvaluationContext.min(arg, groupExpression);
            }
        }
    }

    @Override
    public Function<T, SibillaValue> visitExpressionRelation(YodaModelParser.ExpressionRelationContext ctx) {
        Function<T, SibillaValue> firstArgumentEvaluation = ctx.leftOp.accept(this);
        Function<T, SibillaValue> secondArgumentEvaluation = ctx.rightOp.accept(this);
        BiPredicate<SibillaValue, SibillaValue> op = SibillaValue.getRelationOperator(ctx.oper.getText());
        return arg -> SibillaValue.of(op.test(firstArgumentEvaluation.apply(arg), secondArgumentEvaluation.apply(arg)));
    }

    @Override
    public Function<T, SibillaValue> visitExpressionWeightedRandom(YodaModelParser.ExpressionWeightedRandomContext ctx) {
        Function<T, SibillaValue> minEvaluation = ctx.min.accept(this);
        Function<T, SibillaValue> maxEvaluation = ctx.max.accept(this);
        return arg -> expressionEvaluationContext.rnd(arg, minEvaluation.apply(arg), maxEvaluation.apply(arg));
    }

    @Override
    public Function<T, SibillaValue> visitExpressionExists(YodaModelParser.ExpressionExistsContext ctx) {
        Function<YodaGroupExpressionEvaluationParameters, SibillaValue> predExpression = ctx.expr().accept(
                new YodaFunctionalExpressionEvaluator<>(this.constantsAndParameters, YodaGroupExpressionEvaluationParameters.EXPRESSION_EVALUATION_CONTEXT, registry, groupSolver)
        );
        if (ctx.groupName != null) {
            Set<YodaElementName> elements = groupSolver.apply(ctx.groupName.getText());
            return arg -> expressionEvaluationContext.exists(arg, elements, predExpression);
        } else {
            return arg -> expressionEvaluationContext.exists(arg, predExpression);
        }
    }

    @Override
    public Function<T, SibillaValue> visitExpressionAsin(YodaModelParser.ExpressionAsinContext ctx) {
        return SibillaValue.apply(Math::asin, ctx.argument.accept(this));
    }

    @Override
    public Function<T, SibillaValue> visitExpressionItselfRef(YodaModelParser.ExpressionItselfRefContext ctx) {
        YodaVariable name = registry.get(ctx.ref.getText());
        return arg -> expressionEvaluationContext.itGet(arg, name);
    }

    @Override
    public Function<T, SibillaValue> visitExpressionAnd(YodaModelParser.ExpressionAndContext ctx) {
        Function<T, SibillaValue> firstArgumentEvaluation = ctx.leftOp.accept(this);
        Function<T, SibillaValue> secondArgumentEvaluation = ctx.rightOp.accept(this);
        return SibillaValue.apply(SibillaValue::and, firstArgumentEvaluation, secondArgumentEvaluation);
    }

    @Override
    public Function<T, SibillaValue> visitExpressionMean(YodaModelParser.ExpressionMeanContext ctx) {
        Function<YodaGroupExpressionEvaluationParameters, SibillaValue> groupExpression = ctx.value.accept(
                new YodaFunctionalExpressionEvaluator<>(this.constantsAndParameters, YodaGroupExpressionEvaluationParameters.EXPRESSION_EVALUATION_CONTEXT, registry, groupSolver)
        );
        if (ctx.guard != null) {
            Function<YodaGroupExpressionEvaluationParameters, SibillaValue> groupGuard = ctx.value.accept(
                    new YodaFunctionalExpressionEvaluator<>(this.constantsAndParameters, YodaGroupExpressionEvaluationParameters.EXPRESSION_EVALUATION_CONTEXT, registry, groupSolver)
            );
            if (ctx.groupName != null) {
                return arg -> expressionEvaluationContext.mean(arg, groupSolver.apply(ctx.groupName.getText()), groupGuard, groupExpression);
            } else {
                return arg -> expressionEvaluationContext.mean(arg, groupGuard, groupExpression);
            }
        } else {
            if (ctx.groupName != null) {
                return arg -> expressionEvaluationContext.mean(arg, groupSolver.apply(ctx.groupName.getText()), groupExpression);
            } else {
                return arg -> expressionEvaluationContext.mean(arg, groupExpression);
            }
        }
    }

    @Override
    public Function<T, SibillaValue> visitExpressionCosh(YodaModelParser.ExpressionCoshContext ctx) {
        return SibillaValue.apply(Math::cosh, ctx.argument.accept(this));
    }

    @Override
    public Function<T, SibillaValue> visitExpressionInteger(YodaModelParser.ExpressionIntegerContext ctx) {
        SibillaValue v = SibillaValue.of((Integer.parseInt(ctx.getText())));
        return arg -> v;
    }

    @Override
    public Function<T, SibillaValue> visitExpressionTan(YodaModelParser.ExpressionTanContext ctx) {
        return SibillaValue.apply(Math::tan, ctx.argument.accept(this));
    }

    @Override
    public Function<T, SibillaValue> visitExpressionAcos(YodaModelParser.ExpressionAcosContext ctx) {
        return SibillaValue.apply(Math::acos, ctx.argument.accept(this));
    }

    @Override
    public Function<T, SibillaValue> visitExpressionBrackets(YodaModelParser.ExpressionBracketsContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public Function<T, SibillaValue> visitExpressionMaximum(YodaModelParser.ExpressionMaximumContext ctx) {
        Function<YodaGroupExpressionEvaluationParameters, SibillaValue> groupExpression = ctx.value.accept(
                new YodaFunctionalExpressionEvaluator<>(this.constantsAndParameters, YodaGroupExpressionEvaluationParameters.EXPRESSION_EVALUATION_CONTEXT, registry, groupSolver)
        );
        if (ctx.guard != null) {
            Function<YodaGroupExpressionEvaluationParameters, SibillaValue> groupGuard = ctx.value.accept(
                    new YodaFunctionalExpressionEvaluator<>(this.constantsAndParameters, YodaGroupExpressionEvaluationParameters.EXPRESSION_EVALUATION_CONTEXT, registry, groupSolver)
            );
            if (ctx.groupName != null) {
                return arg -> expressionEvaluationContext.max(arg, groupSolver.apply(ctx.groupName.getText()), groupGuard, groupExpression);
            } else {
                return arg -> expressionEvaluationContext.max(arg, groupGuard, groupExpression);
            }
        } else {
            if (ctx.groupName != null) {
                return arg -> expressionEvaluationContext.max(arg, groupSolver.apply(ctx.groupName.getText()), groupExpression);
            } else {
                return arg -> expressionEvaluationContext.max(arg, groupExpression);
            }
        }
    }

    @Override
    public Function<T, SibillaValue> visitExpressionSin(YodaModelParser.ExpressionSinContext ctx) {
        return SibillaValue.apply(Math::sin, ctx.argument.accept(this));
    }

    @Override
    public Function<T, SibillaValue> visitExpressionRecord(YodaModelParser.ExpressionRecordContext ctx) {
        Map<String, Function<T, SibillaValue>> fieldsEvaluation = ctx.fieldAssignment().stream().collect(Collectors.toMap(f -> f.name.getText(), f -> f.value.accept(this)));
        return arg -> new SibillaRecord(fieldsEvaluation.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().apply(arg))));
    }

    @Override
    public Function<T, SibillaValue> visitExpressionForAll(YodaModelParser.ExpressionForAllContext ctx) {
        Function<YodaGroupExpressionEvaluationParameters, SibillaValue> predExpression = ctx.expr().accept(
                new YodaFunctionalExpressionEvaluator<>(this.constantsAndParameters, YodaGroupExpressionEvaluationParameters.EXPRESSION_EVALUATION_CONTEXT, registry, groupSolver)
        );
        if (ctx.groupName != null) {
            Set<YodaElementName> elements = groupSolver.apply(ctx.groupName.getText());
            return arg -> expressionEvaluationContext.forAll(arg, elements, predExpression);
        } else {
            return arg -> expressionEvaluationContext.forAll(arg, predExpression);
        }
    }

    @Override
    public Function<T, SibillaValue> visitExpressionAdditionalOperation(YodaModelParser.ExpressionAdditionalOperationContext ctx) {
        Function<T, SibillaValue> firstArgumentEvaluation = ctx.leftOp.accept(this);
        Function<T, SibillaValue> secondArgumentEvaluation = ctx.rightOp.accept(this);
        BinaryOperator<SibillaValue> op = SibillaValue.getOperator(ctx.oper.getText());
        return SibillaValue.apply(op, firstArgumentEvaluation, secondArgumentEvaluation);
    }

    @Override
    public Function<T, SibillaValue> visitExpressionTrue(YodaModelParser.ExpressionTrueContext ctx) {
        return arg -> SibillaBoolean.TRUE;
    }

    @Override
    public Function<T, SibillaValue> visitExpressionSinh(YodaModelParser.ExpressionSinhContext ctx) {
        return SibillaValue.apply(Math::sinh, ctx.argument.accept(this));
    }

    @Override
    public Function<T, SibillaValue> visitExpressionReal(YodaModelParser.ExpressionRealContext ctx) {
        SibillaValue v = SibillaValue.of(Double.parseDouble(ctx.getText()));
        return arg -> v;
    }

    @Override
    public Function<T, SibillaValue> visitExpressionNegation(YodaModelParser.ExpressionNegationContext ctx) {
        Function<T, SibillaValue> argumentEvaluation = ctx.argument.accept(this);
        return arg -> SibillaValue.not(argumentEvaluation.apply(arg));
    }

    @Override
    public Function<T, SibillaValue> visitExpressionMultDivOperation(YodaModelParser.ExpressionMultDivOperationContext ctx) {
        Function<T, SibillaValue> firstArgumentEvaluation = ctx.leftOp.accept(this);
        Function<T, SibillaValue> secondArgumentEvaluation = ctx.rightOp.accept(this);
        BinaryOperator<SibillaValue> op = SibillaValue.getOperator(ctx.oper.getText());
        return SibillaValue.apply(op, firstArgumentEvaluation, secondArgumentEvaluation);
    }

    @Override
    public Function<T, SibillaValue> visitExpressionFalse(YodaModelParser.ExpressionFalseContext ctx) {
        return arg -> SibillaBoolean.FALSE;
    }

    @Override
    public Function<T, SibillaValue> visitExpressionSquareRoot(YodaModelParser.ExpressionSquareRootContext ctx) {
        return SibillaValue.apply(Math::sqrt, ctx.argument.accept(this));
    }

    @Override
    public Function<T, SibillaValue> visitExpressionTanh(YodaModelParser.ExpressionTanhContext ctx) {
        return SibillaValue.apply(Math::tanh, ctx.argument.accept(this));
    }

    @Override
    public Function<T, SibillaValue> visitExpressionOr(YodaModelParser.ExpressionOrContext ctx) {
        Function<T, SibillaValue> firstArgumentEvaluation = ctx.leftOp.accept(this);
        Function<T, SibillaValue> secondArgumentEvaluation = ctx.rightOp.accept(this);
        return SibillaValue.apply(SibillaValue::or, firstArgumentEvaluation, secondArgumentEvaluation);
    }

    @Override
    public Function<T, SibillaValue> visitExpressionRecordAccess(YodaModelParser.ExpressionRecordAccessContext ctx) {
        Function<T, SibillaValue> recordEvaluation = ctx.record.accept(this);
        String fieldName = ctx.fieldName.getText();
        return arg -> SibillaValue.access(recordEvaluation.apply(arg), fieldName);
    }

    @Override
    public Function<T, SibillaValue> visitExpressionAbsolute(YodaModelParser.ExpressionAbsoluteContext ctx) {
        Function<T, SibillaValue> argumentEvaluation = ctx.argument.accept(this);
        return arg -> SibillaValue.abs(argumentEvaluation.apply(arg));
    }
}
