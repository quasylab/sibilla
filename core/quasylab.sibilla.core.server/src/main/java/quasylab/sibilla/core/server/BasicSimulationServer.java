package quasylab.sibilla.core.server;

import quasylab.sibilla.core.server.master.MasterCommand;
import quasylab.sibilla.core.server.network.TCPNetworkManager;
import quasylab.sibilla.core.server.network.TCPNetworkManagerType;
import quasylab.sibilla.core.server.slave.SlaveCommand;
import quasylab.sibilla.core.simulator.SimulationTask;
import quasylab.sibilla.core.simulator.Trajectory;
import quasylab.sibilla.core.server.serialization.CustomClassLoader;
import quasylab.sibilla.core.server.serialization.ObjectSerializer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Represent a simple server that executes the simulations passed by a master server
 */
public class BasicSimulationServer implements SimulationServer {

    private static final Logger LOGGER = Logger.getLogger(BasicSimulationServer.class.getName());

    private final TCPNetworkManagerType networkManagerType;
    private ServerSocket serverSocket;
    private ExecutorService taskExecutor = Executors.newCachedThreadPool();
    private ExecutorService connectionExecutor = Executors.newCachedThreadPool();
    private int port;

    /**
     * Creates a simulation server with the given network manager type
     *
     * @param networkManagerType type of the network manager
     */
    public BasicSimulationServer(TCPNetworkManagerType networkManagerType) {
        this.networkManagerType = networkManagerType;
        LOGGER.info(String.format("Creating a new BasicSimulation server that uses: [%s - %s]",
                this.networkManagerType.getClass(), this.networkManagerType.name()));
    }

    @Override
    public void start(int port) throws IOException {
        this.port = port;
        this.startSimulationServer();
    }

    /**
     * Starts a simulation server
     *
     * @throws IOException
     */
    private void startSimulationServer() throws IOException {
        serverSocket = new ServerSocket(port);
        LOGGER.info(String.format("The BasicSimulationServer is now listening for servers on port: [%d]", port));
        while (true) {
            Socket socket = serverSocket.accept();
            connectionExecutor.execute(() -> {
                try {
                    manageMasterMessage(socket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * Manages the messages that come from the master server
     *
     * @param socket socket where the server listens for master messages
     * @throws IOException
     */
    private void manageMasterMessage(Socket socket) throws IOException {
        TCPNetworkManager master = TCPNetworkManager.createNetworkManager(networkManagerType, socket);
        AtomicBoolean masterIsActive = new AtomicBoolean(true);
        try {
            Map<MasterCommand, Runnable> map = Map.of(
                    MasterCommand.PING, () -> respondPingRequest(master),
                    MasterCommand.INIT, () -> loadModelClass(master),
                    MasterCommand.TASK, () -> handleTaskExecution(master),
                    MasterCommand.CLOSE_CONNECTION, () -> closeConnectionWithMaster(masterIsActive, master));
            while (masterIsActive.get()) {
                MasterCommand request = (MasterCommand) ObjectSerializer.deserializeObject(master.readObject());
                LOGGER.info(String.format("[%s] command received by server - %s", request, master.getServerInfo().toString()));
                map.getOrDefault(request, () -> {
                }).run();
            }
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Closes the connection with the master server
     *
     * @param masterActive boolean that tells if the master is active or not
     * @param master       server of the master
     */
    private void closeConnectionWithMaster(AtomicBoolean masterActive, TCPNetworkManager master) {
        try {
            String modelName = (String) ObjectSerializer.deserializeObject(master.readObject());
            LOGGER.info(String.format("[%s] Model name read to be deleted by server - %s", modelName, master.getServerInfo().toString()));
            masterActive.set(false);
            CustomClassLoader.classes.remove(modelName);
            LOGGER.info(String.format("[%s] Model deleted off the class loader", modelName));
            LOGGER.info(String.format("Master closed the connection"));
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads the model class in the memory with the CustomClassLoader
     *
     * @param master server of the master
     */
    private void loadModelClass(TCPNetworkManager master) {
        try {
            String modelName = (String) ObjectSerializer.deserializeObject(master.readObject());
            LOGGER.info(String.format("[%s] Model name read by server - %s", modelName, master.getServerInfo().toString()));
            byte[] myClass = master.readObject();
            CustomClassLoader.defClass(modelName, myClass);
            String classLoadedName = Class.forName(modelName).getName();
            LOGGER.info(String.format("[%s] Class loaded with success", classLoadedName));
        } catch (ClassNotFoundException e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles the simulation execution sent by the server and sends its results to the master
     *
     * @param master server of the master
     */
    private void handleTaskExecution(TCPNetworkManager master) {
        try {
            NetworkTask<?> networkTask = (NetworkTask<?>) ObjectSerializer.deserializeObject(master.readObject());
            List<? extends SimulationTask<?>> tasks = networkTask.getTasks();
            LinkedList<Trajectory<?>> results = new LinkedList<>();
            CompletableFuture<?>[] futures = new CompletableFuture<?>[tasks.size()];
            for (int i = 0; i < tasks.size(); i++) {
                futures[i] = CompletableFuture.supplyAsync(tasks.get(i), taskExecutor);
            }
            CompletableFuture.allOf(futures).join();
            for (SimulationTask<?> task : tasks) {
                results.add(task.getTrajectory());
            }
            master.writeObject(ObjectSerializer.serializeObject(new ComputationResult(results)));
            LOGGER.info(String.format("Computation's results have been sent to the server - %s",
                    master.getServerInfo().toString()));
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Responds to a ping request from the master
     *
     * @param master server of the master
     */
    private void respondPingRequest(TCPNetworkManager master) {
        try {
            master.writeObject(ObjectSerializer.serializeObject(SlaveCommand.PONG));
            LOGGER.info(String.format("Ping request answered, it was sent by the server - %s",
                    master.getServerInfo().toString()));
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
    }

}