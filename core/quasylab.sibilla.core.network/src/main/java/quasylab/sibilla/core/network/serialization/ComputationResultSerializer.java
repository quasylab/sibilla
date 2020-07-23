package quasylab.sibilla.core.network.serialization;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.models.pm.PopulationModel;
import quasylab.sibilla.core.models.pm.PopulationState;
import quasylab.sibilla.core.network.ComputationResult;
import quasylab.sibilla.core.past.State;
import quasylab.sibilla.core.simulator.Trajectory;
import quasylab.sibilla.core.simulator.sampling.Sample;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;

public class ComputationResultSerializer {

    //numero di sample nella traiettoria successiva - 4
    //traiettoria
    public static byte[] serialize(ComputationResult<? extends State> toSerialize, Model<? extends State> model) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (Trajectory trajectory : toSerialize.getResults()) {
            baos.write(ByteBuffer.allocate(4).putInt(trajectory.size()).array());
            baos.write(TrajectorySerializer.serialize(trajectory, model));
        }
        byte[] toReturn = baos.toByteArray();
        baos.close();
        return toReturn;
    }

    public static ComputationResult deserialize(byte[] toDeserialize, Model<? extends State> model) throws IOException {
        //System.out.println(String.format("Computation To deserialize:%d", toDeserialize.length));
        ByteArrayInputStream bais = new ByteArrayInputStream(toDeserialize);
        //System.out.println(String.format("Number of trajectories:%d - Size of trajectories:%d", numberOfTrajectories, sizeOfTrajectories));
        LinkedList<Trajectory> trajectories = new LinkedList<>();
        while (bais.available() > 0) {
            int numberOfSamples = ByteBuffer.wrap(bais.readNBytes(4)).getInt();
            Trajectory<?> trajectory = TrajectorySerializer.deserialize(bais.readNBytes(TrajectorySerializer.getByteSize(model, numberOfSamples)), model, numberOfSamples);
            trajectories.add(trajectory);
        }
        bais.close();
        return new ComputationResult(trajectories);
    }


}
