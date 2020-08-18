package quasylab.sibilla.core.network.benchmark.slave;

import quasylab.sibilla.core.network.ComputationResult;
import quasylab.sibilla.core.network.NetworkInfo;
import quasylab.sibilla.core.network.benchmark.BenchmarkType;
import quasylab.sibilla.core.network.communication.TCPNetworkManager;
import quasylab.sibilla.core.network.compression.Compressor;
import quasylab.sibilla.core.network.serialization.Serializer;
import quasylab.sibilla.core.network.serialization.SerializerType;

import java.io.IOException;
import java.util.List;

public class ApacheSlaveBenchmarkEnvironment extends SlaveBenchmarkEnvironment {

    private Serializer apacheSerializer;

    public ApacheSlaveBenchmarkEnvironment(String benchmarkName, String trajectoryFileDir, String trajectoryFileName, NetworkInfo localInfo, BenchmarkType type) throws IOException {
        super(benchmarkName, trajectoryFileDir, trajectoryFileName, localInfo, type);
        this.apacheSerializer = Serializer.getSerializer(SerializerType.APACHE);
    }

    public ApacheSlaveBenchmarkEnvironment(TCPNetworkManager networkManager, String benchmarkName, String trajectoryFileDir, String trajectoryFileName, BenchmarkType type) throws IOException {
        super(networkManager, benchmarkName, trajectoryFileDir, trajectoryFileName, type);
        this.apacheSerializer = Serializer.getSerializer(SerializerType.APACHE);
    }

    @Override
    protected void serializeCompressAndSend(ComputationResult computationResult, int currentRepetition) {
        final var wrapper = new Object() {
            private byte[] toSend;
        };
        this.mainBenchmarkUnit.run(() -> {
                    wrapper.toSend = apacheSerializer.serialize(computationResult);
                    LOGGER.info(String.format("[%d] Apache Serialization %s - Size: %d - Bytes: %d", currentRepetition, this.benchmarkName, computationResult.getResults().size(), wrapper.toSend.length));
                    return List.of((double) computationResult.getResults().size(), (double) wrapper.toSend.length);
                },
                () -> {
                    wrapper.toSend = Compressor.compress(wrapper.toSend);
                    LOGGER.info(String.format("[%d] Apache Compression %s - Size: %d - Bytes: %d", currentRepetition, this.benchmarkName, computationResult.getResults().size(), wrapper.toSend.length));
                    return List.of((double) wrapper.toSend.length);
                },
                () -> {
                    netManager.writeObject(wrapper.toSend);
                    LOGGER.info(String.format("[%d] Apache %s sent - Size: %d - Bytes: %d", currentRepetition, this.benchmarkName, computationResult.getResults().size(), wrapper.toSend.length));
                    return List.of();
                });
    }

    @Override
    protected String getSerializerName() {
        return "apache";
    }

    @Override
    protected String getMainLabel() {
        return "a";
    }
}
