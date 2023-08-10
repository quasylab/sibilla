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
import it.unicam.quasylab.sibilla.core.models.lio.LIOModel;
import it.unicam.quasylab.sibilla.core.models.lio.LIOState;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import it.unicam.quasylab.sibilla.core.simulator.sampling.SimpleMeasure;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import it.unicam.quasylab.sibilla.langs.util.ErrorCollector;

import java.util.HashMap;
import java.util.Map;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

/**
 * This class is used to generate the measures declared in a model.
 */
public class LIOModelMeasuresGenerator extends LIOModelAgentDependentChecker {

    private final Map<String, ToDoubleFunction<? super LIOState>> measures;

    public LIOModelMeasuresGenerator(ErrorCollector errors, AgentsDefinition definition, Map<String, SibillaValue> constantsAndParameters) {
        super(errors, definition, constantsAndParameters);
        this.measures = new HashMap<>();
    }


    @Override
    public Boolean visitElementMeasure(LIOModelParser.ElementMeasureContext ctx) {
        measures.put(ctx.name.getText(), evalCollectiveExpression(ctx.value)::toDouble);
        return true;
    }

    /**
     * Returns the map containing the measures defined in the model. This method should be invoked only
     * after the visit has been completed.
     *
     * @return the map containing the measures defined in the model.
     */
    public Map<String, Measure<? super LIOState>> getMeasures() {
        return measures.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new SimpleMeasure<>(e.getKey(), e.getValue())));
    }
}
