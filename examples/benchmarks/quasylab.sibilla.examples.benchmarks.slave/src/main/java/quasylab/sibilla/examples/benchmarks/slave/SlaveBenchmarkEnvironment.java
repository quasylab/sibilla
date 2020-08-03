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

    private final String fourRulesTrajectoryFileDir = "trajectories";
    private final String threeRulesTrajectoryFileDir = "trajectories";
    private final String fourRulesTrajectoryFileName = "SEIR 4 rules trajectory FST";
    private final String threeRulesTrajectoryFileName = "SEIR 3 rules trajectory FST";

    private final int step;
    private final int threshold;
    private final int repetitions;

    private Benchmark benchmarkApacheFourRules = new Benchmark(
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

    private Benchmark benchmarkFSTFourRules = new Benchmark(
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

    private Benchmark benchmarkOptimizedFourRules = new Benchmark(
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

    private Benchmark benchmarkApacheThreeRules = new Benchmark(
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

    private Benchmark benchmarkFSTThreeRules = new Benchmark(
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

    private Benchmark benchmarkOptimizedThreeRules = new Benchmark(
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

    public SlaveBenchmarkEnvironment(NetworkInfo masterInfo, Model modelFourRules, Model modelThreeRules, int step, int threshold, int repetitions) throws IOException {
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
    }

    public void run() throws IOException {
        byte[] fourRulesTrajectoryBytes = BytearrayToFile.fromFile(fourRulesTrajectoryFileDir, fourRulesTrajectoryFileName);
        byte[] threeRulesTrajectoryBytes = BytearrayToFile.fromFile(threeRulesTrajectoryFileDir, threeRulesTrajectoryFileName);

        LinkedList<Trajectory<S>> fourRulesTrajectories;
        LinkedList<Trajectory<S>> threeRulesTrajectories;
        netManager.writeObject(fstSerializer.serialize(repetitions));
        netManager.writeObject(fstSerializer.serialize(threshold));

        for (int j = 1; j <= repetitions; j++) {

            AtomicInteger currentRepetition = new AtomicInteger(j);

            fourRulesTrajectories = new LinkedList<>();
            threeRulesTrajectories = new LinkedList<>();

            while (fourRulesTrajectories.size() < threshold && threeRulesTrajectories.size() < threshold) {
                for (int i = 1; i <= step; i++) {
                    fourRulesTrajectories.add((Trajectory<S>) fstSerializer.deserialize(fourRulesTrajectoryBytes));
                    threeRulesTrajectories.add((Trajectory<S>) fstSerializer.deserialize(threeRulesTrajectoryBytes));
                }
                ComputationResult<S> fourRulesComputationResult = new ComputationResult(fourRulesTrajectories);
                ComputationResult<S> threeRulesComputationResult = new ComputationResult(threeRulesTrajectories);

                final var fourRulesWrapper = new Object() {
                    private byte[] toSendApache;
                    private byte[] toSendFst;
                    private byte[] toSendOptimized;
                };

                final var threeRulesWrapper = new Object() {
                    private byte[] toSendApache;
                    private byte[] toSendFst;
                    private byte[] toSendOptimized;
                };

                benchmarkApacheFourRules.run(() -> {
                            fourRulesWrapper.toSendApache = apacheSerializer.serialize(fourRulesComputationResult);
                            LOGGER.info(String.format("[%d] Apache Serialization Four Rules - Size: %d - Bytes: %d", currentRepetition.get(), fourRulesComputationResult.getResults().size(), fourRulesWrapper.toSendApache.length));
                            return List.of((double) fourRulesComputationResult.getResults().size(), (double) fourRulesWrapper.toSendApache.length);
                        },
                        () -> {
                            fourRulesWrapper.toSendApache = Compressor.compress(fourRulesWrapper.toSendApache);
                            LOGGER.info(String.format("[%d] Apache Compression Four Rules - Size: %d - Bytes: %d", currentRepetition.get(), fourRulesComputationResult.getResults().size(), fourRulesWrapper.toSendApache.length));
                            return List.of((double) fourRulesWrapper.toSendApache.length);
                        },
                        () -> {
                            netManager.writeObject(fourRulesWrapper.toSendApache);
                            LOGGER.info(String.format("[%d] Apache Four Rules sent - Size: %d - Bytes: %d", currentRepetition.get(), fourRulesComputationResult.getResults().size(), fourRulesWrapper.toSendApache.length));
                            return List.of();
                        });

                benchmarkFSTFourRules.run(
                        () -> {
                            fourRulesWrapper.toSendFst = fstSerializer.serialize(fourRulesComputationResult);
                            LOGGER.info(String.format("[%d] FST Serialization Four Rules - Size: %d - Bytes: %d", currentRepetition.get(), fourRulesComputationResult.getResults().size(), fourRulesWrapper.toSendFst.length));
                            return List.of((double) fourRulesComputationResult.getResults().size(), (double) fourRulesWrapper.toSendFst.length);
                        },
                        () -> {
                            fourRulesWrapper.toSendFst = Compressor.compress(fourRulesWrapper.toSendFst);
                            LOGGER.info(String.format("[%d] FST Compression Four Rules - Size: %d - Bytes: %d", currentRepetition.get(), fourRulesComputationResult.getResults().size(), fourRulesWrapper.toSendFst.length));
                            return List.of((double) fourRulesWrapper.toSendFst.length);
                        },
                        () -> {
                            netManager.writeObject(fourRulesWrapper.toSendFst);
                            LOGGER.info(String.format("[%d] FST Four Rules Sent - Size: %d - Bytes: %d", currentRepetition.get(), fourRulesComputationResult.getResults().size(), fourRulesWrapper.toSendFst.length));
                            return List.of();
                        }
                );

                benchmarkOptimizedFourRules.run(
                        () -> {
                            fourRulesWrapper.toSendOptimized = ComputationResultSerializer.serialize(fourRulesComputationResult, modelFourRules);
                            LOGGER.info(String.format("[%d] Optimized Serialization Four Rules - Size: %d - Bytes: %d", currentRepetition.get(), fourRulesComputationResult.getResults().size(), fourRulesWrapper.toSendOptimized.length));
                            return List.of((double) fourRulesWrapper.toSendOptimized.length);
                        },
                        () -> {
                            fourRulesWrapper.toSendOptimized = Compressor.compress(fourRulesWrapper.toSendOptimized);
                            LOGGER.info(String.format("[%d] Optimized Compression Four Rules - Size: %d - Bytes: %d", currentRepetition.get(), fourRulesComputationResult.getResults().size(), fourRulesWrapper.toSendOptimized.length));
                            return List.of((double) fourRulesWrapper.toSendOptimized.length);
                        },
                        () -> {
                            netManager.writeObject(fourRulesWrapper.toSendOptimized);
                            LOGGER.info(String.format("[%d] Optimized Four Rules Sent - Size: %d - Bytes: %d", currentRepetition.get(), fourRulesComputationResult.getResults().size(), fourRulesWrapper.toSendOptimized.length));
                            return List.of();
                        }
                );

                benchmarkApacheThreeRules.run(() -> {
                            threeRulesWrapper.toSendApache = apacheSerializer.serialize(threeRulesComputationResult);
                            LOGGER.info(String.format("[%d] Apache Serialization three Rules - Size: %d - Bytes: %d", currentRepetition.get(), threeRulesComputationResult.getResults().size(), threeRulesWrapper.toSendApache.length));
                            return List.of((double) threeRulesComputationResult.getResults().size(), (double) threeRulesWrapper.toSendApache.length);
                        },
                        () -> {
                            threeRulesWrapper.toSendApache = Compressor.compress(threeRulesWrapper.toSendApache);
                            LOGGER.info(String.format("[%d] Apache Compression three Rules - Size: %d - Bytes: %d", currentRepetition.get(), threeRulesComputationResult.getResults().size(), threeRulesWrapper.toSendApache.length));
                            return List.of((double) threeRulesWrapper.toSendApache.length);
                        },
                        () -> {
                            netManager.writeObject(threeRulesWrapper.toSendApache);
                            LOGGER.info(String.format("[%d] Apache Three Rules sent - Size: %d - Bytes: %d", currentRepetition.get(), threeRulesComputationResult.getResults().size(), threeRulesWrapper.toSendApache.length));
                            return List.of();
                        });

                benchmarkFSTThreeRules.run(() -> {
                            threeRulesWrapper.toSendFst = fstSerializer.serialize(threeRulesComputationResult);
                            LOGGER.info(String.format("[%d] FST Serialization three Rules - Size: %d - Bytes: %d", currentRepetition.get(), threeRulesComputationResult.getResults().size(), threeRulesWrapper.toSendFst.length));
                            return List.of((double) threeRulesWrapper.toSendFst.length);
                        },
                        () -> {
                            threeRulesWrapper.toSendFst = Compressor.compress(threeRulesWrapper.toSendFst);
                            LOGGER.info(String.format("[%d] FST Compression three Rules - Size: %d - Bytes: %d", currentRepetition.get(), threeRulesComputationResult.getResults().size(), threeRulesWrapper.toSendFst.length));
                            return List.of((double) threeRulesWrapper.toSendFst.length);
                        },
                        () -> {
                            netManager.writeObject(threeRulesWrapper.toSendFst);
                            LOGGER.info(String.format("[%d] FST three Rules sent - Size: %d - Bytes: %d", currentRepetition.get(), threeRulesComputationResult.getResults().size(), threeRulesWrapper.toSendFst.length));
                            return List.of();
                        });

                benchmarkOptimizedThreeRules.run(
                        () -> {
                            threeRulesWrapper.toSendOptimized = ComputationResultSerializer.serialize(threeRulesComputationResult, modelThreeRules);
                            LOGGER.info(String.format("[%d] Optimized Serialization three Rules - Size: %d - Bytes: %d", currentRepetition.get(), threeRulesComputationResult.getResults().size(), threeRulesWrapper.toSendOptimized.length));
                            return List.of((double) threeRulesComputationResult.getResults().size(), (double) threeRulesWrapper.toSendOptimized.length);
                        },
                        () -> {
                            threeRulesWrapper.toSendOptimized = Compressor.compress(threeRulesWrapper.toSendOptimized);
                            LOGGER.info(String.format("[%d] Optimized Compression three Rules - Size: %d - Bytes: %d", currentRepetition.get(), threeRulesComputationResult.getResults().size(), threeRulesWrapper.toSendOptimized.length));
                            return List.of((double) threeRulesWrapper.toSendOptimized.length);
                        },
                        () -> {
                            netManager.writeObject(threeRulesWrapper.toSendOptimized);
                            LOGGER.info(String.format("[%d] Optimized three Rules sent - Size: %d - Bytes: %d", currentRepetition.get(), threeRulesComputationResult.getResults().size(), threeRulesWrapper.toSendOptimized.length));
                            return List.of();
                        }
                );

            }
        }

        netManager.closeConnection();
    }


    public static void main(String[] args) throws IOException {
        SlaveBenchmarkEnvironment<PopulationState> env = new SlaveBenchmarkEnvironment(new NetworkInfo(InetAddress.getByName(""), 10000,
                TCPNetworkManagerType.DEFAULT), new SEIRModelDefinitionFourRules().createModel(), new SEIRModelDefinitionThreeRules().createModel(), 30, 900, 10);
        env.run();
    }
}
