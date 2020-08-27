package quasylab.sibilla.core.network.benchmark.slave;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.models.State;
import quasylab.sibilla.core.network.ComputationResult;
import quasylab.sibilla.core.network.NetworkInfo;
import quasylab.sibilla.core.network.communication.TCPNetworkManager;
import quasylab.sibilla.core.network.compression.Compressor;
import quasylab.sibilla.core.network.serialization.ComputationResultSerializerType;
import quasylab.sibilla.core.network.serialization.Serializer;
import quasylab.sibilla.core.network.serialization.SerializerType;

import java.io.IOException;
import java.util.List;

public class FstSlaveBenchmarkEnvironment<S extends State> extends SlaveBenchmarkEnvironment {

    private Serializer fstSerializer;

    public FstSlaveBenchmarkEnvironment(String benchmarkName, String trajectoryFileDir, String trajectoryFileName, NetworkInfo localInfo, ComputationResultSerializerType type, Model<S>  model) throws IOException {
        super(benchmarkName, trajectoryFileDir, trajectoryFileName, localInfo, type, model);
        this.fstSerializer = Serializer.getSerializer(SerializerType.FST);
    }

    public FstSlaveBenchmarkEnvironment(TCPNetworkManager networkManager, String benchmarkName, String trajectoryFileDir, String trajectoryFileName, ComputationResultSerializerType type, Model<S>  model) throws IOException {
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
