package quasylab.sibilla.core.network.serialization;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.past.State;
import quasylab.sibilla.core.simulator.sampling.Sample;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class SampleSerializer {

    public static <S extends State> byte[] serialize(Sample<S> sample, Model<S> model) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serialize(baos, sample, model);
        byte[] toReturn = baos.toByteArray();
        baos.close();
        return toReturn;
    }

    public static <S extends State> void serialize(ByteArrayOutputStream toSerializeInto, Sample<S> sample, Model<S> model) throws IOException {
        double time = sample.getTime();
        toSerializeInto.write(ByteBuffer.allocate(8).putDouble(time).array());
        toSerializeInto.write(model.serializeState(sample.getValue()));
    }

    public static <S extends State> Sample<S> deserialize(byte[] toDeserialize, Model<S> model) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(toDeserialize);
        Sample<S> sample = deserialize(bais, model);
        bais.close();
        return sample;
    }

    public static <S extends State> Sample<S> deserialize(ByteArrayInputStream toDeserializeFrom, Model<S> model) throws IOException {
        double time = ByteBuffer.wrap(toDeserializeFrom.readNBytes(8)).getDouble();
        S state = model.deserializeState(toDeserializeFrom.readNBytes(model.stateByteArraySize()));
        return new Sample<>(time, state);
    }

}
