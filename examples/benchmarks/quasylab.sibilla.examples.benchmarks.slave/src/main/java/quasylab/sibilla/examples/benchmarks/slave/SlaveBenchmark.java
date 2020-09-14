package quasylab.sibilla.examples.benchmarks.slave;

import quasylab.sibilla.core.models.pm.PopulationModel;
import quasylab.sibilla.core.models.pm.PopulationModelDefinition;
import quasylab.sibilla.core.models.pm.PopulationState;
import quasylab.sibilla.core.network.NetworkInfo;
import quasylab.sibilla.core.network.benchmark.slave.SlaveBenchmarkEnvironment;
import quasylab.sibilla.core.network.communication.TCPNetworkManager;
import quasylab.sibilla.core.network.communication.TCPNetworkManagerType;
import quasylab.sibilla.core.network.serialization.ComputationResultSerializerType;
import quasylab.sibilla.core.network.serialization.Serializer;
import quasylab.sibilla.core.network.serialization.SerializerType;
import quasylab.sibilla.core.network.util.NetworkUtils;
import quasylab.sibilla.examples.pm.crowds.ChordModel;

import java.io.IOException;

public class SlaveBenchmark {

    public static void main(String[] args) throws IOException {
        Serializer fstSerializer = Serializer.getSerializer(SerializerType.FST);

        String benchmarkName = "prebenchmarkTest";

        NetworkInfo localInfo = new NetworkInfo(NetworkUtils.getLocalAddress(), 10000, TCPNetworkManagerType.DEFAULT);
        TCPNetworkManager networkManager = TCPNetworkManager
                .createNetworkManager((TCPNetworkManagerType) localInfo.getType(), TCPNetworkManager
                        .createServerSocket((TCPNetworkManagerType) localInfo.getType(), localInfo.getPort()).accept());

        ComputationResultSerializerType type = (ComputationResultSerializerType) fstSerializer.deserialize(networkManager.readObject());
        networkManager.writeObject(fstSerializer.serialize(benchmarkName));

        PopulationModelDefinition def = new ChordModel();
        def.setParameter("N",1000);
        PopulationModel model = def.createModel();
        
        SlaveBenchmarkEnvironment<PopulationState> env = SlaveBenchmarkEnvironment.getSlaveBenchmark(networkManager,
                benchmarkName, "src/main/resources", "chordTrajectory_Samplings100_Deadline600_N1000_Samples6",
                model, type);

        env.run();
    }

    private static ComputationResultSerializerType getType(String arg) {
        return ComputationResultSerializerType.valueOf(arg);
    }

}
