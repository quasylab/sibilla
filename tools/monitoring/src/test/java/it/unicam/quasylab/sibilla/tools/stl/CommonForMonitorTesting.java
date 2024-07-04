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

import it.unicam.quasylab.sibilla.core.models.pm.Population;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.simulator.Trajectory;

import java.util.Arrays;

public class CommonForMonitorTesting {

    public static Trajectory<PopulationState> getPopulationTrajectory(double[] timeIntervals, int[]... signals) {
        if(signals.length==0)
            throw new IllegalArgumentException("At least one trajectory is needed");

        Trajectory<PopulationState> trajectory = new Trajectory<>();
        double time = 0.0;

        int numPopulations = signals.length;

        for (int i = 0; i < timeIntervals.length; i++) {
            double currentTimeInterval = timeIntervals[i];
            Population[] populations = new Population[numPopulations];

            for (int j = 0; j < numPopulations; j++) {
                populations[j] = new Population(j, signals[j][i]);
            }

            trajectory.add(time, new PopulationState(numPopulations, populations));
            time += currentTimeInterval;
        }

        trajectory.setEnd(time);

        return trajectory;
    }

    public static  Trajectory<PopulationState> getPopulationTrajectory(int[]... signals) {
        double[] defaultTimeIntervals = new double[signals[0].length];
        Arrays.fill(defaultTimeIntervals, 1.0);
        return getPopulationTrajectory(defaultTimeIntervals,signals);
    }



}
