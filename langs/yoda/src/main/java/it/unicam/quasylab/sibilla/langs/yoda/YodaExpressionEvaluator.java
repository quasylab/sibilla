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

import it.unicam.quasylab.sibilla.core.models.yoda.*;
import it.unicam.quasylab.sibilla.core.util.values.SibillaBoolean;
import it.unicam.quasylab.sibilla.core.util.values.SibillaRecord;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

/**
 * This visitor is used to build a function that represents the parametric evaluation of an expression.
 */
public class YodaExpressionEvaluator extends YodaModelBaseVisitor<Function<YodaExpressionEvaluationContext, SibillaValue>> {

    private final Function<String, Optional<SibillaValue>> constantsAndParameters;

    private final Function<String, Optional<YodaFunction>> functions;

    private final YodaVariableRegistry registry;
    private final Function<String, Set<YodaElementName>> groupSolver;

    public YodaExpressionEvaluator(Function<String, Optional<YodaFunction>> functions,
                                   Function<String, Optional<SibillaValue>> constantsAndParameters,
                                   YodaVariableRegistry registry, Function<String,
                                   Set<YodaElementName>> groupSolver) {
        this.constantsAndParameters = constantsAndParameters;
        this.registry = registry;
        this.groupSolver = groupSolver;
        this.functions = functions;
    }

