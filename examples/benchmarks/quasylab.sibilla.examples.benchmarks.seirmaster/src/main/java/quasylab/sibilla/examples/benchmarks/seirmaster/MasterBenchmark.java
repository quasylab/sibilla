package quasylab.sibilla.examples.benchmarks.seirmaster;

import quasylab.sibilla.core.models.pm.PopulationState;
import quasylab.sibilla.core.network.NetworkInfo;
import quasylab.sibilla.core.network.benchmark.MasterBenchmarkEnvironment;
import quasylab.sibilla.core.network.benchmark.MasterBenchmarkType;
import quasylab.sibilla.core.network.communication.TCPNetworkManagerType;

import java.io.IOException;
import java.net.InetAddress;

public class MasterBenchmark {


    public static void main(String[] args) throws IOException {
        MasterBenchmarkEnvironment<PopulationState> env = MasterBenchmarkEnvironment.getMasterBenchmark(
                "threeRules",
                new NetworkInfo(InetAddress.getByName("localhost"), 10000, TCPNetworkManagerType.DEFAULT),
                getType("OPTIMIZED"),
                new SEIRModelDefinitionThreeRules().createModel(),
                20,
                900,
                1,
                1);

        env.run();
    }

    private static MasterBenchmarkType getType(String arg) {
        return MasterBenchmarkType.valueOf(arg);
    }

}
