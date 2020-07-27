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
        int numberOfSamples = t.size();
        double start = t.getStart();
        double end = t.getEnd();
        long generationTime = t.getGenerationTime();
        int isSuccessfull = t.isSuccesfull() ? 1 : 0;
        //  System.out.println(String.format("Start:%f - End:%f - Generation Time:%f - Is successfull:%d - Number of samples:%d - Size of samples:%d", start, end, (double) generationTime, isSuccessfull, numberOfSamples, sizeOfSamples));
        baos.write(ByteBuffer.allocate(4).putInt(numberOfSamples).array());
        baos.write(ByteBuffer.allocate(8).putDouble(start).array());
        baos.write(ByteBuffer.allocate(8).putDouble(end).array());
        baos.write(ByteBuffer.allocate(8).putLong(generationTime).array());
        baos.write(ByteBuffer.allocate(4).putInt(isSuccessfull).array());
        for (Sample<S> sample : t.getData()) {
            baos.write(SampleSerializer.serialize(sample, model));
        }
        byte[] toReturn = baos.toByteArray();
        //System.out.println(String.format("Trajectory To send:%d", toReturn.length));
        baos.close();
        return toReturn;
    }

    public static <S extends State> Trajectory<S> deserialize(byte[] toDeserialize, Model<S> model, int numberOfSamples) throws IOException {
        //  System.out.println(String.format("Trajectory To deserialize:%d", toDeserialize.length));
        ByteArrayInputStream bais = new ByteArrayInputStream(toDeserialize);
        Trajectory<S> t = new Trajectory<S>();

        double start = ByteBuffer.wrap(bais.readNBytes(8)).getDouble();
        double end = ByteBuffer.wrap(bais.readNBytes(8)).getDouble();
        long generationTime = ByteBuffer.wrap(bais.readNBytes(8)).getLong();
        boolean isSuccessfull = ByteBuffer.wrap(bais.readNBytes(4)).getInt() != 0;

        // System.out.println(String.format("Start:%f - End:%f - Generation Time:%f - Is successfull:%d - Number of samples:%d - Size of samples:%d", start, end, (double) generationTime, isSuccessfull ? 1 : 0, numberOfSamples, sizeOfSamples));
        t.setStart(start);
        t.setEnd(end);
        t.setGenerationTime(generationTime);
        t.setSuccesfull(isSuccessfull);

        for (int i = 0; i < numberOfSamples; i++) {
            Sample<S> newSample = SampleSerializer.deserialize(bais.readNBytes(SampleSerializer.getByteSize(model)), model);
            t.addSample(newSample);
        }
        bais.close();
        return t;
    }

    public static <S extends State> Trajectory<S> deserialize(ByteArrayInputStream toDeserializeFrom, Model<S> model) throws IOException {
        //TODO
        return null;
    }


    public static int getByteSize(Model<? extends State> model, int numberOfSamples) {
        return 8 + 8 + 8 + 4 + (numberOfSamples * SampleSerializer.getByteSize(model));
    }

}
