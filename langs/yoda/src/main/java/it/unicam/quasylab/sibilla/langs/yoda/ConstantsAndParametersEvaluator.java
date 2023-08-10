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

import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

import java.util.HashMap;
import java.util.Map;

public class ConstantsAndParametersEvaluator extends YodaModelBaseVisitor<Boolean> {

    private final Map<String, SibillaValue> constantsAndParameters;

    private final Map<String, SibillaValue> parameters;


    public ConstantsAndParametersEvaluator() {
        this(new HashMap<>());
    }

    public ConstantsAndParametersEvaluator(Map<String, SibillaValue> parameters) {
        this.parameters = parameters;
        this.constantsAndParameters = new HashMap<>();
    }


    @Override
    public Boolean visitConstantDeclaration(YodaModelParser.ConstantDeclarationContext ctx) {
        SibillaValue v = eval(ctx.value);
        constantsAndParameters.put(ctx.name.getText(), v);
        return true;
    }

    @Override
    public Boolean visitParameterDeclaration(YodaModelParser.ParameterDeclarationContext ctx) {
        if (this.parameters.containsKey(ctx.name.getText())) {
            constantsAndParameters.put(ctx.name.getText(), parameters.get(ctx.name.getText()));
        } else {
            SibillaValue v = eval(ctx.value);
            parameters.put(ctx.name.getText(), v);
            constantsAndParameters.put(ctx.name.getText(), v);
        }
        return true;
    }

    private SibillaValue eval(YodaModelParser.ExprContext expr) {
        return expr.accept(new YodaScalarExpressionEvaluator(YodaModelGenerator.getNameEvaluationFunction(constantsAndParameters)));
    }

    @Override
    protected Boolean defaultResult() {
        return true;
    }

    @Override
    protected Boolean aggregateResult(Boolean aggregate, Boolean nextResult) {
        return aggregate && nextResult;
    }

    public Map<String, SibillaValue> getParameters() {
        return this.parameters;
    }

    public Map<String, SibillaValue> getConstantsAndParameters() {
        return this.constantsAndParameters;
    }
}
