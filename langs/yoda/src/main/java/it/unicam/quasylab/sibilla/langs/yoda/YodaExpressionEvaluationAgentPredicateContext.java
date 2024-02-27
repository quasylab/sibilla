/*
 *  Sibilla:  a Java framework designed to support analysis of Collective
 *  Adaptive Systems.
 *
 *              Copyright (C) ${YEAR}.
 *
 *  See the NOTICE file distributed with this work for additional information
 *  regarding copyright ownership.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *    or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package it.unicam.quasylab.sibilla.langs.yoda;

import it.unicam.quasylab.sibilla.core.models.yoda.YodaAgent;
import it.unicam.quasylab.sibilla.core.models.yoda.YodaSceneElement;
import it.unicam.quasylab.sibilla.core.models.yoda.YodaVariable;
import it.unicam.quasylab.sibilla.core.models.yoda.YodaVariableMapping;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import org.apache.commons.math3.random.RandomGenerator;

public class YodaExpressionEvaluationAgentPredicateContext implements YodaExpressionEvaluationContext {
    private final YodaSceneElement it;
    private final YodaSceneElement element;

    public YodaExpressionEvaluationAgentPredicateContext(YodaSceneElement it, YodaSceneElement element) {
        this.it = it;
        this.element = element;
    }

    @Override
    public SibillaValue get(YodaVariable var) {
        return element.get(var);
    }

    @Override
    public SibillaValue it(YodaVariable name) {
        return it.get(name);
    }
}
