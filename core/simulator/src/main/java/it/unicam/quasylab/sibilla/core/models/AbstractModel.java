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

package it.unicam.quasylab.sibilla.core.models;

import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;

import java.util.List;
import java.util.Map;

public abstract class AbstractModel<S extends State> implements MarkovProcess<S>  {
    protected final Map<String, Measure<S>> measuresTable;

    public AbstractModel(Map<String, Measure<S>> measuresTable) {
        this.measuresTable = measuresTable;
    }

    public String[] measures() {
        return measuresTable.keySet().toArray(new String[0]);
    }

    public double measure(String m, S state) {
        Measure<S> measure = measuresTable.get(m);
        if (measure == null) {
            throw new IllegalArgumentException("Species " + m + " is unknown!");
        }
        return measure.measure(state);
    }

    public Measure<S> getMeasure(String m) {
        return measuresTable.get(m);
    }

    public void addMeasures(List<Measure<S>> measures) {
        if (measures != null) {
            measures.forEach(m -> measuresTable.put(m.getName(), m));
        }
    }
}
