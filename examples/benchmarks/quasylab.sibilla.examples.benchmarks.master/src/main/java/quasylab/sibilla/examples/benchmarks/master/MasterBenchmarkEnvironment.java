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
import quasylab.sibilla.core.past.State;

import java.io.IOException;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class MasterBenchmarkEnvironment<S extends State> {

    private Model<S> modelFourRules;
    private Model<S> modelThreeRules;

    private NetworkInfo slaveInfo;
    private TCPNetworkManager netManager;

    private Serializer fstSerializer;
    private Serializer apacheSerializer;

    private MasterBenchmarkType benchmarkType;
    private Logger LOGGER;
    private Benchmark benchmark;
    private Benchmark sendBenchmark;

    private final int step;
    private final int threshold;
    private final int repetitions;
    private int resultsSize;
    private int sentTasksCount;

    public MasterBenchmarkEnvironment(NetworkInfo slaveInfo, Model modelFourRules, Model modelThreeRules, int step, int threshold, int repetitions, int resultsSize, MasterBenchmarkType type) throws IOException {
        this.slaveInfo = slaveInfo;

        netManager = TCPNetworkManager.createNetworkManager(this.slaveInfo);
        fstSerializer = Serializer.getSerializer(SerializerType.FST);
        apacheSerializer = Serializer.getSerializer(SerializerType.APACHE);
        LOGGER = HostLoggerSupplier.getInstance("Master Benchmark").getLogger();

        this.modelFourRules = modelFourRules;
        this.modelThreeRules = modelThreeRules;

        this.step = step;
        this.threshold = threshold;
        this.repetitions = repetitions;
        this.resultsSize = Math.max(1, resultsSize);

        this.sentTasksCount = 0;

        this.benchmarkType = type;
        LOGGER.info(String.format("STARTING MASTER %s BENCHMARK", this.benchmarkType.toString()));
        this.setBenchmark();
        this.run();
        netManager.closeConnection();
    }

    private void run() throws IOException {
        netManager.writeObject(fstSerializer.serialize("Start"));
        netManager.writeObject(fstSerializer.serialize(repetitions));
        netManager.writeObject(fstSerializer.serialize(threshold));


        for (int j = 1; j <= repetitions; j++) {
            AtomicInteger currentRepetition = new AtomicInteger(j);
            while (sentTasksCount < threshold) {
                sentTasksCount += step;

                sendBenchmark.run(() -> {
                    LOGGER.info("-----------------------------------------------");
                    LOGGER.info(String.format("[%d] Sending [%d] tasks", currentRepetition.get(), sentTasksCount));
                    netManager.writeObject(fstSerializer.serialize(sentTasksCount));
                    int currentResultSize = Math.min(resultsSize, sentTasksCount);
                    netManager.writeObject(fstSerializer.serialize(currentResultSize));
                    int receivedTrajectoriesCount = 0;
                    ComputationResult<S> results = new ComputationResult<S>(new LinkedList<>());

                    while (receivedTrajectoriesCount < this.sentTasksCount) {
                        byte[] receivedBytes = read(currentRepetition.get());
                        ComputationResult<S> receivedResults = deserializeAndDecompress(receivedBytes, currentRepetition.get());
                        results.add(receivedResults);
                        receivedTrajectoriesCount = results.getResults().size();
                        LOGGER.info(String.format("[%d] Trajectories received [%d] Total trajectories received [%d/%d]", currentRepetition.get(), receivedResults.getResults().size(), receivedTrajectoriesCount, this.sentTasksCount));

                    }

                    return List.of((double) sentTasksCount, (double) currentResultSize);
                });


                // LOGGER.info(String.format("[%d] Trajectories received: [%d]", currentRepetition.get(), results.getResults().size()));
            }
        }
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
                this.sendBenchmark = new Benchmark(
                        "benchmarks/masterBenchmarking/fourRulesModel/",
                        "fst_sendAndReceive",
                        "csv",
                        "f",
                        List.of("sendAndReceiveTime",
                                "tasks",
                                "resultsSize")
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
                this.sendBenchmark = new Benchmark(
                        "benchmarks/masterBenchmarking/threeRulesModel/",
                        "fst_sendAndReceive",
                        "csv",
                        "f",
                        List.of("sendAndReceiveTime",
                                "tasks",
                                "resultsSize")
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
                this.sendBenchmark = new Benchmark(
                        "benchmarks/masterBenchmarking/fourRulesModel/",
                        "apache_sendAndReceive",
                        "csv",
                        "a",
                        List.of("sendAndReceiveTime",
                                "tasks",
                                "resultsSize")
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
                this.sendBenchmark = new Benchmark(
                        "benchmarks/masterBenchmarking/threeRulesModel/",
                        "apache_sendAndReceive",
                        "csv",
                        "a",
                        List.of("sendAndReceiveTime",
                                "tasks",
                                "resultsSize")
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
                this.sendBenchmark = new Benchmark(
                        "benchmarks/masterBenchmarking/fourRulesModel/",
                        "optimized_sendAndReceive",
                        "csv",
                        "o",
                        List.of("sendAndReceiveTime",
                                "tasks",
                                "resultsSize")
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
                this.sendBenchmark = new Benchmark(
                        "benchmarks/masterBenchmarking/threeRulesModel/",
                        "optimized_sendAndReceive",
                        "csv",
                        "o",
                        List.of("sendAndReceiveTime",
                                "tasks",
                                "resultsSize")
                );
                break;
        }
    }

    private byte[] read(int currentRepetition) throws IOException {
        byte[] toReturn = netManager.readObject();
        LOGGER.info(String.format("[%d] %s compressed received - Bytes: %d", currentRepetition, this.benchmarkType.toString(), toReturn.length));
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
                new NetworkInfo(InetAddress.getByName("localhost"), 10000, TCPNetworkManagerType.DEFAULT),
                new SEIRModelDefinitionFourRules().createModel(),
                new SEIRModelDefinitionThreeRules().createModel(),
                20,
                900,
                1,
                1,
                getType("OPTIMIZEDFOURRULES"));
    }

    private static MasterBenchmarkType getType(String arg) {
        return MasterBenchmarkType.valueOf(arg);
    }
}
