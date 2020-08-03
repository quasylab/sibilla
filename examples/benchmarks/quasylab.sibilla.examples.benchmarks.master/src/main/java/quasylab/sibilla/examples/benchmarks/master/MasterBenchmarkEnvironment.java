package quasylab.sibilla.examples.benchmarks.master;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.models.pm.PopulationState;
import quasylab.sibilla.core.network.ComputationResult;
import quasylab.sibilla.core.network.HostLoggerSupplier;
import quasylab.sibilla.core.network.NetworkInfo;
import quasylab.sibilla.core.network.serialization.*;
import quasylab.sibilla.core.network.util.Benchmark;
import quasylab.sibilla.core.network.communication.TCPNetworkManager;
import quasylab.sibilla.core.network.communication.TCPNetworkManagerType;
import quasylab.sibilla.core.network.compression.Compressor;
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

    private Serializer fstSerializer;
    private Serializer apacheSerializer;

    private MasterBenchmarkType benchmarkType;
    private Logger LOGGER;
    private Benchmark benchmark;

    public MasterBenchmarkEnvironment(NetworkInfo localInfo, Model modelFourRules, Model modelThreeRules, MasterBenchmarkType type) throws IOException {
        this.localInfo = localInfo;

        serverSocket = TCPNetworkManager.createServerSocket((TCPNetworkManagerType) this.localInfo.getType(), this.localInfo.getPort());
        fstSerializer = Serializer.getSerializer(SerializerType.FST);
        apacheSerializer = Serializer.getSerializer(SerializerType.APACHE);
        LOGGER = HostLoggerSupplier.getInstance("Master Benchmark").getLogger();

        this.modelFourRules = modelFourRules;
        this.modelThreeRules = modelThreeRules;
        this.benchmarkType = type;
        LOGGER.info(String.format("STARTING MASTER %s BENCHMARK", this.benchmarkType.toString()));
        this.setBenchmark();
        this.run();
    }

    private void run() throws IOException {
        Socket socket = serverSocket.accept();
        netManager = TCPNetworkManager.createNetworkManager((TCPNetworkManagerType) localInfo.getType(), socket);

        int repetitions = (int) fstSerializer.deserialize(netManager.readObject());
        int threshold = (int) fstSerializer.deserialize(netManager.readObject());

        for (int j = 1; j <= repetitions; j++) {
            AtomicInteger currentRepetition = new AtomicInteger(j);
            int trajectoriesReceived = 0;
            while (trajectoriesReceived < threshold) {
                byte[] received = read(currentRepetition.get());
                ComputationResult<S> results = deserializeAndDecompress(received, currentRepetition.get());
                trajectoriesReceived = results.getResults().size();
            }
        }
        netManager.closeConnection();
    }

    private void setBenchmark() {
        switch (this.benchmarkType) {
            case FSTFOURRULES:
                this.benchmark = new Benchmark(
                        "benchmarks/masterBenchmarking/fourRulesModel/",
                        "fst",
                        "csv",
                        "f",
                        List.of("decomprtime",
                                "desertime",
                                "tasks")
                );
                break;
            case FSTTHREERULES:
                this.benchmark = new Benchmark(
                        "benchmarks/masterBenchmarking/threeRulesModel/",
                        "fst",
                        "csv",
                        "f",
                        List.of("decomprtime",
                                "desertime",
                                "tasks")
                );
                break;
            case APACHEFOURRULES:
                this.benchmark = new Benchmark(
                        "benchmarks/masterBenchmarking/fourRulesModel/",
                        "apache",
                        "csv",
                        "a",
                        List.of("decomprtime",
                                "desertime",
                                "tasks")
                );
                break;
            case APACHETHREERULES:
                this.benchmark = new Benchmark(
                        "benchmarks/masterBenchmarking/threeRulesModel/",
                        "apache",
                        "csv",
                        "a",
                        List.of("decomprtime",
                                "desertime",
                                "tasks")
                );
                break;
            case OPTIMIZEDFOURRULES:
                this.benchmark = new Benchmark(
                        "benchmarks/masterBenchmarking/fourRulesModel/",
                        "optimized",
                        "csv",
                        "o",
                        List.of("decomprtime",
                                "desertime",
                                "tasks")
                );
                break;
            case OPTIMIZEDTHREERULES:
                this.benchmark = new Benchmark(
                        "benchmarks/masterBenchmarking/threeRulesModel/",
                        "optimized",
                        "csv",
                        "o",
                        List.of("decomprtime",
                                "desertime",
                                "tasks")
                );
                break;
        }
    }

    private byte[] read(int currentRepetition) throws IOException {
        byte[] toReturn = new byte[0];
        switch (this.benchmarkType) {
            case FSTFOURRULES:
                toReturn = netManager.readObject();
                LOGGER.info(String.format("[%d] FST Four Rules compressed received - Bytes: %d", currentRepetition, toReturn.length));
                break;
            case FSTTHREERULES:
                toReturn = netManager.readObject();
                LOGGER.info(String.format("[%d] FST Three Rules compressed received - Bytes: %d", currentRepetition, toReturn.length));
                break;
            case APACHEFOURRULES:
                toReturn = netManager.readObject();
                LOGGER.info(String.format("[%d] Apache Four Rules compressed received - Bytes: %d", currentRepetition, toReturn.length));
                break;
            case APACHETHREERULES:
                toReturn = netManager.readObject();
                LOGGER.info(String.format("[%d] Apache Three Rules compressed received - Bytes: %d", currentRepetition, toReturn.length));
                break;
            case OPTIMIZEDFOURRULES:
                toReturn = netManager.readObject();
                LOGGER.info(String.format("[%d] Optimized Four Rules compressed received - Bytes: %d", currentRepetition, toReturn.length));
                break;
            case OPTIMIZEDTHREERULES:
                toReturn = netManager.readObject();
                LOGGER.info(String.format("[%d] Optimized Three Rules compressed received - Bytes: %d", currentRepetition, toReturn.length));
                break;
        }
        return toReturn;
    }

    private ComputationResult<S> deserializeAndDecompress(byte[] bytes, int currentRepetition) throws IOException {
        final var wrapper = new Object() {
            private byte[] received;
            private ComputationResult<S> results;
        };

        wrapper.received = bytes;

        switch (this.benchmarkType) {
            case FSTFOURRULES:
                this.benchmark.run(() -> {
                    wrapper.received = Compressor.decompress(wrapper.received);
                    LOGGER.info(String.format("[%d] FST Four Rules decompressed (serialized) - Bytes: %d", currentRepetition, wrapper.received.length));
                    return List.of();
                }, () -> {
                    wrapper.results = (ComputationResult<S>) fstSerializer.deserialize(wrapper.received);
                    LOGGER.info(String.format("[%d] FST Four Rules deserialized - Size: %d - Bytes: %d", currentRepetition, wrapper.results.getResults().size(), wrapper.received.length));
                    return List.of((double) wrapper.results.getResults().size());
                });
                break;
            case FSTTHREERULES:
                this.benchmark.run(() -> {
                    wrapper.received = Compressor.decompress(wrapper.received);
                    LOGGER.info(String.format("[%d] FST Three Rules decompressed (serialized) - Bytes: %d", currentRepetition, wrapper.received.length));
                    return List.of();
                }, () -> {
                    wrapper.results = (ComputationResult<S>) fstSerializer.deserialize(wrapper.received);
                    LOGGER.info(String.format("[%d] FST Three Rules deserialized - Size: %d - Bytes: %d", currentRepetition, wrapper.results.getResults().size(), wrapper.received.length));
                    return List.of((double) wrapper.results.getResults().size());
                });
                break;
            case APACHEFOURRULES:
                this.benchmark.run(() -> {
                            wrapper.received = Compressor.decompress(wrapper.received);
                            LOGGER.info(String.format("[%d] Apache Four Rules decompressed (serialized) - Bytes: %d", currentRepetition, wrapper.received.length));
                            return List.of();
                        }, () -> {
                            wrapper.results = (ComputationResult<S>) apacheSerializer.deserialize(wrapper.received);
                            LOGGER.info(String.format("[%d] Apache Four Rules deserialized - Size: %d - Bytes: %d", currentRepetition, wrapper.results.getResults().size(), wrapper.received.length));
                            return List.of((double) wrapper.results.getResults().size());
                        }
                );
                break;
            case APACHETHREERULES:
                this.benchmark.run(() -> {
                    wrapper.received = Compressor.decompress(wrapper.received);
                    LOGGER.info(String.format("[%d] Apache Three Rules decompressed (serialized) - Bytes: %d", currentRepetition, wrapper.received.length));
                    return List.of();
                }, () -> {
                    wrapper.results = (ComputationResult<S>) apacheSerializer.deserialize(wrapper.received);
                    LOGGER.info(String.format("[%d] Apache Three Rules deserialized - Size: %d - Bytes: %d", currentRepetition, wrapper.results.getResults().size(), wrapper.received.length));
                    return List.of((double) wrapper.results.getResults().size());
                });
                break;
            case OPTIMIZEDFOURRULES:
                this.benchmark.run(() -> {
                    wrapper.received = Compressor.decompress(wrapper.received);
                    LOGGER.info(String.format("[%d] Optimized Four Rules decompressed (serialized) - Bytes: %d", currentRepetition, wrapper.received.length));
                    return List.of();
                }, () -> {
                    wrapper.results = ComputationResultSerializer.deserialize(wrapper.received,
                            modelFourRules);
                    LOGGER.info(String.format("[%d] Optimized Four Rules deserialized - Size: %d - Bytes: %d", currentRepetition, wrapper.results.getResults().size(), wrapper.received.length));
                    return List.of((double) wrapper.results.getResults().size());
                });
                break;
            case OPTIMIZEDTHREERULES:
                this.benchmark.run(() -> {
                            wrapper.received = Compressor.decompress(wrapper.received);
                            LOGGER.info(String.format("[%d] Optimized Three Rules decompressed (serialized) - Bytes: %d", currentRepetition, wrapper.received.length));
                            return List.of();
                        }, () -> {
                            wrapper.results = ComputationResultSerializer.deserialize(wrapper.received,
                                    modelThreeRules);
                            LOGGER.info(String.format("[%d] Optimized Three Rules deserialized - Size: %d - Bytes: %d", currentRepetition, wrapper.results.getResults().size(), wrapper.received.length));
                            return List.of((double) wrapper.results.getResults().size());
                        }
                );
                break;
        }
        return wrapper.results;
    }


    public static void main(String[] args) throws IOException {
        MasterBenchmarkEnvironment<PopulationState> env = new MasterBenchmarkEnvironment(
                new NetworkInfo(NetworkUtils.getLocalAddress(), 10000, TCPNetworkManagerType.DEFAULT),
                new SEIRModelDefinitionFourRules().createModel(),
                new SEIRModelDefinitionThreeRules().createModel(),
                MasterBenchmarkType.FSTTHREERULES);
    }
}
