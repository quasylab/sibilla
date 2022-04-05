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

import it.unicam.quasylab.sibilla.core.models.EvaluationEnvironment;
import it.unicam.quasylab.sibilla.core.models.slam.*;
import org.antlr.v4.runtime.RuleContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlamModelGenerator {

    private SlamModelParser.ModelContext modelContext;
    private SymbolTable table;


    public EvaluationEnvironment getEvaluationEnvironment() {
        EvaluationEnvironment environment = new EvaluationEnvironment();
        ScalarExpressionEvaluator evaluator = new ScalarExpressionEvaluator(environment);
        modelContext.params.forEach(p -> environment.set(p.name.getText(), p.value.accept(evaluator).toDouble()));
        return environment;
    }

    public Map<String, SlamValue> evalConstants(EvaluationEnvironment environment) {
        HashMap<String, SlamValue> constants = new HashMap<>();
        ScalarExpressionEvaluator evaluator = new ScalarExpressionEvaluator(environment);
        modelContext.consts.forEach(c -> constants.put(c.name.getText(), c.value.accept(evaluator)));
        return constants;
    }

    public MessageRepository getMessageRepository() {
        MessageRepository repository = new MessageRepository();
        modelContext.messages.forEach(m -> repository.addTag(m.tag.getText(),
                m.content.stream().map(RuleContext::getText).map(SlamType::getTypeOf).toArray(SlamType[]::new)));
        return repository;
    }

    public AgentDefinition getAgentDefinition() {
        AgentDefinition definition = new AgentDefinition();
        for (SlamModelParser.DeclarationAgentContext agentDefinition : modelContext.agents) {
            String agentName = agentDefinition.name.getText();
            definition.addAgent(agentName, table.getAgentParameters(agentDefinition.name.getText()));
            definition.setAgentBehaviour(agentName, getAgentBehaviour(agentName, agentDefinition.states));
        }
        return definition;
    }

    private AgentBehaviour getAgentBehaviour(String agentName, List<SlamModelParser.AgentStateDeclarationContext> states) {
        AgentBehaviour behaviour = new AgentBehaviour();
        states.forEach(s -> behaviour.addState(s.name.getText()));
        states.forEach(s -> populateAgentStateInfo(behaviour, s));
        return behaviour;
    }

    private void populateAgentStateInfo(AgentBehaviour behaviour, SlamModelParser.AgentStateDeclarationContext state) {

    }
}
