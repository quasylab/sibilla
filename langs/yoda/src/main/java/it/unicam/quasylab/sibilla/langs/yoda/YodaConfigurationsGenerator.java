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

import it.unicam.quasylab.sibilla.core.models.ParametricDataSet;
import it.unicam.quasylab.sibilla.core.models.ParametricValue;
import it.unicam.quasylab.sibilla.core.models.yoda.*;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class YodaConfigurationsGenerator extends YodaModelBaseVisitor<Boolean> {

    private final Map<String, Function<RandomGenerator, YodaSystemState>> stateSuppliers;

    private Function<RandomGenerator, YodaSystemState> defaultStateSupplier;

    private final Function<String, Optional<SibillaValue>> constantsAndParameters;

    private final YodaAgentsDefinitions definitions;

    private final YodaVariableRegistry variableRegistry;

    private final YodaElementNameRegistry elementNameRegistry;

    public YodaConfigurationsGenerator(Function<String, Optional<SibillaValue>> constantsAndParameters, YodaAgentsDefinitions definitions, YodaVariableRegistry variableRegistry, YodaElementNameRegistry elementNameRegistry) {
        this.stateSuppliers = new HashMap<>();
        this.constantsAndParameters = constantsAndParameters;
        this.definitions = definitions;
        this.variableRegistry = variableRegistry;
        this.elementNameRegistry = elementNameRegistry;
    }



    @Override
    protected Boolean defaultResult() {
        return true;
    }

    @Override
    protected Boolean aggregateResult(Boolean aggregate, Boolean nextResult) {
        return true;
    }

    @Override
    public Boolean visitConfigurationDeclaration(YodaModelParser.ConfigurationDeclarationContext ctx) {
        Function<RandomGenerator, YodaSystemState> supplier = rg -> generateSystemState(rg, ctx.collectives);
        this.stateSuppliers.put(ctx.name.getText(), supplier);
        if (defaultStateSupplier == null) {
            defaultStateSupplier = supplier;
        }
        return true;
    }

    public YodaSystemState generateSystemState(RandomGenerator rg, List<YodaModelParser.CollectiveExpressionContext> collectiveExpressions) {
        YodaCollectiveExpressionEvaluator evaluator = new YodaCollectiveExpressionEvaluator(rg);
        collectiveExpressions.forEach(ce -> ce.accept(evaluator));
        return new YodaSystemState(evaluator.getAgents(), evaluator.getSceneElements());
    }

    public ParametricDataSet<Function<RandomGenerator, YodaSystemState>> getGeneratedConfigurations() {
        ParametricDataSet<Function<RandomGenerator, YodaSystemState>> dataSet = new ParametricDataSet<>();
        for (Map.Entry<String, Function<RandomGenerator, YodaSystemState>> entry: stateSuppliers.entrySet()) {
            dataSet.set(entry.getKey(), new ParametricValue<>(entry.getValue()));
        }
        dataSet.setDefaultState(new ParametricValue<>(defaultStateSupplier));
        return dataSet;
    }

    public class YodaCollectiveExpressionEvaluator extends YodaModelBaseVisitor<Boolean> {


        private final RandomGenerator rg;
        private final List<YodaAgent> listOfAgents = new LinkedList<>();
        private final List<YodaSceneElement> listOfElements = new LinkedList<>();

        private int agentCounter;

        private final Map<String, SibillaValue> localVariables = new HashMap<>();

        public YodaCollectiveExpressionEvaluator(RandomGenerator rg) {
            this.rg = rg;
        }

        public List<YodaAgent> getAgents() {
            return listOfAgents;
        }

        public List<YodaSceneElement> getSceneElements() {
            return listOfElements;
        }

        @Override
        public Boolean visitCollectiveExpressionIndividual(YodaModelParser.CollectiveExpressionIndividualContext ctx) {
            YodaElementName name = elementNameRegistry.get(ctx.elementName.getText());
            if (definitions.isAgent(name)) {
                listOfAgents.add(definitions.getAgent(agentCounter++, name, getVariableMapping(ctx.fieldAssignment())));
                return true;
            }
            if (definitions.isSceneElement(name)) {
                listOfElements.add(definitions.getElement(agentCounter++, name, getVariableMapping(ctx.fieldAssignment())));
                return true;
            }
            return false;
        }

        @Override
        public Boolean visitCollectiveExpressionFor(YodaModelParser.CollectiveExpressionForContext ctx) {
            List<SibillaValue> values = ctx.setOfValues().accept(new CollectiveValuesSetGenerator(rg, getNameSolver()));
            String localVariableName = ctx.name.getText();
            SibillaValue oldValue = localVariables.get(localVariableName);
            for (SibillaValue value: values) {
                localVariables.put(localVariableName, value);
                ctx.body.forEach(ce -> ce.accept(this));
                localVariables.remove(localVariableName);
            }
            if (oldValue != null) localVariables.put(localVariableName, oldValue);
            return true;
        }

        @Override
        public Boolean visitCollectiveExpressionIfElse(YodaModelParser.CollectiveExpressionIfElseContext ctx) {
            if (ctx.guard.accept(new YodaScalarExpressionEvaluator(getNameSolver())).booleanOf()) {
                ctx.thenCollective.forEach(ce -> ce.accept(this));
            } else {
                ctx.elseeCollective.forEach(ce -> ce.accept(this));
            }
            return true;
        }

        private YodaVariableMapping getVariableMapping(List<YodaModelParser.FieldAssignmentContext> nameDeclarationList) {
            return new YodaVariableMapping(
                    nameDeclarationList.stream()
                            .collect(Collectors.toMap(
                                    fa -> variableRegistry.get(fa.name.getText()),
                                    fa -> fa.value.accept(new YodaScalarExpressionEvaluator(getNameSolver())))
                            ));
        }

        private Function<String, Optional<SibillaValue>> getNameSolver() {
            return name -> {
                if (localVariables.containsKey(name)) {
                    return Optional.of(localVariables.get(name));
                } else {
                    return constantsAndParameters.apply(name);
                }
            };
        }

    }

    class CollectiveValuesSetGenerator extends YodaModelBaseVisitor<List<SibillaValue>> {

        private final Function<String, Optional<SibillaValue>> values;
        private final RandomGenerator rg;

        CollectiveValuesSetGenerator(RandomGenerator rg, Function<String, Optional<SibillaValue>> values) {
            this.values = values;
            this.rg = rg;
        }

        @Override
        protected List<SibillaValue> defaultResult() {
            return new LinkedList<>();
        }

        @Override
        protected List<SibillaValue> aggregateResult(List<SibillaValue> aggregate, List<SibillaValue> nextResult) {
            aggregate.addAll(nextResult);
            return aggregate;
        }

        @Override
        public List<SibillaValue> visitSetOfValuesInterval(YodaModelParser.SetOfValuesIntervalContext ctx) {
            SibillaValue from = ctx.from.accept(new YodaScalarExpressionEvaluator(this.values));
            SibillaValue to = ctx.from.accept(new YodaScalarExpressionEvaluator(this.values));
            return IntStream.range(from.intOf(), to.intOf()).mapToObj(SibillaValue::of).collect(Collectors.toList());
        }

        @Override
        public List<SibillaValue> visitSetOfValuesEnumeration(YodaModelParser.SetOfValuesEnumerationContext ctx) {
            YodaScalarExpressionEvaluator evaluator = new YodaScalarExpressionEvaluator(this.values);
            return ctx.values.stream().map(exp -> exp.accept(evaluator)).collect(Collectors.toList());
        }

        @Override
        public List<SibillaValue> visitSetOfValuesRandom(YodaModelParser.SetOfValuesRandomContext ctx) {
            YodaExpressionEvaluator evaluator = new YodaExpressionEvaluator(values, variableRegistry);
            int size = ctx.size.accept(evaluator).apply(YodaExpressionEvaluationContext.EMPTY_CONTEXT).intOf();
            Function<YodaExpressionEvaluationContext, SibillaValue> generator = ctx.generator.accept(evaluator);
            return generateRandomSet(size, rg -> generator.apply(new YodaExpressionEvaluationRandomContext(rg)), ctx.distinct!=null);
        }

        private List<SibillaValue> generateRandomSet(int size, Function<RandomGenerator, SibillaValue> generator, boolean b) {
            Set<SibillaValue> result = new HashSet<>();
            for(int i=0; i<size; i++) {
                SibillaValue value;
                do {
                    value = generator.apply(rg);
                } while (result.contains(value));
                result.add(value);
            }
            return new LinkedList<>(result);
        }

    }

}
