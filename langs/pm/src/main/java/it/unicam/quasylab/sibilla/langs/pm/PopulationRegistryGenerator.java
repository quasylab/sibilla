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
import it.unicam.quasylab.sibilla.core.models.pm.util.PopulationRegistry;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import org.antlr.v4.runtime.Token;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class PopulationRegistryGenerator extends PopulationModelBaseVisitor<PopulationRegistry> {

    private final EvaluationEnvironment environment;
    private final PopulationRegistry registry;

    public PopulationRegistryGenerator(EvaluationEnvironment environment) {
        this.environment = environment;
        this.registry = new PopulationRegistry();
    }

    @Override
    public PopulationRegistry visitModel(PopulationModelParser.ModelContext ctx) {
        ctx.element().forEach(e -> e.accept(this));
        return registry;
    }

    @Override
    public PopulationRegistry visitSpecies_declaration(PopulationModelParser.Species_declarationContext ctx) {
        String name = ctx.name.getText();
        List<List<Integer>> values = PopulationModelGenerator.getValues(environment.getEvaluator(),ctx.range());
        if (values.isEmpty()) {
            registry.register(name);
        } else {
            values.forEach(lst -> registry.register(name, lst.toArray()));
        }
        return registry;
    }

    @Override
    public PopulationRegistry visitLabel_declaration(PopulationModelParser.Label_declarationContext ctx) {
        String name = ctx.name.getText();
        String[] variables = ctx.args.stream().map(Token::getText).toArray(String[]::new);
        registry.addLabel(name, new ParametricValue<>(variables, PopulationModelGenerator.getLabelFunction(variables,environment.getEvaluator(),ctx.species_expression())));
        return registry;
//
//
//        Function<String,Double> resolver = environment.getEvaluator();
//        List<Map<String,Double>> maps = PopulationModelGenerator.getMaps(resolver, ctx.local_variables());
//        maps = PopulationModelGenerator.filter(resolver,ctx.guard_expression(),maps);
//        Set<Integer> integers = new HashSet<>();
//        for (PopulationModelParser.Species_expressionContext se: ctx.species_expression()) {
//            List<Map<String,Double>> localMaps = PopulationModelGenerator.getMaps(resolver, se.local_variables(), maps);
//            localMaps = PopulationModelGenerator.filter(resolver,se.guard_expression(),localMaps);
//            integers.addAll(PopulationModelGenerator.getIndexSet(registry,resolver,localMaps,se.name.getText(), se.expr()));
//        }
//        registry.addLabel(name, integers.stream().mapToInt(i -> i).toArray());
//        return registry;
    }

    @Override
    protected PopulationRegistry defaultResult() {
        return registry;
    }
}
