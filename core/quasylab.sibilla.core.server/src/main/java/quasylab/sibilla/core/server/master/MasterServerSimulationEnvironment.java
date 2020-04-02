package quasylab.sibilla.core.server.master;

import quasylab.sibilla.core.server.NetworkSimulationManager;
import quasylab.sibilla.core.server.ServerInfo;
import quasylab.sibilla.core.server.SimulationDataSet;
import quasylab.sibilla.core.server.client.ClientCommand;
import quasylab.sibilla.core.server.network.TCPNetworkManager;
import quasylab.sibilla.core.server.network.TCPNetworkManagerType;
import quasylab.sibilla.core.server.network.UDPNetworkManager;
import quasylab.sibilla.core.server.network.UDPNetworkManagerType;
import quasylab.sibilla.core.simulator.SimulationEnvironment;
import quasylab.sibilla.core.simulator.pm.State;
import quasylab.sibilla.core.simulator.sampling.SamplingFunction;
import quasylab.sibilla.core.simulator.sampling.SimulationTimeSeries;
import quasylab.sibilla.core.simulator.serialization.CustomClassLoader;
import quasylab.sibilla.core.simulator.serialization.ObjectSerializer;
import quasylab.sibilla.core.util.NetworkUtils;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Handles the simulations requested by clients, manages a list of slave servers
 * who will perform computations
 */
public class MasterServerSimulationEnvironment {

    private static final Logger LOGGER = Logger.getLogger(MasterServerSimulationEnvironment.class.getName());

    private final ServerInfo LOCAL_DISCOVERY_INFO;
    private final ServerInfo LOCAL_SIMULATION_INFO;

    private UDPNetworkManager discoveryNetworkManager;
    private ExecutorService discoveryConnectionExecutor = Executors.newCachedThreadPool();

    private int localSimulationPort;
    private ServerSocket simulationSocket;
    private MasterState state;

    private int remotePort;
    private ExecutorService activitiesExecutors = Executors.newCachedThreadPool();
    private ExecutorService connectionExecutor = Executors.newCachedThreadPool();

    private PropertyChangeSupport updateSupport;

    /**
     * Creates a quasylab.sibilla.core.server.master server with the given
     * information that broadcasts his discovery messages to the given
     * remoteDiscovetyPort
     *
     * @param localDiscoveryPort      port the server listens to
     * @param remoteDiscoveryPort     port the server sends discovery messages to
     * @param discoveryNetworkManager type of network manager used by this server
     * @throws InterruptedException TODO Exception handling
     * @throws IOException          TODO Exception handling
     */
    public MasterServerSimulationEnvironment(int localDiscoveryPort, int remoteDiscoveryPort,
                                             UDPNetworkManagerType discoveryNetworkManager, int localSimulationPort,
                                             TCPNetworkManagerType simulationNetworkManager, int localMonitoringPort,
                                             TCPNetworkManagerType monitoringNetworkManager, PropertyChangeListener... listeners)
            throws IOException {
        LOCAL_DISCOVERY_INFO = new ServerInfo(NetworkUtils.getLocalIp(), localDiscoveryPort, discoveryNetworkManager);
        LOCAL_SIMULATION_INFO = new ServerInfo(NetworkUtils.getLocalIp(), localSimulationPort,
                simulationNetworkManager);
        this.remotePort = remoteDiscoveryPort;
        this.state = new MasterState(LOCAL_SIMULATION_INFO);
        updateSupport = new PropertyChangeSupport(this);
        this.localSimulationPort = localSimulationPort;
        Arrays.stream(listeners).forEach(listener -> {
            this.state.addPropertyChangeListener(listener);
            this.addPropertyChangeListener(listener);
        });

        this.discoveryNetworkManager = UDPNetworkManager.createNetworkManager(LOCAL_DISCOVERY_INFO, true);

        // this.simulationServers = new HashSet<>();

        LOGGER.info(String.format(
                "Starting a new Master server"
                        + "\n- Local discovery port: [%d] - Discovery communication type: [%s - %s]"
                        + "\n- Local simulation handling port: [%d] - Simulation handling communication type[%s - %s]"
                        + "\n- Local monitoring port: [%d] - Monitoring communication type: [%s - %s]",
                LOCAL_DISCOVERY_INFO.getPort(), LOCAL_DISCOVERY_INFO.getType().getClass(),
                LOCAL_DISCOVERY_INFO.getType(), LOCAL_SIMULATION_INFO.getPort(),
                LOCAL_SIMULATION_INFO.getType().getClass(), LOCAL_SIMULATION_INFO.getType(), localMonitoringPort,
                monitoringNetworkManager.getClass(), monitoringNetworkManager));

        activitiesExecutors.execute(this::startDiscoveryServer);
        activitiesExecutors.execute(this::startSimulationServer);
        activitiesExecutors.execute(this::broadcastToInterfaces);

    }

