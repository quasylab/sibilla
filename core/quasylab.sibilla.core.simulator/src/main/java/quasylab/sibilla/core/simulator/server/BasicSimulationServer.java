package quasylab.sibilla.core.simulator.server;

import quasylab.sibilla.core.simulator.NetworkTask;
import quasylab.sibilla.core.simulator.SimulationTask;
import quasylab.sibilla.core.simulator.Trajectory;
import quasylab.sibilla.core.simulator.network.TCPNetworkManager;
import quasylab.sibilla.core.simulator.network.TCPNetworkManagerType;
import quasylab.sibilla.core.simulator.newserver.Command;
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

    private final TCPNetworkManagerType serialization;
    private ServerSocket serverSocket;
    private ExecutorService taskExecutor = Executors.newCachedThreadPool();
    private ExecutorService connectionExecutor = Executors.newCachedThreadPool();


    public BasicSimulationServer(TCPNetworkManagerType serialization) {
        this.serialization = serialization;
        LOGGER.info(String.format("Set serialization type: %s", this.serialization.name()));
    }

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        LOGGER.info(String.format("BasicSimulationServer listening on port %d", port));
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
            LOGGER.info(String.format("Connection accepted by IP %s and port %d",
                    socket.getInetAddress().getHostAddress(), socket.getPort()));
            client = TCPNetworkManager.createNetworkManager(serialization, socket);
            LOGGER.info(String.format("NetworkManager created"));
        }

        public void run() {
            manageClient();
        }

        private void manageClient() {
            try {
                Map<Command, Runnable> map = Map.of(Command.MASTER_PING, () -> respondPingRequest(), Command.MASTER_INIT, () -> loadModelClass(), Command.MASTER_TASK, () -> handleTaskExecution());
                while (true) {
                    Command request = (Command) ObjectSerializer.deserializeObject(client.readObject());
                    LOGGER.info(String.format("Request received: %s", request));
                    map.getOrDefault(request, () -> {
                    }).run();
                }
            } catch (EOFException e) {
                LOGGER.info("Client closed input stream because we timed out or the session has been completed");
            } catch (SocketException e) {
                LOGGER.severe("Client closed output stream because we timed out");
                e.printStackTrace();
            } catch (Exception e) {
                LOGGER.severe(e.getMessage());
                e.printStackTrace();
            }
        }

        private void loadModelClass() {
            try {
                String modelName = (String) ObjectSerializer.deserializeObject(client.readObject());
                LOGGER.info(String.format("Model name read: %s", modelName));
                byte[] myClass = client.readObject();
                LOGGER.info(String.format("Class received"));
                new CustomClassLoader().defClass(modelName, myClass);
                String classLoadedName = Class.forName(modelName).getName();
                LOGGER.info(String.format("Class loaded: %s", classLoadedName));
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
                LOGGER.info(String.format("Computation's results have been sent to the client successfully"));
            } catch (Exception e) {
                LOGGER.severe(e.getMessage());
                e.printStackTrace();
            }
        }


        private void respondPingRequest() {
            try {
                client.writeObject(ObjectSerializer.serializeObject(Command.SLAVE_PONG));
                LOGGER.info(String.format("Ping request answered"));
            } catch (Exception e) {
                LOGGER.severe(e.getMessage());
                e.printStackTrace();
            }
        }
    }
}