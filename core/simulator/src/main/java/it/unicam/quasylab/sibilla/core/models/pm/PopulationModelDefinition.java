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

import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import it.unicam.quasylab.sibilla.core.models.AbstractModelDefinition;
import it.unicam.quasylab.sibilla.core.models.pm.util.PopulationRegistry;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Instances of this class represent the definition of a population model.
 */
public abstract class PopulationModelDefinition extends AbstractModelDefinition<PopulationState> {

    private PopulationRegistry registy;

    protected abstract PopulationRegistry generatePopulationRegistry();

    protected abstract List<PopulationRule> getRules();

    protected abstract List<Measure<PopulationState>> getMeasures();

    protected abstract void registerStates( );

    public PopulationModelDefinition() {
        super();
        this.registerStates();
    }

    @Override
    protected synchronized void reset() {
        this.registy = null;
    }

    @Override
    public synchronized final PopulationModel createModel() {
        PopulationRegistry reg = getRegistry();
        PopulationModel model = new PopulationModel(reg);
        model.addRules(getRules());
        model.addMeasures(createDefaultMeasure());
        model.addMeasures(getMeasures());
        return model;
    }

    protected final PopulationRegistry getRegistry() {
        if (this.registy == null) {
            this.registy = generatePopulationRegistry();
        }
        return this.registy;
    }

    private List<Measure<PopulationState>> createDefaultMeasure() {
        PopulationRegistry reg = getRegistry();
        LinkedList<Measure<PopulationState>> measures = new LinkedList<>();
        IntStream.range(0,reg.size()).sequential().forEach(i -> measures.add(reg.fractionMeasure(i)));
        IntStream.range(0,reg.size()).sequential().forEach(i -> measures.add(reg.occupancyMeasure(i)));
        return measures;
    }

    public static double fraction(double a, double b) {
        if (b==0.0) {
            return 0.0;
        } else {
            return a/b;
        }
    }
}
