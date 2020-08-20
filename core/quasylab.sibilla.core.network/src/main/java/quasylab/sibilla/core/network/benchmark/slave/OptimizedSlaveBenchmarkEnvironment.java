package quasylab.sibilla.core.network.benchmark.slave;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.network.ComputationResult;
import quasylab.sibilla.core.network.NetworkInfo;
import quasylab.sibilla.core.network.benchmark.BenchmarkType;
import quasylab.sibilla.core.network.communication.TCPNetworkManager;
import quasylab.sibilla.core.network.compression.Compressor;
import quasylab.sibilla.core.network.serialization.ComputationResultSerializer;
import quasylab.sibilla.core.past.State;

import java.io.IOException;
import java.util.List;

public class OptimizedSlaveBenchmarkEnvironment<S extends State> extends SlaveBenchmarkEnvironment {


    public OptimizedSlaveBenchmarkEnvironment(String benchmarkName, String trajectoryFileDir, String trajectoryFileName, NetworkInfo localInfo, Model<S> model, BenchmarkType type) throws IOException {
        super(benchmarkName, trajectoryFileDir, trajectoryFileName, localInfo, type, model);
    }

    public OptimizedSlaveBenchmarkEnvironment(TCPNetworkManager networkManager, String benchmarkName, String trajectoryFileDir, String trajectoryFileName, Model<S> model, BenchmarkType type) throws IOException {
        super(networkManager, benchmarkName, trajectoryFileDir, trajectoryFileName, type, model);
    }


    @Override
    protected void serializeCompressAndSend(ComputationResult computationResult, int currentRepetition) {
        final var wrapper = new Object() {
            private byte[] toSend;
        };

        this.mainBenchmarkUnit.run(() -> {
                    wrapper.toSend = ComputationResultSerializer.serialize(computationResult, this.model);
                    LOGGER.info(String.format("[%d] Optimized Serialization %s - Size: %d - Bytes: %d", currentRepetition, this.benchmarkName, computationResult.getResults().size(), wrapper.toSend.length));
                    return List.of((double) computationResult.getResults().size(), (double) wrapper.toSend.length);
                },
                () -> {
                    wrapper.toSend = Compressor.compress(wrapper.toSend);
                    LOGGER.info(String.format("[%d] Optimized Compression %s - Size: %d - Bytes: %d", currentRepetition, this.benchmarkName, computationResult.getResults().size(), wrapper.toSend.length));
                    return List.of((double) wrapper.toSend.length);
                },
                () -> {
                    netManager.writeObject(wrapper.toSend);
                    LOGGER.info(String.format("[%d] Optimized %s Sent - Size: %d - Bytes: %d", currentRepetition, this.benchmarkName, computationResult.getResults().size(), wrapper.toSend.length));
                    return List.of();
                });
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
