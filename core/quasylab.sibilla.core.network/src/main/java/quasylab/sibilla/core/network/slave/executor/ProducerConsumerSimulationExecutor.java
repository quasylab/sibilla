package quasylab.sibilla.core.network.slave.executor;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.network.ComputationResult;
import quasylab.sibilla.core.network.NetworkTask;
import quasylab.sibilla.core.network.communication.TCPNetworkManager;
import quasylab.sibilla.core.simulator.SimulationTask;
import quasylab.sibilla.core.simulator.Trajectory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ProducerConsumerSimulationExecutor extends SimulationExecutor {

    private BlockingQueue<Trajectory<?>> trajectoriesQueue;

    private volatile boolean completed;

    public ProducerConsumerSimulationExecutor(Type exType) {
        super(exType);
        this.trajectoriesQueue = new LinkedBlockingQueue<>();
    }

    @Override
    public void simulate(NetworkTask networkTask, TCPNetworkManager master) {
        completed = false;
        new Thread(() -> producerTask(networkTask)).start();
        new Thread(() -> consumerTask(master, networkTask)).start();
    }

    private void producerTask(NetworkTask<?> networkTask) {
        List<? extends SimulationTask<?>> tasks = networkTask.getTasks();
        System.out.println(tasks.size());
        for (SimulationTask<?> task : tasks) {
            trajectoriesQueue.add(task.get());
        }
        completed = true;
    }

    private void consumerTask(TCPNetworkManager master, NetworkTask<?> networkTask) {
        Model<?> model = networkTask.getTasks().get(0).getUnit().getModel();
        while (!completed) {
            LinkedList<Trajectory> trajectories = new LinkedList<>();
            trajectoriesQueue.drainTo(trajectories);
            if (!trajectories.isEmpty()) {
                sendResult(new ComputationResult(trajectories), master, model);
            }
        }
    }
}
