package quasylab.sibilla.core.network.serialization;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.past.State;
import quasylab.sibilla.core.simulator.sampling.Sample;

import java.io.IOException;

public class SampleSerializer {

    public static <S extends State> byte[] serialize(Sample<S> sample, Model<S> model) throws IOException {
        return model.serializeSample(sample);
    }

    public static <S extends State> Sample<S> deserialize(byte[] toDeserialize, Model<S> model) throws IOException {
        return model.deserializeSample(toDeserialize);
    }

    public static int getByteSize(Model<? extends State> model) {
        return model.sampleByteArraySize();
    }
}
