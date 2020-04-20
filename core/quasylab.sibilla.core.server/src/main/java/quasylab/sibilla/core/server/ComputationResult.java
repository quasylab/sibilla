package quasylab.sibilla.core.server;

import quasylab.sibilla.core.simulator.Trajectory;
import quasylab.sibilla.core.past.State;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class ComputationResult<S extends State> implements Serializable {
    private static final long serialVersionUID = -545122842766553412L;
    private final LinkedList<Trajectory<S>> results;

    public ComputationResult(LinkedList<Trajectory<S>> results) {
        this.results = results;
    }

    public List<Trajectory<S>> getResults() {
        return results;
    }

}