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
    //dimensione sample - 4
    //samples
    public static byte[] serialize(Trajectory<? extends State> t) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        double start = t.getStart();
        double end = t.getEnd();
        long generationTime = t.getGenerationTime();
        int isSuccessfull = t.isSuccesfull() ? 1 : 0;
        int numberOfSamples = t.getData().size();
        int sizeOfSamples = t.getData().get(0).getByteSize();
        //  System.out.println(String.format("Start:%f - End:%f - Generation Time:%f - Is successfull:%d - Number of samples:%d - Size of samples:%d", start, end, (double) generationTime, isSuccessfull, numberOfSamples, sizeOfSamples));
        baos.write(ByteBuffer.allocate(8).putDouble(start).array());
        baos.write(ByteBuffer.allocate(8).putDouble(end).array());
        baos.write(ByteBuffer.allocate(8).putLong(generationTime).array());
        baos.write(ByteBuffer.allocate(4).putInt(isSuccessfull).array());
        baos.write(ByteBuffer.allocate(4).putInt(numberOfSamples).array());
        baos.write(ByteBuffer.allocate(4).putInt(sizeOfSamples).array());
        for (Sample sample : t.getData()) {
            baos.write(SampleSerializer.serialize(sample));
        }
        byte[] toReturn = baos.toByteArray();
        //System.out.println(String.format("Trajectory To send:%d", toReturn.length));
        baos.close();
        return toReturn;
    }

    public static Trajectory deserialize(byte[] toDeserialize, Model<? extends State> model) throws IOException {
        //  System.out.println(String.format("Trajectory To deserialize:%d", toDeserialize.length));
        ByteArrayInputStream bais = new ByteArrayInputStream(toDeserialize);
        Trajectory t = new Trajectory();
        double start = ByteBuffer.wrap(bais.readNBytes(8)).getDouble();
        double end = ByteBuffer.wrap(bais.readNBytes(8)).getDouble();
        long generationTime = ByteBuffer.wrap(bais.readNBytes(8)).getLong();
        boolean isSuccessfull = ByteBuffer.wrap(bais.readNBytes(4)).getInt() != 0;
        int numberOfSamples = ByteBuffer.wrap(bais.readNBytes(4)).getInt();
        int sizeOfSamples = ByteBuffer.wrap(bais.readNBytes(4)).getInt();
        // System.out.println(String.format("Start:%f - End:%f - Generation Time:%f - Is successfull:%d - Number of samples:%d - Size of samples:%d", start, end, (double) generationTime, isSuccessfull ? 1 : 0, numberOfSamples, sizeOfSamples));
        t.setStart(start);
        t.setEnd(end);
        t.setGenerationTime(generationTime);
        t.setSuccesfull(isSuccessfull);
        for (int i = 0; i < numberOfSamples; i++) {
            Sample<?> newSample = SampleSerializer.deserialize(bais.readNBytes(sizeOfSamples), model);
            t.addSample(newSample);
        }
        bais.close();
        return t;
    }

}
