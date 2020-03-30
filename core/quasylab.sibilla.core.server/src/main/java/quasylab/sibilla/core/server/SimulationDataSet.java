package quasylab.sibilla.core.server;

import org.apache.commons.math3.random.RandomGenerator;
import quasylab.sibilla.core.simulator.Model;
import quasylab.sibilla.core.simulator.pm.State;
import quasylab.sibilla.core.simulator.sampling.SamplingFunction;

import java.io.Serializable;

public class SimulationDataSet<S extends State> implements Serializable {

    private static final long serialVersionUID = 1L;
    private final RandomGenerator randomGenerator;
    private final String modelReferenceName;
    private final Model<S> modelReference;
    private final S modelReferenceInitialState;
    private final SamplingFunction<S> modelReferenceSamplingFunction;
    private final int replica;
    private final double deadline;

    public SimulationDataSet(RandomGenerator random, String modelName, Model<S> model, S initialState,
                             SamplingFunction<S> sampling_function, int replica, double deadline, ServerInfo masterServerInfo) {
        this.modelReferenceName = modelName;
        this.randomGenerator = random;
        this.modelReference = model;
        this.modelReferenceInitialState = initialState;
        this.modelReferenceSamplingFunction = sampling_function;
        this.replica = replica;
        this.deadline = deadline;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(deadline);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((modelReference == null) ? 0 : modelReference.hashCode());
        result = prime * result + ((modelReferenceInitialState == null) ? 0 : modelReferenceInitialState.hashCode());
        result = prime * result + ((modelReferenceName == null) ? 0 : modelReferenceName.hashCode());
        result = prime * result
                + ((modelReferenceSamplingFunction == null) ? 0 : modelReferenceSamplingFunction.hashCode());
        result = prime * result + ((randomGenerator == null) ? 0 : randomGenerator.hashCode());
        result = prime * result + replica;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SimulationDataSet other = (SimulationDataSet) obj;
        if (Double.doubleToLongBits(deadline) != Double.doubleToLongBits(other.deadline))
            return false;
        if (modelReference == null) {
            if (other.modelReference != null)
                return false;
        } else if (!modelReference.equals(other.modelReference))
            return false;
        if (modelReferenceInitialState == null) {
            if (other.modelReferenceInitialState != null)
                return false;
        } else if (!modelReferenceInitialState.equals(other.modelReferenceInitialState))
            return false;
        if (modelReferenceName == null) {
            if (other.modelReferenceName != null)
                return false;
        } else if (!modelReferenceName.equals(other.modelReferenceName))
            return false;
        if (modelReferenceSamplingFunction == null) {
            if (other.modelReferenceSamplingFunction != null)
                return false;
        } else if (!modelReferenceSamplingFunction.equals(other.modelReferenceSamplingFunction))
            return false;
        if (randomGenerator == null) {
            if (other.randomGenerator != null)
                return false;
        } else if (!randomGenerator.equals(other.randomGenerator))
            return false;
        if (replica != other.replica)
            return false;
        return true;
    }

    public RandomGenerator getRandomGenerator() {
        return randomGenerator;
    }


    public String getModelReferenceName() {
        return modelReferenceName;
    }


    public Model<S> getModelReference() {
        return modelReference;
    }


    public S getModelReferenceInitialState() {
        return modelReferenceInitialState;
    }


    public SamplingFunction<S> getModelReferenceSamplingFunction() {
        return modelReferenceSamplingFunction;
    }


    public int getReplica() {
        return replica;
    }


    public double getDeadline() {
        return deadline;
    }


    public String toString() {
        return String.format(
                "randomGenerator hashcode: %d \n" + " randomGenerator class: %s \n" + " modelReferenceName: %s \n"
                        + " modelReference hashcode: %d \n" + " modelReference class: %s \n"
                        + " modelReferenceInitialState hashcode: %d \n" + " modelReferenceInitialState class: %s \n"
                        + " modelReferenceSamplingFunction hashcode: %d \n"
                        + " modelReferenceSamplingFunction class: %s \n" + " replica: %d \n deadline: %e \n",
                randomGenerator.hashCode(), randomGenerator.getClass().getName(), modelReferenceName,
                modelReference.hashCode(), modelReference.getClass().getName(), modelReferenceInitialState.hashCode(),
                modelReferenceInitialState.getClass().getName(), modelReferenceSamplingFunction.hashCode(),
                modelReferenceSamplingFunction.getClass().getName(), replica, deadline);
    }
}
