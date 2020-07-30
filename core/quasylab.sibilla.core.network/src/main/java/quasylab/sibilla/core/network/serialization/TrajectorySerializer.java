package quasylab.sibilla.core.network.serialization;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.network.ComputationResult;
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
    //samples
    public static <S extends State> byte[] serialize(Trajectory<S> t, Model<S> model) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serialize(baos, t, model);
        byte[] toReturn = baos.toByteArray();
        //System.out.println(String.format("Trajectory To send:%d", toReturn.length));
        baos.close();
        return toReturn;
    }

    public static <S extends State> void serialize(ByteArrayOutputStream toSerializeInto, Trajectory<S> t, Model<S> model) throws IOException {
        int numberOfSamples = t.size();
        double start = t.getStart();
        double end = t.getEnd();
        long generationTime = t.getGenerationTime();
        int isSuccessfull = t.isSuccesfull() ? 1 : 0;
        //  System.out.println(String.format("Start:%f - End:%f - Generation Time:%f - Is successfull:%d - Number of samples:%d - Size of samples:%d", start, end, (double) generationTime, isSuccessfull, numberOfSamples, sizeOfSamples));
        toSerializeInto.write(ByteBuffer.allocate(4).putInt(numberOfSamples).array());
        toSerializeInto.write(ByteBuffer.allocate(8).putDouble(start).array());
        toSerializeInto.write(ByteBuffer.allocate(8).putDouble(end).array());
        toSerializeInto.write(ByteBuffer.allocate(8).putLong(generationTime).array());
        toSerializeInto.write(ByteBuffer.allocate(4).putInt(isSuccessfull).array());
        for (Sample<S> sample : t.getData()) {
            SampleSerializer.serialize(toSerializeInto, sample, model);
        }
    }

    public static <S extends State> Trajectory<S> deserialize(byte[] toDeserialize, Model<S> model) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(toDeserialize);
        Trajectory<S> t = deserialize(bais, model);
        bais.close();
        return t;
    }

    public static <S extends State> Trajectory<S> deserialize(ByteArrayInputStream toDeserializeFrom, Model<S> model) throws IOException {
        Trajectory<S> t = new Trajectory<S>();

        int numberOfSamples = ByteBuffer.wrap(toDeserializeFrom.readNBytes(4)).getInt();
        double start = ByteBuffer.wrap(toDeserializeFrom.readNBytes(8)).getDouble();
        double end = ByteBuffer.wrap(toDeserializeFrom.readNBytes(8)).getDouble();
        long generationTime = ByteBuffer.wrap(toDeserializeFrom.readNBytes(8)).getLong();
        boolean isSuccessfull = ByteBuffer.wrap(toDeserializeFrom.readNBytes(4)).getInt() != 0;

        t.setStart(start);
        t.setEnd(end);
        t.setGenerationTime(generationTime);
        t.setSuccesfull(isSuccessfull);

        for (int i = 0; i < numberOfSamples; i++) {
            Sample<S> newSample = SampleSerializer.deserialize(toDeserializeFrom, model);
            t.addSample(newSample);
        }
        return t;
    }


}
