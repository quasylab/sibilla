package quasylab.sibilla.examples.benchmarks.seirmaster;

import quasylab.sibilla.core.models.pm.PopulationState;
import quasylab.sibilla.core.network.NetworkInfo;
import quasylab.sibilla.core.network.benchmark.master.MasterBenchmarkEnvironment;
import quasylab.sibilla.core.network.benchmark.BenchmarkType;
import quasylab.sibilla.core.network.communication.TCPNetworkManager;
import quasylab.sibilla.core.network.communication.TCPNetworkManagerType;
import quasylab.sibilla.core.network.serialization.Serializer;
import quasylab.sibilla.core.network.serialization.SerializerType;

import java.io.IOException;
import java.net.InetAddress;

public class MasterBenchmark {


    public static void main(String[] args) throws IOException {
        Serializer fstSerializer = Serializer.getSerializer(SerializerType.FST);

        BenchmarkType type = BenchmarkType.OPTIMIZED;
        NetworkInfo slaveInfo = new NetworkInfo(InetAddress.getByName("localhost"), 10000, TCPNetworkManagerType.DEFAULT);
        TCPNetworkManager networkManager = TCPNetworkManager.createNetworkManager(slaveInfo);

        networkManager.writeObject(fstSerializer.serialize(type));
        String benchmarkName = (String) fstSerializer.deserialize(networkManager.readObject());


        MasterBenchmarkEnvironment<PopulationState> env = MasterBenchmarkEnvironment.getMasterBenchmark(
                networkManager,
                benchmarkName,
                type,
                new SEIRModelDefinitionThreeRules().createModel(),
                20,
                900,
                1,
                900);

        env.run();
    }

    private static BenchmarkType getType(String arg) {
        return BenchmarkType.valueOf(arg);
    }

}
