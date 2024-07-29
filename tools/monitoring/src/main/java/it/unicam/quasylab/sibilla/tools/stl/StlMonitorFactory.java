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

package it.unicam.quasylab.sibilla.tools.stl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StlMonitorFactory<S> {

    private final Map<String, Function<Map<String, Double>, QualitativeMonitor<S>>> qualitativeMonitors = new HashMap<>();

    private final Map<String, Function<Map<String, Double>, QuantitativeMonitor<S>>> quantitativeMonitors = new HashMap<>();

    private final Map<String, Map<String, ToDoubleFunction<Map<String, Double>>>> monitors = new HashMap<>();



    public void addMonitor(String name, Map<String, ToDoubleFunction<Map<String, Double>>> args, Function<Map<String, Double>, QualitativeMonitor<S>> qualitativeMonitorFactory, Function<Map<String, Double>, QuantitativeMonitor<S>> quantitativeMonitorFactory) {
        this.monitors.put(name, args);
        this.qualitativeMonitors.put(name, qualitativeMonitorFactory);
        this.quantitativeMonitors.put(name, quantitativeMonitorFactory);
    }

    public QualitativeMonitor<S> getQualitativeMonitor(String name) {
        return getQualitativeMonitor(name, new HashMap<>());
    }

    public QualitativeMonitor<S> getQualitativeMonitor(String name, Map<String, Double> args) {
        Map<String, Double> argumentMapping = getArgumentMapping(name, args);
        return qualitativeMonitors.get(name).apply(argumentMapping);
    }

    public QuantitativeMonitor<S> getQuantitativeMonitor(String name) {
        return getQuantitativeMonitor(name, new HashMap<>());
    }

    public QuantitativeMonitor<S> getQuantitativeMonitor(String name, Map<String, Double> args) {
        Map<String, Double> argumentMapping = getArgumentMapping(name, args);
        return quantitativeMonitors.get(name).apply(argumentMapping);
    }

    private Map<String, Double> getArgumentMapping(String name, Map<String, Double> args) {
        if (!monitors.containsKey(name)) {
            throw new IllegalArgumentException("Monitor " + name + " does not exist.");
        }
        Map<String, ToDoubleFunction<Map<String, Double>>> monitorParameters = monitors.get(name);
        Map<String, Double> result = new HashMap<>();

        for (Map.Entry<String, ToDoubleFunction<Map<String, Double>>> entry : monitorParameters.entrySet()) {
            String paramName = entry.getKey();
            if (args.containsKey(paramName)) {
                result.put(paramName, args.get(paramName));
            } else {
                result.put(paramName, entry.getValue().applyAsDouble(args));
            }
        }

        return result;
    }

    public  Map<String, Map<String, ToDoubleFunction<Map<String, Double>>>> getMonitors() { return monitors; }

}
