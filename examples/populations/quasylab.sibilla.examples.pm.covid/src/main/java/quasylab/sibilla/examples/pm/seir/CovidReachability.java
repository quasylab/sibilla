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
package quasylab.sibilla.examples.pm.seir;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.models.pm.PopulationModel;
import quasylab.sibilla.core.models.pm.PopulationState;
import quasylab.sibilla.core.simulator.SimulationEnvironment;

import java.io.FileNotFoundException;
import java.net.UnknownHostException;

/**
 * @author loreti
 *
 */
public class CovidReachability {

    public final static int SAMPLINGS = 120;
    public final static double DEADLINE = 120;
    private static final int REPLICA = 10;


    public static void main(String[] argv) throws FileNotFoundException, InterruptedException, UnknownHostException {
        CovidDefinition def = new CovidDefinition();
        SimulationEnvironment simulator = new SimulationEnvironment();
        SimulationEnvironment.silent = false;
        double lambda = 3.75;
        def.setParameter("lambdaMeet",lambda);
        PopulationModel model = def.createModel();
        int A = model.indexOf("A");
        int G = model.indexOf("G");
        double p = simulator.reachability(0.01,0.01,200,def.createModel(),
                def.state(),
                s ->(s.getFraction(A)+s.getFraction(G))>0.5);
        System.out.println("\n\n***"+lambda+"->"+p+"****\n\n");
    }


}
