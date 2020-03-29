package quasylab.sibilla.core.simulator.server;

import quasylab.sibilla.core.simulator.NetworkTask;
import quasylab.sibilla.core.simulator.SimulationTask;
import quasylab.sibilla.core.simulator.Trajectory;
import quasylab.sibilla.core.simulator.network.TCPNetworkManager;
import quasylab.sibilla.core.simulator.network.TCPNetworkManagerType;
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
import java.util.logging.Logger;

public class BasicSimulationServer implements SimulationServer {

    private static final Logger LOGGER = Logger.getLogger(BasicSimulationServer.class.getName());

    private final TCPNetworkManagerType networkManagerType;
    private ServerSocket serverSocket;
    private ExecutorService taskExecutor = Executors.newCachedThreadPool();
    private ExecutorService connectionExecutor = Executors.newCachedThreadPool();


    public BasicSimulationServer(TCPNetworkManagerType networkManagerType) {
        this.networkManagerType = networkManagerType;
        LOGGER.info(String.format("Creating a new BasicSimulation server that uses: [%s - %s]", this.networkManagerType.getClass(), this.networkManagerType.name()));
    }

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        LOGGER.info(String.format("The BasicSimulationServer is now listening for servers on port: [%d]", port));
        while (true) {
            TaskHandler handler = null;
            try {
                handler = new TaskHandler(serverSocket.accept());
            } catch (IOException e) {
                LOGGER.severe(e.getMessage());
                continue;
            }
            connectionExecutor.execute(handler);
        }
    }

    private class TaskHandler implements Runnable {

        private TCPNetworkManager client;

        public TaskHandler(Socket socket) throws IOException {
            client = TCPNetworkManager.createNetworkManager(networkManagerType, socket);
        }

        public void run() {
            manageMaster();
        }

        private void manageMaster() {
            try {
                Map<Command, Runnable> map = Map.of(Command.MASTER_PING, () -> respondPingRequest(), Command.MASTER_INIT, () -> loadModelClass(), Command.MASTER_TASK, () -> handleTaskExecution());
                while (true) {
                    Command request = (Command) ObjectSerializer.deserializeObject(client.readObject());
                    LOGGER.info(String.format("[%s] command received by server - %s", request, client.getServerInfo().toString()));
                    map.getOrDefault(request, () -> {
                    }).run();
                }
            } catch (EOFException e) {
                LOGGER.info("Master closed input stream because we timed out or the session has been completed");
            } catch (SocketException e) {
                LOGGER.severe("Master closed output stream because we timed out");
            } catch (Exception e) {
                LOGGER.severe(e.getMessage());
                e.printStackTrace();
            }
        }

        private void loadModelClass() {
            try {
                String modelName = (String) ObjectSerializer.deserializeObject(client.readObject());
                LOGGER.info(String.format("[%s] Model name read by server - %s", modelName, client.getServerInfo().toString()));
                byte[] myClass = client.readObject();
                new CustomClassLoader().defClass(modelName, myClass);
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

        private void handleTaskExecution() {
            try {
                NetworkTask<?> networkTask = (NetworkTask<?>) ObjectSerializer.deserializeObject(client.readObject());
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
                client.writeObject(ObjectSerializer.serializeObject(new ComputationResult(results)));
                LOGGER.info(String.format("Computation's results have been sent to the server - %s", client.getServerInfo().toString()));
            } catch (Exception e) {
                LOGGER.severe(e.getMessage());
                e.printStackTrace();
            }
        }


        private void respondPingRequest() {
            try {
                client.writeObject(ObjectSerializer.serializeObject(Command.SLAVE_PONG));
                LOGGER.info(String.format("Ping request answered, it was sent by the server - %s", client.getServerInfo().toString()));
            } catch (Exception e) {
                LOGGER.severe(e.getMessage());
                e.printStackTrace();
            }
        }
    }
}