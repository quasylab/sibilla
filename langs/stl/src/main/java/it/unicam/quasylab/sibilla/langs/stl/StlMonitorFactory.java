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

package it.unicam.quasylab.sibilla.langs.stl;

import it.unicam.quasylab.sibilla.core.tools.stl.QualitativeMonitor;
import it.unicam.quasylab.sibilla.core.tools.stl.QuantitativeMonitor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StlMonitorFactory<S> {

    private final Map<String, Function<Map<String, Double>, QualitativeMonitor<S>>> qualitativeMonitors = new HashMap<>();

    private final Map<String, Function<Map<String, Double>, QuantitativeMonitor<S>>> quantitativeMonitors = new HashMap<>();

    private final Map<String, String[]> monitors = new HashMap<>();


    public void addMonitor(String name,  String[] args, Function<Map<String, Double>, QualitativeMonitor<S>> qualitativeMonitorFactory, Function<Map<String, Double>, QuantitativeMonitor<S>> quantitativeMonitorFactory) {
        this.monitors.put(name, args);
        this.qualitativeMonitors.put(name, qualitativeMonitorFactory);
        this.quantitativeMonitors.put(name, quantitativeMonitorFactory);
    }

    public QualitativeMonitor<S> getQualitativeMonitor(String name, double[] args) {
        Map<String, Double> argumentMapping = getArgumentMapping(name, args);
        return qualitativeMonitors.get(name).apply(argumentMapping);
    }

    public QuantitativeMonitor<S> getQuantitativeMonitor(String name, double[] args) {
        Map<String, Double> argumentMapping = getArgumentMapping(name, args);
        return quantitativeMonitors.get(name).apply(argumentMapping);
    }

    private Map<String, Double> getArgumentMapping(String name, double[] args) {
        if (!monitors.containsKey(name)) {
            throw new IllegalArgumentException("Monitor "+name+" does not exist.");
        }
        String[] monitorParameters = monitors.get(name);
        if (monitorParameters.length != args.length) {
            throw new IllegalArgumentException("Illegal number of parameters! Expected "+monitorParameters.length+" are "+args.length);
        }
        return IntStream.range(0, args.length).boxed().collect(Collectors.toMap(i -> monitorParameters[i], i-> args[i]));
    }

}
