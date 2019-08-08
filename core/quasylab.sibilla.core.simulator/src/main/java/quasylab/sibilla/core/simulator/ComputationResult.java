package quasylab.sibilla.core.simulator;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class ComputationResult<S> implements Serializable {
    private static final long serialVersionUID = -545122842766553412L;
    private final LinkedList<Trajectory<S>> results;
    private final long elapsedTime;

    public ComputationResult(LinkedList<Trajectory<S>> results, long elapsedTime){
        this.results = results;
        this.elapsedTime = elapsedTime;
    }

    public List<Trajectory<S>> getResults() {
        return results;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }
    
}