package quasylab.sibilla.core.network.benchmark;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.models.pm.PopulationState;
import quasylab.sibilla.core.network.ComputationResult;
import quasylab.sibilla.core.network.HostLoggerSupplier;
import quasylab.sibilla.core.network.NetworkInfo;
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

    private Benchmark benchmarkDeserializationApacheFourRules = new Benchmark("benchmarks/masterBenchmarking", "Four Rules Model Master Benchmarking - Deserialization - Apache", "csv");
    private Benchmark benchmarkDecompressionApacheFourRules = new Benchmark("benchmarks/masterBenchmarking", "Four Rules Model Master Benchmarking - Decompression - Apache", "csv");

    private Benchmark benchmarkDeserializationFSTFourRules = new Benchmark("benchmarks/masterBenchmarking", "Four Rules Model Master Benchmarking - Deserialization - FST", "csv");
    private Benchmark benchmarkDecompressionFSTFourRules = new Benchmark("benchmarks/masterBenchmarking", "Four Rules Model Master Benchmarking - Decompression - FST", "csv");

    private Benchmark benchmarkDeserializationOptimizedFourRules = new Benchmark("benchmarks/masterBenchmarking", "Four Rules Model Master Benchmarking - Deserialization - Optimized", "csv");
    private Benchmark benchmarkDecompressionOptimizedFourRules = new Benchmark("benchmarks/masterBenchmarking", "Four Rules Model Master Benchmarking - Decompression - Optimized", "csv");

    private Benchmark benchmarkDeserializationApacheThreeRules = new Benchmark("benchmarks/masterBenchmarking", "Three Rules Model Master Benchmarking - Deserialization - Apache", "csv");
    private Benchmark benchmarkDecompressionApacheThreeRules = new Benchmark("benchmarks/masterBenchmarking", "Three Rules Model Master Benchmarking - Decompression - Apache", "csv");

    private Benchmark benchmarkDeserializationFSTThreeRules = new Benchmark("benchmarks/masterBenchmarking", "Three Rules Model Master Benchmarking - Deserialization - FST", "csv");
    private Benchmark benchmarkDecompressionFSTThreeRules = new Benchmark("benchmarks/masterBenchmarking", "Three Rules Model Master Benchmarking - Decompression - FST", "csv");

    private Benchmark benchmarkDeserializationOptimizedThreeRules = new Benchmark("benchmarks/masterBenchmarking", "Three Rules Model Master Benchmarking - Deserialization - Optimized", "csv");
    private Benchmark benchmarkDecompressionOptimizedThreeRules = new Benchmark("benchmarks/masterBenchmarking", "Three Rules Model Master Benchmarking - Decompression - Optimized", "csv");


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

        int threshold = (int) fstSerializer.deserialize(netManager.readObject());
        int trajectoriesReceived = 0;
        while (trajectoriesReceived < threshold) {

            //Ricezione
            fourRulesWrapper.receivedApache = netManager.readObject();
            LOGGER.info(String.format("Apache Four Rules compressed received - Bytes: %d", fourRulesWrapper.receivedApache.length));
            fourRulesWrapper.receivedFst = netManager.readObject();
            LOGGER.info(String.format("FST Four Rules compressed received - Bytes: %d", fourRulesWrapper.receivedFst.length));
            fourRulesWrapper.receivedOptimized = netManager.readObject();
            LOGGER.info(String.format("Optimized Four Rules compressed received - Bytes: %d", fourRulesWrapper.receivedOptimized.length));

            threeRulesWrapper.receivedApache = netManager.readObject();
            LOGGER.info(String.format("Apache Three Rules compressed received - Bytes: %d", threeRulesWrapper.receivedApache.length));
            threeRulesWrapper.receivedFst = netManager.readObject();
            LOGGER.info(String.format("FST Three Rules compressed received - Bytes: %d", threeRulesWrapper.receivedFst.length));
            threeRulesWrapper.receivedOptimized = netManager.readObject();
            LOGGER.info(String.format("Optimized Three Rules compressed received - Bytes: %d", threeRulesWrapper.receivedOptimized.length));

            //Decompressione
            benchmarkDecompressionApacheFourRules.run(() -> {
                fourRulesWrapper.receivedApache = Compressor.decompress(fourRulesWrapper.receivedApache);
                LOGGER.info(String.format("Apache Four Rules decompressed (serialized) - Bytes: %d", fourRulesWrapper.receivedApache.length));
                return List.of();
            });

            benchmarkDecompressionFSTFourRules.run(() -> {
                fourRulesWrapper.receivedFst = Compressor.decompress(fourRulesWrapper.receivedFst);
                LOGGER.info(String.format("FST Four Rules decompressed (serialized) - Bytes: %d", fourRulesWrapper.receivedFst.length));
                return List.of();
            });

            benchmarkDecompressionOptimizedFourRules.run(() -> {
                fourRulesWrapper.receivedOptimized = Compressor.decompress(fourRulesWrapper.receivedOptimized);
                LOGGER.info(String.format("Optimized Four Rules decompressed (serialized) - Bytes: %d", fourRulesWrapper.receivedOptimized.length));
                return List.of();
            });

            benchmarkDecompressionApacheThreeRules.run(() -> {
                threeRulesWrapper.receivedApache = Compressor.decompress(threeRulesWrapper.receivedApache);
                LOGGER.info(String.format("Apache Three Rules decompressed (serialized) - Bytes: %d", threeRulesWrapper.receivedApache.length));
                return List.of();
            });

            benchmarkDecompressionFSTThreeRules.run(() -> {
                threeRulesWrapper.receivedFst = Compressor.decompress(threeRulesWrapper.receivedFst);
                LOGGER.info(String.format("FST Three Rules decompressed (serialized) - Bytes: %d", threeRulesWrapper.receivedFst.length));
                return List.of();
            });

            benchmarkDecompressionOptimizedThreeRules.run(() -> {
                threeRulesWrapper.receivedOptimized = Compressor.decompress(threeRulesWrapper.receivedOptimized);
                LOGGER.info(String.format("Optimized Three Rules decompressed (serialized) - Bytes: %d", threeRulesWrapper.receivedOptimized.length));
                return List.of();
            });

            //Deserializzazione
            benchmarkDeserializationApacheFourRules.run(() -> {
                fourRulesWrapper.resultsApache = (ComputationResult<S>) apacheSerializer.deserialize(fourRulesWrapper.receivedApache);
                LOGGER.info(String.format("Apache Four Rules deserialized - Size: %d - Bytes: %d", fourRulesWrapper.resultsApache.getResults().size(), fourRulesWrapper.receivedApache.length));
                return List.of();
            });

            benchmarkDeserializationFSTFourRules.run(() -> {
                fourRulesWrapper.resultsFST = (ComputationResult<S>) fstSerializer.deserialize(fourRulesWrapper.receivedFst);
                LOGGER.info(String.format("FST Four Rules deserialized - Size: %d - Bytes: %d", fourRulesWrapper.resultsFST.getResults().size(), fourRulesWrapper.receivedFst.length));
                return List.of();
            });

            benchmarkDeserializationOptimizedFourRules.run(() -> {
                fourRulesWrapper.resultsOptimized = ComputationResultSerializer.deserialize(fourRulesWrapper.receivedOptimized,
                        modelFourRules);
                LOGGER.info(String.format("Optimized Four Rules deserialized - Size: %d - Bytes: %d", fourRulesWrapper.resultsOptimized.getResults().size(), fourRulesWrapper.receivedOptimized.length));
                return List.of();
            });

            benchmarkDeserializationApacheThreeRules.run(() -> {
                threeRulesWrapper.resultsApache = (ComputationResult<S>) apacheSerializer.deserialize(threeRulesWrapper.receivedApache);
                LOGGER.info(String.format("Apache Three Rules deserialized - Size: %d - Bytes: %d", threeRulesWrapper.resultsApache.getResults().size(), threeRulesWrapper.receivedApache.length));
                return List.of();
            });

            benchmarkDeserializationFSTThreeRules.run(() -> {
                threeRulesWrapper.resultsFST = (ComputationResult<S>) fstSerializer.deserialize(threeRulesWrapper.receivedFst);
                LOGGER.info(String.format("FST Three Rules deserialized - Size: %d - Bytes: %d", threeRulesWrapper.resultsFST.getResults().size(), threeRulesWrapper.receivedFst.length));
                return List.of();
            });

            benchmarkDeserializationOptimizedThreeRules.run(() -> {
                threeRulesWrapper.resultsOptimized = ComputationResultSerializer.deserialize(threeRulesWrapper.receivedOptimized,
                        modelFourRules);
                LOGGER.info(String.format("Optimized Three Rules deserialized - Size: %d - Bytes: %d", threeRulesWrapper.resultsOptimized.getResults().size(), threeRulesWrapper.receivedOptimized.length));
                return List.of();
            });

            trajectoriesReceived = threeRulesWrapper.resultsOptimized.getResults().size();
        }


        netManager.closeConnection();
    }

    public static void main(String[] args) throws IOException {
        MasterBenchmarkEnvironment<PopulationState> env = new MasterBenchmarkEnvironment(new SEIRModelDefinitionFourRules().createModel(), new SEIRModelDefinitionThreeRules().createModel());
        env.run();

    }
}
