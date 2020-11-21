package quasylab.sibilla.core.network.benchmark.computation;

import quasylab.sibilla.core.models.ModelDefinition;
import quasylab.sibilla.core.models.State;
import quasylab.sibilla.core.network.NetworkTask;
import quasylab.sibilla.core.simulator.SimulationTask;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultithreadComputationBenchmarkEnvironment<S extends State> extends ComputationBenchmarkEnvironment<S> {

    private final ExecutorService taskExecutor = Executors.newFixedThreadPool(16);

    protected MultithreadComputationBenchmarkEnvironment(Type type, String benchmarkName, ModelDefinition<S> modelDefinition,
                                                         int repetitions, int threshold, int step) {
        super(type, benchmarkName, modelDefinition, repetitions, threshold, step);
    }

    @Override
    public void compute(NetworkTask<S> task) {
        List<? extends SimulationTask<?>> tasks = task.getTasks();
        CompletableFuture<?>[] futures = new CompletableFuture<?>[tasks.size()];
        for (int i = 0; i < tasks.size(); i++) {
            futures[i] = CompletableFuture.supplyAsync(tasks.get(i), taskExecutor);
        }
        CompletableFuture.allOf(futures).join();
    }
}
