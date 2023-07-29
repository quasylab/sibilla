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

import it.unicam.quasylab.sibilla.core.models.lio.AgentsDefinition;
import it.unicam.quasylab.sibilla.core.models.lio.LIOCollective;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import it.unicam.quasylab.sibilla.langs.util.ErrorCollector;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * This class is used to generate the predicates declared in a model.
 */
public class LIOModelPredicateGenerator extends LIOModelAgentDependentChecker {

    private final Map<String, Predicate<LIOCollective>> predicates;

    public LIOModelPredicateGenerator(ErrorCollector errors, AgentsDefinition definition, Map<String, SibillaValue> constantsAndParameters) {
        super(errors, definition, constantsAndParameters);
        this.predicates = new HashMap<>();
    }


    @Override
    public Boolean visitElementPredicate(LIOModelParser.ElementPredicateContext ctx) {
        predicates.put(ctx.name.getText(), evalCollectiveExpression(ctx.value)::toBoolean);
        return true;
    }

    /**
     * Returns the map containing the predicates defined in the model. This method should be invoked only
     * after the visit has been completed.
     *
     * @return the map containing the predicates defined in the model.
     */
    public Map<String, Predicate<LIOCollective>> getPredicates() {
        return predicates;
    }
}