    public YodaExpressionEvaluator(Function<String, Optional<YodaFunction>> functions,
                                   Function<String, Optional<SibillaValue>> constantsAndParameters,
                                   YodaVariableRegistry variableRegistry) {
        this(functions, constantsAndParameters, variableRegistry, s -> Set.of());
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionCeiling(YodaModelParser.ExpressionCeilingContext ctx) {
        Function<YodaExpressionEvaluationContext, SibillaValue> arg = ctx.argument.accept(this);
        return SibillaValue.apply(Math::ceil, arg);
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionReference(YodaModelParser.ExpressionReferenceContext ctx) {
        String name = ctx.reference.getText();
        Optional<SibillaValue> oValue = constantsAndParameters.apply(name);
        if (oValue.isPresent()) {
            SibillaValue v = oValue.get();
            return arg -> v;
        }
        Optional<YodaFunction> fValue = functions.apply(name);
        if (fValue.isPresent()) {
            YodaFunction f = fValue.get();
            List<Function<YodaExpressionEvaluationContext, SibillaValue>> params = ctx.params.stream().map(p -> p.accept(this)).toList();
            if (params.size() == f.parameters().length) {
                return arg -> f.body().apply(params.stream().map(e -> e.apply(arg)).toArray(SibillaValue[]::new));
            } else {
                //TODO: Manage type error!
                return arg -> SibillaValue.ERROR_VALUE;
            }
        }

        else {
            YodaVariable var = registry.get(name);
            return arg -> arg.get(var);
        }
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionImplication(YodaModelParser.ExpressionImplicationContext ctx) {
        Function<YodaExpressionEvaluationContext, SibillaValue> firstArgumentEvaluation = ctx.leftOp.accept(this);
        Function<YodaExpressionEvaluationContext, SibillaValue> secondArgumentEvaluation = ctx.rightOp.accept(this);
        return arg -> SibillaValue.of(!firstArgumentEvaluation.apply(arg).booleanOf()||secondArgumentEvaluation.apply(arg).booleanOf());
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionUnary(YodaModelParser.ExpressionUnaryContext ctx) {
        Function<YodaExpressionEvaluationContext, SibillaValue> argumentEvaluation = ctx.arg.accept(this);
        if (ctx.oper.getText().equals("-")) {
            return arg -> SibillaValue.minus(argumentEvaluation.apply(arg));
        } else {
            return argumentEvaluation;
        }
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionCos(YodaModelParser.ExpressionCosContext ctx) {
        return SibillaValue.apply(Math::cos, ctx.argument.accept(this));
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionPowOperation(YodaModelParser.ExpressionPowOperationContext ctx) {
        return SibillaValue.apply(Math::pow, ctx.leftOp.accept(this), ctx.rightOp.accept(this));
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionAtan(YodaModelParser.ExpressionAtanContext ctx) {
        return SibillaValue.apply(Math::atan, ctx.argument.accept(this));
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionFloor(YodaModelParser.ExpressionFloorContext ctx) {
        return SibillaValue.apply(Math::floor, ctx.argument.accept(this));
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionAddSubOperation(YodaModelParser.ExpressionAddSubOperationContext ctx) {
        Function<YodaExpressionEvaluationContext, SibillaValue> firstArgumentEvaluation = ctx.leftOp.accept(this);
        Function<YodaExpressionEvaluationContext, SibillaValue> secondArgumentEvaluation = ctx.rightOp.accept(this);
        BinaryOperator<SibillaValue> op = SibillaValue.getOperator(ctx.oper.getText());
        return SibillaValue.apply(op, firstArgumentEvaluation, secondArgumentEvaluation);
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionRandom(YodaModelParser.ExpressionRandomContext ctx) {
        return YodaExpressionEvaluationContext::rnd;
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionIfThenElse(YodaModelParser.ExpressionIfThenElseContext ctx) {
        Function<YodaExpressionEvaluationContext, SibillaValue> guardEvaluationFunction = ctx.guardExpr.accept(this);
        Function<YodaExpressionEvaluationContext, SibillaValue> thenEvalutaionFunction = ctx.thenBranch.accept(this);
        Function<YodaExpressionEvaluationContext, SibillaValue> elseEvaluationFunction = ctx.elseBranch.accept(this);
        return arg -> (guardEvaluationFunction.apply(arg).booleanOf()?thenEvalutaionFunction.apply(arg):elseEvaluationFunction.apply(arg));
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionMinimum(YodaModelParser.ExpressionMinimumContext ctx) {
        Function<YodaExpressionEvaluationContext, SibillaValue>  expression = ctx.value.accept(this);
        if ((ctx.groupName ==null)&&(ctx.guard == null)) {
            return arg -> arg.min(expression);
        }
        if (ctx.groupName == null) {
            Function<YodaExpressionEvaluationContext, SibillaValue> guard = ctx.guard.accept(this);
            return arg -> arg.min(guard, expression);
        }
        if (ctx.guard == null) {
            Set<YodaElementName> group = groupSolver.apply(ctx.groupName.getText());
            return arg -> arg.min(group, expression);
        }
        Function<YodaExpressionEvaluationContext, SibillaValue> guard = ctx.guard.accept(this);
        Set<YodaElementName> group = groupSolver.apply(ctx.groupName.getText());
        return arg -> arg.min(group, guard, expression);
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionRelation(YodaModelParser.ExpressionRelationContext ctx) {
        Function<YodaExpressionEvaluationContext, SibillaValue> firstArgumentEvaluation = ctx.leftOp.accept(this);
        Function<YodaExpressionEvaluationContext, SibillaValue> secondArgumentEvaluation = ctx.rightOp.accept(this);
        BiPredicate<SibillaValue, SibillaValue> op = SibillaValue.getRelationOperator(ctx.oper.getText());
        return arg -> SibillaValue.of(op.test(firstArgumentEvaluation.apply(arg), secondArgumentEvaluation.apply(arg)));
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionWeightedRandom(YodaModelParser.ExpressionWeightedRandomContext ctx) {
        Function<YodaExpressionEvaluationContext, SibillaValue> minEvaluation = ctx.min.accept(this);
        Function<YodaExpressionEvaluationContext, SibillaValue> maxEvaluation = ctx.max.accept(this);
        return arg -> arg.rnd(minEvaluation.apply(arg), maxEvaluation.apply(arg));
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionExists(YodaModelParser.ExpressionExistsContext ctx) {
        Function<YodaExpressionEvaluationContext, SibillaValue> predExpression = ctx.expr().accept(this);
        if (ctx.groupName != null) {
            Set<YodaElementName> elements = groupSolver.apply(ctx.groupName.getText());
            return arg -> arg.exists(elements, predExpression);
        } else {
            return arg -> arg.exists(predExpression);
        }
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionAsin(YodaModelParser.ExpressionAsinContext ctx) {
        return SibillaValue.apply(Math::asin, ctx.argument.accept(this));
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionItselfRef(YodaModelParser.ExpressionItselfRefContext ctx) {
        YodaVariable name = registry.get(ctx.ref.getText());
        return arg -> arg.it(name);
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionAnd(YodaModelParser.ExpressionAndContext ctx) {
        Function<YodaExpressionEvaluationContext, SibillaValue> firstArgumentEvaluation = ctx.leftOp.accept(this);
        Function<YodaExpressionEvaluationContext, SibillaValue> secondArgumentEvaluation = ctx.rightOp.accept(this);
        return SibillaValue.apply(SibillaValue::and, firstArgumentEvaluation, secondArgumentEvaluation);
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionMean(YodaModelParser.ExpressionMeanContext ctx) {
        Function<YodaExpressionEvaluationContext, SibillaValue>  expression = ctx.value.accept(this);
        if ((ctx.groupName ==null)&&(ctx.guard == null)) {
            return arg -> arg.mean(expression);
        }
        if (ctx.groupName == null) {
            Function<YodaExpressionEvaluationContext, SibillaValue> guard = ctx.guard.accept(this);
            return arg -> arg.mean(guard, expression);
        }
        if (ctx.guard == null) {
            Set<YodaElementName> group = groupSolver.apply(ctx.groupName.getText());
            return arg -> arg.mean(group, expression);
        }
        Function<YodaExpressionEvaluationContext, SibillaValue> guard = ctx.guard.accept(this);
        Set<YodaElementName> group = groupSolver.apply(ctx.groupName.getText());
        return arg -> arg.mean(group, guard, expression);
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionCosh(YodaModelParser.ExpressionCoshContext ctx) {
        return SibillaValue.apply(Math::cosh, ctx.argument.accept(this));
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionInteger(YodaModelParser.ExpressionIntegerContext ctx) {
        SibillaValue v = SibillaValue.of((Integer.parseInt(ctx.getText())));
        return arg -> v;
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionTan(YodaModelParser.ExpressionTanContext ctx) {
        return SibillaValue.apply(Math::tan, ctx.argument.accept(this));
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionAcos(YodaModelParser.ExpressionAcosContext ctx) {
        return SibillaValue.apply(Math::acos, ctx.argument.accept(this));
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionBrackets(YodaModelParser.ExpressionBracketsContext ctx) {
        return ctx.expr().accept(this);
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionMaximum(YodaModelParser.ExpressionMaximumContext ctx) {
        Function<YodaExpressionEvaluationContext, SibillaValue>  expression = ctx.value.accept(this);
        if ((ctx.groupName ==null)&&(ctx.guard == null)) {
            return arg -> arg.max(expression);
        }
        if (ctx.groupName == null) {
            Function<YodaExpressionEvaluationContext, SibillaValue> guard = ctx.guard.accept(this);
            return arg -> arg.max(guard, expression);
        }
        if (ctx.guard == null) {
            Set<YodaElementName> group = groupSolver.apply(ctx.groupName.getText());
            return arg -> arg.max(group, expression);
        }
        Function<YodaExpressionEvaluationContext, SibillaValue> guard = ctx.guard.accept(this);
        Set<YodaElementName> group = groupSolver.apply(ctx.groupName.getText());
        return arg -> arg.max(group, guard, expression);
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionSin(YodaModelParser.ExpressionSinContext ctx) {
        return SibillaValue.apply(Math::sin, ctx.argument.accept(this));
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionRecord(YodaModelParser.ExpressionRecordContext ctx) {
        Map<String, Function<YodaExpressionEvaluationContext, SibillaValue>> fieldsEvaluation = ctx.fieldAssignment().stream().collect(Collectors.toMap(f -> f.name.getText(), f -> f.value.accept(this)));
        return arg -> new SibillaRecord(fieldsEvaluation.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().apply(arg))));
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionForAll(YodaModelParser.ExpressionForAllContext ctx) {
        Function<YodaExpressionEvaluationContext, SibillaValue> predExpression = ctx.expr().accept(this);
        if (ctx.groupName != null) {
            Set<YodaElementName> elements = groupSolver.apply(ctx.groupName.getText());
            return arg -> arg.forAll(elements, predExpression);
        } else {
            return arg -> arg.forAll(predExpression);
        }
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionDistance(YodaModelParser.ExpressionDistanceContext ctx) {
        Function<YodaExpressionEvaluationContext, SibillaValue> v1 = ctx.a1.accept(this);
        Function<YodaExpressionEvaluationContext, SibillaValue> v2 = ctx.a2.accept(this);
        Function<YodaExpressionEvaluationContext, SibillaValue> v3 = ctx.a3.accept(this);
        Function<YodaExpressionEvaluationContext, SibillaValue> v4 = ctx.a4.accept(this);
        return arg -> SibillaValue.distance(
                SibillaValue.of(v1.apply(arg).doubleOf()),
                SibillaValue.of(v2.apply(arg).doubleOf()),
                SibillaValue.of(v3.apply(arg).doubleOf()),
                SibillaValue.of(v4.apply(arg).doubleOf())
                );
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionAngleOf(YodaModelParser.ExpressionAngleOfContext ctx) {
        Function<YodaExpressionEvaluationContext, SibillaValue> v1 = ctx.a1.accept(this);
        Function<YodaExpressionEvaluationContext, SibillaValue> v2 = ctx.a2.accept(this);
        Function<YodaExpressionEvaluationContext, SibillaValue> v3 = ctx.a3.accept(this);
        Function<YodaExpressionEvaluationContext, SibillaValue> v4 = ctx.a4.accept(this);
        return arg -> SibillaValue.angleOf(
                SibillaValue.of(v1.apply(arg).doubleOf()),
                SibillaValue.of(v2.apply(arg).doubleOf()),
                SibillaValue.of(v3.apply(arg).doubleOf()),
                SibillaValue.of(v4.apply(arg).doubleOf())
        );
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionAdditionalOperation(YodaModelParser.ExpressionAdditionalOperationContext ctx) {
        Function<YodaExpressionEvaluationContext, SibillaValue> firstArgumentEvaluation = ctx.leftOp.accept(this);
        Function<YodaExpressionEvaluationContext, SibillaValue> secondArgumentEvaluation = ctx.rightOp.accept(this);
        BinaryOperator<SibillaValue> op = SibillaValue.getOperator(ctx.oper.getText());
        return SibillaValue.apply(op, firstArgumentEvaluation, secondArgumentEvaluation);
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionTrue(YodaModelParser.ExpressionTrueContext ctx) {
        return arg -> SibillaBoolean.TRUE;
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionSinh(YodaModelParser.ExpressionSinhContext ctx) {
        return SibillaValue.apply(Math::sinh, ctx.argument.accept(this));
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionReal(YodaModelParser.ExpressionRealContext ctx) {
        SibillaValue v = SibillaValue.of(Double.parseDouble(ctx.getText()));
        return arg -> v;
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionNegation(YodaModelParser.ExpressionNegationContext ctx) {
        Function<YodaExpressionEvaluationContext, SibillaValue> argumentEvaluation = ctx.argument.accept(this);
        return arg -> SibillaValue.not(argumentEvaluation.apply(arg));
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionMultDivOperation(YodaModelParser.ExpressionMultDivOperationContext ctx) {
        Function<YodaExpressionEvaluationContext, SibillaValue> firstArgumentEvaluation = ctx.leftOp.accept(this);
        Function<YodaExpressionEvaluationContext, SibillaValue> secondArgumentEvaluation = ctx.rightOp.accept(this);
        BinaryOperator<SibillaValue> op = SibillaValue.getOperator(ctx.oper.getText());
        return SibillaValue.apply(op, firstArgumentEvaluation, secondArgumentEvaluation);
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionFalse(YodaModelParser.ExpressionFalseContext ctx) {
        return arg -> SibillaBoolean.FALSE;
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionSquareRoot(YodaModelParser.ExpressionSquareRootContext ctx) {
        return SibillaValue.apply(Math::sqrt, ctx.argument.accept(this));
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionTanh(YodaModelParser.ExpressionTanhContext ctx) {
        return SibillaValue.apply(Math::tanh, ctx.argument.accept(this));
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionOr(YodaModelParser.ExpressionOrContext ctx) {
        Function<YodaExpressionEvaluationContext, SibillaValue> firstArgumentEvaluation = ctx.leftOp.accept(this);
        Function<YodaExpressionEvaluationContext, SibillaValue> secondArgumentEvaluation = ctx.rightOp.accept(this);
        return SibillaValue.apply(SibillaValue::or, firstArgumentEvaluation, secondArgumentEvaluation);
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionRecordAccess(YodaModelParser.ExpressionRecordAccessContext ctx) {
        Function<YodaExpressionEvaluationContext, SibillaValue> recordEvaluation = ctx.record.accept(this);
        String fieldName = ctx.fieldName.getText();
        return arg -> SibillaValue.access(recordEvaluation.apply(arg), fieldName);
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionAbsolute(YodaModelParser.ExpressionAbsoluteContext ctx) {
        Function<YodaExpressionEvaluationContext, SibillaValue> argumentEvaluation = ctx.argument.accept(this);
        return arg -> SibillaValue.abs(argumentEvaluation.apply(arg));
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionCount(YodaModelParser.ExpressionCountContext ctx) {
        Set<YodaElementName> group = groupSolver.apply(ctx.groupName.getText());
        if (ctx.guard == null) {
            return arg -> arg.count(group);
        }
        Function<YodaExpressionEvaluationContext, SibillaValue> guard = ctx.guard.accept(this);
        return arg -> arg.count(group, guard);
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionPi(YodaModelParser.ExpressionPiContext ctx) {
        return eec -> SibillaValue.of(Math.PI);
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionDt(YodaModelParser.ExpressionDtContext ctx) {
        return YodaExpressionEvaluationContext::dt;
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionSum(YodaModelParser.ExpressionSumContext ctx) {
        Function<YodaExpressionEvaluationContext, SibillaValue>  expression = ctx.value.accept(this);
        if ((ctx.groupName ==null)&&(ctx.guard == null)) {
            return arg -> arg.sum(expression);
        }
        if (ctx.groupName == null) {
            Function<YodaExpressionEvaluationContext, SibillaValue> guard = ctx.guard.accept(this);
            return arg -> arg.sum(guard, expression);
        }
        if (ctx.guard == null) {
            Set<YodaElementName> group = groupSolver.apply(ctx.groupName.getText());
            return arg -> arg.sum(group, expression);
        }
        Function<YodaExpressionEvaluationContext, SibillaValue> guard = ctx.guard.accept(this);
        Set<YodaElementName> group = groupSolver.apply(ctx.groupName.getText());
        return arg -> arg.sum(group, guard, expression);
    }

    @Override
    public Function<YodaExpressionEvaluationContext, SibillaValue> visitExpressionAtan2(YodaModelParser.ExpressionAtan2Context ctx) {
        Function<YodaExpressionEvaluationContext, SibillaValue> firstArgumentEvaluation = ctx.left.accept(this);
        Function<YodaExpressionEvaluationContext, SibillaValue> secondArgumentEvaluation = ctx.right.accept(this);
        return arg -> SibillaValue.atan2(firstArgumentEvaluation.apply(arg), secondArgumentEvaluation.apply(arg));
    }
}
