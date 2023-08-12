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

package it.unicam.quasylab.sibilla.langs.lio;

import it.unicam.quasylab.sibilla.core.models.lio.LIOAgent;
import it.unicam.quasylab.sibilla.core.models.lio.LIOAgentName;
import it.unicam.quasylab.sibilla.core.models.lio.LIOAgentDefinitions;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import it.unicam.quasylab.sibilla.langs.util.ErrorCollector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This class is used to generate the atomic propositions declared in a model.
 */
public class LIOModelAtomicGenerator extends LIOModelAgentDependentChecker {

    private final Map<String, Predicate<LIOAgent>> atomic;

    public LIOModelAtomicGenerator(ErrorCollector errors, LIOAgentDefinitions definition, Map<String, SibillaValue> constantsAndParameters) {
        super(errors, definition, constantsAndParameters);
        this.atomic = new HashMap<>();
    }


    @Override
    public Boolean visitElementAtomic(LIOModelParser.ElementAtomicContext ctx) {
        List<Predicate<LIOAgentName>> predicates = ctx.states.stream().map(this::getAgentPredicate).collect(Collectors.toList());
        this.atomic.put(ctx.name.getText(), a -> predicates.stream().anyMatch(p -> p.test(a.getName())));
        return true;
    }

    private Predicate<LIOAgentName> getAgentPredicate(LIOModelParser.AgentPatternContext agentPatternContext) {
        Map<String, Integer> variableIndexes = getAgentPatternVariableIndexes(agentPatternContext.patternElements);
        String name = agentPatternContext.name.getText();
        Predicate<SibillaValue[]> agentIndexPredicate = getAgentIndexPredicate(variableIndexes, agentPatternContext.guard);
        return an -> an.getName().equals(name)&agentIndexPredicate.test(an.getIndexes());
    }

    private Map<String, Integer> getAgentPatternVariableIndexes(List<LIOModelParser.PatternElementContext> patternElements) {
        Map<String, Integer> variableIndexes = new HashMap<>();
        int counter = 0;
        for (LIOModelParser.PatternElementContext pe: patternElements) {
            if (pe instanceof  LIOModelParser.PatternElementVariableContext) {
                variableIndexes.put(((LIOModelParser.PatternElementVariableContext) pe).name.getText(), counter++);
            }
        }
        return variableIndexes;
    }

    /**
     * Returns the map containing the atomic propositions defined in the model. This method should be invoked only
     * after the visit has been completed.
     *
     * @return the map containing the predicates defined in the model.
     */
    public Map<String, Predicate<LIOAgent>> getAtomicPropositions() {
        return atomic;
    }
}
