package quasylab.sibilla.core.network.serialization;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.past.State;
import quasylab.sibilla.core.simulator.sampling.Sample;

import java.io.IOException;

public class SampleSerializer {

    public static byte[] serialize(Sample<? extends State> sample, Model<? extends State> model) throws IOException {
        return model.serializeSample(sample);
    }

    public static Sample deserialize(byte[] toDeserialize, Model<? extends State> model) throws IOException {
        return model.deserializeSample(toDeserialize);
    }

    public static int getByteSize(Model<? extends State> model) {
        return model.sampleByteArraySize();
    }
}
