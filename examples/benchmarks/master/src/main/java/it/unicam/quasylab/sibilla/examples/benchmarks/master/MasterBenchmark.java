package it.unicam.quasylab.sibilla.examples.benchmarks.master;

import it.unicam.quasylab.sibilla.core.models.EvaluationEnvironment;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationModel;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationModelDefinition;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.network.NetworkInfo;
import it.unicam.quasylab.sibilla.core.network.benchmark.master.MasterBenchmarkEnvironment;
import it.unicam.quasylab.sibilla.core.network.communication.TCPNetworkManager;
import it.unicam.quasylab.sibilla.core.network.communication.TCPNetworkManagerType;
import it.unicam.quasylab.sibilla.core.network.serialization.ComputationResultSerializerType;
import it.unicam.quasylab.sibilla.core.network.serialization.Serializer;
import it.unicam.quasylab.sibilla.core.network.serialization.SerializerType;
import it.unicam.quasylab.sibilla.examples.pm.crowds.ChordModel;

import java.io.IOException;
import java.net.InetAddress;

public class MasterBenchmark {

    public static void main(String[] args) throws IOException {
        Serializer fstSerializer = Serializer.getSerializer(SerializerType.FST);

        ComputationResultSerializerType type = ComputationResultSerializerType.APACHE;

        NetworkInfo slaveInfo = new NetworkInfo(InetAddress.getByName("192.168.42.202"), 10000,
                TCPNetworkManagerType.DEFAULT);
        TCPNetworkManager networkManager = TCPNetworkManager.createNetworkManager(slaveInfo);

        networkManager.writeObject(fstSerializer.serialize(type));
        String benchmarkName = (String) fstSerializer.deserialize(networkManager.readObject());

        //PopulationModelDefinition def = new ChordModel();
        PopulationModelDefinition def = new PopulationModelDefinition(
                new EvaluationEnvironment(),
                ChordModel::generatePopulationRegistry,
                ChordModel::getRules,
                ChordModel::getMeasures,
                ChordModel::states);
        def.setParameter("N", 1000);
        PopulationModel model = def.createModel();

        MasterBenchmarkEnvironment<PopulationState> env = MasterBenchmarkEnvironment.getMasterBenchmark(networkManager,
                benchmarkName, type, model, 50, 2000, 10, 2000);

        env.run();
    }

    private static ComputationResultSerializerType getType(String arg) {
        return ComputationResultSerializerType.valueOf(arg);
    }

}
