package quasylab.sibilla.examples.benchmarks.seirslave;

import quasylab.sibilla.core.models.pm.PopulationState;
import quasylab.sibilla.core.network.ComputationResult;
import quasylab.sibilla.core.network.compression.Compressor;
import quasylab.sibilla.core.network.serialization.ComputationResultSerializer;
import quasylab.sibilla.core.network.serialization.Serializer;
import quasylab.sibilla.core.network.serialization.SerializerType;
import quasylab.sibilla.core.network.util.BytearrayToFile;
import quasylab.sibilla.core.past.State;
import quasylab.sibilla.core.simulator.Trajectory;
import quasylab.sibilla.core.simulator.sampling.Sample;

import java.util.LinkedList;
import java.util.List;

public class TestExternalizable<S extends State> {

    public void run() throws Exception {
        byte[] trajectoryBytes = BytearrayToFile.fromFile("src/main/resources", "4 rules fst");
        Serializer serializer = Serializer.getSerializer(SerializerType.FST);
        LinkedList<Trajectory<S>> trajectories = new LinkedList<>();
        trajectories.add((Trajectory<S>) serializer.deserialize(trajectoryBytes));
        trajectories.add((Trajectory<S>) serializer.deserialize(trajectoryBytes));
        ComputationResult<S> result = new ComputationResult(trajectories);

        System.out.println("_______________________________________________________________________");
        List<Trajectory<S>> listTrajectories = result.getResults();
        Trajectory<S> firstTrajectory = listTrajectories.get(0);
        List<Sample<S>> listSamples = firstTrajectory.getData();
        Sample<S> firstSample = listSamples.get(0);
        S state = (S) firstSample.getValue();
        System.out.println(String.format("BEFORE SERIALIZATION \n" +
                "Trajectories: %d \n" +
                "Samples: %d \n" +
                "State class: %s \n" +
                "State: %s", listTrajectories.size(), listSamples.size(), state.getClass().getName(), state.toString()));
        System.out.println("_______________________________________________________________________");
        byte[] serialized = Compressor.compress(ComputationResultSerializer.serialize(result));
        ComputationResult<S> deserialized = ComputationResultSerializer.deserialize(Compressor.decompress(serialized));
        listTrajectories = deserialized.getResults();
        System.out.println(listTrajectories);
        firstTrajectory = listTrajectories.get(0);
        listSamples = firstTrajectory.getData();
        firstSample = listSamples.get(0);
        state = (S) firstSample.getValue();
        System.out.println("_______________________________________________________________________");
        System.out.println(String.format("AFTER SERIALIZATION \n" +
                "Trajectories: %d \n" +
                "Samples: %d \n" +
                "State class: %s \n" +
                "State: %s", listTrajectories.size(), listSamples.size(), state.getClass().getName(), state.toString()));
        System.out.println("_______________________________________________________________________");
    }

    public static void main(String[] args) throws Exception {
        TestExternalizable<PopulationState> test = new TestExternalizable<>();
        test.run();
    }

}
