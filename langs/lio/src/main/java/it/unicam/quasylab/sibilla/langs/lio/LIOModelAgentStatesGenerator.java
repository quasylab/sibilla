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

import it.unicam.quasylab.sibilla.core.models.lio.LIOAgentDefinitions;
import it.unicam.quasylab.sibilla.core.util.datastructures.ImmutableList;
import it.unicam.quasylab.sibilla.core.util.values.SibillaInteger;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import it.unicam.quasylab.sibilla.langs.util.ErrorCollector;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class LIOModelAgentStatesGenerator extends LIOModelAgentDependentChecker {



    public LIOModelAgentStatesGenerator(ErrorCollector errors, LIOAgentDefinitions definition, Map<String, SibillaValue> constantsAndParameters) {
        super(errors, definition, constantsAndParameters);
    }


    @Override
    public Boolean visitElementState(LIOModelParser.ElementStateContext ctx) {
        String name = ctx.name.getText();
        if (ctx.agentParameters.isEmpty()) {
            this.definition.addAgent(name);
        } else {
            generateListOfStateParameters(ctx.agentParameters).forEach(values -> definition.addAgent(name, values));
        }
        return true;
    }

    private List<SibillaValue[]> generateListOfStateParameters(List<LIOModelParser.AgentParameterDeclarationContext> agentParameters) {
        List<ImmutableList<SibillaValue>> values = List.of(ImmutableList.empty(SibillaValue.class));
        for (LIOModelParser.AgentParameterDeclarationContext parameter: agentParameters) {
            values = combine(values, generateValuesFromInterval(parameter.from, parameter.to));
        }
        return values.stream().map(ImmutableList::reverse).map(lst -> lst.toArray(SibillaValue[]::new)).collect(Collectors.toList());
    }

    private List<ImmutableList<SibillaValue>> combine(List<ImmutableList<SibillaValue>> valueListList, List<SibillaValue> valueList) {
        return valueListList.stream().map(lst -> doMultiply(lst, valueList)).flatMap(List::stream).collect(Collectors.toList());
    }

    private List<ImmutableList<SibillaValue>> doMultiply(ImmutableList<SibillaValue> list1, List<SibillaValue> list2) {
        return list2.stream().map(list1::add).collect(Collectors.toList());
    }

    private List<SibillaValue> generateValuesFromInterval(LIOModelParser.ExprContext from, LIOModelParser.ExprContext to) {
        return generateValuesFromInterval(
                evalGlobalExpression(from).intOf(),
                evalGlobalExpression(to).intOf()+1
        );
    }

    private List<SibillaValue> generateValuesFromInterval(int from, int to) {
        return IntStream.range(from, to).mapToObj(SibillaInteger::new).collect(Collectors.toList());
    }

}
