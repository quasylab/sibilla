package quasylab.sibilla.core.server.client;

import org.apache.commons.math3.random.RandomGenerator;
import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.server.ServerInfo;
import quasylab.sibilla.core.server.SimulationDataSet;
import quasylab.sibilla.core.server.master.MasterCommand;
import quasylab.sibilla.core.server.network.TCPNetworkManager;
import quasylab.sibilla.core.server.serialization.ClassBytesLoader;
import quasylab.sibilla.core.server.serialization.ObjectSerializer;
import quasylab.sibilla.core.models.MarkovProcess;
import quasylab.sibilla.core.past.State;
import quasylab.sibilla.core.simulator.sampling.SamplingFunction;

import java.util.logging.Logger;

/**
 * Sends request to a quasylab.sibilla.core.server.master server for the
 * execution of simulations
 *
 * @param <S> The class of the state of the model
 */
public class ClientSimulationEnvironment<S extends State> {

    private static final Logger LOGGER = Logger.getLogger(ClientSimulationEnvironment.class.getName());

    private SimulationDataSet<S> data;
    private TCPNetworkManager masterServerNetworkManager;
    private SamplingFunction samplingFunction;

    /**
     * Creates a new client that sends simulation commands with the parameters of
     * the simulation to execute and the ServerInfo of the
     * master server that will manage such simulation
     *
     * @param random            RandomGenerator of the simulation
     *                          model
     * @param model             The model that will be simulated
     * @param initialState      The initial state of the model
     * @param sampling_function The sampling function that will be used to collect
     *                          data
     * @param replica           Repetitions of the simulation
     * @param deadline          Time interval between two samplings
     * @param masterServerInfo  ServerInfo of the
     *                          quasylab.sibilla.core.server.master server
     * @throws Exception TODO Exception handling
     */
    public ClientSimulationEnvironment(RandomGenerator random, Model<S> model, S initialState,
                                       SamplingFunction<S> sampling_function, int replica, double deadline, ServerInfo masterServerInfo)
            throws Exception {
        if (model.getModelDefinition() == null) {
            throw new IllegalArgumentException("A model without definition cannot be serialised!");
        }
        this.data = new SimulationDataSet<S>(random, model.getModelDefinition(), model, initialState, sampling_function, replica,
                deadline, masterServerInfo);
        this.masterServerNetworkManager = TCPNetworkManager.createNetworkManager(masterServerInfo);

        LOGGER.info(String.format("Starting a new client that will submit the simulation to the server - %s",
                masterServerInfo.toString()));

        this.initConnection(masterServerNetworkManager);
        this.sendSimulationInfo(masterServerNetworkManager);
        this.closeConnection(masterServerNetworkManager);
    }

    /**
     * Closes the connection with the given master server
     *
     * @param server server the connection has to be closed with
     * @throws Exception TODO ???
     */
    private void closeConnection(TCPNetworkManager server) throws Exception {
        server.writeObject(ObjectSerializer.serializeObject(ClientCommand.CLOSE_CONNECTION));
        LOGGER.info(String.format("[%s] command sent to the server - %s", ClientCommand.CLOSE_CONNECTION,
                server.getServerInfo().toString()));
        server.writeObject(ObjectSerializer.serializeObject(this.data.getModelName().getClass().getCanonicalName()));
        this.masterServerNetworkManager.closeConnection();
        LOGGER.info(String.format("Closed the connection with the master - %s", server.getServerInfo()));
    }

