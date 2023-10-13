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

package it.unicam.quasylab.sibilla.core.network.benchmark.slave;

import it.unicam.quasylab.sibilla.core.models.Model;
import it.unicam.quasylab.sibilla.core.models.State;
import it.unicam.quasylab.sibilla.core.network.ComputationResult;
import it.unicam.quasylab.sibilla.core.network.HostLoggerSupplier;
import it.unicam.quasylab.sibilla.core.network.NetworkInfo;
import it.unicam.quasylab.sibilla.core.network.benchmark.BenchmarkUnit;
import it.unicam.quasylab.sibilla.core.network.communication.TCPNetworkManager;
import it.unicam.quasylab.sibilla.core.network.communication.TCPNetworkManagerType;
import it.unicam.quasylab.sibilla.core.network.serialization.ComputationResultSerializerType;
import it.unicam.quasylab.sibilla.core.network.serialization.Serializer;
import it.unicam.quasylab.sibilla.core.network.serialization.SerializerType;
import it.unicam.quasylab.sibilla.core.network.serialization.TrajectorySerializer;
import it.unicam.quasylab.sibilla.core.network.util.BytearrayToFile;
import it.unicam.quasylab.sibilla.core.simulator.Trajectory;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Environment designed to test a single interaction of a Slave Server with a Master Server to benchmark activities such as serialization, compression and sending of {@link ComputationResult} objects.
 * It doesn't compute the trajectories that will be sent to the Master Server.
 * To be extended by all the classes associated with a particular type of {@link ComputationResult} serialization.
 *
 * @param <S> {@link State} related to the {@link ComputationResult} objects to send to the Master.
 * @author Stelluti Francesco Pio
 * @author Zamponi Marco
 */
public abstract class SlaveBenchmarkEnvironment<S extends State> {

    protected ComputationResultSerializerType computationResultSerializerType;
    protected BenchmarkUnit mainBenchmarkUnit;
    protected BenchmarkUnit sendBenchmarkUnit;
    protected String benchmarkName;
    private final Serializer serializer;
    protected Logger LOGGER;
    private ServerSocket serverSocket;
    protected TCPNetworkManager netManager;
    private int repetitions;
    private int threshold;
    private int resultsSize;
    private int currentTasksCount;
    protected Model<S> model;
    private final String trajectoryFileDir;
    private final String trajectoryFileName;

    protected SlaveBenchmarkEnvironment(String benchmarkName, String trajectoryFileDir, String trajectoryFileName,
                                        NetworkInfo localInfo, ComputationResultSerializerType computationResultSerializerType, Model<S> model) throws IOException {
        this(TCPNetworkManager.createNetworkManager((TCPNetworkManagerType) localInfo.getType(), TCPNetworkManager
                        .createServerSocket((TCPNetworkManagerType) localInfo.getType(), localInfo.getPort()).accept()),
                benchmarkName, trajectoryFileDir, trajectoryFileName, computationResultSerializerType, model);
    }

    protected SlaveBenchmarkEnvironment(TCPNetworkManager networkManager, String benchmarkName,
                                        String trajectoryFileDir, String trajectoryFileName, ComputationResultSerializerType computationResultSerializerType, Model<S> model) {
        this.benchmarkName = benchmarkName;
        this.model = model;
        this.trajectoryFileDir = trajectoryFileDir;
        this.trajectoryFileName = trajectoryFileName;
        this.computationResultSerializerType = computationResultSerializerType;
        this.mainBenchmarkUnit = getMainBenchmarkUnit();
        this.sendBenchmarkUnit = getSendBenchmarkUnit();
        serializer = Serializer.getSerializer(SerializerType.FST);
        this.LOGGER = HostLoggerSupplier.getInstance("Slave Benchmark").getLogger();
        this.currentTasksCount = 0;

        netManager = networkManager;
    }

    /**
     * Factory method that returns the requested {@link SlaveBenchmarkEnvironment} instance
     *
     * @param benchmarkName      name associated with the requested benchmark
     * @param trajectoryFileDir  directory in which the serialized trajectory file are placed
     * @param trajectoryFileName name of the serialized trajectory file
     * @param localInfo          related to the Slave Server to simulate
     * @param model              associated with the trajectories to send
     * @param type               type of the {@link ComputationResult} serialier requested, related to a particular class extension of {@link SlaveBenchmarkEnvironment}
     * @param <S>                {@link State} related to the model and to the {@link ComputationResult} objects to send to the Master
     * @return {@link SlaveBenchmarkEnvironment} instance requested
     * @throws IOException
     */
    public static <S extends State> SlaveBenchmarkEnvironment getSlaveBenchmark(String benchmarkName,
                                                                                String trajectoryFileDir, String trajectoryFileName, NetworkInfo localInfo, Model<S> model,
                                                                                ComputationResultSerializerType type) throws IOException {
        switch (type) {
            case APACHE:
                return new ApacheSlaveBenchmarkEnvironment<>(benchmarkName, trajectoryFileDir, trajectoryFileName,
                        localInfo, type, model);
            case FST:
                return new FstSlaveBenchmarkEnvironment<>(benchmarkName, trajectoryFileDir, trajectoryFileName,
                        localInfo, type, model);
            case CUSTOM:
                return new CustomSlaveBenchmarkEnvironment<>(benchmarkName, trajectoryFileDir, trajectoryFileName,
                        localInfo, model, type);
        }
        return null;
    }

