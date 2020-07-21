package quasylab.sibilla.core.network.serialization;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.past.State;
import quasylab.sibilla.core.simulator.Trajectory;
import quasylab.sibilla.core.simulator.sampling.Sample;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class TrajectorySerializer {

    //start - 8
    //end - 8
    //generationtime - 8
    //successfull - 4
    //numero di sample - 4
    //dimensione dei sample - 4
    //samples
    public static byte[] serialize(Trajectory<? extends State> t) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(ByteBuffer.allocate(8).putDouble(t.getStart()).array());
        baos.write(ByteBuffer.allocate(8).putDouble(t.getEnd()).array());
        baos.write(ByteBuffer.allocate(8).putDouble(t.getGenerationTime()).array());
        baos.write(ByteBuffer.allocate(4).putInt(t.isSuccesfull() ? 1 : 0).array());
        baos.write(ByteBuffer.allocate(4).putInt(t.getData().size()).array());
        baos.write(ByteBuffer.allocate(4).putInt(t.getData().get(0).getByteSize()).array());
        for (Sample sample : t.getData()) {
            baos.write(SampleSerializer.serialize(sample));
        }
        byte[] toReturn = baos.toByteArray();
        baos.close();
        return toReturn;
    }

    public static Trajectory deserialize(byte[] toDeserialize, Model<? extends State> model) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(toDeserialize);
        Trajectory t = new Trajectory();
        t.setStart(ByteBuffer.wrap(bais.readNBytes(8)).getDouble());
        t.setEnd(ByteBuffer.wrap(bais.readNBytes(8)).getDouble());
        t.setGenerationTime(ByteBuffer.wrap(bais.readNBytes(8)).getLong());
        t.setSuccesfull(ByteBuffer.wrap(bais.readNBytes(4)).getInt() != 0);
        int numberOfSamples = ByteBuffer.wrap(bais.readNBytes(4)).getInt();
        int sizeOfSamples = ByteBuffer.wrap(bais.readNBytes(4)).getInt();
        for (int i = 0; i < numberOfSamples; i++) {
            Sample<?> newSample = SampleSerializer.deserialize(bais.readNBytes(sizeOfSamples), model);
            t.addSample(newSample);
        }
        bais.close();
        return t;
    }

}
