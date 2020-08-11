package quasylab.sibilla.core.network.slave.executor;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.network.ComputationResult;
import quasylab.sibilla.core.network.NetworkTask;
import quasylab.sibilla.core.network.communication.TCPNetworkManager;
import quasylab.sibilla.core.simulator.SimulationTask;
import quasylab.sibilla.core.simulator.Trajectory;

import java.util.LinkedList;
import java.util.List;

public class SequentialSimulationExecutor extends SimulationExecutor {
    @Override
    public void simulate(NetworkTask networkTask, TCPNetworkManager master) {
        List<? extends SimulationTask<?>> tasks = networkTask.getTasks();
        LinkedList<Trajectory<?>> results = new LinkedList<>();
        Model model = tasks.get(0).getUnit().getModel();
        for (int i = 0; i < tasks.size(); i++) {
            Trajectory trajectory = tasks.get(i).get();
            results.add(trajectory);
        }
        sendResult(new ComputationResult(results), master, model);
    }
}
