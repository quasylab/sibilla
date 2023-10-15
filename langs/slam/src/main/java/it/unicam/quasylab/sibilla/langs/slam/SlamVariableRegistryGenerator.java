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

import it.unicam.quasylab.sibilla.core.models.slam.data.VariableRegistry;

public class SlamVariableRegistryGenerator extends SlamModelBaseVisitor<Boolean> {

    private final VariableRegistry variableRegistry = new VariableRegistry();

    @Override
    public Boolean visitAgentParameter(SlamModelParser.AgentParameterContext ctx) {
        variableRegistry.record(ctx.name.getText());
        return true;
    }

    @Override
    public Boolean visitPatternVariable(SlamModelParser.PatternVariableContext ctx) {
        variableRegistry.record(ctx.name.getText());
        return true;
    }

    @Override
    public Boolean visitAgentCommandLet(SlamModelParser.AgentCommandLetContext ctx) {
        variableRegistry.record(ctx.name.getText());
        return true;
    }

    @Override
    protected Boolean defaultResult() {
        return true;
    }

    @Override
    protected Boolean aggregateResult(Boolean aggregate, Boolean nextResult) {
        return aggregate&&nextResult;
    }

    public VariableRegistry getVariableRegistry() {
        return variableRegistry;
    }
}
