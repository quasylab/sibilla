package quasylab.sibilla.examples.benchmarks.master;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.models.pm.PopulationState;
import quasylab.sibilla.core.network.ComputationResult;
import quasylab.sibilla.core.network.HostLoggerSupplier;
import quasylab.sibilla.core.network.NetworkInfo;
import quasylab.sibilla.core.network.util.Benchmark;
import quasylab.sibilla.core.network.communication.TCPNetworkManager;
import quasylab.sibilla.core.network.communication.TCPNetworkManagerType;
import quasylab.sibilla.core.network.compression.Compressor;
import quasylab.sibilla.core.network.serialization.ComputationResultSerializer;
import quasylab.sibilla.core.network.serialization.Serializer;
import quasylab.sibilla.core.network.serialization.SerializerType;
import quasylab.sibilla.core.network.util.NetworkUtils;
import quasylab.sibilla.core.past.State;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class MasterBenchmarkEnvironment<S extends State> {

    private Model<S> modelFourRules;
    private Model<S> modelThreeRules;

    private NetworkInfo localInfo;
    private ServerSocket serverSocket;
    private TCPNetworkManager netManager;

    private Serializer apacheSerializer;
    private Serializer fstSerializer;

    private Logger LOGGER;

    private Benchmark benchmarkDeserializationApacheFourRules = new Benchmark(
            "benchmarks/masterBenchmarking/fourRulesModel/apache",
            "deserialization",
            "csv",
            "a",
            "deser"
    );

    private Benchmark benchmarkDecompressionApacheFourRules = new Benchmark(
            "benchmarks/masterBenchmarking/fourRulesModel/apache",
            "decompression",
            "csv",
            "a",
            "decomp"
    );

    private Benchmark benchmarkDeserializationFSTFourRules = new Benchmark(
            "benchmarks/masterBenchmarking/fourRulesModel/fst",
            "deserialization",
            "csv",
            "f",
            "deser"
    );

    private Benchmark benchmarkDecompressionFSTFourRules = new Benchmark(
            "benchmarks/masterBenchmarking/fourRulesModel/fst",
            "decompression",
            "csv",
            "f",
            "decomp"
    );

    private Benchmark benchmarkDeserializationOptimizedFourRules = new Benchmark(
            "benchmarks/masterBenchmarking/fourRulesModel/optimized",
            "deserialization",
            "csv",
            "o",
            "deser"
    );

    private Benchmark benchmarkDecompressionOptimizedFourRules = new Benchmark(
            "benchmarks/masterBenchmarking/fourRulesModel/optimized",
            "decompression",
            "csv",
            "o",
            "decomp"
    );

    private Benchmark benchmarkDeserializationApacheThreeRules = new Benchmark(
            "benchmarks/masterBenchmarking/threeRulesModel/apache",
            "deserialization",
            "csv",
            "a",
            "deser"
    );
    private Benchmark benchmarkDecompressionApacheThreeRules = new Benchmark(
            "benchmarks/masterBenchmarking/threeRulesModel/apache",
            "decompression",
            "csv",
            "a",
            "decomp"
    );

    private Benchmark benchmarkDeserializationFSTThreeRules = new Benchmark(
            "benchmarks/masterBenchmarking/threeRulesModel/fst",
            "deserialization",
            "csv",
            "f",
            "deser"
    );

    private Benchmark benchmarkDecompressionFSTThreeRules = new Benchmark(
            "benchmarks/masterBenchmarking/threeRulesModel/fst",
            "decompression",
            "csv",
            "f",
            "decomp"
    );

    private Benchmark benchmarkDeserializationOptimizedThreeRules = new Benchmark(
            "benchmarks/masterBenchmarking/threeRulesModel/optimized",
            "deserialization",
            "csv",
            "o",
            "deser"
    );

    private Benchmark benchmarkDecompressionOptimizedThreeRules = new Benchmark(
            "benchmarks/masterBenchmarking/threeRulesModel/optimized",
            "decompression",
            "csv",
            "o",
            "decomp"
    );


    public MasterBenchmarkEnvironment(Model modelFourRules, Model modelThreeRules) throws IOException {
        localInfo = new NetworkInfo(NetworkUtils.getLocalAddress(), 10000,
                TCPNetworkManagerType.DEFAULT);

        serverSocket = TCPNetworkManager.createServerSocket((TCPNetworkManagerType) localInfo.getType(), localInfo.getPort());
        apacheSerializer = Serializer.getSerializer(SerializerType.APACHE);
        fstSerializer = Serializer.getSerializer(SerializerType.FST);
        LOGGER = HostLoggerSupplier.getInstance("Master Benchmark").getLogger();

        this.modelFourRules = modelFourRules;
        this.modelThreeRules = modelThreeRules;

    }

    public void run() throws IOException {
        Socket socket = serverSocket.accept();
        netManager = TCPNetworkManager.createNetworkManager((TCPNetworkManagerType) localInfo.getType(), socket);


        int repetitions = (int) fstSerializer.deserialize(netManager.readObject());
        int threshold = (int) fstSerializer.deserialize(netManager.readObject());

        for (int j = 1; j <= repetitions; j++) {

            AtomicInteger currentRepetition = new AtomicInteger(j);
            int trajectoriesReceived = 0;

            while (trajectoriesReceived < threshold) {
                final var fourRulesWrapper = new Object() {
                    private byte[] receivedApache;
                    private byte[] receivedFst;
                    private byte[] receivedOptimized;

                    private ComputationResult<S> resultsApache;
                    private ComputationResult<S> resultsFST;
                    private ComputationResult<S> resultsOptimized;
                };

                final var threeRulesWrapper = new Object() {
                    private byte[] receivedApache;
                    private byte[] receivedFst;
                    private byte[] receivedOptimized;

                    private ComputationResult<S> resultsApache;
                    private ComputationResult<S> resultsFST;
                    private ComputationResult<S> resultsOptimized;

                };
                //Ricezione
                fourRulesWrapper.receivedApache = netManager.readObject();
                LOGGER.info(String.format("[%d] Apache Four Rules compressed received - Bytes: %d", currentRepetition.get(), fourRulesWrapper.receivedApache.length));
                fourRulesWrapper.receivedFst = netManager.readObject();
                LOGGER.info(String.format("[%d] FST Four Rules compressed received - Bytes: %d", currentRepetition.get(), fourRulesWrapper.receivedFst.length));
                fourRulesWrapper.receivedOptimized = netManager.readObject();
                LOGGER.info(String.format("[%d] Optimized Four Rules compressed received - Bytes: %d", currentRepetition.get(), fourRulesWrapper.receivedOptimized.length));

                threeRulesWrapper.receivedApache = netManager.readObject();
                LOGGER.info(String.format("[%d] Apache Three Rules compressed received - Bytes: %d", currentRepetition.get(), threeRulesWrapper.receivedApache.length));
                threeRulesWrapper.receivedFst = netManager.readObject();
                LOGGER.info(String.format("[%d] FST Three Rules compressed received - Bytes: %d", currentRepetition.get(), threeRulesWrapper.receivedFst.length));
                threeRulesWrapper.receivedOptimized = netManager.readObject();
                LOGGER.info(String.format("[%d] Optimized Three Rules compressed received - Bytes: %d", currentRepetition.get(), threeRulesWrapper.receivedOptimized.length));

                //Decompressione
                benchmarkDecompressionApacheFourRules.run(() -> {
                    fourRulesWrapper.receivedApache = Compressor.decompress(fourRulesWrapper.receivedApache);
                    LOGGER.info(String.format("[%d] Apache Four Rules decompressed (serialized) - Bytes: %d", currentRepetition.get(), fourRulesWrapper.receivedApache.length));
                    return List.of();
                });

                benchmarkDecompressionFSTFourRules.run(() -> {
                    fourRulesWrapper.receivedFst = Compressor.decompress(fourRulesWrapper.receivedFst);
                    LOGGER.info(String.format("[%d] FST Four Rules decompressed (serialized) - Bytes: %d", currentRepetition.get(), fourRulesWrapper.receivedFst.length));
                    return List.of();
                });

                benchmarkDecompressionOptimizedFourRules.run(() -> {
                    fourRulesWrapper.receivedOptimized = Compressor.decompress(fourRulesWrapper.receivedOptimized);
                    LOGGER.info(String.format("[%d] Optimized Four Rules decompressed (serialized) - Bytes: %d", currentRepetition.get(), fourRulesWrapper.receivedOptimized.length));
                    return List.of();
                });

                benchmarkDecompressionApacheThreeRules.run(() -> {
                    threeRulesWrapper.receivedApache = Compressor.decompress(threeRulesWrapper.receivedApache);
                    LOGGER.info(String.format("[%d] Apache Three Rules decompressed (serialized) - Bytes: %d", currentRepetition.get(), threeRulesWrapper.receivedApache.length));
                    return List.of();
                });

                benchmarkDecompressionFSTThreeRules.run(() -> {
                    threeRulesWrapper.receivedFst = Compressor.decompress(threeRulesWrapper.receivedFst);
                    LOGGER.info(String.format("[%d] FST Three Rules decompressed (serialized) - Bytes: %d", currentRepetition.get(), threeRulesWrapper.receivedFst.length));
                    return List.of();
                });

                benchmarkDecompressionOptimizedThreeRules.run(() -> {
                    threeRulesWrapper.receivedOptimized = Compressor.decompress(threeRulesWrapper.receivedOptimized);
                    LOGGER.info(String.format("[%d] Optimized Three Rules decompressed (serialized) - Bytes: %d", currentRepetition.get(), threeRulesWrapper.receivedOptimized.length));
                    return List.of();
                });

                //Deserializzazione
                benchmarkDeserializationApacheFourRules.run(() -> {
                    fourRulesWrapper.resultsApache = (ComputationResult<S>) apacheSerializer.deserialize(fourRulesWrapper.receivedApache);
                    LOGGER.info(String.format("[%d] Apache Four Rules deserialized - Size: %d - Bytes: %d", currentRepetition.get(), fourRulesWrapper.resultsApache.getResults().size(), fourRulesWrapper.receivedApache.length));
                    return List.of();
                });

                benchmarkDeserializationFSTFourRules.run(() -> {
                    fourRulesWrapper.resultsFST = (ComputationResult<S>) fstSerializer.deserialize(fourRulesWrapper.receivedFst);
                    LOGGER.info(String.format("[%d] FST Four Rules deserialized - Size: %d - Bytes: %d", currentRepetition.get(), fourRulesWrapper.resultsFST.getResults().size(), fourRulesWrapper.receivedFst.length));
                    return List.of();
                });

                benchmarkDeserializationOptimizedFourRules.run(() -> {
                    fourRulesWrapper.resultsOptimized = ComputationResultSerializer.deserialize(fourRulesWrapper.receivedOptimized,
                            modelFourRules);
                    LOGGER.info(String.format("[%d] Optimized Four Rules deserialized - Size: %d - Bytes: %d", currentRepetition.get(), fourRulesWrapper.resultsOptimized.getResults().size(), fourRulesWrapper.receivedOptimized.length));
                    return List.of();
                });

                benchmarkDeserializationApacheThreeRules.run(() -> {
                    threeRulesWrapper.resultsApache = (ComputationResult<S>) apacheSerializer.deserialize(threeRulesWrapper.receivedApache);
                    LOGGER.info(String.format("[%d] Apache Three Rules deserialized - Size: %d - Bytes: %d", currentRepetition.get(), threeRulesWrapper.resultsApache.getResults().size(), threeRulesWrapper.receivedApache.length));
                    return List.of();
                });

                benchmarkDeserializationFSTThreeRules.run(() -> {
                    threeRulesWrapper.resultsFST = (ComputationResult<S>) fstSerializer.deserialize(threeRulesWrapper.receivedFst);
                    LOGGER.info(String.format("[%d] FST Three Rules deserialized - Size: %d - Bytes: %d", currentRepetition.get(), threeRulesWrapper.resultsFST.getResults().size(), threeRulesWrapper.receivedFst.length));
                    return List.of();
                });

                benchmarkDeserializationOptimizedThreeRules.run(() -> {
                    threeRulesWrapper.resultsOptimized = ComputationResultSerializer.deserialize(threeRulesWrapper.receivedOptimized,
                            modelFourRules);
                    LOGGER.info(String.format("[%d] Optimized Three Rules deserialized - Size: %d - Bytes: %d", currentRepetition.get(), threeRulesWrapper.resultsOptimized.getResults().size(), threeRulesWrapper.receivedOptimized.length));
                    return List.of();
                });

                trajectoriesReceived = threeRulesWrapper.resultsOptimized.getResults().size();
            }

        }


        netManager.closeConnection();
    }

    public static void main(String[] args) throws IOException {
        MasterBenchmarkEnvironment<PopulationState> env = new MasterBenchmarkEnvironment(new SEIRModelDefinitionFourRules().createModel(), new SEIRModelDefinitionThreeRules().createModel());
        env.run();

    }
}
