package quasylab.sibilla.core.network.benchmark.computation;

import quasylab.sibilla.core.models.ModelDefinition;
import quasylab.sibilla.core.models.State;
import quasylab.sibilla.core.network.HostLoggerSupplier;
import quasylab.sibilla.core.network.NetworkTask;
import quasylab.sibilla.core.network.benchmark.BenchmarkUnit;
import quasylab.sibilla.core.simulator.DefaultRandomGenerator;
import quasylab.sibilla.core.simulator.SimulationTask;
import quasylab.sibilla.core.simulator.SimulationUnit;
import quasylab.sibilla.core.simulator.sampling.SamplePredicate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public abstract class ComputationBenchmarkEnvironment<S extends State> {
    private static final double DEADLINE = 600;
    protected Type type;
    protected BenchmarkUnit mainBenchmarkUnit;
    protected String benchmarkName;
    protected ModelDefinition<S> model;
    protected Logger LOGGER;
    private int repetitions = 1;
    private int threshold = 900;
    private int step = 20;
    private int tasksCount;

    protected ComputationBenchmarkEnvironment(Type type, String benchmarkName, ModelDefinition<S> modelDefinition,
                                              int repetitions, int threshold, int step) {
        this.type = type;
        this.benchmarkName = benchmarkName;
        this.mainBenchmarkUnit = getMainBenchmarkUnit();
        this.model = modelDefinition;
        this.repetitions = repetitions;
        this.threshold = threshold;
        this.step = step;
        tasksCount = 0;
        LOGGER = HostLoggerSupplier.getInstance("Computation Benchmark").getLogger();
    }

    public static <S extends State> ComputationBenchmarkEnvironment<S> getComputationEnvironment(Type type, String benchmarkName, ModelDefinition<S> model, int repetitions, int threshold, int step) {
        switch (type) {
            case MULTITHREAD:
                return new MultithreadComputationBenchmarkEnvironment<>(type, benchmarkName, model, repetitions, threshold, step);
            case SEQUENTIAL:
            default:
                return new SequentialComputationBenchmarkEnvironment<>(type, benchmarkName, model, repetitions, threshold, step);
        }
    }

    private String getMainLabel() {
        return type.label;
    }

    private List<String> getMainBenchmarkLabels() {
        return List.of("computation_time", "tasks");
    }

    private String getDirectory() {
        return String.format("benchmarks/computationBenchmarking/%s/", this.benchmarkName);
    }

    private String getBenchmarkExtension() {
        return "csv";
    }

    private BenchmarkUnit getMainBenchmarkUnit() {
        return new BenchmarkUnit(this.getDirectory(),
                String.format("%s_compute", this.type.fullName),
                this.getBenchmarkExtension(),
                this.getMainLabel(),
                this.getMainBenchmarkLabels());
    }

    public abstract void compute(NetworkTask<S> task);

    public void run() {
        LOGGER.info(String.format("STARTING %s COMPUTATION BENCHMARK", getMainLabel()));

        for (int j = 1; j <= repetitions; j++) {
            AtomicInteger currentRepetition = new AtomicInteger(j);
            while (tasksCount < threshold) {
                tasksCount += step;

                SimulationUnit<S> unit = new SimulationUnit<S>(model.createModel(), model.state(),
                        SamplePredicate.timeDeadlinePredicate(DEADLINE));

                List<SimulationTask<S>> tasks = new ArrayList<>();
                for (int i = 0; i < tasksCount; i++) {
                    tasks.add(new SimulationTask<>(i, new DefaultRandomGenerator(), unit));
                }
                NetworkTask<S> task = new NetworkTask<>(tasks);

                mainBenchmarkUnit.run(() -> {
                    LOGGER.info("-----------------------------------------------");
                    LOGGER.info(String.format("[%d] Computing [%d] tasks", currentRepetition.get(), tasksCount));
                    compute(task);
                    LOGGER.info(String.format("[%d] Trajectories computed [%d]", currentRepetition.get(), tasksCount));
                    return List.of((double) tasksCount);
                });
            }
        }
    }

    public enum Type {
        SEQUENTIAL("s", "Sequential"), MULTITHREAD("m", "Multithread");

        private final String label;
        private final String fullName;

        Type(String label, String fullName) {
            this.label = label;
            this.fullName = fullName;
        }

        public String getLabel() {
            return label;
        }
    }
}
