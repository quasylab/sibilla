package quasylab.sibilla.core.server.client;

import org.apache.commons.math3.random.RandomGenerator;
import quasylab.sibilla.core.server.ServerInfo;
import quasylab.sibilla.core.server.SimulationDataSet;
import quasylab.sibilla.core.server.master.MasterCommand;
import quasylab.sibilla.core.server.network.TCPNetworkManager;
import quasylab.sibilla.core.simulator.Model;
import quasylab.sibilla.core.simulator.pm.State;
import quasylab.sibilla.core.simulator.sampling.SamplingFunction;
import quasylab.sibilla.core.simulator.serialization.ClassBytesLoader;
import quasylab.sibilla.core.simulator.serialization.ObjectSerializer;

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

    /**
     * Creates a new client that sends simulation commands with the parameters of
     * the simulation to execute and the ServerInfo of the
     * quasylab.sibilla.core.server.master server that will manage such simulation
     *
     * @param random            RandomGenerator of the simulation
     * @param modelName         String with the name of the class that defines the
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
    public ClientSimulationEnvironment(RandomGenerator random, String modelName, Model<S> model, S initialState,
            SamplingFunction<S> sampling_function, int replica, double deadline, ServerInfo masterServerInfo)
            throws Exception {
        this.data = new SimulationDataSet<S>(random, modelName, model, initialState, sampling_function, replica,
                deadline, masterServerInfo);
        this.masterServerNetworkManager = TCPNetworkManager.createNetworkManager(masterServerInfo);

        LOGGER.info(String.format("Starting a new client that will submit the simulation to the server - %s",
                masterServerInfo.toString()));

        this.initConnection(masterServerNetworkManager);
        this.sendSimulationInfo(masterServerNetworkManager);
        this.masterServerNetworkManager.closeConnection();
    }

    /**
     * Sends the original class to the quasylab.sibilla.core.server.master server
     *
     * @param server NetworkManager to the quasylab.sibilla.core.server.master
     *               server
     * @throws Exception TODO Exception handling
     */
    private void initConnection(TCPNetworkManager server) throws Exception {
        byte[] classBytes = ClassBytesLoader.loadClassBytes(data.getModelName());

        server.writeObject(ObjectSerializer.serializeObject(ClientCommand.INIT));
        LOGGER.info(String.format("[%s] command sent to the server - %s", ClientCommand.INIT,
                server.getServerInfo().toString()));
        server.writeObject(ObjectSerializer.serializeObject(data.getModelName()));
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
     * quasylab.sibilla.core.server.master server
     *
     * @param targetServer NetworkManager to the quasylab.sibilla.core.server.master
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
    }

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
