package quasylab.sibilla.core.network.serialization;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.past.State;
import quasylab.sibilla.core.simulator.sampling.Sample;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class SampleSerializer {

    //time - 8
    //dimensione dello state - 4
    //state
    public static byte[] serialize(Sample<? extends State> sample) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(ByteBuffer.allocate(8).putDouble(sample.getTime()).array());
        baos.write(ByteBuffer.allocate(4).putInt(sample.getValue().getByteSize()).array());
        baos.write(StateSerializer.serialize(sample.getValue()));

        byte[] toReturn = baos.toByteArray();
        baos.close();
        return toReturn;
    }

    public static Sample deserialize(byte[] toDeserialize, Model<? extends State> model) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(toDeserialize);
        double time = ByteBuffer.wrap(bais.readNBytes(8)).getDouble();
        int sizeOfState = ByteBuffer.wrap(bais.readNBytes(4)).getInt();
        State newState = StateSerializer.deserialize(bais.readNBytes(sizeOfState), model);
        bais.close();
        return new Sample(time, newState);
    }

}
