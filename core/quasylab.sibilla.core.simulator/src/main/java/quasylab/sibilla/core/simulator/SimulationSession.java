package quasylab.sibilla.core.simulator;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


import quasylab.sibilla.core.simulator.sampling.SamplingFunction;

public class SimulationSession<S> {
    private int expectedTasks = 0;
    private SamplingFunction<S> sampling_function;
    private BlockingQueue<SimulationTask<S>> waitingTasks = new LinkedBlockingQueue<>();
    private List<SimulationTask<S>> tasks = new LinkedList<>();

    public SimulationSession(int expectedTasks, SamplingFunction<S> sampling_function){
        this.expectedTasks = expectedTasks;
        this.sampling_function = sampling_function;
    }

    public int getExpectedTasks(){
        return expectedTasks;
    }

    public void taskCompleted(){
        expectedTasks--;
    }

    public SamplingFunction<S> getSamplingFunction(){
        return sampling_function;
    }

    public BlockingQueue<SimulationTask<S>> getQueue(){
        return waitingTasks;
    }

    public List<SimulationTask<S>> getTasks(){
        return tasks;
    }
}