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
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import it.unicam.quasylab.sibilla.langs.util.ErrorCollector;

import java.util.Map;

public class LIOModelActionsGenerator extends LIOModelAgentDependentChecker {

    public LIOModelActionsGenerator(AgentsDefinition definition, ErrorCollector errors, Map<String, SibillaValue> constantsAndParameters) {
        super(errors, definition, constantsAndParameters);
    }


    @Override
    public Boolean visitElementAction(LIOModelParser.ElementActionContext ctx) {
        CollectiveExpressionEvaluationFunction evaluationFunction = evalCollectiveExpression(ctx.probability);
        this.definition.addAction(ctx.name.getText(), s -> evaluationFunction.eval(s).doubleOf());
        return true;
    }


}
