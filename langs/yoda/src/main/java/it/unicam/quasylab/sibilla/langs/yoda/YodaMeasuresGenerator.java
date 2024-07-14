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

package it.unicam.quasylab.sibilla.langs.yoda;

import it.unicam.quasylab.sibilla.core.models.yoda.YodaElementNameRegistry;
import it.unicam.quasylab.sibilla.core.models.yoda.YodaSystemState;
import it.unicam.quasylab.sibilla.core.models.yoda.YodaVariableRegistry;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import it.unicam.quasylab.sibilla.core.simulator.sampling.SimpleMeasure;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

public class YodaMeasuresGenerator extends YodaModelBaseVisitor<Boolean> {


    private final Map<String, ToDoubleFunction<YodaSystemState>> measures = new HashMap<>();
    private final Function<String, Optional<SibillaValue>> constantsAndParameters;
    private final  Function<String, Optional<YodaFunction>> functions;
    private final YodaVariableRegistry variableRegistry;
    private final YodaElementNameRegistry registry;

    public YodaMeasuresGenerator( Function<String, Optional<YodaFunction>> functions, Function<String, Optional<SibillaValue>> constantsAndParameters, YodaVariableRegistry variableRegistry, YodaElementNameRegistry registry) {
        this.constantsAndParameters = constantsAndParameters;
        this.variableRegistry = variableRegistry;
        this.registry = registry;
        this.functions = functions;
    }

    @Override
    public Boolean visitMeasureDeclaration(YodaModelParser.MeasureDeclarationContext ctx) {
        YodaExpressionEvaluator evaluator = new YodaExpressionEvaluator(functions, constantsAndParameters, variableRegistry, registry::getGroup);
        Function<YodaExpressionEvaluationContext, SibillaValue> measureFunction = ctx.measure.accept(evaluator);
        measures.put(ctx.name.getText(), sys -> measureFunction.apply(new YodaExpressionEvaluationSystemContext(sys)).doubleOf());
        return true;
    }

    @Override
    protected Boolean defaultResult() {
        return true;
    }

    @Override
    protected Boolean aggregateResult(Boolean aggregate, Boolean nextResult) {
        return aggregate && nextResult;
    }

    public Map<String, Measure<YodaSystemState>> getMeasures() {
        return this.measures.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new SimpleMeasure<>(e.getKey(), e.getValue())));
    }

}
