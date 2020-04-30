package quasylab.sibilla.core.server;

import org.apache.commons.math3.random.RandomGenerator;
import quasylab.sibilla.core.models.MarkovProcess;
import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.models.ModelDefinition;
import quasylab.sibilla.core.past.State;
import quasylab.sibilla.core.simulator.sampling.SamplingFunction;

import java.io.Serializable;

public class SimulationDataSet<S extends State> implements Serializable {

    private static final long serialVersionUID = 1L;
    private final RandomGenerator randomGenerator;
    private final ModelDefinition<S> modelName;
    private final Model<S> model;
    private final S modelInitialState;
    private final SamplingFunction<S> modelSamplingFunction;
    private final int replica;
    private final double deadline;

    public SimulationDataSet(RandomGenerator random, ModelDefinition<S> modelName, Model<S> model, S initialState,
                             SamplingFunction<S> sampling_function, int replica, double deadline, ServerInfo masterServerInfo) {
        this.modelName = modelName;
        this.randomGenerator = random;
        this.model = model;
        this.modelInitialState = initialState;
        this.modelSamplingFunction = sampling_function;
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
        result = prime * result + ((model == null) ? 0 : model.hashCode());
        result = prime * result + ((modelInitialState == null) ? 0 : modelInitialState.hashCode());
        result = prime * result + ((modelName == null) ? 0 : modelName.hashCode());
        result = prime * result
                + ((modelSamplingFunction == null) ? 0 : modelSamplingFunction.hashCode());
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
        if (model == null) {
            if (other.model != null)
                return false;
        } else if (!model.equals(other.model))
            return false;
        if (modelInitialState == null) {
            if (other.modelInitialState != null)
                return false;
        } else if (!modelInitialState.equals(other.modelInitialState))
            return false;
        if (modelName == null) {
            if (other.modelName != null)
                return false;
        } else if (!modelName.equals(other.modelName))
            return false;
        if (modelSamplingFunction == null) {
            if (other.modelSamplingFunction != null)
                return false;
        } else if (!modelSamplingFunction.equals(other.modelSamplingFunction))
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


    public ModelDefinition<S> getModelName() {
        return modelName;
    }


    public Model<S> getModel() {
        return model;
    }


    public S getModelInitialState() {
        return modelInitialState;
    }


    public SamplingFunction<S> getModelSamplingFunction() {
        return modelSamplingFunction;
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
                randomGenerator.hashCode(), randomGenerator.getClass().getName(), modelName,
                model.hashCode(), model.getClass().getName(), modelInitialState.hashCode(),
                modelInitialState.getClass().getName(), modelSamplingFunction.hashCode(),
                modelSamplingFunction.getClass().getName(), replica, deadline);
    }
}
