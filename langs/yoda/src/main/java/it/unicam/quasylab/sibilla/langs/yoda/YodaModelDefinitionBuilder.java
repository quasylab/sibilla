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

import it.unicam.quasylab.sibilla.core.models.yoda.YodaType;
import it.unicam.quasylab.sibilla.core.models.yoda.YodaValue;
import it.unicam.quasylab.sibilla.core.models.yoda.YodaVariable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class YodaModelDefinitionBuilder extends YodaModelBaseVisitor<Boolean> {

    private final Map<String, YodaValue> parameters;
    private final Map<String, YodaValue> constants;
    private final Map<String, YodaVariable> variables;

    public YodaModelDefinitionBuilder() {
        this(new HashMap<>());
    }

    public YodaModelDefinitionBuilder(Map<String, YodaValue> parameters) {
        this.parameters = parameters;
        this.constants = new HashMap<>();
        this.variables = new HashMap<>();
    }

    @Override
    public Boolean visitModel(YodaModelParser.ModelContext ctx) {
        for (YodaModelParser.ElementContext e: ctx.element()) {
            if (!ctx.accept(this)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Boolean visitParameterDeclaration(YodaModelParser.ParameterDeclarationContext ctx) {
        String name = ctx.name.getText();
        if (!parameters.containsKey(name)) {
            YodaValue value = ctx.expr().accept(new ScalarExpressionEvaluator(getConstantNameSolver()));
            parameters.put(name, value);
        }
        return true;
    }

    @Override
    public Boolean visitConstantDeclaration(YodaModelParser.ConstantDeclarationContext ctx) {
        String name = ctx.name.getText();
        YodaValue value = ctx.expr().accept(new ScalarExpressionEvaluator(getConstantNameSolver()));
        constants.put(name, value);
        return true;
    }


    @Override
    public Boolean visitAgentDeclaration(YodaModelParser.AgentDeclarationContext ctx) {
        ctx.knowledgeDeclaration().fields.forEach(this::recordAgentField);
        return super.visitAgentDeclaration(ctx);
    }

    private void recordAgentField(YodaModelParser.FieldDeclarationContext field) {
        //TODO:
    }

    private synchronized YodaVariable getOrCreateVariable(String name, YodaType type) {
        if (variables.containsKey(name)) {
            return variables.get(name);
        } else {
            YodaVariable newVariable = new YodaVariable(variables.size(), name, type);
            variables.put(name, newVariable);
            return newVariable;
        }
    }

    private Function<String, YodaValue> getConstantNameSolver() {
        return name -> {
            if (parameters.containsKey(name)) {
                return parameters.get(name);
            }
            if (constants.containsKey(name)) {
                return parameters.get(name);
            }
            return YodaValue.NONE;
        };
    }
}
