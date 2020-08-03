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

    private Benchmark benchmarkApacheFourRules = new Benchmark(
            "benchmarks/masterBenchmarking/fourRulesModel/",
            "apache",
            "csv",
            "a",
            List.of("decomprtime",
                    "desertime",
                    "tasks")
    );

    private Benchmark benchmarkFSTFourRules = new Benchmark(
            "benchmarks/masterBenchmarking/fourRulesModel/",
            "fst",
            "csv",
            "f",
            List.of("decomprtime",
                    "desertime",
                    "tasks")
    );

    private Benchmark benchmarkOptimizedFourRules = new Benchmark(
            "benchmarks/masterBenchmarking/fourRulesModel/",
            "optimized",
            "csv",
            "o",
            List.of("decomprtime",
                    "desertime",
                    "tasks")
    );

    private Benchmark benchmarkApacheThreeRules = new Benchmark(
            "benchmarks/masterBenchmarking/threeRulesModel/",
            "apache",
            "csv",
            "a",
            List.of("decomprtime",
                    "desertime",
                    "tasks")
    );

    private Benchmark benchmarkFSTThreeRules = new Benchmark(
            "benchmarks/masterBenchmarking/threeRulesModel/",
            "fst",
            "csv",
            "f",
            List.of("decomprtime",
                    "desertime",
                    "tasks")
    );

    private Benchmark benchmarkOptimizedThreeRules = new Benchmark(
            "benchmarks/masterBenchmarking/threeRulesModel/",
            "optimized",
            "csv",
            "o",
            List.of("decomprtime",
                    "desertime",
                    "tasks")
    );


    public MasterBenchmarkEnvironment(NetworkInfo localInfo, Model modelFourRules, Model modelThreeRules) throws IOException {
        this.localInfo = localInfo;

        serverSocket = TCPNetworkManager.createServerSocket((TCPNetworkManagerType) this.localInfo.getType(), this.localInfo.getPort());
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


                benchmarkApacheFourRules.run(() -> {
                            fourRulesWrapper.receivedApache = Compressor.decompress(fourRulesWrapper.receivedApache);
                            LOGGER.info(String.format("[%d] Apache Four Rules decompressed (serialized) - Bytes: %d", currentRepetition.get(), fourRulesWrapper.receivedApache.length));
                            return List.of();
                        }, () -> {
                            fourRulesWrapper.resultsApache = (ComputationResult<S>) apacheSerializer.deserialize(fourRulesWrapper.receivedApache);
                            LOGGER.info(String.format("[%d] Apache Four Rules deserialized - Size: %d - Bytes: %d", currentRepetition.get(), fourRulesWrapper.resultsApache.getResults().size(), fourRulesWrapper.receivedApache.length));
                            return List.of((double) fourRulesWrapper.resultsApache.getResults().size());
                        }
                );

                benchmarkFSTFourRules.run(() -> {
                    fourRulesWrapper.receivedFst = Compressor.decompress(fourRulesWrapper.receivedFst);
                    LOGGER.info(String.format("[%d] FST Four Rules decompressed (serialized) - Bytes: %d", currentRepetition.get(), fourRulesWrapper.receivedFst.length));
                    return List.of();
                }, () -> {
                    fourRulesWrapper.resultsFST = (ComputationResult<S>) fstSerializer.deserialize(fourRulesWrapper.receivedFst);
                    LOGGER.info(String.format("[%d] FST Four Rules deserialized - Size: %d - Bytes: %d", currentRepetition.get(), fourRulesWrapper.resultsFST.getResults().size(), fourRulesWrapper.receivedFst.length));
                    return List.of((double) fourRulesWrapper.resultsFST.getResults().size());
                });

                benchmarkOptimizedFourRules.run(() -> {
                    fourRulesWrapper.receivedOptimized = Compressor.decompress(fourRulesWrapper.receivedOptimized);
                    LOGGER.info(String.format("[%d] Optimized Four Rules decompressed (serialized) - Bytes: %d", currentRepetition.get(), fourRulesWrapper.receivedOptimized.length));
                    return List.of();
                }, () -> {
                    fourRulesWrapper.resultsOptimized = ComputationResultSerializer.deserialize(fourRulesWrapper.receivedOptimized,
                            modelFourRules);
                    LOGGER.info(String.format("[%d] Optimized Four Rules deserialized - Size: %d - Bytes: %d", currentRepetition.get(), fourRulesWrapper.resultsOptimized.getResults().size(), fourRulesWrapper.receivedOptimized.length));
                    return List.of((double) fourRulesWrapper.resultsOptimized.getResults().size());
                });

                benchmarkApacheThreeRules.run(() -> {
                    threeRulesWrapper.receivedApache = Compressor.decompress(threeRulesWrapper.receivedApache);
                    LOGGER.info(String.format("[%d] Apache Three Rules decompressed (serialized) - Bytes: %d", currentRepetition.get(), threeRulesWrapper.receivedApache.length));
                    return List.of();
                }, () -> {
                    threeRulesWrapper.resultsApache = (ComputationResult<S>) apacheSerializer.deserialize(threeRulesWrapper.receivedApache);
                    LOGGER.info(String.format("[%d] Apache Three Rules deserialized - Size: %d - Bytes: %d", currentRepetition.get(), threeRulesWrapper.resultsApache.getResults().size(), threeRulesWrapper.receivedApache.length));
                    return List.of((double) threeRulesWrapper.resultsApache.getResults().size());
                });

                benchmarkFSTThreeRules.run(() -> {
                    threeRulesWrapper.receivedFst = Compressor.decompress(threeRulesWrapper.receivedFst);
                    LOGGER.info(String.format("[%d] FST Three Rules decompressed (serialized) - Bytes: %d", currentRepetition.get(), threeRulesWrapper.receivedFst.length));
                    return List.of();
                }, () -> {
                    threeRulesWrapper.resultsFST = (ComputationResult<S>) fstSerializer.deserialize(threeRulesWrapper.receivedFst);
                    LOGGER.info(String.format("[%d] FST Three Rules deserialized - Size: %d - Bytes: %d", currentRepetition.get(), threeRulesWrapper.resultsFST.getResults().size(), threeRulesWrapper.receivedFst.length));
                    return List.of((double) threeRulesWrapper.resultsFST.getResults().size());
                });

                benchmarkOptimizedThreeRules.run(() -> {
                            threeRulesWrapper.receivedOptimized = Compressor.decompress(threeRulesWrapper.receivedOptimized);
                            LOGGER.info(String.format("[%d] Optimized Three Rules decompressed (serialized) - Bytes: %d", currentRepetition.get(), threeRulesWrapper.receivedOptimized.length));
                            return List.of();
                        }, () -> {
                            threeRulesWrapper.resultsOptimized = ComputationResultSerializer.deserialize(threeRulesWrapper.receivedOptimized,
                                    modelThreeRules);
                            LOGGER.info(String.format("[%d] Optimized Three Rules deserialized - Size: %d - Bytes: %d", currentRepetition.get(), threeRulesWrapper.resultsOptimized.getResults().size(), threeRulesWrapper.receivedOptimized.length));
                            return List.of((double) threeRulesWrapper.resultsOptimized.getResults().size());
                        }
                );

                trajectoriesReceived = threeRulesWrapper.resultsOptimized.getResults().size();
            }

        }


        netManager.closeConnection();
    }

    public static void main(String[] args) throws IOException {
        MasterBenchmarkEnvironment<PopulationState> env = new MasterBenchmarkEnvironment(new NetworkInfo(NetworkUtils.getLocalAddress(), 10000,
                TCPNetworkManagerType.DEFAULT), new SEIRModelDefinitionFourRules().createModel(), new SEIRModelDefinitionThreeRules().createModel());
        env.run();

    }
}
