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

import it.unicam.quasylab.sibilla.core.models.yoda.YodaVariableRegistry;

public class YodaVariableCollector extends YodaModelBaseVisitor<Boolean> {

    private final YodaVariableRegistry registry = new YodaVariableRegistry();

    @Override
    public Boolean visitAgentDeclaration(YodaModelParser.AgentDeclarationContext ctx) {
        ctx.agentStateAttributes.forEach(nd -> registry.add(nd.name.getText()));
        ctx.agentFeaturesAttributes.forEach(nd -> registry.add(nd.name.getText()));
        ctx.agentObservationsAttributes.forEach(nd -> registry.add(nd.name.getText()));
        ctx.actionBody().forEach(ab -> ab.accept(this));
        return true;
    }

    @Override
    public Boolean visitSystemDeclaration(YodaModelParser.SystemDeclarationContext ctx) {
        ctx.globalAttributes.forEach(au -> au.accept(this));
        ctx.agentSensing.forEach(as -> as.accept(this));
        ctx.agentDynamics.forEach(ad -> ad.accept(this));
        return true;
    }

    @Override
    public Boolean visitAttributeUpdateIfThenElse(YodaModelParser.AttributeUpdateIfThenElseContext ctx) {
        ctx.thenBlock.forEach(au -> au.accept(this));
        ctx.elseBlock.forEach(au -> au.accept(this));
        return true;
    }

    @Override
    public Boolean visitAttributeUpdateLetBlock(YodaModelParser.AttributeUpdateLetBlockContext ctx) {
        ctx.names.forEach(n -> registry.add(n.getText()));
        ctx.body.forEach(au -> au.accept(this));
        return true;
    }

    @Override
    public Boolean visitSceneElementDeclaration(YodaModelParser.SceneElementDeclarationContext ctx) {
        ctx.elementFeaturesAttributes.forEach(nd -> registry.add(nd.name.getText()));
        return true;
    }

    @Override
    public Boolean visitFunctionStatementLet(YodaModelParser.FunctionStatementLetContext ctx) {
        ctx.names.forEach(n -> registry.add(n.getText()));
        ctx.functionStatement().accept(this);
        return true;
    }

    @Override
    protected Boolean defaultResult() {
        return true;
    }

    @Override
    protected Boolean aggregateResult(Boolean aggregate, Boolean nextResult) {
        return aggregate && nextResult;
    }

    public YodaVariableRegistry getVariableRegistry() {
        return registry;
    }
}
