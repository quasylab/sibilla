package quasylab.sibilla.core.network.slave.executor;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.network.ComputationResult;
import quasylab.sibilla.core.network.NetworkTask;
import quasylab.sibilla.core.network.benchmark.BenchmarkUnit;
import quasylab.sibilla.core.network.communication.TCPNetworkManager;
import quasylab.sibilla.core.network.compression.Compressor;
import quasylab.sibilla.core.network.serialization.ComputationResultSerializer;

import java.io.IOException;
import java.util.List;


/**
 * Represents an executor of simulations that manages the computation and
 * the sending of simulation results to a master server
 *
 * @author Stelluti Francesco Pio
 * @author Zamponi Marco
 */
public abstract class SimulationExecutor {

    /**
     * The BenchmarkUnit associated to the SimulationExecutor to measure performances.
     */
    private final BenchmarkUnit benchmark;

    /**
     * Creates a new SimulationExecutor, starting the BenchmarkUnit associated to it to measure performances.
     *
     * @param exType the type of SimulationExecutor to create
     */
    public SimulationExecutor(Type exType) {
        this.benchmark = new BenchmarkUnit("benchmarks/slave",
                String.format("slave_%s", exType),
                "csv",
                "o",
                List.of("exectime",
                        "tasks"
                ));
    }

    /**
     * Factory method for SimulationExecutor creation. Creates a SimulationExecutor based on the type
     * passed in input.
     *
     * @param exType the type of SimulationExecutor to create
     * @return the created SimulationExecutor
     */
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

    /**
     * Executes the simulation of the given NetworkTask and sends the results to the master server.
     *
     * @param networkTask the network task to simulate
     * @param master      the NetworkManager of the master server the results will be sent to
     */
    public abstract void simulate(NetworkTask networkTask, TCPNetworkManager master);

    /**
     * Executes the simulation of the given NetworkTask and sends the results to the master server.
     * Meanwhile measures the performances of the code
     *
     * @param networkTask the network task to simulate
     * @param master      the NetworkManager of the master server the results will be sent to
     */
    public void simulateWithBenchmark(NetworkTask networkTask, TCPNetworkManager master) {

        benchmark.run(() -> {
            simulate(networkTask, master);
            return List.of((double) networkTask.getTasks().size());
        });
    }

    /**
     * Serializes, compresses and sends the simulation results to a master server.
     *
     * @param results the ComputationResult to be sent
     * @param master  the NetworkManager of the master server the results will be sent to
     * @param model   the Model of the executed simulation
     */
    protected void sendResult(ComputationResult results, TCPNetworkManager master, Model model) {
        try {
            master.writeObject(Compressor.compress(ComputationResultSerializer.serialize(results, model)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Represent the type of SimulationExecutor
     */
    public enum Type {
        SEQUENTIAL, SINGLE_TRAJECTORY_SEQUENTIAL, MULTITHREADED, SINGLE_TRAJECTORY_MULTITHREADED
    }
}
