package quasylab.sibilla.core.simulator;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class ComputationResult<S> implements Serializable {
    private static final long serialVersionUID = -545122842766553412L;
    private final LinkedList<Trajectory<S>> results;
    private final int reachCount;

    public ComputationResult(LinkedList<Trajectory<S>> results, int reachCount){
        this.results = results;
        this.reachCount = reachCount;
    }

    public List<Trajectory<S>> getResults() {
        return results;
    }

    public int getReach(){
        return reachCount;
    }
    
}