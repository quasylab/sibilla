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

package it.unicam.quasylab.sibilla.tools.tracing;

import it.unicam.quasylab.sibilla.core.util.SimulationData;

import java.util.List;
import java.util.stream.Stream;

/**
 * Instances of this class are used to collect the data traced at a given time step.
 */
public record TracingData(double time, double x, double y, double z, double direction, String colour, String shape) {

    public void writeCSV(String name, List<TracingData> data) {

    }

    public String[] values() {
        return new String[] {
            String.format(java.util.Locale.US,"%f", time),
            String.format(java.util.Locale.US,"%f", x),
            String.format(java.util.Locale.US,"%f", y),
            String.format(java.util.Locale.US,"%f", z),
            String.format(java.util.Locale.US,"%f", direction),
            shape,
            colour
        };
    }

}
