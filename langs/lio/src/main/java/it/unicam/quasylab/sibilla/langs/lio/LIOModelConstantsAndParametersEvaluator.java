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

import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import it.unicam.quasylab.sibilla.langs.util.ErrorCollector;

import java.util.HashMap;
import java.util.Map;

public class LIOModelConstantsAndParametersEvaluator extends LIOModelParseTreeChecker {

    private final Map<String, SibillaValue> constantsAndParameters = new HashMap<>();
    private final Map<String, SibillaValue> parameters;


    public LIOModelConstantsAndParametersEvaluator(Map<String, SibillaValue> parameters, ErrorCollector errors) {
        super(errors);
        this.parameters = parameters;
    }

    @Override
    public Boolean visitModel(LIOModelParser.ModelContext ctx) {
        boolean flag = true;
        for (LIOModelParser.ElementContext element: ctx.element()) {
            flag &= element.accept(this);
        }
        return flag;
    }

    @Override
    public Boolean visitElementConstant(LIOModelParser.ElementConstantContext ctx) {
        constantsAndParameters.put(ctx.name.getText(), evalExpression(ctx.value));
        return true;
    }

    private SibillaValue evalExpression(LIOModelParser.ExprContext value) {
        return value.accept(new GlobalExpressionEvaluator(this.errors, constantsAndParameters));
    }

    @Override
    public Boolean visitElementParam(LIOModelParser.ElementParamContext ctx) {
        if (parameters.containsKey(ctx.name.getText())) {
            constantsAndParameters.put(ctx.name.getText(), parameters.get(ctx.name.getText()));
        } else {
            SibillaValue value = evalExpression(ctx.value);
            constantsAndParameters.put(ctx.name.getText(), value);
            parameters.put(ctx.name.getText(), value);
        }
        return true;
    }


    /**
     * Returns the map associating parameters to their values.
     *
     * @return the map associating parameters to their values.
     */
    public Map<String, SibillaValue> getParameters() {
        return parameters;
    }

    /**
     * Returns the map associating constants and parameters to their values.
     *
     * @return the map associating parameters to their values.
     */
    public Map<String, SibillaValue> getConstantsAndParameters() {
        return constantsAndParameters;
    }
}
