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

package quasylab.sibilla.core.network.benchmark;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.network.ComputationResult;
import quasylab.sibilla.core.network.NetworkInfo;
import quasylab.sibilla.core.network.compression.Compressor;
import quasylab.sibilla.core.network.serialization.ComputationResultSerializer;
import quasylab.sibilla.core.past.State;

import java.io.IOException;
import java.util.List;

public class OptimizedMasterBenchmarkEnvironment<S extends State> extends MasterBenchmarkEnvironment {
    private Model<S> model;

    public OptimizedMasterBenchmarkEnvironment(String benchmarkName, NetworkInfo slaveInfo, MasterBenchmarkType type, Model model, int step, int threshold, int repetitions, int resultsSize) throws IOException {
        super(benchmarkName, slaveInfo, type, step, threshold, repetitions, resultsSize);
        this.model = model;
    }

    @Override
    protected ComputationResult deserializeAndDecompress(byte[] bytes, int currentRepetition) {
        final var wrapper = new Object() {
            private byte[] received;
            private ComputationResult<S> results;
        };

        wrapper.received = bytes;

        this.mainBenchmarkUnit.run(() -> {
                    wrapper.received = Compressor.decompress(wrapper.received);
                    LOGGER.info(String.format("[%d] Optimized %s decompressed (serialized) - Bytes: %d", currentRepetition, this.benchmarkName, wrapper.received.length));
                    return List.of();
                }, () -> {
                    wrapper.results = ComputationResultSerializer.deserialize(wrapper.received,
                            this.model);
                    LOGGER.info(String.format("[%d] Optimized %s deserialized - Size: %d - Bytes: %d", currentRepetition, this.benchmarkName, wrapper.results.getResults().size(), wrapper.received.length));
                    return List.of((double) wrapper.results.getResults().size());
                }
        );

        return wrapper.results;
    }

    @Override
    protected String getSerializerName() {
        return "optimized";
    }

    @Override
    protected String getMainLabel() {
        return "o";
    }
}