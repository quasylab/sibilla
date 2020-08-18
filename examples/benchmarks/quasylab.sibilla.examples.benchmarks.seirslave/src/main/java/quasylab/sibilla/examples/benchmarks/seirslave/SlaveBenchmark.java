package quasylab.sibilla.examples.benchmarks.seirslave;

import quasylab.sibilla.core.models.pm.PopulationState;
import quasylab.sibilla.core.network.NetworkInfo;
import quasylab.sibilla.core.network.benchmark.BenchmarkType;
import quasylab.sibilla.core.network.benchmark.slave.SlaveBenchmarkEnvironment;
import quasylab.sibilla.core.network.communication.TCPNetworkManager;
import quasylab.sibilla.core.network.communication.TCPNetworkManagerType;
import quasylab.sibilla.core.network.serialization.Serializer;
import quasylab.sibilla.core.network.serialization.SerializerType;
import quasylab.sibilla.core.network.util.NetworkUtils;

import java.io.IOException;

public class SlaveBenchmark {

    public static void main(String[] args) throws IOException {
        Serializer fstSerializer = Serializer.getSerializer(SerializerType.FST);
        NetworkInfo localInfo = new NetworkInfo(NetworkUtils.getLocalAddress(), 10000, TCPNetworkManagerType.DEFAULT);
        TCPNetworkManager networkManager = TCPNetworkManager.createNetworkManager((TCPNetworkManagerType) localInfo.getType(), TCPNetworkManager.createServerSocket((TCPNetworkManagerType) localInfo.getType(), localInfo.getPort()).accept());

        BenchmarkType type = (BenchmarkType) fstSerializer.deserialize(networkManager.readObject());
        String benchmarkName = (String) fstSerializer.deserialize(networkManager.readObject());

        SlaveBenchmarkEnvironment<PopulationState> env = SlaveBenchmarkEnvironment.getSlaveBenchmark(
                networkManager,
                benchmarkName,
                "src/main/resources",
                "SEIR 3 rules trajectory FST",
                new SEIRModelDefinitionThreeRules().createModel(),
                type);

        env.run();
    }

    private static BenchmarkType getType(String arg) {
        return BenchmarkType.valueOf(arg);
    }

}
