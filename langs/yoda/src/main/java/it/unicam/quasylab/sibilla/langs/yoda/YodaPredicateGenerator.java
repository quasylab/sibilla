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
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public class YodaPredicateGenerator extends YodaModelBaseVisitor<Boolean> {


    private final Map<String, Predicate<YodaSystemState>> predicates = new HashMap<>();
    private final Function<String, Optional<SibillaValue>> constantsAndParameters;
    private final YodaVariableRegistry variableRegistry;
    private final YodaElementNameRegistry registry;

    public YodaPredicateGenerator(Function<String, Optional<SibillaValue>> constantsAndParameters, YodaVariableRegistry variableRegistry, YodaElementNameRegistry registry) {
        this.constantsAndParameters = constantsAndParameters;
        this.variableRegistry = variableRegistry;
        this.registry = registry;
    }

    @Override
    public Boolean visitMeasureDeclaration(YodaModelParser.MeasureDeclarationContext ctx) {
        YodaFunctionalExpressionEvaluator<YodaSystemState> evaluator = new YodaFunctionalExpressionEvaluator<>(constantsAndParameters, new YodaSystemStateExpressionEvaluationContext(), variableRegistry, registry::getGroup);
        Function<YodaSystemState, SibillaValue> measureFunction = ctx.measure.accept(evaluator);
        predicates.put(ctx.name.getText(), sys -> measureFunction.apply(sys).booleanOf());
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

    public Map<String, Predicate<YodaSystemState>> getPredicates() {
        return predicates;
    }

}
