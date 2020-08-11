package quasylab.sibilla.core.network.slave.executor;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.network.ComputationResult;
import quasylab.sibilla.core.network.NetworkTask;
import quasylab.sibilla.core.network.communication.TCPNetworkManager;
import quasylab.sibilla.core.network.compression.Compressor;
import quasylab.sibilla.core.network.serialization.ComputationResultSerializer;

import java.io.IOException;

public abstract class SimulationExecutor {

    public abstract void simulate(NetworkTask networkTask, TCPNetworkManager master);

    protected void sendResult(ComputationResult results, TCPNetworkManager master, Model model) {
        try {
            master.writeObject(Compressor.compress(ComputationResultSerializer.serialize(results, model)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
