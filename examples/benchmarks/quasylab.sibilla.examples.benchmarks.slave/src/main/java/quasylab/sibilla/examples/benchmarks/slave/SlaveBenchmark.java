package quasylab.sibilla.examples.benchmarks.slave;

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

        String benchmarkName = "prebenchmarkTest";

        NetworkInfo localInfo = new NetworkInfo(NetworkUtils.getLocalAddress(), 10000, TCPNetworkManagerType.DEFAULT);
        TCPNetworkManager networkManager = TCPNetworkManager
                .createNetworkManager((TCPNetworkManagerType) localInfo.getType(), TCPNetworkManager
                        .createServerSocket((TCPNetworkManagerType) localInfo.getType(), localInfo.getPort()).accept());

        ComputationResultSerializerType type = (ComputationResultSerializerType) fstSerializer
                .deserialize(networkManager.readObject());
        networkManager.writeObject(fstSerializer.serialize(benchmarkName));

        PopulationModelDefinition def = new ChordModel();
        def.setParameter("N", 1000);
        PopulationModel model = def.createModel();

        SlaveBenchmarkEnvironment<PopulationState> env = SlaveBenchmarkEnvironment.getSlaveBenchmark(networkManager,
                benchmarkName, "src/main/resources", "chordTrajectory_Samplings100_Deadline600_N1000_Samples6", model,
                type);

        env.run();
    }

    private static ComputationResultSerializerType getType(String arg) {
        return ComputationResultSerializerType.valueOf(arg);
    }

}
