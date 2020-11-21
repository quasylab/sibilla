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

package it.unicam.quasylab.sibilla.core.network.benchmark.master;

import it.unicam.quasylab.sibilla.core.models.Model;
import it.unicam.quasylab.sibilla.core.models.State;
import it.unicam.quasylab.sibilla.core.network.ComputationResult;
import it.unicam.quasylab.sibilla.core.network.HostLoggerSupplier;
import it.unicam.quasylab.sibilla.core.network.NetworkInfo;
import it.unicam.quasylab.sibilla.core.network.benchmark.BenchmarkUnit;
import it.unicam.quasylab.sibilla.core.network.communication.TCPNetworkManager;
import it.unicam.quasylab.sibilla.core.network.serialization.ComputationResultSerializerType;
import it.unicam.quasylab.sibilla.core.network.serialization.Serializer;
import it.unicam.quasylab.sibilla.core.network.serialization.SerializerType;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Environment designed to test a single interaction of a Master Server with a Slave Server to benchmark activities such as reception, deserialization and decompression of {@link ComputationResult} objects.
 * To be extended by all the classes associated with a particular type of {@link ComputationResult} serialization.
 *
 * @param <S> {@link State} related to the {@link ComputationResult} objects to send to the Master.
 * @author Stelluti Francesco Pio
 * @author Zamponi Marco
 */
public abstract class MasterBenchmarkEnvironment<S extends State> {

    protected ComputationResultSerializerType benchmarkType;
    protected BenchmarkUnit mainBenchmarkUnit;
    protected BenchmarkUnit sendBenchmarkUnit;
    private Serializer serializer;
    protected Logger LOGGER;
    private TCPNetworkManager netManager;
    protected final String benchmarkName;
    private final int step;
    private final int threshold;
    private final int repetitions;
    private int resultsSize;
    private int sentTasksCount;

    protected MasterBenchmarkEnvironment(String benchmarkName, NetworkInfo slaveInfo, ComputationResultSerializerType benchmarkType, int step, int threshold, int repetitions, int resultsSize) throws IOException {
        this(TCPNetworkManager.createNetworkManager(slaveInfo), benchmarkName, benchmarkType, step, threshold, repetitions, resultsSize);
    }

    protected MasterBenchmarkEnvironment(TCPNetworkManager netManager, String benchmarkName, ComputationResultSerializerType benchmarkType, int step, int threshold, int repetitions, int resultsSize) throws IOException {
        this.netManager = netManager;
        LOGGER = HostLoggerSupplier.getInstance("Master Benchmark").getLogger();

        this.benchmarkName = benchmarkName;
        this.step = step;
        this.threshold = threshold;
        this.repetitions = repetitions;
        this.resultsSize = Math.max(1, resultsSize);
        this.sentTasksCount = 0;
        this.benchmarkType = benchmarkType;
        this.mainBenchmarkUnit = this.getMainBenchmarkUnit();
        this.sendBenchmarkUnit = this.getSendBenchmarkUnit();
        serializer = Serializer.getSerializer(SerializerType.FST);
    }


    /**
     * Factory method that returns the requested {@link MasterBenchmarkEnvironment} instance
     *
     * @param benchmarkName name associated with the requested benchmark
     * @param slaveInfo     used to identify the Slave Server benchmark environment to communicate with
     * @param type          type of the {@link ComputationResult} serialier requested, related to a particular class extension of {@link MasterBenchmarkEnvironment}
     * @param model         associated with the trajectories to be received
     * @param step          difference in terms of number of tasks between a batch of tasks sent to the Slave Server benchmark environment and the next one to be sent.
     * @param threshold     maximum number of tasks contained in a single batch of tasks sent to the Slave Server benchmark environment
     * @param repetitions   number of times the benchmark needs to be repeated
     * @param resultsSize   number of times the benchmark needs to be repeated
     * @param <S>           {@link State} related to the model and to the {@link ComputationResult} objects to receive
     * @return {@link MasterBenchmarkEnvironment} instance requested
     * @throws IOException
     */
    public static <S extends State> MasterBenchmarkEnvironment getMasterBenchmark(String benchmarkName, NetworkInfo slaveInfo, ComputationResultSerializerType type, Model model, int step, int threshold, int repetitions, int resultsSize) throws IOException {
        switch (type) {
            case FST:
                return new FstMasterBenchmarkEnvironment<>(benchmarkName, slaveInfo, type, step, threshold, repetitions, resultsSize);
            case APACHE:
                return new ApacheMasterBenchmarkEnvironment<>(benchmarkName, slaveInfo, type, step, threshold, repetitions, resultsSize);
            case CUSTOM:
                return new CustomMasterBenchmarkEnvironment<>(benchmarkName, slaveInfo, type, model, step, threshold, repetitions, resultsSize);
        }
        return null;
    }

