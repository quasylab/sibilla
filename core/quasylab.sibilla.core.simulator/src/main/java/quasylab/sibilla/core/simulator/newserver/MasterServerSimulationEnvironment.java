package quasylab.sibilla.core.simulator.newserver;

import quasylab.sibilla.core.simulator.NetworkSimulationManager;
import quasylab.sibilla.core.simulator.SimulationEnvironment;
import quasylab.sibilla.core.simulator.network.TCPNetworkManager;
import quasylab.sibilla.core.simulator.network.TCPNetworkManagerType;
import quasylab.sibilla.core.simulator.network.UDPNetworkManager;
import quasylab.sibilla.core.simulator.network.UDPNetworkManagerType;
import quasylab.sibilla.core.simulator.pm.PopulationState;
import quasylab.sibilla.core.simulator.serialization.CustomClassLoader;
import quasylab.sibilla.core.simulator.serialization.ObjectSerializer;
import quasylab.sibilla.core.simulator.server.ServerInfo;
import quasylab.sibilla.core.util.NetworkUtils;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Handles the simulations requested by clients, manages a list of slave servers who will perform computations
 */
public class MasterServerSimulationEnvironment {

    private static final Logger LOGGER = Logger.getLogger(MasterServerSimulationEnvironment.class.getName());

    private final ServerInfo LOCAL_DISCOVERY_INFO;
    private final ServerInfo LOCAL_SIMULATION_INFO;

    private UDPNetworkManager discoveryNetworkManager;
    private ExecutorService discoveryConnectionExecutor;
    private HashSet<ServerInfo> simulationServers;

    private ServerSocket simulationSocket;
    private TCPNetworkManager simulationNetworkManager;


    private int remotePort;

    /**
     * Creates a master server with the given information that broadcasts his discovery messages to the given remoteDiscovetyPort
     *
     * @param localDiscoveryPort      port the server listens to
     * @param remoteDiscoveryPort     port the server sends discovery messages to
     * @param discoveryNetworkManager type of network manager used by this server
     * @throws InterruptedException TODO Exception handling
     * @throws IOException          TODO Exception handling
     */
    public MasterServerSimulationEnvironment(int localDiscoveryPort, int remoteDiscoveryPort, UDPNetworkManagerType discoveryNetworkManager, int localSimulationPort, TCPNetworkManagerType simulationNetworkManager) throws InterruptedException, IOException {
        LOCAL_DISCOVERY_INFO = new ServerInfo(NetworkUtils.getLocalIp(), localDiscoveryPort, discoveryNetworkManager);
        LOCAL_SIMULATION_INFO = new ServerInfo(NetworkUtils.getLocalIp(), localSimulationPort, simulationNetworkManager);
        this.remotePort = remoteDiscoveryPort;
        this.discoveryNetworkManager = UDPNetworkManager.createNetworkManager(LOCAL_DISCOVERY_INFO, true);
        LOGGER.info(String.format("Listening on port: %d", localDiscoveryPort));
        this.discoveryConnectionExecutor = Executors.newFixedThreadPool(2);
        this.simulationServers = new HashSet<ServerInfo>();

        this.simulationSocket = new ServerSocket(localSimulationPort);

        new Thread(() -> startDiscoveryServer()).start();

        new Thread(() -> startSimulationServer()).start();

        while (true) {
            broadcastToInterfaces();
            Thread.sleep(20000);
        }
    }


    private void broadcastToInterfaces() throws SocketException {
        NetworkInterface.networkInterfaces().filter(networkInterface -> {
            try {
                return !networkInterface.isLoopback() && networkInterface.isUp();
            } catch (SocketException e) {
                e.printStackTrace();
            }
            return false;
        }).forEach(networkInterface -> {
            networkInterface.getInterfaceAddresses().stream().map(interfaceAddress -> interfaceAddress.getBroadcast()).filter(Objects::nonNull).forEach(this::broadcastToSingleInterface);
        });
    }


