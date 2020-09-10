package quasylab.sibilla.core.network.benchmark.computation;

import quasylab.sibilla.core.models.ModelDefinition;
import quasylab.sibilla.core.models.State;
import quasylab.sibilla.core.network.NetworkTask;
import quasylab.sibilla.core.simulator.SimulationTask;

import java.util.List;

public class SequentialComputationBenchmarkEnvironment<S extends State> extends ComputationBenchmarkEnvironment<S> {
    protected SequentialComputationBenchmarkEnvironment(Type type, String benchmarkName, ModelDefinition<S> modelDefinition,
                                                        int repetitions, int threshold, int step) {
        super(type, benchmarkName, modelDefinition, repetitions, threshold, step);
    }

    @Override
    public void compute(NetworkTask<S> task) {
        List<? extends SimulationTask<S>> tasks = task.getTasks();
        for (int i = 0; i < tasks.size(); i++) {
            tasks.get(i).get();
        }
    }
}
