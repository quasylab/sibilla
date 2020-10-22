package quasylab.sibilla.examples.benchmarks.seirslave;

import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.network.NetworkInfo;
import it.unicam.quasylab.sibilla.core.network.benchmark.slave.SlaveBenchmarkEnvironment;
import it.unicam.quasylab.sibilla.core.network.communication.TCPNetworkManager;
import it.unicam.quasylab.sibilla.core.network.communication.TCPNetworkManagerType;
import it.unicam.quasylab.sibilla.core.network.serialization.ComputationResultSerializerType;
import it.unicam.quasylab.sibilla.core.network.serialization.Serializer;
import it.unicam.quasylab.sibilla.core.network.serialization.SerializerType;
import it.unicam.quasylab.sibilla.core.network.util.NetworkUtils;

import java.io.IOException;

public class SlaveBenchmark {

    public static void main(String[] args) throws IOException {
        Serializer fstSerializer = Serializer.getSerializer(SerializerType.FST);
        String benchmarkName = "testNewOpti4Rules";
        NetworkInfo localInfo = new NetworkInfo(NetworkUtils.getLocalAddress(), 10000, TCPNetworkManagerType.DEFAULT);
        TCPNetworkManager networkManager = TCPNetworkManager
                .createNetworkManager((TCPNetworkManagerType) localInfo.getType(), TCPNetworkManager
                        .createServerSocket((TCPNetworkManagerType) localInfo.getType(), localInfo.getPort()).accept());

        ComputationResultSerializerType type = (ComputationResultSerializerType) fstSerializer.deserialize(networkManager.readObject());
        networkManager.writeObject(fstSerializer.serialize(benchmarkName));

        SlaveBenchmarkEnvironment<PopulationState> env = SlaveBenchmarkEnvironment.getSlaveBenchmark(networkManager,
                benchmarkName, "src/main/resources", "SEIR_4_Rules_Trajectory_Custom_5128",
                new SEIRModelDefinitionThreeRules().createModel(), type);

        env.run();
    }

    private static ComputationResultSerializerType getType(String arg) {
        return ComputationResultSerializerType.valueOf(arg);
    }

}
