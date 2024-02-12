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

package it.unicam.quasylab.sibilla.langs.enba.generators;

import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.expressions.ExpressionContext;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.states.Agent;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.states.AgentState;
import it.unicam.quasylab.sibilla.langs.enba.ExtendedNBABaseVisitor;
import it.unicam.quasylab.sibilla.langs.enba.ExtendedNBAParser;
import it.unicam.quasylab.sibilla.langs.enba.symbols.SymbolTable;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class StateSetGenerator extends ExtendedNBABaseVisitor<Map<String, Function<RandomGenerator, AgentState>>> {

    private final SymbolTable table;
    private final Map<String, Function<RandomGenerator, AgentState>> stateSet;

    public StateSetGenerator(SymbolTable table) {
        this.table = table;
        this.stateSet = new HashMap<>();
    }

    @Override
    public Map<String, Function<RandomGenerator, AgentState>> visitModel(ExtendedNBAParser.ModelContext ctx) {
        ctx.element().forEach(e -> e.accept(this));
        return stateSet;
    }

    @Override
    public Map<String, Function<RandomGenerator, AgentState>> visitSystem_declaration(ExtendedNBAParser.System_declarationContext ctx) {
        this.stateSet.put(ctx.name.getText(), getStateBuilder(ctx.components.system_component()));
        return stateSet;
    }

    private Function<RandomGenerator, AgentState> getStateBuilder(List<ExtendedNBAParser.System_componentContext> systemComponentsctx) {
        Map<Agent, Long> occupancies = new HashMap<>();
        AgentExpressionGenerator agentExpressionGenerator = new AgentExpressionGenerator(this.table, null, null);
        for(ExtendedNBAParser.System_componentContext sctx : systemComponentsctx) {
            Long agentPopulationSize = sctx.INTEGER() == null ? 1 : Long.parseLong(sctx.INTEGER().getText());
            Agent newAgent = sctx.agent_expression().accept(agentExpressionGenerator).eval(new ExpressionContext(Collections.emptyList(), Collections.emptyList(), null));
            occupancies.put(newAgent, agentPopulationSize+occupancies.getOrDefault(newAgent, 0L));
        }
        return r -> new AgentState(occupancies);
    }

    @Override
    protected Map<String, Function<RandomGenerator, AgentState>> defaultResult() {
        return stateSet;
    }
}
