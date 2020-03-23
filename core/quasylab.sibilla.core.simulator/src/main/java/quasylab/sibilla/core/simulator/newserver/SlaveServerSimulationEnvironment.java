package quasylab.sibilla.core.simulator.newserver;

import quasylab.sibilla.core.simulator.network.TCPNetworkManagerType;
import quasylab.sibilla.core.simulator.network.UDPDefaultNetworkManager;
import quasylab.sibilla.core.simulator.serialization.ObjectSerializer;
import quasylab.sibilla.core.simulator.server.BasicSimulationServer;
import quasylab.sibilla.core.simulator.server.ServerInfo;
import quasylab.sibilla.core.simulator.server.SimulationServer;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Instantiates the single simulation servers, sends informations about them to the master
 */
public class SlaveServerSimulationEnvironment {

    private static final Logger LOGGER = Logger.getLogger(SlaveServerSimulationEnvironment.class.getName());

    private int localDiscoveryPort;
    private HashSet<ServerInfo> simulationServersInfo;
    private HashSet<SimulationServer> simulationServers;
    /**
     * Create a slave server listening on a given port and creates the simulation servers with the given info
     *
     * @param localDiscoveryPort    port the server listens to
     * @param simulationServersInfo collection of ServerInfo in which simulation servers will be instantiated
     */
    public SlaveServerSimulationEnvironment(int localDiscoveryPort, Set<ServerInfo> simulationServersInfo) {
        this.localDiscoveryPort = localDiscoveryPort;
        this.simulationServersInfo = new HashSet<>(simulationServersInfo);
        this.simulationServers = new HashSet<>();
        this.startupSimulationServers();
        new Thread(this::startDiscoveryServer).start();
    }

    /**
     * Starts up the simulation servers defined in simulationServersInfo
     */
    private void startupSimulationServers() {
        simulationServersInfo.forEach(info -> {
            new Thread(() -> startupSingleSimulationServer(info)).start();
        });
        LOGGER.info("Simulation servers have been initiated");
    }

    /**
     * Starts up a single simulation server with the info passed
     *
     * @param info ServerInfo in which the server will be instantiated
     */
    private void startupSingleSimulationServer(ServerInfo info) {
        SimulationServer server = new BasicSimulationServer((TCPNetworkManagerType) info.getType());
        this.simulationServers.add(server);
        LOGGER.info(String.format("A new server has been created with the port %d and the serialization type %s", info.getPort(),
                info.getType().name()));
        try {
            server.start(info.getPort());
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    /**
     * Starts the server that listens for the masters and sends them info about the owned simulation servers
     */
    private void startDiscoveryServer() {
        try {
            DatagramSocket discoverySocket = new DatagramSocket(localDiscoveryPort);
            LOGGER.info(String.format("Listening for masters on port: %d", localDiscoveryPort));
            UDPDefaultNetworkManager manager = new UDPDefaultNetworkManager(discoverySocket);
            while (true) {
                ServerInfo masterInfo = (ServerInfo) ObjectSerializer.deserializeObject(manager.readObject());
                LOGGER.info(String.format("Master informations received: %s", masterInfo.toString()));
                manageDiscoveryMessage(manager, masterInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Manages a message from the master
     *
     * @param manager    UDPNetworkManager that handles the sending of messages
     * @param masterInfo ServerInfo of the master server
     * @throws Exception TODO Exception handling
     */
    private void manageDiscoveryMessage(UDPDefaultNetworkManager manager, ServerInfo masterInfo) throws Exception {
        manager.writeObject(ObjectSerializer.serializeObject(simulationServersInfo), masterInfo.getAddress(), masterInfo.getPort());
        LOGGER.info(String.format("Simulation servers infos sent to the master server"));
    }
}
