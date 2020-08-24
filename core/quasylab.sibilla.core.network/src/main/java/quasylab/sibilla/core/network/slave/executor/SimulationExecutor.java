package quasylab.sibilla.core.network.slave.executor;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.network.ComputationResult;
import quasylab.sibilla.core.network.NetworkTask;
import quasylab.sibilla.core.network.communication.TCPNetworkManager;
import quasylab.sibilla.core.network.compression.Compressor;
import quasylab.sibilla.core.network.serialization.ComputationResultSerializer;
import quasylab.sibilla.core.network.benchmark.BenchmarkUnit;

import java.io.IOException;
import java.util.List;

public abstract class SimulationExecutor {

    private final Type type;
    private final BenchmarkUnit benchmark;

    public SimulationExecutor(Type exType) {
        this.type = exType;
        this.benchmark = new BenchmarkUnit("benchmarks/slave",
                String.format("slave_%s", this.type),
                "csv",
                "o",
                List.of("exectime",
                        "tasks"
                ));
    }

    public static SimulationExecutor getExecutor(Type exType) {
        switch (exType) {
            case MULTITHREADED:
                return new MultithreadedSimulationExecutor(exType);
            case SEQUENTIAL:
                return new SequentialSimulationExecutor(exType);
            case SINGLE_TRAJECTORY_SEQUENTIAL:
                return new SingleTrajectorySequentialSimulationExecutor(exType);
            case SINGLE_TRAJECTORY_MULTITHREADED:
            default:
                return new SingleTrajectoryMultithreadedSimulationExecutor(exType);
        }
    }

    public abstract void simulate(NetworkTask networkTask, TCPNetworkManager master);

    public void simulateWithBenchmark(NetworkTask networkTask, TCPNetworkManager master) {

        benchmark.run(() -> {
            simulate(networkTask, master);
            return List.of((double) networkTask.getTasks().size());
        });
    }

    protected void sendResult(ComputationResult results, TCPNetworkManager master, Model model) {
        try {
            master.writeObject(Compressor.compress(ComputationResultSerializer.serialize(results, model)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public enum Type {
        SEQUENTIAL, SINGLE_TRAJECTORY_SEQUENTIAL, MULTITHREADED, SINGLE_TRAJECTORY_MULTITHREADED
    }
}
