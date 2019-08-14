package quasylab.sibilla.core.simulator.manager;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import quasylab.sibilla.core.simulator.SimulationTask;
import quasylab.sibilla.core.simulator.sampling.SamplingFunction;

public class SimulationSession<S> {
    private int expectedTasks = 0;
    private int reachCount = 0;
    private SamplingFunction<S> sampling_function;
    private BlockingQueue<SimulationTask<S>> waitingTasks = new LinkedBlockingQueue<>();
    private List<Long> elapsedTimes = new LinkedList<>();

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

    public void incrementReach(int value){
        reachCount += value;
    }

    public void incrementReach(){
        incrementReach(1);
    }

    public int getReach(){
        return reachCount;
    }

    public SamplingFunction<S> getSamplingFunction(){
        return sampling_function;
    }

    public BlockingQueue<SimulationTask<S>> getQueue(){
        return waitingTasks;
    }

    public List<Long> getTimeList(){
        return elapsedTimes;
    }
}