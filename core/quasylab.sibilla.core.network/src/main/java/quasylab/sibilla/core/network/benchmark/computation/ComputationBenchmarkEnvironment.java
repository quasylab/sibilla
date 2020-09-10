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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public abstract class ComputationBenchmarkEnvironment<S extends State> {
    private static final double DEADLINE = 600;
    protected BenchmarkUnit mainBenchmarkUnit;
    protected String benchmarkName;
    protected ModelDefinition<S> model;
    protected Logger LOGGER;
    private int repetitions = 1;
    private int threshold = 900;
    private int step = 20;
    private int tasksCount;

    protected ComputationBenchmarkEnvironment(String benchmarkName, ModelDefinition<S> modelDefinition) {
        this.benchmarkName = benchmarkName;
        this.mainBenchmarkUnit = getMainBenchmarkUnit();
        this.model = modelDefinition;
        tasksCount = 0;
        LOGGER = HostLoggerSupplier.getInstance("Computation Benchmark").getLogger();
    }

    public static ComputationBenchmarkEnvironment getComputationEnvironment(ModelDefinition model) {
        return new MultithreadComputationBenchmarkEnvironment("s", model);
    }

    private String getMainLabel() {
        return "sas";
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
                "%s_compute", this.getBenchmarkExtension(),
                this.getMainLabel(), this.getMainBenchmarkLabels());
    }

    public abstract void compute(NetworkTask task);

    public void run() {
        LOGGER.info(String.format("STARTING COMPUTATION %s BENCHMARK", getMainLabel()));


        for (int j = 1; j <= repetitions; j++) {
            while (tasksCount < threshold) {
                tasksCount += step;

                SimulationUnit<S> unit = new SimulationUnit<S>(model.createModel(), model.state(), SamplePredicate.timeDeadlinePredicate(DEADLINE));

                List<SimulationTask<S>> tasks = new ArrayList<>();
                for (int i = 0; i < tasksCount; i++) {
                    tasks.add(new SimulationTask<>(i, new DefaultRandomGenerator(), unit));
                }
                NetworkTask<S> task = new NetworkTask<>(tasks);

                mainBenchmarkUnit.run(() -> {
                    LOGGER.info(String.format("Computating %d tasks", tasksCount));
                    compute(task);
                    return List.of((double) tasksCount);
                });
            }
        }
    }

}
