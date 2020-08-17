package quasylab.sibilla.examples.benchmarks.seirslave;

import quasylab.sibilla.core.models.pm.PopulationState;
import quasylab.sibilla.core.network.NetworkInfo;
import quasylab.sibilla.core.network.benchmark.SlaveSeirBenchmarkEnvironment;
import quasylab.sibilla.core.network.benchmark.SlaveBenchmarkType;
import quasylab.sibilla.core.network.communication.TCPNetworkManagerType;
import quasylab.sibilla.core.network.util.NetworkUtils;

import java.io.IOException;

public class SlaveBenchmark {

    public static void main(String[] args) throws IOException {
        SlaveSeirBenchmarkEnvironment<PopulationState> env = new SlaveSeirBenchmarkEnvironment(
                "src/main/resources",
                "src/main/resources",
                "SEIR 4 rules trajectory FST",
                "SEIR 3 rules trajectory FST",
                new NetworkInfo(NetworkUtils.getLocalAddress(), 10000, TCPNetworkManagerType.DEFAULT),
                new SEIRModelDefinitionFourRules().createModel(),
                new SEIRModelDefinitionThreeRules().createModel(),
                getType("OPTIMIZEDTHREERULES"));
    }

    private static SlaveBenchmarkType getType(String arg) {
        return SlaveBenchmarkType.valueOf(arg);
    }

}
