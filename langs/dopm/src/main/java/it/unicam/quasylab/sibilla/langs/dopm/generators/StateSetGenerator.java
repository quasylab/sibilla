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

package it.unicam.quasylab.sibilla.langs.dopm.generators;

import it.unicam.quasylab.sibilla.core.models.dopm.states.Agent;
import it.unicam.quasylab.sibilla.core.models.dopm.states.DataOrientedPopulationState;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelBaseVisitor;
import it.unicam.quasylab.sibilla.langs.dopm.DataOrientedPopulationModelParser;
import it.unicam.quasylab.sibilla.langs.dopm.evaluators.ExpressionEvaluator;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;
import java.util.function.Function;

public class StateSetGenerator extends DataOrientedPopulationModelBaseVisitor<Map<String, Function<RandomGenerator, DataOrientedPopulationState>>> {

    private final Map<String, Function<RandomGenerator, DataOrientedPopulationState>> stateSet;

    public StateSetGenerator() {
        this.stateSet = new HashMap<>();
    }


    @Override
    public Map<String, Function<RandomGenerator, DataOrientedPopulationState>> visitModel(DataOrientedPopulationModelParser.ModelContext ctx) {
        ctx.element().forEach(e -> e.accept(this));
        return stateSet;
    }

    @Override
    public Map<String, Function<RandomGenerator, DataOrientedPopulationState>> visitSystem_declaration(DataOrientedPopulationModelParser.System_declarationContext ctx) {
        this.stateSet.put(ctx.name.getText(), getStateBuilder(ctx.agents.agent_instantation()));
        return stateSet;
    }

    private Function<RandomGenerator, DataOrientedPopulationState> getStateBuilder(List<DataOrientedPopulationModelParser.Agent_instantationContext> agentsctx) {
        List<Agent> agents = new ArrayList<>();
        for(DataOrientedPopulationModelParser.Agent_instantationContext actx : agentsctx) {
            Long occupancy = actx.INTEGER().getText() != null ? Long.parseLong(actx.INTEGER().getText()) :1;
            for(int i =0; i< occupancy; ++i) {
                String species = actx.agent_expression().name.getText();
                Map<String, SibillaValue> data = new HashMap<>();
                for (DataOrientedPopulationModelParser.Var_assContext vctx : actx.agent_expression().vars.var_ass()) {
                    String name = vctx.name.getText();
                    SibillaValue value = vctx.accept(new ExpressionEvaluator(c -> Optional.empty()));
                    data.put(name, value);
                    agents.add(new Agent(species, data));
                }
            }
        }
        DataOrientedPopulationState state = new DataOrientedPopulationState(agents);
        return r -> state;
    }

    @Override
    protected Map<String, Function<RandomGenerator, DataOrientedPopulationState>> defaultResult() {
        return stateSet;
    }


}
