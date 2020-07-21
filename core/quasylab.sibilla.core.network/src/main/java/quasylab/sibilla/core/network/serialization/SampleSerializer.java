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
        double time = sample.getTime();
        int sizeOfState = sample.getValue().getByteSize();
        //  System.out.println(String.format("Time:%f - Size of state:%d", time, sizeOfState));
        baos.write(ByteBuffer.allocate(8).putDouble(time).array());
        baos.write(ByteBuffer.allocate(4).putInt(sizeOfState).array());
        baos.write(StateSerializer.serialize(sample.getValue()));

        byte[] toReturn = baos.toByteArray();
        //  System.out.println(String.format("Sample To send:%d", toReturn.length));
        baos.close();
        return toReturn;
    }

    public static Sample deserialize(byte[] toDeserialize, Model<? extends State> model) throws IOException {
        // System.out.println(String.format("Sample To deserialize:%d", toDeserialize.length));
        ByteArrayInputStream bais = new ByteArrayInputStream(toDeserialize);
        double time = ByteBuffer.wrap(bais.readNBytes(8)).getDouble();
        int sizeOfState = ByteBuffer.wrap(bais.readNBytes(4)).getInt();
        //System.out.println(String.format("Time:%f - Size of state:%d", time, sizeOfState));
        State newState = StateSerializer.deserialize(bais.readNBytes(sizeOfState), model);
        bais.close();
        return new Sample(time, newState);
    }

}
