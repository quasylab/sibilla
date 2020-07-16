package quasylab.sibilla.core.network.serialization;

import org.apache.commons.lang3.SerializationUtils;
import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.network.ComputationResult;
import quasylab.sibilla.core.past.State;
import quasylab.sibilla.core.simulator.Trajectory;
import quasylab.sibilla.core.simulator.sampling.Sample;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

public class ComputationResultSerializer {

    public static byte[] serialize(ComputationResult<? extends State> toSerialize, Model<? extends State> model) throws IOException {
        List<? extends Trajectory<? extends State>> trajectories = toSerialize.getResults();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (Trajectory<? extends State> trajectory : trajectories) {
            baos.write(serializeTrajectory(trajectory, model));
        }
        return baos.toByteArray();
    }

    private static byte[] serializeTrajectory(Trajectory<? extends State> t, Model<? extends State> model) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(ByteBuffer.allocate(4).putInt(t.size()).array());
        baos.write(ByteBuffer.allocate(8).putDouble(t.getStart()).array());
        baos.write(ByteBuffer.allocate(8).putDouble(t.getEnd()).array());
        baos.write(ByteBuffer.allocate(8).putDouble(t.getGenerationTime()).array());
        baos.write(ByteBuffer.allocate(4).putInt(t.isSuccesfull() ? 1 : 0).array());
        for (Sample<? extends State> sample : t.getData()) {
            baos.write(serializeSample(sample, model));
        }
        return baos.toByteArray();
    }

    private static byte[] serializeSample(Sample<? extends State> sample, Model<? extends State> model) {
        return model.toByteArray(sample);
    }

    public static ComputationResult deserialize(byte[] toDeserialize, Model<? extends State> model) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(toDeserialize);
        LinkedList<Trajectory> trajectories = new LinkedList<>();
        while (bais.available() > 0) {
            int samples = ByteBuffer.wrap(bais.readNBytes(4)).getInt();
            trajectories.add(deserializeTrajectory(bais.readNBytes(samples * model.sampleByteArraySize()), model, samples));
        }
        return new ComputationResult(trajectories);
    }

    private static Trajectory deserializeTrajectory(byte[] toDeserialize, Model<? extends State> model, int samples) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(toDeserialize);
        Trajectory t = new Trajectory();
        t.setStart(ByteBuffer.wrap(bais.readNBytes(8)).getDouble());
        t.setEnd(ByteBuffer.wrap(bais.readNBytes(8)).getDouble());
        t.setGenerationTime(ByteBuffer.wrap(bais.readNBytes(8)).getLong());
        t.setSuccesfull(ByteBuffer.wrap(bais.readNBytes(4)).getInt() != 0);
        for (int i = 0; i < samples; i++) {
            t.getData().add(deserializeSample(bais.readNBytes(model.sampleByteArraySize()), model));
        }
        return t;
    }

    private static Sample deserializeSample(byte[] toDeserialize, Model<? extends State> model) {
        return model.fromByteArray(toDeserialize);
    }
}
