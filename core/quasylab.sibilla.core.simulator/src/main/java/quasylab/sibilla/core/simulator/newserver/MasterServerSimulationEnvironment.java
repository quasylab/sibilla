package quasylab.sibilla.core.simulator.newserver;

import quasylab.sibilla.core.simulator.NetworkSimulationManager;
import quasylab.sibilla.core.simulator.SimulationEnvironment;
import quasylab.sibilla.core.simulator.network.TCPNetworkManager;
import quasylab.sibilla.core.simulator.network.TCPNetworkManagerType;
import quasylab.sibilla.core.simulator.network.UDPNetworkManager;
import quasylab.sibilla.core.simulator.network.UDPNetworkManagerType;
import quasylab.sibilla.core.simulator.pm.State;
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

    private MonitoringServer monitoringServer;
    private MasterState state;

    private int remotePort;

    /**
     * Creates a quasylab.sibilla.core.server.master server with the given information that broadcasts his discovery messages to the given remoteDiscovetyPort
     *
     * @param localDiscoveryPort      port the server listens to
     * @param remoteDiscoveryPort     port the server sends discovery messages to
     * @param discoveryNetworkManager type of network manager used by this server
     * @throws InterruptedException TODO Exception handling
     * @throws IOException          TODO Exception handling
     */
    public MasterServerSimulationEnvironment(int localDiscoveryPort, int remoteDiscoveryPort, UDPNetworkManagerType discoveryNetworkManager, int localSimulationPort, TCPNetworkManagerType simulationNetworkManager, int localMonitoringPort, TCPNetworkManagerType monitoringNetworkManager) throws InterruptedException, IOException {
        LOCAL_DISCOVERY_INFO = new ServerInfo(NetworkUtils.getLocalIp(), localDiscoveryPort, discoveryNetworkManager);
        LOCAL_SIMULATION_INFO = new ServerInfo(NetworkUtils.getLocalIp(), localSimulationPort, simulationNetworkManager);
        this.remotePort = remoteDiscoveryPort;
        this.state = new MasterState(LOCAL_SIMULATION_INFO);
        this.monitoringServer = new MonitoringServer(localMonitoringPort, monitoringNetworkManager);
        this.state.addPropertyChangeListener(this.monitoringServer);
        this.discoveryNetworkManager = UDPNetworkManager.createNetworkManager(LOCAL_DISCOVERY_INFO, true);
        this.discoveryConnectionExecutor = Executors.newFixedThreadPool(2);
        this.simulationServers = new HashSet<>();

        LOGGER.info(String.format("Starting a new Master server" +
                        "\n- Local discovery port: [%d] - Discovery communication type: [%s - %s]" +
                        "\n- Local simulation handling port: [%d] - Simulation handling communication type[%s - %s]" +
                        "\n- Local monitoring port: [%d] - Monitoring communication type: [%s - %s]",
                LOCAL_DISCOVERY_INFO.getPort(), LOCAL_DISCOVERY_INFO.getType().getClass(), LOCAL_DISCOVERY_INFO.getType(),
                LOCAL_SIMULATION_INFO.getPort(), LOCAL_SIMULATION_INFO.getType().getClass(), LOCAL_SIMULATION_INFO.getType(),
                localMonitoringPort, monitoringNetworkManager.getClass(), monitoringNetworkManager));
        this.simulationSocket = new ServerSocket(localSimulationPort);

        new Thread(this::startDiscoveryServer).start();

        new Thread(this::startSimulationServer).start();

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
        }).forEach(networkInterface -> networkInterface.getInterfaceAddresses().stream().map(InterfaceAddress::getBroadcast).filter(Objects::nonNull).forEach(this::broadcastToSingleInterface));
    }


    /**
     * Sends a broadcast message to a specified interface, associated with the ip passed as an argument
     */
    private void broadcastToSingleInterface(InetAddress address) {
        try {
            discoveryNetworkManager.writeObject(ObjectSerializer.serializeObject(LOCAL_DISCOVERY_INFO), address, remotePort);
            LOGGER.info(String.format("Sent the discovery broadcast packet to the port: [%d]", remotePort));
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Manages the informations received by slave servers
     *
     * @param info Set of informations received by a slave server
     */
    public void manageServers(Set<ServerInfo> info) {
        info.forEach(singleInfo -> {
            if (simulationServers.add(singleInfo)) {
                LOGGER.info(String.format("Added simulation server - %s", singleInfo.toString()));
            }
            //LOGGER.warning("This server was already present: " + singleInfo.toString());
        });
    }

    /**
     * Starts the server that listens for simulations to execute
     */
    public void startSimulationServer() {
        LOGGER.info(String.format("The server is now listening for clients on port: [%d]", LOCAL_SIMULATION_INFO.getPort()));
        while (true) {
            try {
                Socket socket = simulationSocket.accept();
                new Thread(() -> {
                    try {
                        manageSimulationMessage(socket);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            } catch (IOException e) {
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
    private void manageSimulationMessage(Socket socket) {
        TCPNetworkManager simulationNetworkManager = null;
        try {
            simulationNetworkManager = TCPNetworkManager.createNetworkManager((TCPNetworkManagerType) LOCAL_SIMULATION_INFO.getType(), socket);
            TCPNetworkManager finalSimulationNetworkManager = simulationNetworkManager;
            Map<Command, Runnable> map = Map.of(Command.CLIENT_PING, () -> this.respondPingRequest(finalSimulationNetworkManager), Command.CLIENT_INIT, () -> this.loadModelClass(finalSimulationNetworkManager), Command.CLIENT_DATA, () -> this.handleSimulationDataSet(finalSimulationNetworkManager));
            while (true) {
                Command command = (Command) ObjectSerializer.deserializeObject(simulationNetworkManager.readObject());
                LOGGER.info(String.format("[%s] command received by client - %s", command, simulationNetworkManager.getServerInfo().toString()));
                map.getOrDefault(command, () -> {
                }).run();

            }
        } catch (Exception e) {
            simulationNetworkManager.closeConnection();
          //  e.printStackTrace();
        }

    }

    private void handleSimulationDataSet(TCPNetworkManager client) {
        try {
            SimulationDataSet<State> dataSet = (SimulationDataSet<State>) ObjectSerializer.deserializeObject(client.readObject());
            LOGGER.info(String.format("Simulation datas received by the client - %s", client.getServerInfo().toString()));

            this.submitSimulations(dataSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void submitSimulations(SimulationDataSet dataSet) {
        SimulationEnvironment sim = new SimulationEnvironment(
                NetworkSimulationManager.getNetworkSimulationManagerFactory(new ArrayList<>(simulationServers),
                        dataSet.getModelReferenceName(), this.state));
        try {
            sim.simulate(dataSet.getRandomGenerator(), dataSet.getModelReference(), dataSet.getModelReferenceInitialState(), dataSet.getModelReferenceSamplingFunction(), dataSet.getReplica(), dataSet.getDeadline(), false);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void loadModelClass(TCPNetworkManager client) {
        try {
            String modelName = (String) ObjectSerializer.deserializeObject(client.readObject());
            LOGGER.info(String.format("[%s] Model name read by server - IP: [%s] Port: [%d]", modelName, client.getSocket().getInetAddress().getHostAddress(), client.getSocket().getPort()));
            byte[] modelBytes = client.readObject();
            new CustomClassLoader().defClass(modelName, modelBytes);
            String classLoadedName = Class.forName(modelName).getName();
            LOGGER.info(String.format("[%s] Class loaded with success", classLoadedName));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void respondPingRequest(TCPNetworkManager client) {
        try {
            client.writeObject(ObjectSerializer.serializeObject(Command.MASTER_PONG));
            LOGGER.info(String.format("Ping request answered, it was sent by the server - IP: [%s] Port: [%d]", client.getSocket().getInetAddress().getHostAddress(), client.getSocket().getPort()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MonitoringServer getMonitoringServer(){
        return this.monitoringServer;
    }

}
