package quasylab.sibilla.examples.pm.seir;

import java.io.IOException;
import java.util.logging.Logger;

import quasylab.sibilla.core.simulator.network.TCPNetworkManagerType;
import quasylab.sibilla.core.simulator.pm.PopulationState;
import quasylab.sibilla.core.simulator.server.BasicSimulationServer;
import quasylab.sibilla.core.simulator.server.SimulationServer;

public class TestServer {

    final static TCPNetworkManagerType serType = TCPNetworkManagerType.DEFAULT;
    final static int port = 8080;
    private static final Logger LOGGER = Logger.getLogger(TestServer.class.getName());

    public static void main(String[] argv) {
        SimulationServer server1 = new BasicSimulationServer(serType);
        LOGGER.info(String.format("A new server has been created with the port %d and the serialization type %s", port,
                serType.name()));
        new Thread(() -> {
            try {
                server1.start(port);
            } catch (IOException e) {
                LOGGER.severe(e.getMessage());
            }
        }).start();
    }
}