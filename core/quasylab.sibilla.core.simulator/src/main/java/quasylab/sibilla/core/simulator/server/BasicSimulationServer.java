package quasylab.sibilla.core.simulator.server;

import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import quasylab.sibilla.core.simulator.SimulationTask;
import quasylab.sibilla.core.simulator.Trajectory;
import quasylab.sibilla.core.simulator.NetworkTask;
import quasylab.sibilla.core.simulator.serialization.CustomClassLoader;
import quasylab.sibilla.core.simulator.serialization.SerializationType;
import quasylab.sibilla.core.simulator.serialization.Serializer;

public class BasicSimulationServer<S> implements SimulationServer<S> {
    private ServerSocket serverSocket;
    private ExecutorService taskExecutor = Executors.newCachedThreadPool();
    private ExecutorService connectionExecutor = Executors.newCachedThreadPool();
    private final SerializationType serialization;
    private static final Logger LOGGER = Logger.getLogger(BasicSimulationServer.class.getName());

    public BasicSimulationServer(SerializationType serialization) {
        this.serialization = serialization;
        LOGGER.info(String.format("Set serialization type: %s", this.serialization.name()));
    }

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        LOGGER.info(String.format("Listening on port %d", port));
        while (true) {
            TaskHandler handler = null;
            try {
                handler = new TaskHandler(serverSocket.accept());
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            connectionExecutor.execute(handler);
        }
    }

    private class TaskHandler implements Runnable {

        private CustomClassLoader cloader;
        private Serializer client;

        public TaskHandler(Socket socket) throws IOException {
            LOGGER.info(String.format("Connection accepted by IP %s and port %d",
                    socket.getInetAddress().getHostAddress(), socket.getPort()));
            cloader = new CustomClassLoader();
            client = Serializer.createSerializer(socket, cloader, serialization);
            LOGGER.info(String.format("Serializer created"));
        }

        public void run() {
            try {
                init();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            manageClient();
        }

        private void init() throws Exception {
            String modelName;
            modelName = (String) client.readObject();
            LOGGER.info(String.format("Model name read: %s", modelName));
            byte[] myClass = (byte[]) client.readObject();
            LOGGER.info(String.format("Class received"));
            cloader.defClass(modelName, myClass);
            LOGGER.info(String.format("Class loaded"));
        }

        private void manageClient() {
            try {
                while (true) {
                    String request = (String) client.readObject();
                    LOGGER.info(String.format("Request received: %s", request));
                    if (request.equals("PING")) {
                        client.writeObject("PONG");
                        LOGGER.info(String.format("Ping request answered"));
                        continue;
                    }
                    @SuppressWarnings("unchecked")
                    NetworkTask<S> ntask = ((NetworkTask<S>) client.readObject());

                    List<SimulationTask<S>> tasks = ntask.getTasks();
                    LinkedList<Trajectory<S>> results = new LinkedList<>();
                    CompletableFuture<?>[] futures = new CompletableFuture<?>[tasks.size()];
                    for (int i = 0; i < tasks.size(); i++) {
                        futures[i] = CompletableFuture.supplyAsync(tasks.get(i), taskExecutor);
                    }
                    CompletableFuture.allOf(futures).join();
                    for (SimulationTask<S> task : tasks) {
                        results.add(task.getTrajectory());
                    }
                    client.writeObject(new ComputationResult<>(results));
                    LOGGER.info(String.format("Computation's results have been sent to the client successfully"));
                }

            } catch (EOFException e) {
                LOGGER.info("Client closed input stream because we timed out or the session has been completed");
                return;
            } catch (SocketException e) {
                LOGGER.info("Client closed output stream because we timed out");
                return;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }
}