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

package it.unicam.quasylab.sibilla.core.models.pm;

import it.unicam.quasylab.sibilla.core.models.AbstractModelDefinition;
import it.unicam.quasylab.sibilla.core.models.EvaluationEnvironment;
import it.unicam.quasylab.sibilla.core.models.StateSet;
import it.unicam.quasylab.sibilla.core.models.pm.util.PopulationRegistry;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * Instances of this class represent the definition of a population model.
 */
public class PopulationModelDefinition extends AbstractModelDefinition<PopulationState> {

    private final Function<EvaluationEnvironment,PopulationRegistry> registryBuilder;
    private final BiFunction<EvaluationEnvironment,PopulationRegistry,List<PopulationRule>> rulesBuilder;
    private final BiFunction<EvaluationEnvironment,PopulationRegistry,Map<String,Measure<PopulationState>>> measuresBuilder;
    private final BiFunction<EvaluationEnvironment,PopulationRegistry,Map<String, Predicate<PopulationState>>> predicatesBuilder;
    private final BiFunction<EvaluationEnvironment, PopulationRegistry, StateSet<PopulationState>> statesBuilder;
    //EvaluationEnvironment -> Populationregistry -> double[] ->

    private PopulationRegistry registry;
    private List<PopulationRule> rules;
    private Map<String,Measure<PopulationState>> measures;
    private PopulationModel model;
    private StateSet<PopulationState> states;
    private Map<String, Predicate<PopulationState>> predicates;

    /**
     * Create a new PopulationModelDefinition with the given functions used to build the elements of a definition.
     * Only default measures are generated.
     *
     * @param registryBuilder function used to build the population registry.
     * @param rulesBuilder function used to build the population rules.
     */
    public PopulationModelDefinition(
            Function<EvaluationEnvironment, PopulationRegistry> registryBuilder,
            BiFunction<EvaluationEnvironment, PopulationRegistry, List<PopulationRule>> rulesBuilder,
            BiFunction<EvaluationEnvironment, PopulationRegistry, StateSet<PopulationState>> statesBuilder
    ) {
        this(new EvaluationEnvironment(),registryBuilder,rulesBuilder,null, null, statesBuilder);
    }

    /**
     * Create a new PopulationModelDefinition with the given functions used to build the elements of a definition.
     *
     * @param environment evalutaion environment used to build the model.
     * @param registryBuilder function used to build the population registry.
     * @param rulesBuilder function used to build the population rules.
     * @param measuresBuilder function used to build the measures.
     */
    public PopulationModelDefinition(
            EvaluationEnvironment environment,
            Function<EvaluationEnvironment, PopulationRegistry> registryBuilder,
            BiFunction<EvaluationEnvironment, PopulationRegistry, List<PopulationRule>> rulesBuilder,
            BiFunction<EvaluationEnvironment, PopulationRegistry, Map<String, Measure<PopulationState>>> measuresBuilder,
            BiFunction<EvaluationEnvironment, PopulationRegistry, Map<String, Predicate<PopulationState>>> predicatesBuilder,
            BiFunction<EvaluationEnvironment, PopulationRegistry, StateSet<PopulationState>> statesBuilder) {
        super(environment);
        this.registryBuilder = registryBuilder;
        this.rulesBuilder = rulesBuilder;
        this.measuresBuilder = measuresBuilder;
        this.predicatesBuilder = predicatesBuilder;
        this.statesBuilder = statesBuilder;
    }

    @Override
    protected void clearCache() {
        this.registry = null;
        this.rules = null;
        this.model = null;
        this.measures = null;
        this.states = null;
    }


    @Override
    public synchronized final PopulationModel createModel() {
        if (model == null) {
            PopulationRegistry registry = getRegistry();
            List<PopulationRule> rules = getRules();
            Map<String,Measure<PopulationState>> measures = getMeasures();
            Map<String,Predicate<PopulationState>> predicates = getPredicates();
            model = new PopulationModel(registry,rules,measures, predicates);
        }
        return model;
    }

    /**
     * Generate the measures used in the model generated by using the current environment.
     *
     * @return the measures used in the model generated by using the current environment.
     */
    private Map<String, Measure<PopulationState>> getMeasures() {
        if (measures == null) {
            measures = getDefaultMeasure();
            if (measuresBuilder != null) {
                measures.putAll(measuresBuilder.apply(getEnvironment(),getRegistry()));
            }
        }
        return measures;
    }

    private Map<String, Predicate<PopulationState>> getPredicates() {
        if (predicates == null) {
            predicates = new TreeMap<>();
            if (predicatesBuilder != null) {
                predicates.putAll(predicatesBuilder.apply(getEnvironment(),getRegistry()));
            }
        }
        return predicates;
    }

    /**
     * Generate the rules used in the model generated by using the current environment.
     *
     * @return the rules used in the model generated by using the current environment.
     */
    private List<PopulationRule> getRules() {
        if (rules == null) {
            this.rules = new LinkedList<>();
            this.rules.addAll(rulesBuilder.apply(getEnvironment(),getRegistry()));
        }
        return rules;
    }


    /**
     * Generate the population registry associated with the current evaluation environment.
     *
     * @return the population registry associated with the current evaluation environment.
     */
    private PopulationRegistry getRegistry() {
        if (this.registry == null) {
            this.registry = registryBuilder.apply(getEnvironment());
        }
        return this.registry;
    }

    /**
     * Generate the default measures associated with each model.
     *
     * @return
     */
    private Map<String,Measure<PopulationState>> getDefaultMeasure() {
        PopulationRegistry reg = getRegistry();
        Map<String,Measure<PopulationState>> measures = new TreeMap<>();
        IntStream.range(0,reg.size()).sequential().boxed().map(i -> reg.fractionMeasure(i)).
                forEach(m -> measures.put(m.getName(),m));
        IntStream.range(0,reg.size()).sequential().boxed().map(i -> reg.occupancyMeasure(i)).
                forEach(m -> measures.put(m.getName(),m));
        return measures;
    }


    @Override
    public StateSet<PopulationState> getStates() {
        if (states == null) {
            states = statesBuilder.apply(getEnvironment(),getRegistry());
        }
        return states;
    }


}
