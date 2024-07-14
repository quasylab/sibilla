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

package it.unicam.quasylab.sibilla.core.models.yoda;

import it.unicam.quasylab.sibilla.core.models.AbstractModelDefinition;
import it.unicam.quasylab.sibilla.core.models.EvaluationEnvironment;
import it.unicam.quasylab.sibilla.core.models.ParametricDataSet;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class YodaModelDefinition extends AbstractModelDefinition<YodaSystemState> {


    private final YodaModelElementProvider elementProvider;

    private YodaModel model;
    private ParametricDataSet<Function<RandomGenerator, YodaSystemState>> states;
    private Map<String, Measure<YodaSystemState>> measures;
    private Map<String, Predicate<YodaSystemState>> predicates;

    public YodaModelDefinition(
            EvaluationEnvironment environment, YodaModelElementProvider elementProvider){
        super(environment);
        this.elementProvider =elementProvider;
    }

    @Override
    protected void clearCache(){
        this.states = null;
        this.measures = null;
        this.predicates = null;
        this.model = null;
    }

    //TODO
    public ParametricDataSet<Function<RandomGenerator, YodaSystemState>> getStates() {
        if (states == null){
            generateElements();
        }
        return states;
    }

    private void generateElements() {
        this.elementProvider.setEvaluationEnvironment(getEnvironment());
        this.states = this.elementProvider.getSystemStates();
        this.measures = this.elementProvider.getMeasures();
        this.predicates = this.elementProvider.getPredicates();
        this.model = new YodaModel(this.measures, this.predicates);
    }


    @Override
    public YodaModel createModel() {
        if (model == null){
            generateElements();
        }
        return model;
    }

    @Override
    public boolean isAnInitialConfiguration(String name) {
        return getStates().isDefined(name);
    }

    @Override
    public Function<RandomGenerator, YodaSystemState> getConfiguration(String name, double[] args) {
        return getStates().state(name, args);
    }

    private Map<String, Predicate<YodaSystemState>> getPredicates() {
        if (this.predicates == null) {
            generateElements();
        }
        return this.predicates;
    }

    private Map<String, Measure<YodaSystemState>> getMeasures() {
        if (this.measures == null) {
            generateElements();
        }
        return this.measures;
    }



    public boolean isAState(String name) {
        return getStates().isDefined(name);
    }

    @Override
    public int defaultConfigurationArity() {
        return getStates().arity();
    }

    @Override
    public int configurationArity(String name) {
        return getStates().arity(name);
    }

    @Override
    public String[] configurations() {
        return getStates().states();
    }

    @Override
    public Function<RandomGenerator,YodaSystemState> getDefaultConfiguration(double... args) {
        return getStates().get(args);
    }

    @Override
    public String getStateInfo(String name) {
        return getStates().getInfo(name);
    }

}
