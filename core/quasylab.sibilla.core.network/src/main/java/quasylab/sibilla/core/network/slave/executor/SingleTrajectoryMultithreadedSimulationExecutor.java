package quasylab.sibilla.core.network.slave.executor;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.network.ComputationResult;
import quasylab.sibilla.core.network.NetworkTask;
import quasylab.sibilla.core.network.communication.TCPNetworkManager;
import quasylab.sibilla.core.network.serialization.ComputationResultSerializerType;
import quasylab.sibilla.core.simulator.SimulationTask;
import quasylab.sibilla.core.simulator.Trajectory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SingleTrajectoryMultithreadedSimulationExecutor extends SimulationExecutor {

    private final ExecutorService taskExecutor = Executors.newCachedThreadPool();

    public SingleTrajectoryMultithreadedSimulationExecutor(ExecutorType exType, ComputationResultSerializerType crSerializerType) {
        super(exType, crSerializerType);
    }

    @Override
    public void simulate(NetworkTask networkTask, TCPNetworkManager master) {
        List<? extends SimulationTask<?>> tasks = networkTask.getTasks();
        Model model = tasks.get(0).getUnit().getModel();
        CompletableFuture<?>[] futures = new CompletableFuture<?>[tasks.size()];
        LinkedList<Trajectory<?>> trajectories = new LinkedList<>();

        this.computationBenchmark.run(() -> {
            for (int i = 0; i < tasks.size(); i++) {
                futures[i] = CompletableFuture.supplyAsync(tasks.get(i), taskExecutor);
            }
            CompletableFuture.allOf(futures).join();
            for (SimulationTask<?> task : tasks) {
                Trajectory trajectory = task.getTrajectory();
                /*try {
                BytearrayToFile.toFile(TrajectorySerializer.serialize(toAdd, model), "optTrajectories", String.format("SEIR_4_Rules_Trajectory_Custom_%d", toAdd.getData().size()));
           System.out.println("Trajectory on file");
            } catch (Exception e) {
                e.printStackTrace();
            }*/
                trajectories.add(trajectory);
            }
            return List.of((double) tasks.size());
        });

        for (Trajectory singleTrajectory : trajectories) {
            sendResult(new ComputationResult(new LinkedList<>(List.of(singleTrajectory))), master, model);
        }
    }
}
