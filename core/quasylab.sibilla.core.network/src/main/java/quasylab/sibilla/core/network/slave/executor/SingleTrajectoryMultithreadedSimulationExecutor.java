package quasylab.sibilla.core.network.slave.executor;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.network.ComputationResult;
import quasylab.sibilla.core.network.NetworkTask;
import quasylab.sibilla.core.network.communication.TCPNetworkManager;
import quasylab.sibilla.core.simulator.SimulationTask;
import quasylab.sibilla.core.simulator.Trajectory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SingleTrajectoryMultithreadedSimulationExecutor extends SimulationExecutor {

    private final ExecutorService taskExecutor = Executors.newCachedThreadPool();

    public SingleTrajectoryMultithreadedSimulationExecutor(Type exType) {
        super(exType);
    }

    @Override
    public void simulate(NetworkTask networkTask, TCPNetworkManager master) {
        List<? extends SimulationTask<?>> tasks = networkTask.getTasks();
        Model model = tasks.get(0).getUnit().getModel();
        CompletableFuture<?>[] futures = new CompletableFuture<?>[tasks.size()];
        for (int i = 0; i < tasks.size(); i++) {
            futures[i] = CompletableFuture.supplyAsync(tasks.get(i), taskExecutor);
        }
        CompletableFuture.allOf(futures).join();
        for (SimulationTask<?> task : tasks) {
            LinkedList<Trajectory> trajectories = new LinkedList<>();
            trajectories.add(task.getTrajectory());
            sendResult(new ComputationResult(trajectories), master, model);
        }

    }
}
