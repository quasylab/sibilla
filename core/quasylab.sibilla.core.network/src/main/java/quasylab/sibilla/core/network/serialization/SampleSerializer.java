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
        double time = sample.getTime();
        baos.write(ByteBuffer.allocate(8).putDouble(time).array());
        baos.write(model.serializeState(sample.getValue()));
        byte[] toReturn = baos.toByteArray();
        baos.close();
        return toReturn;
    }

    public static <S extends State> Sample<S> deserialize(byte[] toDeserialize, Model<S> model) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(toDeserialize);
        double time = ByteBuffer.wrap(bais.readNBytes(8)).getDouble();
        State state = model.deserializeState(bais.readNBytes(model.stateByteArraySize()));
        bais.close();
        return new Sample(time, state);
    }

    public static <S extends State> Sample<S> deserialize(ByteArrayInputStream toDeserializeFrom, Model<S> model) throws IOException {
        //TODO
        return null;
    }

    public static int getByteSize(Model<? extends State> model) {
        return 8 + model.stateByteArraySize();
    }
}
