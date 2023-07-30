package it.unicam.quasylab.sibilla.examples.benchmarks.slave;


import it.unicam.quasylab.sibilla.core.models.EvaluationEnvironment;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationModel;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationModelDefinition;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.network.ComputationResult;
import it.unicam.quasylab.sibilla.core.network.compression.Compressor;
import it.unicam.quasylab.sibilla.core.network.serialization.ComputationResultSerializer;
import it.unicam.quasylab.sibilla.core.network.serialization.Serializer;
import it.unicam.quasylab.sibilla.core.network.serialization.SerializerType;
import it.unicam.quasylab.sibilla.core.network.serialization.TrajectorySerializer;
import it.unicam.quasylab.sibilla.core.network.util.BytearrayToFile;
import it.unicam.quasylab.sibilla.core.simulator.Trajectory;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Sample;
import it.unicam.quasylab.sibilla.core.util.values.SibillaDouble;
import it.unicam.quasylab.sibilla.examples.pm.crowds.ChordModel;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class SerializationBenchmark {
    public static void main(String[] args) throws IOException {

        //PopulationModelDefinition def = new ChordModel();
        PopulationModelDefinition def = new PopulationModelDefinition(
                new EvaluationEnvironment(),
                ChordModel::generatePopulationRegistry,
                ChordModel::getRules,
                ChordModel::getMeasures,
                (e, r) -> new HashMap<>(),
                ChordModel::states);
        def.setParameter("N",new SibillaDouble(1000));
        PopulationModel model = def.createModel();

        List<Trajectory> trajectories = new LinkedList();
        byte[] trajectoryBytes = BytearrayToFile.fromFile(".", "chordTrajectory_Samplings100_Deadline600_N1000_Samples6");

        Trajectory toAdd = TrajectorySerializer.deserialize(trajectoryBytes, model);
        Sample firstSample =(Sample) toAdd.getData().get(0);
        PopulationState firstState = (PopulationState) firstSample.getValue();
        System.out.printf("Population model registry size: %d", model.stateByteArraySize() / 4);
        System.out.printf("\nChord with externalizable\nSamples: %d\nState population vector size: %d", toAdd.getData().size(), firstState.getPopulationVector().length);
        trajectories.add(toAdd);

        ComputationResult result = new ComputationResult(trajectories);

        byte[] customBytes = ComputationResultSerializer.serialize(result, model);
        byte[] customBytesCompressed = Compressor.compress(customBytes);
        byte[] apacheBytes = Serializer.getSerializer(SerializerType.APACHE).serialize(result);
        byte[] apacheBytesCompressed = Compressor.compress(apacheBytes);
        byte[] fstBytes = Serializer.getSerializer(SerializerType.FST).serialize(result);
        byte[] fstBytesCompressed = Compressor.compress(fstBytes);

        System.out.printf("\nCustom bytes: %d\nApache bytes: %d\nFst bytes: %d", customBytes.length, apacheBytes.length, fstBytes.length);
        System.out.printf("\nCustom bytes compressed: %d\nApache bytes compressed: %d\nFst bytes compressed: %d", customBytesCompressed.length, apacheBytesCompressed.length, fstBytesCompressed.length);
    }
}
