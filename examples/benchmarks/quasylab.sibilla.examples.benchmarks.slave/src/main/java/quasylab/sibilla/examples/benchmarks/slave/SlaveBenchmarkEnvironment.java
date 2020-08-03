package quasylab.sibilla.examples.benchmarks.slave;


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
import quasylab.sibilla.core.network.util.BytearrayToFile;
import quasylab.sibilla.core.past.State;
import quasylab.sibilla.core.simulator.Trajectory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class SlaveBenchmarkEnvironment<S extends State> {

    private Model<S> modelFourRules;
    private Model<S> modelThreeRules;

    private NetworkInfo masterInfo;
    private TCPNetworkManager netManager;

    private Serializer apacheSerializer;
    private Serializer fstSerializer;
    private Logger LOGGER;

    private final String fourRulesTrajectoryFileDir = "src/main/resources";
    private final String threeRulesTrajectoryFileDir = "src/main/resources";
    private final String fourRulesTrajectoryFileName = "SEIR 4 rules trajectory FST";
    private final String threeRulesTrajectoryFileName = "SEIR 3 rules trajectory FST";

    private final int step;
    private final int threshold;
    private final int repetitions;

    private SlaveBenchmarkType benchmarkType;
    private Benchmark benchmark;

    public SlaveBenchmarkEnvironment(NetworkInfo masterInfo, Model modelFourRules, Model modelThreeRules, int step, int threshold, int repetitions, SlaveBenchmarkType type) throws IOException {
        this.masterInfo = masterInfo;
        netManager = TCPNetworkManager.createNetworkManager(this.masterInfo);
        apacheSerializer = Serializer.getSerializer(SerializerType.APACHE);
        fstSerializer = Serializer.getSerializer(SerializerType.FST);
        LOGGER = HostLoggerSupplier.getInstance("Slave Benchmark").getLogger();

        this.modelFourRules = modelFourRules;
        this.modelThreeRules = modelThreeRules;
        this.step = step;
        this.threshold = threshold;
        this.repetitions = repetitions;
        this.benchmarkType = type;
        LOGGER.info(String.format("STARTING SLAVE %s BENCHMARK", this.benchmarkType.toString()));

        this.setBenchmark();
        this.run();
        netManager.closeConnection();
    }

    private void setBenchmark() {
        switch (this.benchmarkType) {
            case FSTFOURRULES:
                this.benchmark = new Benchmark(
                        "benchmarks/slaveBenchmarking/fourRulesModel/",
                        "fst",
                        "csv",
                        "f",
                        List.of("sertime",
                                "tasks",
                                "serbytes",
                                "comprtime",
                                "comprbytes",
                                "sendtime")
                );
                break;
            case FSTTHREERULES:
                this.benchmark = new Benchmark(
                        "benchmarks/slaveBenchmarking/threeRulesModel/",
                        "fst",
                        "csv",
                        "f",
                        List.of(
                                "sertime",
                                "tasks",
                                "serbytes",
                                "comprtime",
                                "comprbytes",
                                "sendtime")
                );
                break;
            case APACHEFOURRULES:
                this.benchmark = new Benchmark(
                        "benchmarks/slaveBenchmarking/fourRulesModel/",
                        "apache",
                        "csv",
                        "a",
                        List.of("sertime",
                                "tasks",
                                "serbytes",
                                "comprtime",
                                "comprbytes",
                                "sendtime")
                );
                break;
            case APACHETHREERULES:
                this.benchmark = new Benchmark(
                        "benchmarks/slaveBenchmarking/threeRulesModel/",
                        "apache",
                        "csv",
                        "a",
                        List.of("sertime",
                                "tasks",
                                "serbytes",
                                "comprtime",
                                "comprbytes",
                                "sendtime")
                );
                break;
            case OPTIMIZEDFOURRULES:
                this.benchmark = new Benchmark(
                        "benchmarks/slaveBenchmarking/fourRulesModel/",
                        "optimized",
                        "csv",
                        "o",
                        List.of("sertime",
                                "tasks",
                                "serbytes",
                                "comprtime",
                                "comprbytes",
                                "sendtime")
                );
                break;
            case OPTIMIZEDTHREERULES:
                this.benchmark = new Benchmark(
                        "benchmarks/slaveBenchmarking/threeRulesModel/",
                        "optimized",
                        "csv",
                        "o",
                        List.of(
                                "sertime",
                                "tasks",
                                "serbytes",
                                "comprtime",
                                "comprbytes",
                                "sendtime")
                );
                break;
        }
    }

    private void run() throws IOException {
        netManager.writeObject(fstSerializer.serialize(repetitions));
        netManager.writeObject(fstSerializer.serialize(threshold));

        for (int j = 1; j <= repetitions; j++) {
            AtomicInteger currentRepetition = new AtomicInteger(j);
            LinkedList<Trajectory<S>> trajectories = new LinkedList<>();
            while (trajectories.size() < threshold) {
                ComputationResult<S> computationResult = getComputationResult(trajectories, step);
                this.serializeCompressAndSend(computationResult, currentRepetition.get());
            }
        }

    }

    private ComputationResult getComputationResult(LinkedList<Trajectory<S>> trajectories, int step) throws IOException {
        byte[] trajectoryBytes = new byte[0];
        switch (benchmarkType) {
            case OPTIMIZEDTHREERULES:
            case APACHETHREERULES:
            case FSTTHREERULES:
                trajectoryBytes = BytearrayToFile.fromFile(threeRulesTrajectoryFileDir, threeRulesTrajectoryFileName);
                break;
            case OPTIMIZEDFOURRULES:
            case APACHEFOURRULES:
            case FSTFOURRULES:
                trajectoryBytes = BytearrayToFile.fromFile(fourRulesTrajectoryFileDir, fourRulesTrajectoryFileName);
                break;
        }
        for (int i = 1; i <= step; i++) {
            trajectories.add((Trajectory<S>) fstSerializer.deserialize(trajectoryBytes));
        }
        return new ComputationResult(trajectories);
    }

    private void serializeCompressAndSend(ComputationResult<S> computationResult, int currentRepetition) {
        final var wrapper = new Object() {
            private byte[] toSend;
        };

        switch (this.benchmarkType) {
            case FSTFOURRULES:
                this.benchmark.run(() -> {
                            wrapper.toSend = fstSerializer.serialize(computationResult);
                            LOGGER.info(String.format("[%d] FST Serialization Four Rules - Size: %d - Bytes: %d", currentRepetition, computationResult.getResults().size(), wrapper.toSend.length));
                            return List.of((double) computationResult.getResults().size(), (double) wrapper.toSend.length);
                        },
                        () -> {
                            wrapper.toSend = Compressor.compress(wrapper.toSend);
                            LOGGER.info(String.format("[%d] FST Compression Four Rules - Size: %d - Bytes: %d", currentRepetition, computationResult.getResults().size(), wrapper.toSend.length));
                            return List.of((double) wrapper.toSend.length);
                        },
                        () -> {
                            netManager.writeObject(wrapper.toSend);
                            LOGGER.info(String.format("[%d] FST Four Rules Sent - Size: %d - Bytes: %d", currentRepetition, computationResult.getResults().size(), wrapper.toSend.length));
                            return List.of();
                        });
                break;
            case FSTTHREERULES:
                this.benchmark.run(() -> {
                            wrapper.toSend = fstSerializer.serialize(computationResult);
                            LOGGER.info(String.format("[%d] FST Serialization three Rules - Size: %d - Bytes: %d", currentRepetition, computationResult.getResults().size(), wrapper.toSend.length));
                            return List.of((double) computationResult.getResults().size(), (double) wrapper.toSend.length);
                        },
                        () -> {
                            wrapper.toSend = Compressor.compress(wrapper.toSend);
                            LOGGER.info(String.format("[%d] FST Compression three Rules - Size: %d - Bytes: %d", currentRepetition, computationResult.getResults().size(), wrapper.toSend.length));
                            return List.of((double) wrapper.toSend.length);
                        },
                        () -> {
                            netManager.writeObject(wrapper.toSend);
                            LOGGER.info(String.format("[%d] FST three Rules sent - Size: %d - Bytes: %d", currentRepetition, computationResult.getResults().size(), wrapper.toSend.length));
                            return List.of();
                        });
                break;
            case APACHEFOURRULES:
                this.benchmark.run(() -> {
                            wrapper.toSend = apacheSerializer.serialize(computationResult);
                            LOGGER.info(String.format("[%d] Apache Serialization Four Rules - Size: %d - Bytes: %d", currentRepetition, computationResult.getResults().size(), wrapper.toSend.length));
                            return List.of((double) computationResult.getResults().size(), (double) wrapper.toSend.length);
                        },
                        () -> {
                            wrapper.toSend = Compressor.compress(wrapper.toSend);
                            LOGGER.info(String.format("[%d] Apache Compression Four Rules - Size: %d - Bytes: %d", currentRepetition, computationResult.getResults().size(), wrapper.toSend.length));
                            return List.of((double) wrapper.toSend.length);
                        },
                        () -> {
                            netManager.writeObject(wrapper.toSend);
                            LOGGER.info(String.format("[%d] Apache Four Rules sent - Size: %d - Bytes: %d", currentRepetition, computationResult.getResults().size(), wrapper.toSend.length));
                            return List.of();
                        });
                break;
            case APACHETHREERULES:
                this.benchmark.run(() -> {
                            wrapper.toSend = apacheSerializer.serialize(computationResult);
                            LOGGER.info(String.format("[%d] Apache Serialization three Rules - Size: %d - Bytes: %d", currentRepetition, computationResult.getResults().size(), wrapper.toSend.length));
                            return List.of((double) computationResult.getResults().size(), (double) wrapper.toSend.length);
                        },
                        () -> {
                            wrapper.toSend = Compressor.compress(wrapper.toSend);
                            LOGGER.info(String.format("[%d] Apache Compression three Rules - Size: %d - Bytes: %d", currentRepetition, computationResult.getResults().size(), wrapper.toSend.length));
                            return List.of((double) wrapper.toSend.length);
                        },
                        () -> {
                            netManager.writeObject(wrapper.toSend);
                            LOGGER.info(String.format("[%d] Apache Three Rules sent - Size: %d - Bytes: %d", currentRepetition, computationResult.getResults().size(), wrapper.toSend.length));
                            return List.of();
                        });
                break;
            case OPTIMIZEDFOURRULES:
                this.benchmark.run(() -> {
                            wrapper.toSend = ComputationResultSerializer.serialize(computationResult, modelFourRules);
                            LOGGER.info(String.format("[%d] Optimized Serialization Four Rules - Size: %d - Bytes: %d", currentRepetition, computationResult.getResults().size(), wrapper.toSend.length));
                            return List.of((double) computationResult.getResults().size(), (double) wrapper.toSend.length);
                        },
                        () -> {
                            wrapper.toSend = Compressor.compress(wrapper.toSend);
                            LOGGER.info(String.format("[%d] Optimized Compression Four Rules - Size: %d - Bytes: %d", currentRepetition, computationResult.getResults().size(), wrapper.toSend.length));
                            return List.of((double) wrapper.toSend.length);
                        },
                        () -> {
                            netManager.writeObject(wrapper.toSend);
                            LOGGER.info(String.format("[%d] Optimized Four Rules Sent - Size: %d - Bytes: %d", currentRepetition, computationResult.getResults().size(), wrapper.toSend.length));
                            return List.of();
                        });
                break;
            case OPTIMIZEDTHREERULES:
                this.benchmark.run(() -> {
                            wrapper.toSend = ComputationResultSerializer.serialize(computationResult, modelThreeRules);
                            LOGGER.info(String.format("[%d] Optimized Serialization three Rules - Size: %d - Bytes: %d", currentRepetition, computationResult.getResults().size(), wrapper.toSend.length));
                            return List.of((double) computationResult.getResults().size(), (double) wrapper.toSend.length);
                        },
                        () -> {
                            wrapper.toSend = Compressor.compress(wrapper.toSend);
                            LOGGER.info(String.format("[%d] Optimized Compression three Rules - Size: %d - Bytes: %d", currentRepetition, computationResult.getResults().size(), wrapper.toSend.length));
                            return List.of((double) wrapper.toSend.length);
                        },
                        () -> {
                            netManager.writeObject(wrapper.toSend);
                            LOGGER.info(String.format("[%d] Optimized three Rules sent - Size: %d - Bytes: %d", currentRepetition, computationResult.getResults().size(), wrapper.toSend.length));
                            return List.of();
                        });
                break;
        }
    }

    public static void main(String[] args) throws IOException {
        SlaveBenchmarkEnvironment<PopulationState> env = new SlaveBenchmarkEnvironment(
                new NetworkInfo(InetAddress.getByName("192.168.42.202"), 10000, TCPNetworkManagerType.DEFAULT),
                new SEIRModelDefinitionFourRules().createModel(),
                new SEIRModelDefinitionThreeRules().createModel(),
                30,
                900,
                1,
                getType(args[0]));
        env.run();
    }

    private static SlaveBenchmarkType getType(String arg) {
        return SlaveBenchmarkType.valueOf(arg);
    }
}
