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

    //numero di traiettorie - 4

    //dimensione traiettoria successiva - 4
    //traiettoria successiva
    public static byte[] serialize(ComputationResult<? extends State> toSerialize) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int numberOfTrajectories = toSerialize.getResults().size();
        //System.out.println(String.format("Number of trajectories:%d - Size of trajectories:%d", numberOfTrajectories, sizeOfTrajectories));
        baos.write(ByteBuffer.allocate(4).putInt(numberOfTrajectories).array());
        for (Trajectory trajectory : toSerialize.getResults()) {
            int nextTrajectorySize = trajectory.getByteSize();
            baos.write(ByteBuffer.allocate(4).putInt(nextTrajectorySize).array());
            baos.write(TrajectorySerializer.serialize(trajectory));
        }
        byte[] toReturn = baos.toByteArray();
        // System.out.println(String.format("Computation To send:%d", toReturn.length));
        baos.close();
        return toReturn;
    }

    public static ComputationResult deserialize(byte[] toDeserialize, Model<? extends State> model) throws IOException {
        //System.out.println(String.format("Computation To deserialize:%d", toDeserialize.length));
        ByteArrayInputStream bais = new ByteArrayInputStream(toDeserialize);
        int numberOfTrajectories = ByteBuffer.wrap(bais.readNBytes(4)).getInt();
        //System.out.println(String.format("Number of trajectories:%d - Size of trajectories:%d", numberOfTrajectories, sizeOfTrajectories));
        LinkedList<Trajectory> trajectories = new LinkedList<>();
        for (int i = 0; i < numberOfTrajectories; i++) {
            int sizeOfNextTrajectory = ByteBuffer.wrap(bais.readNBytes(4)).getInt();
            Trajectory<?> newTrajectory = TrajectorySerializer.deserialize(bais.readNBytes(sizeOfNextTrajectory), model);
            trajectories.add(newTrajectory);
        }
        bais.close();
        return new ComputationResult(trajectories);
    }


}