    /**
     * Adds a PropertyChangeListener object that will receive updates from this
     * object
     *
     * @param pcl the object to notify of updated
     */
    public synchronized void addPropertyChangeListener(PropertyChangeListener pcl) {
        updateSupport.addPropertyChangeListener(pcl);
    }

    /**
     * Updates all the PropertyChangeListener objects that have been registered
     *
     * @param results List<SimulationTimeSeries> containing the simulations' results
     */
    private void updateListeners(List<SimulationTimeSeries> results) {
        updateSupport.firePropertyChange("Results", null, results);
    }

    /**
     * Broadcast the discovery message to all the host's network interfaces
     */
    private void broadcastToInterfaces() {
        try {
            while (true) {
                NetworkInterface.networkInterfaces().filter(networkInterface -> {
                    try {
                        return !networkInterface.isLoopback() && networkInterface.isUp();
                    } catch (SocketException e) {
                        e.printStackTrace();
                    }
                    return false;
                }).forEach(networkInterface -> networkInterface.getInterfaceAddresses().stream()
                        .map(InterfaceAddress::getBroadcast).filter(Objects::nonNull)
                        .forEach(this::broadcastToSingleInterface));
                Thread.sleep(20000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a broadcast message to a specified network interface, associated with
     * the ip passed as an argument
     */
    private void broadcastToSingleInterface(InetAddress address) {
        try {
            discoveryNetworkManager.writeObject(ObjectSerializer.serializeObject(LOCAL_DISCOVERY_INFO), address,
                    remotePort);
            LOGGER.info(String.format("Sent the discovery broadcast packet to the port: [%d]", remotePort));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the server that receives messages from slave servers about their
     * informations
     */
    private void startDiscoveryServer() {
        while (true) {
            try {
                Set<ServerInfo> slaveSimulationServers = (Set<ServerInfo>) ObjectSerializer
                        .deserializeObject(discoveryNetworkManager.readObject());
                discoveryConnectionExecutor.execute(() -> {
                    try {
                        manageServers(slaveSimulationServers);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                LOGGER.info(String.format("Current set of servers: %s", state.getServers().toString()));
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
            if (state.addServer(singleInfo)) {
                LOGGER.info(String.format("Added simulation server - %s", singleInfo.toString()));
            }
            // LOGGER.warning("This server was already present: " + singleInfo.toString());
        });
    }

    /**
     * Starts the server that listens for simulations to execute
     */
    public void startSimulationServer() {
        try {
            this.simulationSocket = new ServerSocket(localSimulationPort);
            LOGGER.info(String.format("The server is now listening for clients on port: [%d]",
                    LOCAL_SIMULATION_INFO.getPort()));
            while (true) {
                Socket socket = simulationSocket.accept();
                connectionExecutor.execute(() -> {
                    try {
                        manageClientMessage(socket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles a message sent by a client and executes the simulation that has been
     * sent
     *
     * @param socket Socket on which the message arrived
     * @throws Exception TODO exception handling
     */
    private void manageClientMessage(Socket socket) throws IOException {
        TCPNetworkManager simulationNetworkManager = TCPNetworkManager
                .createNetworkManager((TCPNetworkManagerType) LOCAL_SIMULATION_INFO.getType(), socket);
        AtomicBoolean clientIsActive = new AtomicBoolean(true);
        try {
            Map<ClientCommand, Runnable> map = Map.of(
                    ClientCommand.PING, () -> this.respondPingRequest(simulationNetworkManager),
                    ClientCommand.INIT, () -> this.loadModelClass(simulationNetworkManager),
                    ClientCommand.DATA, () -> this.handleSimulationDataSet(simulationNetworkManager),
                    ClientCommand.CLOSE_CONNECTION, () -> this.closeConnectionWithClient(simulationNetworkManager, clientIsActive));
            while (clientIsActive.get()) {
                ClientCommand command = (ClientCommand) ObjectSerializer
                        .deserializeObject(simulationNetworkManager.readObject());
                LOGGER.info(String.format("[%s] command received by client - %s", command,
                        simulationNetworkManager.getServerInfo().toString()));
                map.getOrDefault(command, () -> {
                }).run();
            }
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }

    }

    private void closeConnectionWithClient(TCPNetworkManager client, AtomicBoolean clientActive) {
        try {
            String modelName = (String) ObjectSerializer.deserializeObject(client.readObject());
            LOGGER.info(String.format("[%s] Model name read to be deleted by client - %s", modelName, client.getServerInfo().toString()));
            clientActive.set(false);
            CustomClassLoader.classes.remove(modelName);
            LOGGER.info(String.format("[%s] Model deleted off the class loader", modelName));
            LOGGER.info(String.format("Client closed the connection"));
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * The server receives the simulation datas' from the client and submits a new
     * set of simulations
     *
     * @param client the TCPNetworkManager that represents the connection with the
     *               client
     */
    private void handleSimulationDataSet(TCPNetworkManager client) {
        try {
            SimulationDataSet<State> dataSet = (SimulationDataSet<State>) ObjectSerializer
                    .deserializeObject(client.readObject());
            LOGGER.info(
                    String.format("Simulation datas received by the client - %s", client.getServerInfo().toString()));
            client.writeObject(ObjectSerializer.serializeObject(MasterCommand.DATA_RESPONSE));
            LOGGER.info(String.format("[%s] command sent to the client - %s", MasterCommand.DATA_RESPONSE,
                    client.getServerInfo().toString()));
            client.writeObject(ObjectSerializer.serializeObject(MasterCommand.RESULTS));
            client.writeObject(ObjectSerializer.serializeObject(this.submitSimulations(dataSet)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * The server submits a new set of simulations
     *
     * @param dataSet containing all the simulation oriented datas
     */
    private SamplingFunction submitSimulations(SimulationDataSet dataSet) {
        SimulationEnvironment sim = new SimulationEnvironment(NetworkSimulationManager
                .getNetworkSimulationManagerFactory(dataSet.getModelName(), this.state));
        try {
            sim.simulate(dataSet.getRandomGenerator(), dataSet.getModel(),
                    dataSet.getModelInitialState(), dataSet.getModelSamplingFunction(),
                    dataSet.getReplica(), dataSet.getDeadline(), false);
            this.updateListeners(
                    dataSet.getModelSamplingFunction().getSimulationTimeSeries(dataSet.getReplica()));
            this.state.increaseExecutedSimulations();
            return dataSet.getModelSamplingFunction();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Manages the ending of all the submitted simulations
     * @param dataSet containing all the simulation oriented datas
     */
    /*private void endSimulations(SimulationDataSet dataSet){

        CustomClassLoader.classes.remove(dataSet.getModelName());
    }*/

    /**
     * The server receives the class containing the model upon which the simulations
     * are built
     *
     * @param client the TCPNetworkManager that represents the connection with the
     *               client
     */
    private void loadModelClass(TCPNetworkManager client) {
        try {
            String modelName = (String) ObjectSerializer.deserializeObject(client.readObject());
            LOGGER.info(String.format("[%s] Model name read by server - IP: [%s] Port: [%d]", modelName,
                    client.getSocket().getInetAddress().getHostAddress(), client.getSocket().getPort()));
            byte[] modelBytes = client.readObject();
            new CustomClassLoader().defClass(modelName, modelBytes);
            String classLoadedName = Class.forName(modelName).getName();
            LOGGER.info(String.format("[%s] Class loaded with success", classLoadedName));
            client.writeObject(ObjectSerializer.serializeObject(MasterCommand.INIT_RESPONSE));
            LOGGER.info(String.format("[%s] command sent to the client - %s", MasterCommand.INIT_RESPONSE,
                    client.getServerInfo().toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * The server responds to a ping request received by the client
     *
     * @param client the TCPNetworkManager that represents the connection with the
     *               client
     */
    private void respondPingRequest(TCPNetworkManager client) {
        try {
            client.writeObject(ObjectSerializer.serializeObject(MasterCommand.PONG));
            LOGGER.info(String.format("[%s] command sent to the client - %s", MasterCommand.PONG,
                    client.getServerInfo().toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
