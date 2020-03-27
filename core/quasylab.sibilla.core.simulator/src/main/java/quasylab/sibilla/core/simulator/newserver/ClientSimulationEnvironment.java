package quasylab.sibilla.core.simulator.newserver;

import java.util.logging.Logger;

import org.apache.commons.math3.random.AbstractRandomGenerator;

import quasylab.sibilla.core.simulator.Model;
import quasylab.sibilla.core.simulator.network.TCPNetworkManager;
import quasylab.sibilla.core.simulator.pm.State;
import quasylab.sibilla.core.simulator.sampling.SamplingFunction;
import quasylab.sibilla.core.simulator.serialization.ClassBytesLoader;
import quasylab.sibilla.core.simulator.serialization.ObjectSerializer;
import quasylab.sibilla.core.simulator.server.ServerInfo;

/**
 * Sends request to a master server for the execution of simulations
 *
 * @param <S> The class of the state of the model
 */
public class ClientSimulationEnvironment<S extends State> {

    private static final Logger LOGGER = Logger.getLogger(ClientSimulationEnvironment.class.getName());

    private SimulationDataSet<S> data;
    private TCPNetworkManager masterServerNetworkManager;

    /**
     * Creates a new client that sends simulation commands with the parameters of the simulation to execute and the ServerInfo of the master server that will manage such simulation
     *
     * @param random            RandomGenerator of the simulation
     * @param modelName         String with the name of the class that defines the model
     * @param model             The model that will be simulated
     * @param initialState      The initial state of the model
     * @param sampling_function The sampling function that will be used to collect data
     * @param replica           Repetitions of the simulation
     * @param deadline          Time interval between two samplings
     * @param masterServerInfo  ServerInfo of the master server
     * @throws Exception TODO Exception handling
     */
    public ClientSimulationEnvironment(AbstractRandomGenerator random, String modelName, Model<S> model, S initialState,
                                       SamplingFunction<S> sampling_function, int replica, double deadline, ServerInfo masterServerInfo)
            throws Exception {
        this.data = new SimulationDataSet<S>(random, modelName, model, initialState, sampling_function, replica,
                deadline, masterServerInfo);
        this.masterServerNetworkManager = TCPNetworkManager.createNetworkManager(masterServerInfo);

        LOGGER.info(String.format("Starting a new client that will submit the simulation to the server - %s", masterServerInfo.toString()));


        this.initConnection(masterServerNetworkManager);
        this.sendSimulationInfo(masterServerNetworkManager);
        this.masterServerNetworkManager.closeConnection();
    }

    /**
     * Sends the original class to the master server
     *
     * @param server NetworkManager to the master server
     * @throws Exception TODO Exception handling
     */
    private void initConnection(TCPNetworkManager server) throws Exception {
        byte[] classBytes = ClassBytesLoader.loadClassBytes(data.getModelReferenceName());

        server.writeObject(ObjectSerializer.serializeObject(Command.CLIENT_INIT));
        LOGGER.info(String.format("[%s] command sent to the server - %s", Command.CLIENT_INIT, server.getServerInfo().toString()));
        server.writeObject(ObjectSerializer.serializeObject(data.getModelReferenceName()));
        LOGGER.info(String.format("[%s] Model name has been sent to the server - %s", data.getModelReferenceName(), server.getServerInfo().toString()));
        server.writeObject(classBytes);
        LOGGER.info(String.format("Class bytes have been sent to the server - %s", server.getServerInfo().toString()));
    }

    /**
     * Sends the info of the simulation to execute to the master server
     *
     * @param targetServer NetworkManager to the master server
     * @throws Exception TODO exception handling
     */
    private void sendSimulationInfo(TCPNetworkManager targetServer) throws Exception {
        targetServer.writeObject(ObjectSerializer.serializeObject(Command.CLIENT_DATA));
        LOGGER.info(String.format("[%s] command sent to the server - %s", Command.CLIENT_DATA, targetServer.getServerInfo().toString()));
        targetServer.writeObject(ObjectSerializer.serializeObject(data));
        LOGGER.info(String.format("Simulation datas have been sent to the server - %s", targetServer.getServerInfo().toString()));
    }

    private void sendPing(TCPNetworkManager targetServer) throws Exception {
        targetServer.writeObject(ObjectSerializer.serializeObject(Command.CLIENT_PING));
        LOGGER.info(String.format("[%s] command sent to the server - %s", Command.CLIENT_PING, targetServer.getServerInfo().toString()));
        LOGGER.info(String.format("Ping has been sent to the server"));
        Command answer = (Command) ObjectSerializer.deserializeObject(targetServer.readObject());
        LOGGER.info(String.format("Answer received: [%s]", answer));
    }

}
