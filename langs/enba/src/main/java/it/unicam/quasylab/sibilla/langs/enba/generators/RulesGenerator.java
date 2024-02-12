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
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.expressions.ExpressionFunction;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.Rule;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.transitions.InputTransition;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.transitions.OutputTransition;
import it.unicam.quasylab.sibilla.core.util.values.SibillaBoolean;
import it.unicam.quasylab.sibilla.langs.enba.ExtendedNBABaseVisitor;
import it.unicam.quasylab.sibilla.langs.enba.ExtendedNBAParser;
import it.unicam.quasylab.sibilla.langs.enba.symbols.SymbolTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

public class RulesGenerator extends ExtendedNBABaseVisitor<List<Rule>> {
    private record OutputAction(
            String species,
            ExtendedNBAParser.Broadcast_output_tupleContext outputCtx,
            Rule rule
    ) {}
    private Map<String, List<OutputAction>> outputActions;
    private final SymbolTable table;

    public RulesGenerator(SymbolTable table) {
        this.table = table;
        this.outputActions = new HashMap<>();
    }

    @Override
    public List<Rule> visitModel(ExtendedNBAParser.ModelContext ctx) {
        ctx.element()
                .stream()
                .filter(e -> e.process_declaration() != null)
                .forEach(e -> generateProcess(
                        e.process_declaration().name.getText(),
                        e.process_declaration().body,
                        false
                ));

        ctx.element()
                .stream()
                .filter(e -> e.process_declaration() != null)
                .forEach(e -> generateProcess(
                        e.process_declaration().name.getText(),
                        e.process_declaration().body,
                        true
                ));

        return outputActions
                .values()
                .stream()
                .flatMap(l -> l.stream().map(o -> o.rule))
                .toList();
    }
    public void generateProcess(String species, ExtendedNBAParser.Process_bodyContext ctx, boolean inputPass) {
        int predicateSpeciesId = this.table.getSpeciesId(species);
        generateProcess(
                species,
                (agentSpecies, context) -> agentSpecies == predicateSpeciesId,
                ctx,
                inputPass
        );
    }

    private void generateProcess(
            String species,
            BiPredicate<Integer, ExpressionContext> predicate,
            ExtendedNBAParser.Process_bodyContext ctx,
            boolean inputPass
    ) {
        if(ctx.conditional_process() != null) {
            ExpressionFunction conditionalPredicate = ctx
                    .conditional_process()
                    .predicate
                    .accept(new ExpressionGenerator(table, species, null));
            generateProcess(
                    species,
                    (agentSpecies, context) -> predicate.test(agentSpecies, context) &&
                            (conditionalPredicate.eval(context) == SibillaBoolean.TRUE),
                    ctx.conditional_process().then,
                    inputPass
            );
            generateProcess(
                    species,
                    (agentSpecies, context) -> predicate.test(agentSpecies, context) &&
                            (conditionalPredicate.eval(context) == SibillaBoolean.FALSE),
                    ctx.conditional_process().else_,
                    inputPass
            );
        } else if(ctx.choice_process() != null) {
            generateActions(species, predicate, ctx.choice_process().action_tuple(), inputPass);
        }
    }

    private void generateActions(
            String species,
            BiPredicate<Integer, ExpressionContext> predicate,
            List<ExtendedNBAParser.Action_tupleContext> actions,
            boolean inputPass
    ) {
        for(ExtendedNBAParser.Action_tupleContext action : actions) {
            if(action.broadcast_input_tuple() != null && inputPass) {
                generateInputAction(species, predicate, action.broadcast_input_tuple());
            } else if(action.broadcast_output_tuple() != null && !inputPass) {
                generateOutputAction(species, predicate, action.broadcast_output_tuple());
            }
        }
    }

    private void generateInputAction(
            String species,
            BiPredicate<Integer, ExpressionContext> predicate,
            ExtendedNBAParser.Broadcast_input_tupleContext action
    ) {
        String channel = action.broadcast_input_action().channel.getText();
        if(outputActions.containsKey(channel)) {
            for(OutputAction out : outputActions.get(channel)) {
                ExpressionFunction outputPredicate = out.outputCtx.broadcast_output_action().predicate.accept(new ExpressionGenerator(this.table, species, null));
                ExpressionFunction senderPredicate = action.broadcast_input_action().predicate.accept(new ExpressionGenerator(this.table, out.species, null));
                ExpressionFunction probability = action.broadcast_input_action().probability.accept(new ExpressionGenerator(this.table, species, null));
                InputTransition inputTransition = new InputTransition(
                        (s, c) -> predicate.test(s, c) && (outputPredicate.eval(c) == SibillaBoolean.TRUE),
                        c -> senderPredicate.eval(c) == SibillaBoolean.TRUE,
                        c -> probability.eval(c).doubleOf(),
                        action.agent_mutation().accept(new AgentMutationGenerator(table, species, out.species))
                );
                out.rule.getInputs().add(inputTransition);
            }
        }
    }

    private void generateOutputAction(
            String species,
            BiPredicate<Integer, ExpressionContext> predicate,
            ExtendedNBAParser.Broadcast_output_tupleContext action
    ) {
        ExpressionFunction rate = action.broadcast_output_action().rate.accept(
                new ExpressionGenerator(this.table, species, null)
        );

        OutputTransition output = new OutputTransition(
                predicate,
                context -> rate.eval(context).doubleOf(),
                action.agent_mutation().accept(new AgentMutationGenerator(this.table, species, null))
        );

        String channel = action.broadcast_output_action().channel.getText();

        if(!outputActions.containsKey(channel)) {
            outputActions.put(channel, new ArrayList<>());
        }
        outputActions.get(channel).add(new OutputAction(species, action, new Rule(output, new ArrayList<>())));
    }

    @Override
    protected List<Rule> defaultResult() {
        return new ArrayList<>();
    }
}
