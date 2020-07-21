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
    //dimensione delle traiettorie - 4
    //traiettorie
    public static byte[] serialize(ComputationResult<? extends State> toSerialize) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(ByteBuffer.allocate(4).putInt(toSerialize.getResults().size()).array());
        baos.write(ByteBuffer.allocate(4).putInt(toSerialize.getResults().get(0).getByteSize()).array());
        for (Trajectory trajectory : toSerialize.getResults()) {
            baos.write(TrajectorySerializer.serialize(trajectory));
        }
        byte[] toReturn = baos.toByteArray();
        baos.close();
        return toReturn;
    }

    public static ComputationResult deserialize(byte[] toDeserialize, Model<? extends State> model) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(toDeserialize);
        int numberOfTrajectories = ByteBuffer.wrap(bais.readNBytes(4)).getInt();
        int sizeOfTrajectories = ByteBuffer.wrap(bais.readNBytes(4)).getInt();
        LinkedList<Trajectory> trajectories = new LinkedList<>();
        for (int i = 0; i < numberOfTrajectories; i++) {
            Trajectory<?> newTrajectory = TrajectorySerializer.deserialize(bais.readNBytes(sizeOfTrajectories), model);
            trajectories.add(newTrajectory);
        }
        bais.close();
        return new ComputationResult(trajectories);
    }


}
