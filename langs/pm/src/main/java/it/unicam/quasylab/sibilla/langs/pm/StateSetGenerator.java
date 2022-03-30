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

package it.unicam.quasylab.sibilla.langs.pm;

import it.unicam.quasylab.sibilla.core.models.EvaluationEnvironment;
import it.unicam.quasylab.sibilla.core.models.ParametricValue;
import it.unicam.quasylab.sibilla.core.models.StateSet;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.models.pm.util.PopulationRegistry;
import org.antlr.v4.runtime.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class StateSetGenerator extends PopulationModelBaseVisitor<StateSet<PopulationState>> {

    private final StateSet<PopulationState> stateSet;
    private final EvaluationEnvironment environment;
    private final PopulationRegistry registry;

    public StateSetGenerator(EvaluationEnvironment environment, PopulationRegistry registry) {
        this.environment = environment;
        this.registry = registry;
        this.stateSet = new StateSet<>();
    }


    @Override
    public StateSet<PopulationState> visitModel(PopulationModelParser.ModelContext ctx) {
        ctx.element().forEach(e -> e.accept(this));
        return stateSet;
    }

    @Override
    public StateSet<PopulationState> visitSystem_declaration(PopulationModelParser.System_declarationContext ctx) {
        stateSet.set(ctx.name.getText(), getStateBuilder(ctx.args,ctx.species_pattern()));
        return stateSet;
    }

    private ParametricValue<PopulationState> getStateBuilder(List<Token> args, PopulationModelParser.Species_patternContext species_pattern) {
        String[] variables = args.stream().sequential().map(Token::getText).toArray(String[]::new);
        return new ParametricValue<>(variables,d -> getPopulationState(PopulationModelGenerator.getMap(variables,d),species_pattern));
    }

    @Override
    protected StateSet<PopulationState> defaultResult() {
        return stateSet;
    }


    public PopulationState getPopulationState(Map<String,Double> map, PopulationModelParser.Species_patternContext species_pattern) {
        return registry.createPopulationState(PopulationModelGenerator.getPopulationArray(registry,environment.getEvaluator(),map,species_pattern.species_pattern_element()));
    }


}