    /**
     * Factory method that returns the requested {@link SlaveBenchmarkEnvironment} instance.
     *
     * @param networkManager     used to communicate with the Master Server benchmark environment
     * @param benchmarkName      name associated with the requested benchmark
     * @param trajectoryFileDir  directory in which the serialized trajectory file are placed
     * @param trajectoryFileName name of the serialized trajectory file
     * @param model              associated with the trajectories to send
     * @param type               of the {@link SlaveBenchmarkEnvironment} instance requested
     * @param <S>                {@link State} related to the model and to the {@link ComputationResult} objects to send to the Master
     * @return {@link SlaveBenchmarkEnvironment} instance requested
     * @throws IOException
     */
    public static <S extends State> SlaveBenchmarkEnvironment getSlaveBenchmark(TCPNetworkManager networkManager,
                                                                                String benchmarkName, String trajectoryFileDir, String trajectoryFileName, Model<S> model,
                                                                                ComputationResultSerializerType type) throws IOException {
        switch (type) {
            case APACHE:
                return new ApacheSlaveBenchmarkEnvironment<>(networkManager, benchmarkName, trajectoryFileDir,
                        trajectoryFileName, type, model);
            case FST:
                return new FstSlaveBenchmarkEnvironment<>(networkManager, benchmarkName, trajectoryFileDir,
                        trajectoryFileName, type, model);
            case CUSTOM:
                return new CustomSlaveBenchmarkEnvironment<>(networkManager, benchmarkName, trajectoryFileDir,
                        trajectoryFileName, model, type);
        }
        return null;
    }

    private ComputationResult getComputationResult(LinkedList<Trajectory<S>> trajectories, int resultSize)
            throws IOException {
        byte[] trajectoryBytes = BytearrayToFile.fromFile(trajectoryFileDir, trajectoryFileName);
        for (int i = 1; i <= resultSize; i++) {
            Trajectory<S> toAdd = TrajectorySerializer.deserialize(trajectoryBytes, this.model);
            // System.out.println("Samples: " + toAdd.getData().size());
            trajectories.add(toAdd);
        }
        return new ComputationResult(trajectories);
    }

    /**
     * Method to define into the classes that extend {@link SlaveBenchmarkEnvironment}.
     * It is associated with a particular type of serialization of {@link ComputationResult} objects.
     * Serializes, compresses and sends to a Master Server benchmark environment a {@link ComputationResult} object.
     *
     * @param computationResult object to serialize
     * @param currentRepetition
     */
    protected abstract void serializeCompressAndSend(ComputationResult<S> computationResult, int currentRepetition);

    private String getSerializerName() {
        return this.computationResultSerializerType.getFullName();
    }

    private String getMainLabel() {
        return this.computationResultSerializerType.getLabel();
    }

    private List<String> getMainBenchmarkLabels() {
        return List.of("sertime", "trajectories", "serbytes", "comprtime", "comprbytes", "sendtime");
    }

    private List<String> getSendBenchmarkLabels() {
        return List.of("receiveandsendtime",
                "resultsSize", "tasks");
    }

    private String getDirectory() {
        return String.format("benchmarks/slaveBenchmarking/%s/", this.benchmarkName);
    }

    private String getBenchmarkExtension() {
        return "csv";
    }

    private BenchmarkUnit getMainBenchmarkUnit() {
        return new BenchmarkUnit(this.getDirectory(),
                String.format("%s_compressSerializeAndSend", this.getSerializerName()), this.getBenchmarkExtension(),
                this.getMainLabel(), this.getMainBenchmarkLabels());
    }

    private BenchmarkUnit getSendBenchmarkUnit() {
        return new BenchmarkUnit(
                this.getDirectory(),
                String.format("%s_receiveAndSend", this.getSerializerName()),
                this.getBenchmarkExtension(),
                this.getMainLabel(),
                this.getSendBenchmarkLabels()
        );
    }

    /**
     * Initiates and manages the communication with a Master Server related benchmark environment.
     * It deserializes a trajectory from a file and builds {@link ComputationResult} according to the parameters (such as repetitions, size of results and threshold) received by the Master Server benchmark environment.
     *
     * @throws IOException
     */
    public void run() throws IOException {
        LOGGER.info(String.format("STARTING SLAVE %s BENCHMARK", this.computationResultSerializerType.toString()));

        this.repetitions = (int) serializer.deserialize(netManager.readObject());
        this.threshold = (int) serializer.deserialize(netManager.readObject());

        for (int j = 1; j <= repetitions; j++) {
            AtomicInteger currentRepetition = new AtomicInteger(j);
            while (currentTasksCount < threshold) {
                this.sendBenchmarkUnit.run(() -> {
                    this.currentTasksCount = (int) serializer.deserialize(netManager.readObject());
                    this.resultsSize = (int) serializer.deserialize(netManager.readObject());
                    LOGGER.info("-----------------------------------------------");
                    LOGGER.info(String.format("[%d] Received [%d] tasks. Groups of [%d] trajectories",
                            currentRepetition.get(), currentTasksCount, resultsSize));

                    ComputationResult<S> computationResult = this.getComputationResult(new LinkedList<>(), resultsSize);
                    for (int i = resultsSize; i <= currentTasksCount; i += resultsSize) {
                        this.serializeCompressAndSend(computationResult, currentRepetition.get());
                        LOGGER.info(String.format("[%d] Trajectories sent [%d/%d]", currentRepetition.get(), i,
                                currentTasksCount));
                    }
                    return List.of((double) resultsSize, (double) currentTasksCount);
                });

            }
            this.currentTasksCount = (int) serializer.deserialize(netManager.readObject());
        }
        netManager.closeConnection();
    }

}