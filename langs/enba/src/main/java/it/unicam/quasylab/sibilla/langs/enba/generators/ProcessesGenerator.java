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
        int speciesId = this.table.getSpeciesId(species);
        Process process = new Process(speciesId, new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
        generateProcess(
                (agentSpecies, context) -> agentSpecies == speciesId,
                process,
                ctx
        );
        processes.add(process);
    }

    private void generateProcess(
            BiPredicate<Integer, ExpressionContext> predicate,
            Process process,
            ExtendedNBAParser.Process_bodyContext ctx
    ) {
        if(ctx.conditional_process() != null) {
            ExpressionFunction conditionalPredicate = ctx
                    .conditional_process()
                    .predicate
                    .accept(new ExpressionGenerator(table));
            generateProcess(
                    (agentSpecies, context) -> predicate.test(agentSpecies, context) &&
                            (conditionalPredicate.eval(context) == SibillaBoolean.TRUE),
                    process,
                    ctx.conditional_process().then
            );
            generateProcess(
                    (agentSpecies, context) -> predicate.test(agentSpecies, context) &&
                            (conditionalPredicate.eval(context) == SibillaBoolean.FALSE),
                    process,
                    ctx.conditional_process().else_
            );
        } else if(ctx.choice_process() != null) {
            generateActions(predicate, process, ctx.choice_process().action_tuple());
        }
    }

    private void generateActions(
            BiPredicate<Integer, ExpressionContext> predicate,
            Process process,
            List<ExtendedNBAParser.Action_tupleContext> actions
    ) {
        for(ExtendedNBAParser.Action_tupleContext tctx : actions) {
            if(tctx.input_tuple() != null) {
                generateInputAction(
                        predicate,
                        tctx.input_tuple(),
                        process
                );
            } else if(tctx.output_tuple() != null) {
                generateOutputAction(
                        predicate,
                        tctx.output_tuple(),
                        process
                );
            }
        }
    }

    private void generateInputAction(
            BiPredicate<Integer, ExpressionContext> predicate,
            ExtendedNBAParser.Input_tupleContext tctx,
            Process process
    ) {
        String channel = tctx.input_action().channel.getText();

        ExpressionFunction probability = tctx.input_action().probability.accept(
                new ExpressionGenerator(this.table)
        );

        ExpressionFunction senderPredicate = tctx.input_action().predicate.accept(
                new ExpressionGenerator(this.table)
        );

        InputAction input = new InputAction(
                channel,
                predicate,
                (context) -> senderPredicate.eval(context) == SibillaBoolean.TRUE,
                context -> probability.eval(context).doubleOf(),
                tctx.agent_mutation().accept(new AgentMutationGenerator(this.table))
        );

        if(tctx.input_action().broadcast != null) {
            if(!process.getBroadcastInputs().containsKey(channel)) {
                process.getBroadcastInputs().put(channel, new ArrayList<>());
            }
            process.getBroadcastInputs().get(channel).add(input);
        } else {
            if(!process.getUnicastInputs().containsKey(channel)) {
                process.getUnicastInputs().put(channel, new ArrayList<>());
            }
            process.getUnicastInputs().get(channel).add(input);
        }
    }

    private void generateOutputAction(
            BiPredicate<Integer, ExpressionContext> predicate,
            ExtendedNBAParser.Output_tupleContext tctx,
            Process process
    ) {
        String channel = tctx.output_action().channel.getText();

        ExpressionFunction rate = tctx.output_action().rate.accept(
                new ExpressionGenerator(this.table)
        );

        ExpressionFunction receiverPredicate = tctx.output_action().predicate.accept(
                new ExpressionGenerator(this.table)
        );

        OutputAction output = new OutputAction(
                channel,
                predicate,
                (context) -> receiverPredicate.eval(context) == SibillaBoolean.TRUE,
                context -> rate.eval(context).doubleOf(),
                tctx.agent_mutation().accept(new AgentMutationGenerator(this.table))
        );

        if(tctx.output_action().broadcast != null) {
            if(!process.getBroadcastOutputs().containsKey(channel)) {
                process.getBroadcastOutputs().put(channel, new ArrayList<>());
            }
            process.getBroadcastOutputs().get(channel).add(output);
        } else {
            if(!process.getUnicastOutputs().containsKey(channel)) {
                process.getUnicastOutputs().put(channel, new ArrayList<>());
            }
            process.getUnicastOutputs().get(channel).add(output);
        }
    }

    @Override
    protected List<Process> defaultResult() {
        return new ArrayList<>();
    }
}
