package quasylab.sibilla.core.network.serialization;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.models.State;
import quasylab.sibilla.core.simulator.sampling.Sample;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Utility class that handles serialization and deserialization of Samples.
 * Data is serialized and deserialized directly into byte arrays to reduce time.
 *
 * @author Stelluti Francesco Pio
 * @author Zamponi Marco
 */
public class SampleSerializer {

    /**
     * Serialize a Sample into an array of bytes.
     *
     * @param sample the sample to serialize
     * @param model  the model of the simulation
     * @param <S>    the state class
     * @return byte array of serialized result
     * @throws IOException
     */
    public static <S extends State> byte[] serialize(Sample<S> sample, Model<S> model) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serialize(baos, sample, model);
        byte[] toReturn = baos.toByteArray();
        baos.close();
        return toReturn;
    }

    /**
     * Serialize a Sample and put its results inside a ByteArrayOutputStream
     *
     * @param toSerializeInto the output stream where the serialized data will be put
     * @param sample          the sample to serialize
     * @param model           the model of the simulation
     * @param <S>             the state class
     * @throws IOException
     */
    public static <S extends State> void serialize(ByteArrayOutputStream toSerializeInto, Sample<S> sample, Model<S> model) throws IOException {
        double time = sample.getTime();
        toSerializeInto.write(ByteBuffer.allocate(8).putDouble(time).array());
        toSerializeInto.write(model.serializeState(sample.getValue()));
    }

    /**
     * Deserialize a byte array into a Sample
     *
     * @param toDeserialize the byte array that contains serialized data
     * @param model the model of the simulation
     * @param <S> the state class
     * @return the deserialized Sample
     * @throws IOException
     */
    public static <S extends State> Sample<S> deserialize(byte[] toDeserialize, Model<S> model) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(toDeserialize);
        Sample<S> sample = deserialize(bais, model);
        bais.close();
        return sample;
    }

    /**
     * Deserialize data from an InputStream into a Sample
     *
     * @param toDeserializeFrom the input stream that contains serialized data
     * @param model the model of the simulation
     * @param <S> the state class
     * @return the deserialized Sample
     * @throws IOException
     */
    public static <S extends State> Sample<S> deserialize(ByteArrayInputStream toDeserializeFrom, Model<S> model) throws IOException {
        double time = ByteBuffer.wrap(toDeserializeFrom.readNBytes(8)).getDouble();
        S state = model.deserializeState(toDeserializeFrom.readNBytes(model.stateByteArraySize()));
        return new Sample<>(time, state);
    }

}