    /**
     * Sends the original class to the quasylab.sibilla.core.server.master server
     *
     * @param server NetworkManager to the quasylab.sibilla.core.server.master
     *               server
     * @throws Exception TODO Exception handling
     */
    private void initConnection(TCPNetworkManager server) throws Exception {
        byte[] classBytes = ClassBytesLoader.loadClassBytes(data.getModelName().getClass().getCanonicalName());

        server.writeObject(ObjectSerializer.serializeObject(ClientCommand.INIT));
        LOGGER.info(String.format("[%s] command sent to the server - %s", ClientCommand.INIT,
                server.getServerInfo().toString()));
        server.writeObject(ObjectSerializer.serializeObject(data.getModelName().getClass().getCanonicalName()));
        LOGGER.info(String.format("[%s] Model name has been sent to the server - %s", data.getModelName(),
                server.getServerInfo().toString()));
        server.writeObject(classBytes);
        LOGGER.info(String.format("Class bytes have been sent to the server - %s", server.getServerInfo().toString()));
        try {
            MasterCommand answer = (MasterCommand) ObjectSerializer.deserializeObject(server.readObject());
            if (answer.equals(MasterCommand.INIT_RESPONSE)) {
                LOGGER.info(String.format("Answer received: [%s]", answer));
            } else {
                LOGGER.severe(
                        String.format("The answer received wasn't expected. There was an error MasterServer's side"));
            }
        } catch (ClassCastException e) {
            LOGGER.severe(String.format("The answer received wasn't expected. There was an error MasterServer's side"));
        }
    }

    /**
     * Sends the info of the simulation to execute to the
     * master server
     *
     * @param targetServer NetworkManager to the master
     *                     server
     * @throws Exception TODO exception handling
     */
    private void sendSimulationInfo(TCPNetworkManager targetServer) throws Exception {
        targetServer.writeObject(ObjectSerializer.serializeObject(ClientCommand.DATA));
        LOGGER.info(String.format("[%s] command sent to the server - %s", ClientCommand.DATA,
                targetServer.getServerInfo().toString()));
        targetServer.writeObject(ObjectSerializer.serializeObject(data));
        LOGGER.info(String.format("Simulation datas have been sent to the server - %s",
                targetServer.getServerInfo().toString()));
        try {
            MasterCommand answer = (MasterCommand) ObjectSerializer.deserializeObject(targetServer.readObject());
            if (answer.equals(MasterCommand.DATA_RESPONSE)) {
                LOGGER.info(String.format("Answer received: [%s]", answer));
            } else {
                LOGGER.severe(
                        String.format("The answer received wasn't expected. There was an error MasterServer's side"));
            }
        } catch (ClassCastException e) {
            LOGGER.severe(String.format("The answer received wasn't expected. There was an error MasterServer's side"));
        }
        try {
            MasterCommand command = (MasterCommand) ObjectSerializer.deserializeObject(targetServer.readObject());
            LOGGER.info(String.format("[%s] command read by the master - %s", command,
                    targetServer.getServerInfo().toString()));
            if (command.equals(MasterCommand.RESULTS)) {
                this.samplingFunction = (SamplingFunction) ObjectSerializer.deserializeObject(targetServer.readObject());
                LOGGER.severe(
                        String.format("The simulation results have been received correctly"));
            } else {
                LOGGER.severe(String.format("The simulation results haven't been received"));
            }
        } catch (ClassCastException e) {
            LOGGER.severe(String.format("The simulation results haven't been received"));
        }
    }

    /**
     * Sends a ping command to the given master server
     *
     * @param targetServer server to send the ping command to
     * @throws Exception TODO ???
     */
    private void sendPing(TCPNetworkManager targetServer) throws Exception {
        targetServer.writeObject(ObjectSerializer.serializeObject(ClientCommand.PING));
        LOGGER.info(String.format("[%s] command sent to the server - %s", ClientCommand.PING,
                targetServer.getServerInfo().toString()));
        LOGGER.info(String.format("Ping has been sent to the server"));
        try {
            MasterCommand answer = (MasterCommand) ObjectSerializer.deserializeObject(targetServer.readObject());
            if (answer.equals(MasterCommand.PONG)) {
                LOGGER.info(String.format("Answer received: [%s]", answer));
            } else {
                LOGGER.severe(
                        String.format("The answer received wasn't expected. There was an error MasterServer's side"));
            }
        } catch (ClassCastException e) {
            LOGGER.severe(String.format("The answer received wasn't expected. There was an error MasterServer's side"));
        }

    }

}