    /**
     * Sends a broadcast message to a specified interface, associated with the ip passed as an argument
     */
    private void broadcastToSingleInterface(InetAddress address) {
        try {
            LOGGER.info(String.format("Address: %s", address.toString()));
            discoveryNetworkManager.writeObject(ObjectSerializer.serializeObject(LOCAL_DISCOVERY_INFO), address, remotePort);
            LOGGER.info("Sent broadcast packet");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the server that receives messages from slave servers about their informations
     */
    private void startDiscoveryServer() {
        while (true) {
            try {
                Set<ServerInfo> slaveSimulationServers = (Set<ServerInfo>) ObjectSerializer.deserializeObject(discoveryNetworkManager.readObject());
                discoveryConnectionExecutor.execute(() -> {
                    try {
                        manageServers(slaveSimulationServers);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                LOGGER.info(String.format("Current set of servers: %s", simulationServers.toString()));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Manages the informations received by slave servers
     *
     * @param info Set of informations received by a slave server
     * @throws Exception TODO Exception handling
     */
    public void manageServers(Set<ServerInfo> info) throws Exception {
        LOGGER.info(String.format("Read from the NetworkManager: %s", info.toString()));
        info.forEach(singleInfo -> {
            if (simulationServers.add(singleInfo)) {
                LOGGER.info("Added server " + singleInfo.toString());
            } else {
                //LOGGER.warning("This server was already present: " + singleInfo.toString());
            }
        });
    }

    /**
     * Starts the server that listens for simulations to execute
     */
    public void startSimulationServer() {
        while (true) {
            try {
                Socket socket = simulationSocket.accept();
                manageSimulationMessage(socket);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Handles a message sent by a client and executes the simulation that has been sent
     *
     * @param socket Socket on which the message arrived
     * @throws Exception TODO exception handling
     */
    private void manageSimulationMessage(Socket socket) throws Exception {
        this.simulationNetworkManager = TCPNetworkManager.createNetworkManager((TCPNetworkManagerType) LOCAL_SIMULATION_INFO.getType(), socket);
        Map<Command, Runnable> map = Map.of(Command.CLIENT_PING, () -> respondPingRequest(), Command.CLIENT_INIT, () -> loadModelClass(), Command.CLIENT_DATA, () -> handleSimulationDataSet());
        String command = (String) ObjectSerializer.deserializeObject(simulationNetworkManager.readObject());
        LOGGER.info(String.format("%s command received by the client", command));
        map.getOrDefault(command, () -> {
        }).run();
    }

    private void handleSimulationDataSet() {
        try {
            // TODO state interface?
            SimulationDataSet<PopulationState> dataSet = (SimulationDataSet<PopulationState>) ObjectSerializer.deserializeObject(simulationNetworkManager.readObject());
            SimulationEnvironment sim = new SimulationEnvironment(
                    NetworkSimulationManager.getNetworkSimulationManagerFactory(new ArrayList<>(simulationServers),
                            dataSet.getModelReferenceName()));
            sim.simulate(dataSet.getRandomGenerator(), dataSet.getModelReference(), dataSet.getModelReferenceInitialState(), dataSet.getModelReferenceSamplingFunction(), dataSet.getReplica(), dataSet.getDeadline(), false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadModelClass() {
        try {
            String modelName = (String) ObjectSerializer.deserializeObject(simulationNetworkManager.readObject());
            LOGGER.info(String.format("Model name read: %s", modelName));
            byte[] myClass = new byte[0];
            myClass = (byte[]) simulationNetworkManager.readObject();
            LOGGER.info(String.format("Class received"));
            new CustomClassLoader().defClass(modelName, myClass);
            LOGGER.info(String.format("Class loaded"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void respondPingRequest() {
        try {
            simulationNetworkManager.writeObject(ObjectSerializer.serializeObject("PONG"));
            LOGGER.info(String.format("Ping request answered"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
