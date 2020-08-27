package quasylab.sibilla.core.network.benchmark.slave;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.models.State;
import quasylab.sibilla.core.network.ComputationResult;
import quasylab.sibilla.core.network.HostLoggerSupplier;
import quasylab.sibilla.core.network.NetworkInfo;
import quasylab.sibilla.core.network.benchmark.BenchmarkUnit;
import quasylab.sibilla.core.network.communication.TCPNetworkManager;
import quasylab.sibilla.core.network.communication.TCPNetworkManagerType;
import quasylab.sibilla.core.network.serialization.ComputationResultSerializerType;
import quasylab.sibilla.core.network.serialization.Serializer;
import quasylab.sibilla.core.network.serialization.SerializerType;
import quasylab.sibilla.core.network.serialization.TrajectorySerializer;
import quasylab.sibilla.core.network.util.BytearrayToFile;
import quasylab.sibilla.core.simulator.Trajectory;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public abstract class SlaveBenchmarkEnvironment<S extends State> {
    protected ComputationResultSerializerType computationResultSerializerType;
    protected BenchmarkUnit mainBenchmarkUnit;
    protected String benchmarkName;
    private Serializer serializer;
    protected Logger LOGGER;
    private ServerSocket serverSocket;
    protected TCPNetworkManager netManager;
    private int repetitions;
    private int threshold;
    private int resultsSize;
    private int currentTasksCount;
    protected Model<S> model;
    private final String trajectoryFileDir;
    private final String trajectoryFileName;

    protected SlaveBenchmarkEnvironment(String benchmarkName, String trajectoryFileDir, String trajectoryFileName,
                                        NetworkInfo localInfo, ComputationResultSerializerType computationResultSerializerType, Model<S> model) throws IOException {
        this(TCPNetworkManager.createNetworkManager((TCPNetworkManagerType) localInfo.getType(), TCPNetworkManager
                        .createServerSocket((TCPNetworkManagerType) localInfo.getType(), localInfo.getPort()).accept()),
                benchmarkName, trajectoryFileDir, trajectoryFileName, computationResultSerializerType, model);
    }

    protected SlaveBenchmarkEnvironment(TCPNetworkManager networkManager, String benchmarkName,
                                        String trajectoryFileDir, String trajectoryFileName, ComputationResultSerializerType computationResultSerializerType, Model<S> model){
        this.benchmarkName = benchmarkName;
        this.model = model;
        this.trajectoryFileDir = trajectoryFileDir;
        this.trajectoryFileName = trajectoryFileName;
        this.mainBenchmarkUnit = getMainBenchmarkUnit();
        serializer = Serializer.getSerializer(SerializerType.FST);
        this.LOGGER = HostLoggerSupplier.getInstance("Slave Benchmark").getLogger();
        this.currentTasksCount = 0;
        this.computationResultSerializerType = computationResultSerializerType;
        netManager = networkManager;
    }

    public static <S extends State> SlaveBenchmarkEnvironment getSlaveBenchmark(String benchmarkName,
                                                                                String trajectoryFileDir, String trajectoryFileName, NetworkInfo localInfo, Model<S> model,
                                                                                ComputationResultSerializerType type) throws IOException {
        switch (type) {
            case APACHE:
                return new ApacheSlaveBenchmarkEnvironment<>(benchmarkName, trajectoryFileDir, trajectoryFileName,
                        localInfo, type, model);
            case FST:
                return new FstSlaveBenchmarkEnvironment<>(benchmarkName, trajectoryFileDir, trajectoryFileName,
                        localInfo, type, model);
            case CUSTOM:
                return new OptimizedSlaveBenchmarkEnvironment<>(benchmarkName, trajectoryFileDir, trajectoryFileName,
                        localInfo, model, type);
        }
        return null;
    }

    public static <S extends State> SlaveBenchmarkEnvironment getSlaveBenchmark(TCPNetworkManager networkManager,
                                                                                String benchmarkName, String trajectoryFileDir, String trajectoryFileName, Model<S> model,
                                                                                ComputationResultSerializerType type) throws IOException {
        switch (type) {
            case APACHE:
                return new ApacheSlaveBenchmarkEnvironment<>(networkManager, benchmarkName, trajectoryFileDir,
                        trajectoryFileName, type, model);
            case FST:
                return new FstSlaveBenchmarkEnvironment<>(networkManager, benchmarkName, trajectoryFileDir,
                        trajectoryFileName, type, model);
            case CUSTOM:
                return new OptimizedSlaveBenchmarkEnvironment<>(networkManager, benchmarkName, trajectoryFileDir,
                        trajectoryFileName, model, type);
        }
        return null;
    }

    private ComputationResult getComputationResult(LinkedList<Trajectory<S>> trajectories, int resultSize)
            throws IOException {
        byte[] trajectoryBytes = BytearrayToFile.fromFile(trajectoryFileDir, trajectoryFileName);
        for (int i = 1; i <= resultSize; i++) {
            Trajectory<S> toAdd = TrajectorySerializer.deserialize(trajectoryBytes, this.model);
            // System.out.println("Samples: " + toAdd.getData().size());
            trajectories.add(toAdd);
        }
        return new ComputationResult(trajectories);
    }

    protected abstract void serializeCompressAndSend(ComputationResult<S> computationResult, int currentRepetition);

    private String getSerializerName() {
        return this.computationResultSerializerType.getFullName();
    }

    private String getMainLabel() {
        return this.computationResultSerializerType.getLabel();
    }

    private List<String> getMainBenchmarkLabels() {
        return List.of("sertime", "trajectories", "serbytes", "comprtime", "comprbytes", "sendtime");
    }

    private String getDirectory() {
        return String.format("benchmarks/slaveBenchmarking/%s/", this.benchmarkName);
    }

    private String getBenchmarkExtension() {
        return "csv";
    }

    private BenchmarkUnit getMainBenchmarkUnit() {
        return new BenchmarkUnit(this.getDirectory(),
                String.format("%s_compressSerializeAndSend", this.getSerializerName()), this.getBenchmarkExtension(),
                this.getMainLabel(), this.getMainBenchmarkLabels());
    }

    public void run() throws IOException {
        LOGGER.info(String.format("STARTING SLAVE %s BENCHMARK", this.computationResultSerializerType.toString()));

        this.repetitions = (int) serializer.deserialize(netManager.readObject());
        this.threshold = (int) serializer.deserialize(netManager.readObject());

        for (int j = 1; j <= repetitions; j++) {
            AtomicInteger currentRepetition = new AtomicInteger(j);
            while (currentTasksCount < threshold) {
                this.currentTasksCount = (int) serializer.deserialize(netManager.readObject());
                this.resultsSize = (int) serializer.deserialize(netManager.readObject());
                LOGGER.info("-----------------------------------------------");
                LOGGER.info(String.format("[%d] Received [%d] tasks. Groups of [%d] trajectories",
                        currentRepetition.get(), currentTasksCount, resultsSize));

                ComputationResult<S> computationResult = this.getComputationResult(new LinkedList<>(), resultsSize);
                for (int i = resultsSize; i <= currentTasksCount; i += resultsSize) {
                    this.serializeCompressAndSend(computationResult, currentRepetition.get());
                    LOGGER.info(String.format("[%d] Trajectories sent [%d/%d]", currentRepetition.get(), i,
                            currentTasksCount));
                }
            }
        }
        netManager.closeConnection();
    }

}
