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

package it.unicam.quasylab.sibilla.core.network.slave.executor;

import it.unicam.quasylab.sibilla.core.models.Model;
import it.unicam.quasylab.sibilla.core.network.ComputationResult;
import it.unicam.quasylab.sibilla.core.network.NetworkTask;
import it.unicam.quasylab.sibilla.core.network.communication.TCPNetworkManager;
import it.unicam.quasylab.sibilla.core.network.compression.Compressor;
import it.unicam.quasylab.sibilla.core.network.benchmark.BenchmarkUnit;
import it.unicam.quasylab.sibilla.core.network.serialization.ComputationResultSerializer;
import it.unicam.quasylab.sibilla.core.network.serialization.ComputationResultSerializerType;
import it.unicam.quasylab.sibilla.core.network.serialization.Serializer;
import it.unicam.quasylab.sibilla.core.network.serialization.SerializerType;

import java.io.IOException;
import java.util.List;

/**
 * Represents an executor of simulations that manages the computation and the
 * sending of simulation results to a master server
 *
 * @author Stelluti Francesco Pio
 * @author Zamponi Marco
 */
public abstract class SimulationExecutor {

    private final ExecutorType executorType;

    protected final BenchmarkUnit computationBenchmark;
    private final BenchmarkUnit sendBenchmark;

    // TODO
    private final ComputationResultSerializerType crSerializerType;

    /**
     * Creates a new SimulationExecutor, starting the BenchmarkUnit associated to it
     * to measure performances.
     *
     * @param exType           the type of SimulationExecutor to create
     * @param crSerializerType //TODO
     */
    public SimulationExecutor(ExecutorType exType, ComputationResultSerializerType crSerializerType) {
        this.executorType = exType;
        this.crSerializerType = crSerializerType;

        this.computationBenchmark = new BenchmarkUnit("sibillaBenchmarks/slaveBenchmarking/",
                String.format("%s_computation", this.executorType), "csv", "o", List.of("comptime", "tasks"));

        this.sendBenchmark = new BenchmarkUnit("sibillaBenchmarks/slaveBenchmarking/",
                String.format("%s_resultsCompressSerializeAndSend", this.crSerializerType.toString()), "csv",
                this.crSerializerType.getLabel(),
                List.of("sertime", "trajectories", "serbytes", "comprtime", "comprbytes", "sendtime"));
    }

    /**
     * Factory method for SimulationExecutor creation. Creates a SimulationExecutor
     * based on the type passed in input.
     *
     * @param exType           the type of SimulationExecutor to create
     * @param crSerializerType // TODO
     * @return the created SimulationExecutor
     */
    public static SimulationExecutor getExecutor(ExecutorType exType,
                                                 ComputationResultSerializerType crSerializerType) {
        switch (exType) {
            case MULTITHREADED:
                return new MultithreadedSimulationExecutor(exType, crSerializerType);
            case SEQUENTIAL:
                return new SequentialSimulationExecutor(exType, crSerializerType);
            case SINGLE_TRAJECTORY_SEQUENTIAL:
                return new SingleTrajectorySequentialSimulationExecutor(exType, crSerializerType);
            case SINGLE_TRAJECTORY_MULTITHREADED:
            default:
                return new SingleTrajectoryMultithreadedSimulationExecutor(exType, crSerializerType);
        }
    }

    /**
     * Executes the simulation of the given NetworkTask and sends the results to the
     * master server.
     *
     * @param networkTask the network task to simulate
     * @param master      the NetworkManager of the master server the results will
     *                    be sent to
     */
    public abstract void simulate(NetworkTask networkTask, TCPNetworkManager master);

    /**
     * Serializes, compresses and sends the simulation results to a master server.
     *
     * @param results the ComputationResult to be sent
     * @param master  the NetworkManager of the master server the results will be
     *                sent to
     * @param model   the Model of the executed simulation
     */
    protected void sendResult(ComputationResult results, TCPNetworkManager master, Model model) {
        final var wrapper = new Object() {
            private byte[] toSend;
        };

        this.sendBenchmark.run(() -> {
            wrapper.toSend = this.serializeComputationResult(results, model);
            return List.of((double) results.getResults().size(), (double) wrapper.toSend.length);
        }, () -> {
            wrapper.toSend = Compressor.compress(wrapper.toSend);
            return List.of((double) wrapper.toSend.length);
        }, () -> {
            master.writeObject(wrapper.toSend);
            return List.of();
        });

        /*
         * try { master.writeObject(Compressor.compress(this.serializeComputationResult(
         * results, model))); } catch (IOException e) { e.printStackTrace(); }
         */
    }

    // TODO
    private byte[] serializeComputationResult(ComputationResult results, Model model) throws IOException {
        switch (this.crSerializerType) {
            case FST:
                return Serializer.getSerializer(SerializerType.FST).serialize(results);
            case APACHE:
                return Serializer.getSerializer(SerializerType.APACHE).serialize(results);
            default:
            case CUSTOM:
                return ComputationResultSerializer.serialize(results, model);
        }
    }

    /**
     * Represent the type of SimulationExecutor
     */
    public enum ExecutorType {
        SEQUENTIAL, SINGLE_TRAJECTORY_SEQUENTIAL, MULTITHREADED, SINGLE_TRAJECTORY_MULTITHREADED
    }
}
