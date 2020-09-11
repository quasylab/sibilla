package quasylab.sibilla.core.network.slave.executor;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.network.ComputationResult;
import quasylab.sibilla.core.network.NetworkTask;
import quasylab.sibilla.core.network.benchmark.BenchmarkUnit;
import quasylab.sibilla.core.network.communication.TCPNetworkManager;
import quasylab.sibilla.core.network.compression.Compressor;
import quasylab.sibilla.core.network.serialization.ComputationResultSerializer;
import quasylab.sibilla.core.network.serialization.ComputationResultSerializerType;
import quasylab.sibilla.core.network.serialization.Serializer;
import quasylab.sibilla.core.network.serialization.SerializerType;

import java.io.IOException;
import java.util.List;

/**
 * Represents an executor of simulations that manages the computation and the
 * sending of simulation results to a master server
 *
 * @author Stelluti Francesco Pio
 * @author Zamponi Marco
 */
public abstract class SimulationExecutor {

    private final ExecutorType executorType;

    protected final BenchmarkUnit computationBenchmark;
    private final BenchmarkUnit sendBenchmark;

    //TODO
    private final ComputationResultSerializerType crSerializerType;


    /**
     * Creates a new SimulationExecutor, starting the BenchmarkUnit associated to it to measure performances.
     *
     * @param exType           the type of SimulationExecutor to create
     * @param crSerializerType //TODO
     */
    public SimulationExecutor(ExecutorType exType, ComputationResultSerializerType crSerializerType) {
        this.executorType = exType;
        this.crSerializerType = crSerializerType;

        this.computationBenchmark = new BenchmarkUnit("sibillaBenchmarks/slaveBenchmarking/",
                String.format("%s_computation", this.executorType), "csv", "o", List.of("comptime", "tasks"));

        this.sendBenchmark = new BenchmarkUnit("sibillaBenchmarks/slaveBenchmarking/",
                String.format("%s_resultsCompressSerializeAndSend", this.crSerializerType.toString()), "csv",
                this.crSerializerType.getLabel(),
                List.of("sertime", "trajectories", "serbytes", "comprtime", "comprbytes", "sendtime"));
    }

    /**
     * Factory method for SimulationExecutor creation. Creates a SimulationExecutor based on the type
     * passed in input.
     *
     * @param exType           the type of SimulationExecutor to create
     * @param crSerializerType // TODO
     * @return the created SimulationExecutor
     */
    public static SimulationExecutor getExecutor(ExecutorType exType,
                                                 ComputationResultSerializerType crSerializerType) {
        switch (exType) {
            case MULTITHREADED:
                return new MultithreadedSimulationExecutor(exType, crSerializerType);
            case SEQUENTIAL:
                return new SequentialSimulationExecutor(exType, crSerializerType);
            case SINGLE_TRAJECTORY_SEQUENTIAL:
                return new SingleTrajectorySequentialSimulationExecutor(exType, crSerializerType);
            case SINGLE_TRAJECTORY_MULTITHREADED:
            default:
                return new SingleTrajectoryMultithreadedSimulationExecutor(exType, crSerializerType);
        }
    }

    /**
     * Executes the simulation of the given NetworkTask and sends the results to the
     * master server.
     *
     * @param networkTask the network task to simulate
     * @param master      the NetworkManager of the master server the results will
     *                    be sent to
     */
    public abstract void simulate(NetworkTask networkTask, TCPNetworkManager master);

    /**
     * Serializes, compresses and sends the simulation results to a master server.
     *
     * @param results the ComputationResult to be sent
     * @param master  the NetworkManager of the master server the results will be sent to
     * @param model   the Model of the executed simulation
     */
    protected void sendResult(ComputationResult results, TCPNetworkManager master, Model model) {
        final var wrapper = new Object() {
            private byte[] toSend;
        };

        this.sendBenchmark.run(() -> {
            wrapper.toSend = this.serializeComputationResult(results, model);
            return List.of((double) results.getResults().size(), (double) wrapper.toSend.length);
        }, () -> {
            wrapper.toSend = Compressor.compress(wrapper.toSend);
            return List.of((double) wrapper.toSend.length);
        }, () -> {
            master.writeObject(wrapper.toSend);
            return List.of();
        });

        /*
        try {
            master.writeObject(Compressor.compress(this.serializeComputationResult(results, model)));
        } catch (IOException e) {
            e.printStackTrace();
        }
         */
    }

    //TODO
    private byte[] serializeComputationResult(ComputationResult results, Model model) throws IOException {
        switch (this.crSerializerType) {
            case FST:
                return Serializer.getSerializer(SerializerType.FST).serialize(results);
            case APACHE:
                return Serializer.getSerializer(SerializerType.APACHE).serialize(results);
            default:
            case CUSTOM:
                return ComputationResultSerializer.serialize(results, model);
        }
    }

    /**
     * Represent the type of SimulationExecutor
     */
    public enum ExecutorType {
        SEQUENTIAL, SINGLE_TRAJECTORY_SEQUENTIAL, MULTITHREADED, SINGLE_TRAJECTORY_MULTITHREADED
    }
}
