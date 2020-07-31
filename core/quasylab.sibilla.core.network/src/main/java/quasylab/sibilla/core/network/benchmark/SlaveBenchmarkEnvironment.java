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
import quasylab.sibilla.core.network.util.BytearrayToFile;
import quasylab.sibilla.core.past.State;
import quasylab.sibilla.core.simulator.Trajectory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;
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
    private final String fourRulesTrajectoryFileName = "4 rules trajectory FST";
    private final String threeRulesTrajectoryFileName = "3 rules trajectory FST";

    private final int step = 30;
    private final int threshold = 900;

    private Benchmark benchmarkSerializationApacheFourRules = new Benchmark("benchmarks/slaveBenchmarking", "Four Rules Model Slave Benchmarking - Serialization - Apache", "csv");
    private Benchmark benchmarkCompressionApacheFourRules = new Benchmark("benchmarks/slaveBenchmarking", "Four Rules Model Slave Benchmarking - Compression - Apache", "csv");
    private Benchmark benchmarkSendApacheFourRules = new Benchmark("benchmarks/slaveBenchmarking", "Four Rules Model Slave Benchmarking - Send - Apache", "csv");

    private Benchmark benchmarkSerializationFSTFourRules = new Benchmark("benchmarks/slaveBenchmarking", "Four Rules Model Slave Benchmarking - Serialization - FST", "csv");
    private Benchmark benchmarkCompressionFSTFourRules = new Benchmark("benchmarks/slaveBenchmarking", "Four Rules Model Slave Benchmarking - Compression - FST", "csv");
    private Benchmark benchmarkSendFSTFourRules = new Benchmark("benchmarks/slaveBenchmarking", "Four Rules Model Slave Benchmarking - Send - FST", "csv");

    private Benchmark benchmarkSerializationOptimizedFourRules = new Benchmark("benchmarks/slaveBenchmarking", "Four Rules Model Slave Benchmarking - Serialization - Optimized", "csv");
    private Benchmark benchmarkCompressionOptimizedFourRules = new Benchmark("benchmarks/slaveBenchmarking", "Four Rules Model Slave Benchmarking - Compression - Optimized", "csv");
    private Benchmark benchmarkSendOptimizedFourRules = new Benchmark("benchmarks/slaveBenchmarking", "Four Rules Model Slave Benchmarking - Send - Optimized", "csv");

    private Benchmark benchmarkSerializationApacheThreeRules = new Benchmark("benchmarks/slaveBenchmarking", "Three Rules Model Slave Benchmarking - Serialization - Apache", "csv");
    private Benchmark benchmarkCompressionApacheThreeRules = new Benchmark("benchmarks/slaveBenchmarking", "Three Rules Model Slave Benchmarking - Compression - Apache", "csv");
    private Benchmark benchmarkSendApacheThreeRules = new Benchmark("benchmarks/slaveBenchmarking", "Three Rules Model Slave Benchmarking - Send - Apache", "csv");

    private Benchmark benchmarkSerializationFSTThreeRules = new Benchmark("benchmarks/slaveBenchmarking", "Three Rules Model Slave Benchmarking - Serialization - FST", "csv");
    private Benchmark benchmarkCompressionFSTThreeRules = new Benchmark("benchmarks/slaveBenchmarking", "Three Rules Model Slave Benchmarking - Compression - FST", "csv");
    private Benchmark benchmarkSendFSTThreeRules = new Benchmark("benchmarks/slaveBenchmarking", "Three Rules Model Slave Benchmarking - Send - FST", "csv");

    private Benchmark benchmarkSerializationOptimizedThreeRules = new Benchmark("benchmarks/slaveBenchmarking", "Three Rules Model Slave Benchmarking - Serialization - Optimized", "csv");
    private Benchmark benchmarkCompressionOptimizedThreeRules = new Benchmark("benchmarks/slaveBenchmarking", "Three Rules Model Slave Benchmarking - Compression - Optimized", "csv");
    private Benchmark benchmarkSendOptimizedThreeRules = new Benchmark("benchmarks/slaveBenchmarking", "Three Rules Model Slave Benchmarking - Send - Optimized", "csv");


    public SlaveBenchmarkEnvironment(Model modelFourRules, Model modelThreeRules) throws IOException {
        masterInfo = new NetworkInfo(InetAddress.getByName(""), 10000,
                TCPNetworkManagerType.DEFAULT);
        netManager = TCPNetworkManager.createNetworkManager(masterInfo);
        apacheSerializer = Serializer.getSerializer(SerializerType.APACHE);
        fstSerializer = Serializer.getSerializer(SerializerType.FST);
        LOGGER = HostLoggerSupplier.getInstance("Slave Benchmark").getLogger();

        this.modelFourRules = modelFourRules;
        this.modelThreeRules = modelThreeRules;
    }

    public void run() throws IOException {
        byte[] fourRulesTrajectoryBytes = BytearrayToFile.fromFile(fourRulesTrajectoryFileDir, fourRulesTrajectoryFileName);
        byte[] threeRulesTrajectoryBytes = BytearrayToFile.fromFile(threeRulesTrajectoryFileDir, threeRulesTrajectoryFileName);

        LinkedList<Trajectory<S>> fourRulesTrajectories = new LinkedList<>();
        LinkedList<Trajectory<S>> threeRulesTrajectories = new LinkedList<>();


        netManager.writeObject(fstSerializer.serialize(threshold));

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

            //Benchmark di serializzazione
            benchmarkSerializationApacheFourRules.run(() -> {
                fourRulesWrapper.toSendApache = apacheSerializer.serialize(fourRulesComputationResult);
                LOGGER.info(String.format("Apache Serialization Four Rules - Size: %d - Bytes: %d", fourRulesComputationResult.getResults().size(), fourRulesWrapper.toSendApache.length));
                return List.of((double) fourRulesComputationResult.getResults().size(), (double) fourRulesWrapper.toSendApache.length);
            });

            benchmarkSerializationFSTFourRules.run(() -> {
                fourRulesWrapper.toSendFst = fstSerializer.serialize(fourRulesComputationResult);
                LOGGER.info(String.format("FST Serialization Four Rules - Size: %d - Bytes: %d", fourRulesComputationResult.getResults().size(), fourRulesWrapper.toSendFst.length));
                return List.of((double) fourRulesComputationResult.getResults().size(), (double) fourRulesWrapper.toSendFst.length);
            });

            benchmarkSerializationOptimizedFourRules.run(() -> {
                fourRulesWrapper.toSendOptimized = ComputationResultSerializer.serialize(fourRulesComputationResult, modelFourRules);
                LOGGER.info(String.format("Optimized Serialization Four Rules - Size: %d - Bytes: %d", fourRulesComputationResult.getResults().size(), fourRulesWrapper.toSendOptimized.length));
                return List.of((double) fourRulesComputationResult.getResults().size(), (double) fourRulesWrapper.toSendOptimized.length);
            });

            benchmarkSerializationApacheThreeRules.run(() -> {
                threeRulesWrapper.toSendApache = apacheSerializer.serialize(threeRulesComputationResult);
                LOGGER.info(String.format("Apache Serialization three Rules - Size: %d - Bytes: %d", threeRulesComputationResult.getResults().size(), threeRulesWrapper.toSendApache.length));
                return List.of((double) threeRulesComputationResult.getResults().size(), (double) threeRulesWrapper.toSendApache.length);
            });

            benchmarkSerializationFSTThreeRules.run(() -> {
                threeRulesWrapper.toSendFst = fstSerializer.serialize(threeRulesComputationResult);
                LOGGER.info(String.format("FST Serialization three Rules - Size: %d - Bytes: %d", threeRulesComputationResult.getResults().size(), threeRulesWrapper.toSendFst.length));
                return List.of((double) threeRulesComputationResult.getResults().size(), (double) threeRulesWrapper.toSendFst.length);
            });

            benchmarkSerializationOptimizedThreeRules.run(() -> {
                threeRulesWrapper.toSendOptimized = ComputationResultSerializer.serialize(threeRulesComputationResult, modelThreeRules);
                LOGGER.info(String.format("Optimized Serialization three Rules - Size: %d - Bytes: %d", threeRulesComputationResult.getResults().size(), threeRulesWrapper.toSendOptimized.length));
                return List.of((double) threeRulesComputationResult.getResults().size(), (double) threeRulesWrapper.toSendOptimized.length);
            });

            //Benchmark di compressione
            benchmarkCompressionApacheFourRules.run(() -> {
                fourRulesWrapper.toSendApache = Compressor.compress(fourRulesWrapper.toSendApache);
                LOGGER.info(String.format("Apache Compression Four Rules - Size: %d - Bytes: %d", fourRulesComputationResult.getResults().size(), fourRulesWrapper.toSendApache.length));
                return List.of((double) fourRulesComputationResult.getResults().size(), (double) fourRulesWrapper.toSendApache.length);
            });

            benchmarkCompressionFSTFourRules.run(() -> {
                fourRulesWrapper.toSendFst = Compressor.compress(fourRulesWrapper.toSendFst);
                LOGGER.info(String.format("FST Compression Four Rules - Size: %d - Bytes: %d", fourRulesComputationResult.getResults().size(), fourRulesWrapper.toSendFst.length));
                return List.of((double) fourRulesComputationResult.getResults().size(), (double) fourRulesWrapper.toSendFst.length);
            });

            benchmarkCompressionOptimizedFourRules.run(() -> {
                fourRulesWrapper.toSendOptimized = Compressor.compress(fourRulesWrapper.toSendOptimized);
                LOGGER.info(String.format("Optimized Compression Four Rules - Size: %d - Bytes: %d", fourRulesComputationResult.getResults().size(), fourRulesWrapper.toSendOptimized.length));
                return List.of((double) fourRulesComputationResult.getResults().size(), (double) fourRulesWrapper.toSendOptimized.length);
            });

            benchmarkCompressionApacheThreeRules.run(() -> {
                threeRulesWrapper.toSendApache = Compressor.compress(threeRulesWrapper.toSendApache);
                LOGGER.info(String.format("Apache Compression three Rules - Size: %d - Bytes: %d", threeRulesComputationResult.getResults().size(), threeRulesWrapper.toSendApache.length));
                return List.of((double) threeRulesComputationResult.getResults().size(), (double) threeRulesWrapper.toSendApache.length);
            });

            benchmarkCompressionFSTThreeRules.run(() -> {
                threeRulesWrapper.toSendFst = Compressor.compress(threeRulesWrapper.toSendFst);
                LOGGER.info(String.format("FST Compression three Rules - Size: %d - Bytes: %d", threeRulesComputationResult.getResults().size(), threeRulesWrapper.toSendFst.length));
                return List.of((double) threeRulesComputationResult.getResults().size(), (double) threeRulesWrapper.toSendFst.length);
            });

            benchmarkCompressionOptimizedThreeRules.run(() -> {
                threeRulesWrapper.toSendOptimized = Compressor.compress(threeRulesWrapper.toSendOptimized);
                LOGGER.info(String.format("Optimized Compression three Rules - Size: %d - Bytes: %d", threeRulesComputationResult.getResults().size(), threeRulesWrapper.toSendOptimized.length));
                return List.of((double) threeRulesComputationResult.getResults().size(), (double) threeRulesWrapper.toSendOptimized.length);
            });

            //Benchmark di invio
            benchmarkSendApacheFourRules.run(() -> {
                netManager.writeObject(fourRulesWrapper.toSendApache);
                LOGGER.info(String.format("Apache Four Rules sent - Size: %d - Bytes: %d", fourRulesComputationResult.getResults().size(), fourRulesWrapper.toSendApache.length));
                return List.of((double) fourRulesComputationResult.getResults().size());
            });

            benchmarkSendFSTFourRules.run(() -> {
                netManager.writeObject(fourRulesWrapper.toSendFst);
                LOGGER.info(String.format("FST Four Rules Sent - Size: %d - Bytes: %d", fourRulesComputationResult.getResults().size(), fourRulesWrapper.toSendFst.length));
                return List.of((double) fourRulesComputationResult.getResults().size());
            });

            benchmarkSendOptimizedFourRules.run(() -> {
                netManager.writeObject(fourRulesWrapper.toSendOptimized);
                LOGGER.info(String.format("Optimized Four Rules Sent - Size: %d - Bytes: %d", fourRulesComputationResult.getResults().size(), fourRulesWrapper.toSendOptimized.length));
                return List.of((double) fourRulesComputationResult.getResults().size());
            });

            benchmarkSendApacheThreeRules.run(() -> {
                netManager.writeObject(threeRulesWrapper.toSendApache);
                LOGGER.info(String.format("Apache Three Rules sent - Size: %d - Bytes: %d", threeRulesComputationResult.getResults().size(), threeRulesWrapper.toSendApache.length));
                return List.of((double) threeRulesComputationResult.getResults().size());
            });

            benchmarkSendFSTThreeRules.run(() -> {
                netManager.writeObject(threeRulesWrapper.toSendFst);
                LOGGER.info(String.format("FST three Rules sent - Size: %d - Bytes: %d", threeRulesComputationResult.getResults().size(), threeRulesWrapper.toSendFst.length));
                return List.of((double) threeRulesComputationResult.getResults().size());
            });

            benchmarkSendOptimizedThreeRules.run(() -> {
                netManager.writeObject(threeRulesWrapper.toSendOptimized);
                LOGGER.info(String.format("Optimized three Rules sent - Size: %d - Bytes: %d", threeRulesComputationResult.getResults().size(), threeRulesWrapper.toSendOptimized.length));
                return List.of((double) threeRulesComputationResult.getResults().size());
            });

        }

        netManager.closeConnection();
    }


    public static void main(String[] args) throws IOException {
        SlaveBenchmarkEnvironment<PopulationState> env = new SlaveBenchmarkEnvironment(new SEIRModelDefinitionFourRules().createModel(), new SEIRModelDefinitionThreeRules().createModel());
        env.run();
    }
}
