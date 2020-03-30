package quasylab.sibilla.core.server.slave;

import quasylab.sibilla.core.server.BasicSimulationServer;
import quasylab.sibilla.core.server.ServerInfo;
import quasylab.sibilla.core.server.SimulationServer;
import quasylab.sibilla.core.server.network.TCPNetworkManagerType;
import quasylab.sibilla.core.server.network.UDPDefaultNetworkManager;
import quasylab.sibilla.core.simulator.serialization.ObjectSerializer;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Instantiates the single simulation servers, sends informations about them to the quasylab.sibilla.core.server.master
 */
public class SlaveServerSimulationEnvironment {

    private static final Logger LOGGER = Logger.getLogger(SlaveServerSimulationEnvironment.class.getName());

    private int localDiscoveryPort;
    private HashSet<ServerInfo> simulationServersInfo;
    private HashSet<SimulationServer> simulationServers;
    private boolean alreadyDiscovered;

    /**
     * Create a slave server listening on a given port and creates the simulation servers with the given info
     *
     * @param localDiscoveryPort    port the server listens to
     * @param simulationServersInfo collection of ServerInfo in which simulation servers will be instantiated
     */
    public SlaveServerSimulationEnvironment(int localDiscoveryPort, Set<ServerInfo> simulationServersInfo) {
        this.alreadyDiscovered = false;
        this.localDiscoveryPort = localDiscoveryPort;
        this.simulationServersInfo = new HashSet<>(simulationServersInfo);
        this.simulationServers = new HashSet<>();
        LOGGER.info(String.format("Starting a new Slave server - It will respond for discovery messages on port [%d] and will create simulation servers on ports %s", localDiscoveryPort, simulationServersInfo.stream().map(serverInfo -> serverInfo.getPort()).collect(Collectors.toList())));

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
    }

    /**
     * Starts up a single simulation server with the info passed
     *
     * @param info ServerInfo in which the server will be instantiated
     */
    private void startupSingleSimulationServer(ServerInfo info) {
        SimulationServer server = new BasicSimulationServer((TCPNetworkManagerType) info.getType());
        this.simulationServers.add(server);
        LOGGER.info(String.format("A new simulation server has been created - %s", info.toString()));
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
            LOGGER.info(String.format("Now listening for discovery messages on port: [%d]", localDiscoveryPort));
            UDPDefaultNetworkManager manager = new UDPDefaultNetworkManager(discoverySocket);
            while (!alreadyDiscovered) {
                ServerInfo masterInfo = (ServerInfo) ObjectSerializer.deserializeObject(manager.readObject());
                LOGGER.info(String.format("Discovered by the server - %s", masterInfo.toString()));
                manageDiscoveryMessage(manager, masterInfo);
                this.alreadyDiscovered = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Manages a message from the quasylab.sibilla.core.server.master
     *
     * @param manager    UDPNetworkManager that handles the sending of messages
     * @param masterInfo ServerInfo of the quasylab.sibilla.core.server.master server
     * @throws Exception TODO Exception handling
     */
    private void manageDiscoveryMessage(UDPDefaultNetworkManager manager, ServerInfo masterInfo) throws Exception {
        manager.writeObject(ObjectSerializer.serializeObject(simulationServersInfo), masterInfo.getAddress(), masterInfo.getPort());
        LOGGER.info(String.format("Sent the discovery response to the server - %s", masterInfo.toString()));
    }
}
