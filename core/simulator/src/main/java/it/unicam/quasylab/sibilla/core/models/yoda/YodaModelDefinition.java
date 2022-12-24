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

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class YodaModelDefinition extends AbstractModelDefinition<YodaSystemState> {

    //private final Function<EvaluationEnvironment, ParametricDataSet<Function<RandomGenerator, YodaSystemState>>> statesBuilder;
    //private final BiFunction<EvaluationEnvironment, , Map<String, Measure<? super YodaSystemState>>> measuresBuilder;

    private ParametricDataSet<Function<RandomGenerator, YodaSystemState>> states;
    private Map<String, Measure<? super YodaSystemState>> measures;
    private YodaModel model;

    public YodaModelDefinition(
            EvaluationEnvironment environment){
        super(environment);
    }

    //TODO
    @Override
    protected void clearCache(){
        this.states = null;
        this.measures = null;
        this.model = null;
    }

    //TODO
    @Override
    public ParametricDataSet<Function<RandomGenerator, YodaSystemState>> getStates() {
        if (states == null){
            //states = statesBuilder.apply(getEnvironment());
        }
        return states;
    }


    //TODO
    @Override
    public YodaModel createModel() {
        if (model == null){
        }
        return model;
    }

}
