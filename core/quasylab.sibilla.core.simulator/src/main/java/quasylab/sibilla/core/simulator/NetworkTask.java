package quasylab.sibilla.core.simulator;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class NetworkTask<S> implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<SimulationTask<S>> tasks;

    public NetworkTask(List<SimulationTask<S>> tasks){
        this.tasks = tasks;
    }

    public NetworkTask(SimulationTask<S> task){
        this(new LinkedList<>(Arrays.asList(task)));
    }

    public List<SimulationTask<S>> getTasks() {
        return tasks;
    }

}