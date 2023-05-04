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

public class AgentPatternValidator extends SlamModelBaseVisitor<Boolean> {


    private final SymbolTable table;

    public AgentPatternValidator(SymbolTable table) {
        this.table = table;
    }

    @Override
    public Boolean visitAgentPatternNegation(SlamModelParser.AgentPatternNegationContext ctx) {
        return ctx.arg.accept(this);
    }

    @Override
    public Boolean visitAgentPatternAny(SlamModelParser.AgentPatternAnyContext ctx) {
        return false;
    }

    @Override
    public Boolean visitAgentPatternNamed(SlamModelParser.AgentPatternNamedContext ctx) {
        return super.visitAgentPatternNamed(ctx);
    }

    @Override
    public Boolean visitAgentPatternProperty(SlamModelParser.AgentPatternPropertyContext ctx) {
        return super.visitAgentPatternProperty(ctx);
    }

    @Override
    public Boolean visitAgentPatternConjunction(SlamModelParser.AgentPatternConjunctionContext ctx) {
        return super.visitAgentPatternConjunction(ctx);
    }

    @Override
    public Boolean visitAgentPatternDisjunction(SlamModelParser.AgentPatternDisjunctionContext ctx) {
        return super.visitAgentPatternDisjunction(ctx);
    }

    @Override
    public Boolean visitAgentPatternBrackets(SlamModelParser.AgentPatternBracketsContext ctx) {
        return ctx.agentPattern().accept(this);
    }
}
