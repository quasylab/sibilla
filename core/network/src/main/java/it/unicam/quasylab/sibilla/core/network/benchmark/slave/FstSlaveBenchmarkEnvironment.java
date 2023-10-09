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
import it.unicam.quasylab.sibilla.core.network.NetworkInfo;
import it.unicam.quasylab.sibilla.core.network.communication.TCPNetworkManager;
import it.unicam.quasylab.sibilla.core.network.compression.Compressor;
import it.unicam.quasylab.sibilla.core.network.serialization.ComputationResultSerializerType;
import it.unicam.quasylab.sibilla.core.network.serialization.FSTSerializer;
import it.unicam.quasylab.sibilla.core.network.serialization.Serializer;
import it.unicam.quasylab.sibilla.core.network.serialization.SerializerType;

import java.io.IOException;
import java.util.List;

/**
 * Extension of {@link SlaveBenchmarkEnvironment} based upon custom {@link FSTSerializer}
 *
 * @param <S> {@link State} related to the {@link ComputationResult} objects to send to the Master.
 * @author Stelluti Francesco Pio
 * @author Zamponi Marco
 */
public class FstSlaveBenchmarkEnvironment<S extends State> extends SlaveBenchmarkEnvironment {

    private final Serializer fstSerializer;

    public FstSlaveBenchmarkEnvironment(String benchmarkName, String trajectoryFileDir, String trajectoryFileName, NetworkInfo localInfo, ComputationResultSerializerType type, Model<S> model) throws IOException {
        super(benchmarkName, trajectoryFileDir, trajectoryFileName, localInfo, type, model);
        this.fstSerializer = Serializer.getSerializer(SerializerType.FST);
    }

    public FstSlaveBenchmarkEnvironment(TCPNetworkManager networkManager, String benchmarkName, String trajectoryFileDir, String trajectoryFileName, ComputationResultSerializerType type, Model<S> model) throws IOException {
        super(networkManager, benchmarkName, trajectoryFileDir, trajectoryFileName, type, model);
        this.fstSerializer = Serializer.getSerializer(SerializerType.FST);
    }


    @Override
    protected void serializeCompressAndSend(ComputationResult computationResult, int currentRepetition) {
        final var wrapper = new Object() {
            private byte[] toSend;
        };
        this.mainBenchmarkUnit.run(() -> {
                    wrapper.toSend = fstSerializer.serialize(computationResult);
                    LOGGER.info(String.format("[%d] FST Serialization %s - Size: %d - Bytes: %d", currentRepetition, this.benchmarkName, computationResult.getResults().size(), wrapper.toSend.length));
                    return List.of((double) computationResult.getResults().size(), (double) wrapper.toSend.length);
                },
                () -> {
                    wrapper.toSend = Compressor.compress(wrapper.toSend);
                    LOGGER.info(String.format("[%d] FST Compression %s - Size: %d - Bytes: %d", currentRepetition, this.benchmarkName, computationResult.getResults().size(), wrapper.toSend.length));
                    return List.of((double) wrapper.toSend.length);
                },
                () -> {
                    netManager.writeObject(wrapper.toSend);
                    LOGGER.info(String.format("[%d] FST %s Sent - Size: %d - Bytes: %d", currentRepetition, this.benchmarkName, computationResult.getResults().size(), wrapper.toSend.length));
                    return List.of();
                });
    }

}
