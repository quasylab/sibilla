/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 * Copyright (C) 2020.
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package quasylab.sibilla.examples.pm.molecule;

import quasylab.sibilla.core.simulator.SimulationEnvironment;

public class mainMolecules {

    public static void main (String[] args) throws InterruptedException {
                /*
        CovidDefinition def = new CovidDefinition();
        SimulationEnvironment simulator = new SimulationEnvironment();
        SimulationEnvironment.silent = false;
        double lambda = 3.75;
        double p = simulator.reachability(0.01,0.01,200,def.createModel(lambda),
                def.state(),
                s ->(s.getFraction(CovidDefinition.A)+s.getFraction(CovidDefinition.G))>0.5);
        System.out.println("\n\n***"+lambda+"->"+p+"****\n\n");
         */

        MoleculeDefiniton def = new MoleculeDefiniton();
        SimulationEnvironment simulator = new SimulationEnvironment();
        SimulationEnvironment.silent = false;
        final double lambda = 1;

        double increment = 0.245;
        double[] results = new double[11];

        for (int i = 5; i <= 10 ; i++) {
            final double percentage = increment;
            int occupancy = i;
            double p = simulator.reachability(0.01,0.01,0.005,def.createModel(lambda),
                    def.state(10,10,0,0),
                    s ->(s.getOccupancy(MoleculeDefiniton.NaPositive))>=occupancy);
            increment = increment + 0.0001;
            results[i] = p;
            System.out.println(""+occupancy+"->"+p+"****\n\n");

        }

        for( int i=0 ; i<=10 ; i++ ) {
            System.out.println(i+"->"+results[i]);
        }
    }

}
