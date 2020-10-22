package quasylab.sibilla.examples.benchmarks.seirmaster;

import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.network.NetworkInfo;
import it.unicam.quasylab.sibilla.core.network.benchmark.master.MasterBenchmarkEnvironment;
import it.unicam.quasylab.sibilla.core.network.communication.TCPNetworkManager;
import it.unicam.quasylab.sibilla.core.network.communication.TCPNetworkManagerType;
import it.unicam.quasylab.sibilla.core.network.serialization.ComputationResultSerializerType;
import it.unicam.quasylab.sibilla.core.network.serialization.Serializer;
import it.unicam.quasylab.sibilla.core.network.serialization.SerializerType;

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

        MasterBenchmarkEnvironment<PopulationState> env = MasterBenchmarkEnvironment.getMasterBenchmark(
                networkManager, benchmarkName, type, new SEIRModelDefinitionThreeRules().createModel(),
                20, 900, 1, 900);

        env.run();
    }

    private static ComputationResultSerializerType getType(String arg) {
        return ComputationResultSerializerType.valueOf(arg);
    }

}
