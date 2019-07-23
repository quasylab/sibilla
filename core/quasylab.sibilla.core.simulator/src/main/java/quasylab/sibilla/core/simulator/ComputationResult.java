package quasylab.sibilla.core.simulator;

import java.io.Serializable;

public class ComputationResult<S> implements Serializable {
    private static final long serialVersionUID = -545122842766553412L;
    private final Trajectory<S> trajectory;
    private final long elapsedTime;

    public ComputationResult(Trajectory<S> trajectory, long elapsedTime){
        this.trajectory = trajectory;
        this.elapsedTime = elapsedTime;
    }

    public Trajectory<S> getTrajectory() {
        return trajectory;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }
    
}