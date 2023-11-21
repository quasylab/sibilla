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

package it.unicam.quasylab.sibilla.core.runtime;

import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.simulator.SequentialSimulationManager;
import it.unicam.quasylab.sibilla.core.simulator.SimulationManagerFactory;
import it.unicam.quasylab.sibilla.core.simulator.ThreadSimulationManager;

import java.net.URL;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

public class SimulationManagerTest {

    private final static int[] REPLICAS = new int[] { 1, 10, 100, 1000 };

    private static String finalResult = "";

    private final static double[][] ARGS = new double[][] { new double[] {9, 1},
            new double[] {90, 10},
            new double[] {900, 100} /*,
            new double[] {9000, 1000},
            new double[] {90000, 10000} */
    };

    public static void main(String[] args) {
        try {
            String result = "";
            result = getResult("Sequential", runSimulations(SequentialSimulationManager::new, 100, 1.0, REPLICAS, ARGS));
            finalResult += result;
            System.out.println(result);
            result = getResult("Fixed Thread Pool (size 2)", runSimulations(ThreadSimulationManager.getFixedThreadSimulationManagerFactory(2), 100, 1.0, REPLICAS, ARGS));
            finalResult += result;
            System.out.println(result);
            result = getResult("Fixed Thread Pool (size 4)", runSimulations(ThreadSimulationManager.getFixedThreadSimulationManagerFactory(4), 100, 1.0, REPLICAS, ARGS));
            finalResult += result;
            System.out.println(result);
            result = getResult("Fixed Thread Pool (size 6)", runSimulations(ThreadSimulationManager.getFixedThreadSimulationManagerFactory(6), 100, 1.0, REPLICAS, ARGS));
            finalResult += result;
            System.out.println(result);
            result = getResult("Fixed Thread Pool (size 8)", runSimulations(ThreadSimulationManager.getFixedThreadSimulationManagerFactory(8), 100, 1.0, REPLICAS, ARGS));
            finalResult += result;
            System.out.println(result);
            result = getResult("Fixed Thread Pool (size 10)", runSimulations(ThreadSimulationManager.getFixedThreadSimulationManagerFactory(8), 100, 1.0, REPLICAS, ARGS));
            finalResult += result;
            System.out.println(result);
            result = getResult("Cached Threads Pool", runSimulations(ThreadSimulationManager.getCachedThreadSimulationManagerFactory(), 100, 1.0, REPLICAS, ARGS));
            finalResult += result;
            System.out.println(result);
            result = getResult("Work Stealing Pool", runSimulations(ThreadSimulationManager.getWorkStealingPoolSimulationManagerFactory(), 100, 1.0, REPLICAS, ARGS));
            finalResult += result;
            System.out.println(result);
            System.out.println("\n\n\n\n");
            System.out.println(finalResult);
        } catch (CommandExecutionException e) {
            e.printStackTrace();
        }
    }

    private static SibillaRuntime getRuntimeWithModule() throws CommandExecutionException {
        SibillaRuntime sr = new SibillaRuntime();
        sr.loadModule(PopulationModelModule.MODULE_NAME);
        return sr;
    }

    private static URL getResource(String name) {
        return SimulationManagerTest.class.getClassLoader().getResource(name);
    }

    private static double[][] runSimulations(SimulationManagerFactory factory, double deadline, double dt, int[] replicas, double[][] args) throws CommandExecutionException {
        SibillaRuntime sr = getRuntimeWithModule();
        sr.setSimulationManagerFactory(factory);
        sr.load(getResource("pm/seir/seir.pm"));
        sr.setDeadline(deadline);
        sr.setDt(dt);
        sr.addAllMeasures();
        double[][] time = new double[replicas.length][args.length];
        for( int i=0; i<replicas.length; i++) {
            for( int j=0; j<args.length; j++) {
                sr.setReplica(replicas[i]);
                sr.setParameter("startS", args[j][0]);
                sr.setParameter("startI", args[j][1]);
                sr.setConfiguration("initial_1");
                sr.addAllMeasures();
                long start = System.currentTimeMillis();
                sr.simulate(i+" "+j);
                time[i][j] = System.currentTimeMillis()-start;
            }
        }
        return time;
    }

    private static String getResult(String label, double[][] times) {
        StringBuilder result = new StringBuilder("### "+label+"\n\n");
        result.append("|  | ").append(Stream.of(ARGS).map(a -> String.format("%.0f", a[0]+a[1])).collect(Collectors.joining(" | "))).append(" | \n");
        for(int i=0; i<times.length; i++) {
            result.append(" | ").append(REPLICAS[i]).append(" | ").append(DoubleStream.of(times[i]).mapToObj(d -> String.format("%.3f", d / 1000)).collect(Collectors.joining(" | "))).append(" | ");
            result.append(" \n ");
        }
        result.append("\n\n\n");
        return result.toString();
    }

}
