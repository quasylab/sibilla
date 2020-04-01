package quasylab.sibilla.core.server;

import quasylab.sibilla.core.server.master.MasterCommand;
import quasylab.sibilla.core.server.network.TCPNetworkManager;
import quasylab.sibilla.core.server.network.TCPNetworkManagerType;
import quasylab.sibilla.core.server.slave.SlaveCommand;
import quasylab.sibilla.core.simulator.SimulationTask;
import quasylab.sibilla.core.simulator.Trajectory;
import quasylab.sibilla.core.simulator.serialization.CustomClassLoader;
import quasylab.sibilla.core.simulator.serialization.ObjectSerializer;

import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class BasicSimulationServer implements SimulationServer {

    private static final Logger LOGGER = Logger.getLogger(BasicSimulationServer.class.getName());

    private final TCPNetworkManagerType networkManagerType;
    private ServerSocket serverSocket;
    private ExecutorService taskExecutor = Executors.newCachedThreadPool();
    private ExecutorService connectionExecutor = Executors.newCachedThreadPool();
    private int port;

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

    private void manageMasterMessage(Socket socket) throws IOException {
        TCPNetworkManager master = TCPNetworkManager.createNetworkManager(networkManagerType, socket);
        AtomicBoolean masterIsActive = new AtomicBoolean(true);
        String masterModelName;
        try {
            Map<MasterCommand, Runnable> map = Map.of(MasterCommand.PING, () -> respondPingRequest(master), MasterCommand.INIT, () -> loadModelClass(master), MasterCommand.TASK, () -> handleTaskExecution(master), MasterCommand.CLOSE_CONNECTION, () -> closeConnectionWithMaster(masterIsActive, master));
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