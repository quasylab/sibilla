package quasylab.sibilla.core.simulator.server;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import quasylab.sibilla.core.simulator.Trajectory;

public class ComputationResult<S> implements Serializable {
    private static final long serialVersionUID = -545122842766553412L;
    private final LinkedList<Trajectory<S>> results;

    public ComputationResult(LinkedList<Trajectory<S>> results){
        this.results = results;
    }

    public List<Trajectory<S>> getResults() {
        return results;
    }

}