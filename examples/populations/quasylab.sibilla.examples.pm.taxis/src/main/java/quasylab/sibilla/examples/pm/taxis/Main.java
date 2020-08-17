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

package quasylab.sibilla.examples.pm.taxis;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.models.pm.PopulationState;
import quasylab.sibilla.core.simulator.SimulationEnvironment;


public class Main {

    public static void main (String[] args) throws InterruptedException {


        TaxiDefinition def = new TaxiDefinition();
        SimulationEnvironment simulator = new SimulationEnvironment();
        SimulationEnvironment.silent = false;

        System.out.println("");

        int size = 100;
        while (size == 100) {
            size++;
            int users = 100;
            int taxis = 60;

            PopulationState state = def.state(taxis, 0, 0, 0, users,0,0,0);
            Model<PopulationState> model = def.createModel();
            double probability = simulator.reachability(0.01, 0.01, 720, model,
                    state , s -> (fractionOfAngryUsers(s) > 0.1));
            // def.state(0,0,0,0,2000,100)
            System.out.println("");
            System.out.println("===================================================================");
            System.out.println("");


            System.out.println("User = " + users);
            System.out.println("Taxi = " + taxis);

            System.out.println("");
            System.out.println("Probability = " + probability);
            System.out.println("");
            System.out.println("===================================================================");
            System.out.println("");


        }
    }

    public static double fractionOfAngryUsers(PopulationState s) {
        double totalUsers = s.getOccupancy(TaxiDefinition.S,TaxiDefinition.L,TaxiDefinition.A,TaxiDefinition.W,TaxiDefinition.I);
        double fraction = s.getOccupancy(TaxiDefinition.A)/(totalUsers);
        return fraction;
    }

}
