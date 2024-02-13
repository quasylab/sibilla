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
import it.unicam.quasylab.sibilla.core.models.carma.targets.enba.processes.Process;
import it.unicam.quasylab.sibilla.core.models.carma.targets.enba.processes.actions.InputAction;
import it.unicam.quasylab.sibilla.core.models.carma.targets.enba.processes.actions.OutputAction;
import it.unicam.quasylab.sibilla.core.util.values.SibillaBoolean;
import it.unicam.quasylab.sibilla.langs.enba.ExtendedNBABaseVisitor;
import it.unicam.quasylab.sibilla.langs.enba.ExtendedNBAParser;
import it.unicam.quasylab.sibilla.langs.enba.symbols.SymbolTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

public class ProcessesGenerator extends ExtendedNBABaseVisitor<List<Process>> {
    private List<Process> processes;
    private final SymbolTable table;

    public ProcessesGenerator(SymbolTable table) {
        this.table = table;
        this.processes = new ArrayList<>();
    }

    @Override
    public List<Process> visitModel(ExtendedNBAParser.ModelContext ctx) {
        ctx.element()
                .stream()
                .filter(e -> e.process_declaration() != null)
                .forEach(e -> generateProcess(
                        e.process_declaration().name.getText(),
                        e.process_declaration().body
                ));

        return processes;
    }
    public void generateProcess(String species, ExtendedNBAParser.Process_bodyContext ctx) {
        int predicateSpeciesId = this.table.getSpeciesId(species);
        generateProcess(
                species,
                (agentSpecies, context) -> agentSpecies == predicateSpeciesId,
                ctx
        );
    }

    private void generateProcess(
            String species,
            BiPredicate<Integer, ExpressionContext> predicate,
            ExtendedNBAParser.Process_bodyContext ctx
    ) {
        if(ctx.conditional_process() != null) {
            ExpressionFunction conditionalPredicate = ctx
                    .conditional_process()
                    .predicate
                    .accept(new ExpressionGenerator(table));
            generateProcess(
                    species,
                    (agentSpecies, context) -> predicate.test(agentSpecies, context) &&
                            (conditionalPredicate.eval(context) == SibillaBoolean.TRUE),
                    ctx.conditional_process().then
            );
            generateProcess(
                    species,
                    (agentSpecies, context) -> predicate.test(agentSpecies, context) &&
                            (conditionalPredicate.eval(context) == SibillaBoolean.FALSE),
                    ctx.conditional_process().else_
            );
        } else if(ctx.choice_process() != null) {
            generateActions(species, predicate, ctx.choice_process().action_tuple());
        }
    }

    private void generateActions(
            String species,
            BiPredicate<Integer, ExpressionContext> predicate,
            List<ExtendedNBAParser.Action_tupleContext> actions
    ) {
        Map<String, List<OutputAction>> outputs = new HashMap<>();
        Map<String, List<InputAction>> inputs = new HashMap<>();
        for(ExtendedNBAParser.Action_tupleContext action : actions) {
            if(action.broadcast_input_tuple() != null) {
                generateInputAction(predicate, action.broadcast_input_tuple(), inputs);
            } else if(action.broadcast_output_tuple() != null) {
                generateOutputAction(predicate, action.broadcast_output_tuple(), outputs);
            }
        }
        processes.add(new Process(this.table.getSpeciesId(species), outputs, inputs));
    }

    private void generateInputAction(
            BiPredicate<Integer, ExpressionContext> predicate,
            ExtendedNBAParser.Broadcast_input_tupleContext action,
            Map<String, List<InputAction>> inputs
    ) {
        String channel = action.broadcast_input_action().channel.getText();

        if(!inputs.containsKey(channel)) {
            inputs.put(channel, new ArrayList<>());
        }

        ExpressionFunction probability = action.broadcast_input_action().probability.accept(
                new ExpressionGenerator(this.table)
        );

        ExpressionFunction senderPredicate = action.broadcast_input_action().predicate.accept(
                new ExpressionGenerator(this.table)
        );

        InputAction input = new InputAction(
                channel,
                predicate,
                (context) -> senderPredicate.eval(context) == SibillaBoolean.TRUE,
                context -> probability.eval(context).doubleOf(),
                action.agent_mutation().accept(new AgentMutationGenerator(this.table))
        );

        inputs.get(channel).add(input);
    }

    private void generateOutputAction(
            BiPredicate<Integer, ExpressionContext> predicate,
            ExtendedNBAParser.Broadcast_output_tupleContext action,
            Map<String, List<OutputAction>> outputs
    ) {
        String channel = action.broadcast_output_action().channel.getText();

        if(!outputs.containsKey(channel)) {
            outputs.put(channel, new ArrayList<>());
        }

        ExpressionFunction rate = action.broadcast_output_action().rate.accept(
                new ExpressionGenerator(this.table)
        );

        ExpressionFunction receiverPredicate = action.broadcast_output_action().predicate.accept(
                new ExpressionGenerator(this.table)
        );

        OutputAction output = new OutputAction(
                channel,
                predicate,
                (context) -> receiverPredicate.eval(context) == SibillaBoolean.TRUE,
                context -> rate.eval(context).doubleOf(),
                action.agent_mutation().accept(new AgentMutationGenerator(this.table))
        );

        outputs.get(channel).add(output);
    }

    @Override
    protected List<Process> defaultResult() {
        return new ArrayList<>();
    }
}
