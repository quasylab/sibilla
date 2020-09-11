package quasylab.sibilla.examples.benchmarks.seirmaster;

import quasylab.sibilla.core.models.pm.PopulationModel;
import quasylab.sibilla.core.models.pm.PopulationState;
import quasylab.sibilla.core.network.NetworkInfo;
import quasylab.sibilla.core.network.benchmark.master.MasterBenchmarkEnvironment;
import quasylab.sibilla.core.network.communication.TCPNetworkManager;
import quasylab.sibilla.core.network.communication.TCPNetworkManagerType;
import quasylab.sibilla.core.network.serialization.ComputationResultSerializerType;
import quasylab.sibilla.core.network.serialization.Serializer;
import quasylab.sibilla.core.network.serialization.SerializerType;
import quasylab.sibilla.examples.pm.crowds.ChordModel;

import java.io.IOException;
import java.net.InetAddress;

public class MasterBenchmark {

    public static void main(String[] args) throws IOException {
        Serializer fstSerializer = Serializer.getSerializer(SerializerType.FST);

        ComputationResultSerializerType type = ComputationResultSerializerType.CUSTOM;
        NetworkInfo slaveInfo = new NetworkInfo(InetAddress.getByName("localhost"), 10000,
                TCPNetworkManagerType.DEFAULT);
        TCPNetworkManager networkManager = TCPNetworkManager.createNetworkManager(slaveInfo);

        networkManager.writeObject(fstSerializer.serialize(type));
        String benchmarkName = (String) fstSerializer.deserialize(networkManager.readObject());

        ChordModel def = new ChordModel();
        def.setParameter("N",10);
        PopulationModel model = def.createModel();

        MasterBenchmarkEnvironment<PopulationState> env = MasterBenchmarkEnvironment.getMasterBenchmark(
                networkManager, benchmarkName, type, model,
                50, 2000, 5, 2000);

        env.run();
    }

    private static ComputationResultSerializerType getType(String arg) {
        return ComputationResultSerializerType.valueOf(arg);
    }

}
