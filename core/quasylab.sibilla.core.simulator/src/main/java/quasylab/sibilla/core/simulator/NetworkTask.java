package quasylab.sibilla.core.simulator;

import java.io.Serializable;

public class NetworkTask<S> implements Serializable {
    private static final long serialVersionUID = 1L;
    private SimulationTask<S> task;
    private int repetitions;

    public NetworkTask(SimulationTask<S> task, int repetitions){
        this.task = task;
        this.repetitions = repetitions;
    }

    public SimulationTask<S> getTask() {
        return task;
    }

    public int getRepetitions() {
        return repetitions;
    }

}