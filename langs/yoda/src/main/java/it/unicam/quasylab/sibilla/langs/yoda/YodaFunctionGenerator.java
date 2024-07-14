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

package it.unicam.quasylab.sibilla.langs.yoda;

import it.unicam.quasylab.sibilla.core.models.yoda.YodaVariable;
import it.unicam.quasylab.sibilla.core.models.yoda.YodaVariableMapping;
import it.unicam.quasylab.sibilla.core.models.yoda.YodaVariableRegistry;
import it.unicam.quasylab.sibilla.core.util.datastructures.Pair;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import org.antlr.v4.runtime.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;


public class YodaFunctionGenerator extends YodaModelBaseVisitor<Boolean> {

    private final Function<String, Optional<SibillaValue>> constantsAndParameters;

    private final Map<String, YodaFunction> functions;

    private final YodaVariableRegistry registry;

    public YodaFunctionGenerator(Function<String, Optional<SibillaValue>> constantsAndParameters, YodaVariableRegistry registry) {
        this.constantsAndParameters = constantsAndParameters;
        this.functions = new HashMap<>();
        this.registry = registry;
    }

    @Override
    protected Boolean defaultResult() {
        return true;
    }

    @Override
    public Boolean visitFunctionDeclaration(YodaModelParser.FunctionDeclarationContext ctx) {
        String functionName = ctx.name.getText();
        YodaVariable[] parameters = ctx.args.stream().map(Token::getText).map(registry::get).toArray(YodaVariable[]::new);
        Function<YodaExpressionEvaluationContext, SibillaValue> body = ctx.functionStatement().accept(new YodaFunctionBodyGenerator());
        functions.put(functionName, new YodaFunction(parameters, args ->
            body.apply(new YodaExpressionEvaluationContextFunction(parameters, args))
        ));
        return true;
    }

    public Optional<YodaFunction> getFunction(String name) {
        if (functions.containsKey(name)) {
            return Optional.of(functions.get(name));
        } else {
            return Optional.empty();
        }
    }

    public class YodaFunctionBodyGenerator extends YodaModelBaseVisitor<Function<YodaExpressionEvaluationContext, SibillaValue>> {

        @Override
        public Function<YodaExpressionEvaluationContext, SibillaValue> visitFunctionStatementIfThenElse(YodaModelParser.FunctionStatementIfThenElseContext ctx) {
            Function<YodaExpressionEvaluationContext, SibillaValue> guard = ctx.guard.accept(new YodaExpressionEvaluator(YodaFunctionGenerator.this::getFunction, constantsAndParameters, registry));
            Function<YodaExpressionEvaluationContext, SibillaValue> thenStatement = ctx.thenStatement.accept(this);
            Function<YodaExpressionEvaluationContext, SibillaValue> elseStatement = ctx.elseStatement.accept(this);
            return arg -> (guard.apply(arg).booleanOf()?thenStatement.apply(arg):elseStatement.apply(arg));
        }

        @Override
        public Function<YodaExpressionEvaluationContext, SibillaValue> visitFunctionStatementLet(YodaModelParser.FunctionStatementLetContext ctx) {
            YodaExpressionEvaluator evaluator = new YodaExpressionEvaluator(YodaFunctionGenerator.this::getFunction, constantsAndParameters, registry);
            YodaVariable[] variables = ctx.names.stream().map(Token::getText).map(registry::get).toArray(YodaVariable[]::new);
            YodaModelParser.ExprContext[] values = ctx.values.toArray(new YodaModelParser.ExprContext[0]);
            List<Pair<YodaVariable, Function<YodaExpressionEvaluationContext, SibillaValue>>> updates = IntStream.range(0, variables.length).mapToObj(i -> new Pair<>(variables[i], values[i].accept(evaluator))).toList();
            Function<YodaExpressionEvaluationContext, SibillaValue> body = ctx.functionStatement().accept(this);
            return YodaExpressionEvaluationLetContext.let(updates, body);
        }

        @Override
        public Function<YodaExpressionEvaluationContext, SibillaValue> visitFunctionStatementReturn(YodaModelParser.FunctionStatementReturnContext ctx) {
            return ctx.expr().accept(new YodaExpressionEvaluator(YodaFunctionGenerator.this::getFunction, constantsAndParameters, registry));
        }
    }

}
