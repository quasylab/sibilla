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
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package quasylab.sibilla.core.network.benchmark.master;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.network.ComputationResult;
import quasylab.sibilla.core.network.HostLoggerSupplier;
import quasylab.sibilla.core.network.NetworkInfo;
import quasylab.sibilla.core.network.benchmark.BenchmarkType;
import quasylab.sibilla.core.network.benchmark.BenchmarkUnit;
import quasylab.sibilla.core.network.communication.TCPNetworkManager;
import quasylab.sibilla.core.network.serialization.Serializer;
import quasylab.sibilla.core.network.serialization.SerializerType;
import quasylab.sibilla.core.past.State;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Executes a benchmark from the point of view of a Master Server in order to measure all the implemented optimizations.
 *
 * @param <S> The {@link quasylab.sibilla.core.past.State} of the simulation model.
 */
public abstract class MasterBenchmarkEnvironment<S extends State> {

    protected BenchmarkType type;
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

    protected MasterBenchmarkEnvironment(String benchmarkName, NetworkInfo slaveInfo, BenchmarkType type, int step, int threshold, int repetitions, int resultsSize) throws IOException {
        this(TCPNetworkManager.createNetworkManager(slaveInfo), benchmarkName, type, step, threshold, repetitions, resultsSize);
    }

    protected MasterBenchmarkEnvironment(TCPNetworkManager netManager, String benchmarkName, BenchmarkType type, int step, int threshold, int repetitions, int resultsSize) throws IOException {
        this.netManager = netManager;
        LOGGER = HostLoggerSupplier.getInstance("Master Benchmark").getLogger();

        this.benchmarkName = benchmarkName;
        this.step = step;
        this.threshold = threshold;
        this.repetitions = repetitions;
        this.resultsSize = Math.max(1, resultsSize);
        this.sentTasksCount = 0;
        this.type = type;
        this.mainBenchmarkUnit = this.getMainBenchmarkUnit();
        this.sendBenchmarkUnit = this.getSendBenchmarkUnit();
        serializer = Serializer.getSerializer(SerializerType.FST);
    }


    /**
     * Factory method.
     *
     * @param benchmarkName name given to the to be initiated benchmark session.
     * @param slaveInfo     used to communicate with a Slave Server that is executing its own benchmark.
     * @param type          type of the benchmark to be executed.
     * @param model         simulation model withing all the MasterBenchmarkEnvironment classes that need it
     * @param step          difference in terms of number of tasks between a batch of tasks sent to a Slave Server that is executing its own benchmark and the next one to be sent.
     * @param threshold     maximum number of tasks contained in a single batch of tasks sent to a Slave Server that is executing its own benchmark.
     * @param repetitions   number of times the benchmark needs to be repeated.
     * @param resultsSize   number of trajectories contained in the ComputationResult objects received from a Slave Server that is executing its own benchmark.
     * @param <S>           State upon which the simulation is based.
     * @return MasterBenchmarkEnvironment related to the MasterBenchmarkType value passed as parameter.
     * @throws IOException
     */
    public static <S extends State> MasterBenchmarkEnvironment getMasterBenchmark(String benchmarkName, NetworkInfo slaveInfo, BenchmarkType type, Model model, int step, int threshold, int repetitions, int resultsSize) throws IOException {
        switch (type) {
            case FST:
                return new FstMasterBenchmarkEnvironment<S>(benchmarkName, slaveInfo, type, step, threshold, repetitions, resultsSize);
            case APACHE:
                return new ApacheMasterBenchmarkEnvironment<S>(benchmarkName, slaveInfo, type, step, threshold, repetitions, resultsSize);
            case OPTIMIZED:
                return new OptimizedMasterBenchmarkEnvironment<S>(benchmarkName, slaveInfo, type, model, step, threshold, repetitions, resultsSize);
        }
        return null;
    }

    public static <S extends State> MasterBenchmarkEnvironment getMasterBenchmark(TCPNetworkManager networkManager, String benchmarkName, BenchmarkType type, Model model, int step, int threshold, int repetitions, int resultsSize) throws IOException {
        switch (type) {
            case FST:
                return new FstMasterBenchmarkEnvironment<S>(networkManager, benchmarkName, type, step, threshold, repetitions, resultsSize);
            case APACHE:
                return new ApacheMasterBenchmarkEnvironment<S>(networkManager, benchmarkName, type, step, threshold, repetitions, resultsSize);
            case OPTIMIZED:
                return new OptimizedMasterBenchmarkEnvironment<S>(networkManager, benchmarkName, type, model, step, threshold, repetitions, resultsSize);
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
     * Initiates the benchmark.
     *
     * @throws IOException
     */
    public void run() throws IOException {
        LOGGER.info(String.format("STARTING MASTER %s BENCHMARK", this.type.toString()));
        netManager.writeObject(serializer.serialize(repetitions));
        netManager.writeObject(serializer.serialize(threshold));


        for (int j = 1; j <= repetitions; j++) {
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
        }
        netManager.closeConnection();
    }

    private byte[] read(int currentRepetition) throws IOException {
        byte[] toReturn = netManager.readObject();
        LOGGER.info(String.format("[%d] %s compressed received - Bytes: %d", currentRepetition, this.type.toString(), toReturn.length));
        return toReturn;
    }


    protected abstract ComputationResult<S> deserializeAndDecompress(byte[] bytes, int currentRepetition);

    protected abstract String getSerializerName();

    protected abstract String getMainLabel();

    private List<String> getMainBenchmarkLabels() {
        return List.of("decomprtime",
                "desertime",
                "tasks");
    }

    private List<String> getSendBenchmarkLabels() {
        return List.of("sendAndReceiveTime",
                "tasks",
                "resultsSize");
    }

    private String getDirectory() {
        return String.format("benchmarks/masterBenchmarking/%s/", this.benchmarkName);
    }


    private String getBenchmarkExtension() {
        return "csv";
    }

    /**
     * @return MasterBenchmarkType associated with the given benchmark.
     */
    public BenchmarkType getType() {
        return this.type;
    }

}
