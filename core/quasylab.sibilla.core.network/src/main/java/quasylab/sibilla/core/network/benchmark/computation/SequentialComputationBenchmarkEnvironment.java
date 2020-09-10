package quasylab.sibilla.core.network.benchmark.computation;

import quasylab.sibilla.core.models.ModelDefinition;
import quasylab.sibilla.core.network.NetworkTask;
import quasylab.sibilla.core.simulator.SimulationTask;
import quasylab.sibilla.core.simulator.Trajectory;

import java.util.List;

public class SequentialComputationBenchmarkEnvironment extends ComputationBenchmarkEnvironment {
    protected SequentialComputationBenchmarkEnvironment(String benchmarkName, ModelDefinition modelDefinition) {
        super(benchmarkName, modelDefinition);
    }

    @Override
    public void compute(NetworkTask task) {
        List<? extends SimulationTask<?>> tasks = task.getTasks();
        for (int i = 0; i < tasks.size(); i++) {
            tasks.get(i).get();
        }
    }
}