    /**
     * Factory method that returns the requested {@link MasterBenchmarkEnvironment} instance
     *
     * @param networkManager used to communicate with the Slave Server benchmark environment
     * @param benchmarkName  name associated with the requested benchmark
     * @param type           type of the {@link ComputationResult} serialier requested, related to a particular class extension of {@link MasterBenchmarkEnvironment}
     * @param model          associated with the trajectories to be received
     * @param step           difference in terms of number of tasks between a batch of tasks sent to the Slave Server benchmark environment and the next one to be sent.
     * @param threshold      maximum number of tasks contained in a single batch of tasks sent to the Slave Server benchmark environment
     * @param repetitions    number of times the benchmark needs to be repeated
     * @param resultsSize    number of times the benchmark needs to be repeated
     * @param <S>            {@link State} related to the model and to the {@link ComputationResult} objects to receive
     * @return {@link MasterBenchmarkEnvironment} instance requested
     * @throws IOException
     */
    public static <S extends State> MasterBenchmarkEnvironment getMasterBenchmark(TCPNetworkManager networkManager, String benchmarkName, ComputationResultSerializerType type, Model model, int step, int threshold, int repetitions, int resultsSize) throws IOException {
        switch (type) {
            case FST:
                return new FstMasterBenchmarkEnvironment<>(networkManager, benchmarkName, type, step, threshold, repetitions, resultsSize);
            case APACHE:
                return new ApacheMasterBenchmarkEnvironment<>(networkManager, benchmarkName, type, step, threshold, repetitions, resultsSize);
            case CUSTOM:
                return new CustomMasterBenchmarkEnvironment<>(networkManager, benchmarkName, type, model, step, threshold, repetitions, resultsSize);
        }
        return null;
    }


    private BenchmarkUnit getMainBenchmarkUnit() {
        return new BenchmarkUnit(
                this.getDirectory(),
                String.format("%s_decompressAndDeserialize", this.getSerializerName()),
                this.getBenchmarkExtension(),
                this.getMainLabel(),
                this.getMainBenchmarkLabels()
        );
    }

    private BenchmarkUnit getSendBenchmarkUnit() {
        return new BenchmarkUnit(
                this.getDirectory(),
                String.format("%s_sendAndReceive", this.getSerializerName()),
                this.getBenchmarkExtension(),
                this.getMainLabel(),
                this.getSendBenchmarkLabels()
        );
    }

    /**
     * Initiates and manages  the communication with a Slave Server related benchmark environment.
     *
     * @throws IOException
     */
    public void run() throws IOException {
        LOGGER.info(String.format("STARTING MASTER %s BENCHMARK", this.benchmarkType.toString()));
        netManager.writeObject(serializer.serialize(repetitions));
        netManager.writeObject(serializer.serialize(threshold));
        

        for (int j = 1; j <= repetitions; j++) {
            System.out.println("Repetitions " + j + "/" + repetitions);
            AtomicInteger currentRepetition = new AtomicInteger(j);
            while (sentTasksCount < threshold) {
                sentTasksCount += step;

                sendBenchmarkUnit.run(() -> {
                    LOGGER.info("-----------------------------------------------");
                    LOGGER.info(String.format("[%d] Sending [%d] tasks", currentRepetition.get(), sentTasksCount));
                    netManager.writeObject(serializer.serialize(sentTasksCount));
                    int currentResultSize = Math.min(resultsSize, sentTasksCount);
                    netManager.writeObject(serializer.serialize(currentResultSize));
                    int receivedTrajectoriesCount = 0;
                    ComputationResult<S> results = new ComputationResult<S>(new LinkedList<>());

                    while (receivedTrajectoriesCount < this.sentTasksCount) {
                        byte[] receivedBytes = read(currentRepetition.get());
                        ComputationResult<S> receivedResults = this.deserializeAndDecompress(receivedBytes, currentRepetition.get());
                        results.add(receivedResults);
                        receivedTrajectoriesCount = results.getResults().size();
                        LOGGER.info(String.format("[%d] Trajectories received [%d] Total trajectories received [%d/%d]", currentRepetition.get(), receivedResults.getResults().size(), receivedTrajectoriesCount, this.sentTasksCount));

                    }

                    return List.of((double) sentTasksCount, (double) currentResultSize);
                });


                // LOGGER.info(String.format("[%d] Trajectories received: [%d]", currentRepetition.get(), results.getResults().size()));
            }
            sentTasksCount = 0;
            netManager.writeObject(serializer.serialize(sentTasksCount));
        }
        netManager.closeConnection();
    }

    private byte[] read(int currentRepetition) throws IOException {
        byte[] toReturn = netManager.readObject();
        LOGGER.info(String.format("[%d] %s compressed received - Bytes: %d", currentRepetition, this.benchmarkType.toString(), toReturn.length));
        return toReturn;
    }


    /**
     * Method to define into the classes that extend {@link MasterBenchmarkEnvironment}.
     * It is associated with a particular type of serialization of {@link ComputationResult} objects
     * Deserializes and decompresses a {@link ComputationResult} object received from a Slave Server benchmark environment.
     *
     * @param bytes             associated with a {@link ComputationResult} object to deserialize
     * @param currentRepetition
     */
    protected abstract ComputationResult<S> deserializeAndDecompress(byte[] bytes, int currentRepetition);

    private String getSerializerName() {
        return this.benchmarkType.getFullName();
    }

    private String getMainLabel() {
        return this.benchmarkType.getLabel();
    }

    private List<String> getMainBenchmarkLabels() {
        return List.of("decomprtime",
                "desertime",
                "trajectories");
    }

    private List<String> getSendBenchmarkLabels() {
        return List.of("sendandreceivetime",
                "tasks",
                "resultsSize");
    }

    private String getDirectory() {
        return String.format("benchmarks/masterBenchmarking/%s/", this.benchmarkName);
    }


    private String getBenchmarkExtension() {
        return "csv";
    }


}
