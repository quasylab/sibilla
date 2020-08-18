package quasylab.sibilla.core.network.serialization;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.network.ComputationResult;
import quasylab.sibilla.core.past.State;
import quasylab.sibilla.core.simulator.Trajectory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;

/**
 * Utility class that handles serialization and deserialization of ComputationResults.
 * Data is serialized and deserialized directly into byte arrays to reduce time.
 *
 * @author Stelluti Francesco Pio
 * @author Zamponi Marco
 */
public class ComputationResultSerializer {

    /**
     * Serialize a ComputationResult into an array of bytes.
     *
     * @param toSerialize the results to serialize
     * @param model       the model of the simulation
     * @param <S>         the state class
     * @return byte array of serialized result
     * @throws IOException
     */
    public static <S extends State> byte[] serialize(ComputationResult<S> toSerialize, Model<S> model) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serialize(baos, toSerialize, model);
        byte[] toReturn = baos.toByteArray();
        baos.close();
        return toReturn;
    }

    /**
     * Serialize a ComputationResult and put its results inside a ByteArrayOutputStream
     *
     * @param toSerializeInto the output stream where the serialized data will be put
     * @param toSerialize     the results to serialize
     * @param model           the model of the simulation
     * @param <S>             the state class
     * @throws IOException
     */
    public static <S extends State> void serialize(ByteArrayOutputStream toSerializeInto, ComputationResult<S> toSerialize, Model<S> model) throws IOException {
        for (Trajectory<S> trajectory : toSerialize.getResults()) {
            TrajectorySerializer.serialize(toSerializeInto, trajectory, model);
        }
    }

    /**
     * Deserialize a byte array into a ComputationResult
     *
     * @param toDeserialize the byte array that contains serialized data
     * @param model         the model of the simulation
     * @param <S>           the state class
     * @return the deserialized ComputationResult
     * @throws IOException
     */
    public static <S extends State> ComputationResult<S> deserialize(byte[] toDeserialize, Model<S> model) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(toDeserialize);
        ComputationResult<S> result = deserialize(bais, model);
        bais.close();
        return result;
    }

    /**
     * Deserialize data from an InputStream into a ComputationResult
     *
     * @param toDeserializeFrom the input stream that contains serialized data
     * @param model             the model of the simulation
     * @param <S>               the state class
     * @return the deserialized ComputationResult
     * @throws IOException
     */
    public static <S extends State> ComputationResult<S> deserialize(ByteArrayInputStream toDeserializeFrom, Model<S> model) throws IOException {
        LinkedList<Trajectory<S>> trajectories = new LinkedList<>();
        while (toDeserializeFrom.available() > 0) {
            Trajectory<S> trajectory = TrajectorySerializer.deserialize(toDeserializeFrom, model);
            trajectories.add(trajectory);
        }
        return new ComputationResult<>(trajectories);
    }


}
