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

public class SingleTrajectorySequentialSimulationExecutor extends SimulationExecutor {
    public SingleTrajectorySequentialSimulationExecutor(ExecutorType exType, ComputationResultSerializerType crSerializerType) {
        super(exType, crSerializerType);
    }

    @Override
    public void simulate(NetworkTask networkTask, TCPNetworkManager master) {
        List<? extends SimulationTask<?>> tasks = networkTask.getTasks();
        Model model = tasks.get(0).getUnit().getModel();
        LinkedList<Trajectory> trajectories = new LinkedList<>();

        this.computationBenchmark.run(() -> {
            for (int i = 0; i < tasks.size(); i++) {
                Trajectory trajectory = tasks.get(i).get();
                trajectories.add(trajectory);
            }
            return List.of((double) tasks.size());
        });

        for (Trajectory singleTrajectory : trajectories) {
            sendResult(new ComputationResult(new LinkedList<>(List.of(singleTrajectory))), master, model);
        }

    }
}
