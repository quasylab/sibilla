package quasylab.sibilla.core.network.benchmark.computation;

import quasylab.sibilla.core.models.ModelDefinition;
import quasylab.sibilla.core.network.NetworkTask;
import quasylab.sibilla.core.simulator.SimulationTask;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultithreadComputationBenchmarkEnvironment extends ComputationBenchmarkEnvironment {

    private final ExecutorService taskExecutor = Executors.newFixedThreadPool(16);

    protected MultithreadComputationBenchmarkEnvironment(String benchmarkName, ModelDefinition modelDefinition) {
        super(benchmarkName, modelDefinition);
    }

    @Override
    public void compute(NetworkTask task) {
        List<? extends SimulationTask<?>> tasks = task.getTasks();
        CompletableFuture<?>[] futures = new CompletableFuture<?>[tasks.size()];
        for (int i = 0; i < tasks.size(); i++) {
            futures[i] = CompletableFuture.supplyAsync(tasks.get(i), taskExecutor);
        }
        CompletableFuture.allOf(futures).join();
    }
}
