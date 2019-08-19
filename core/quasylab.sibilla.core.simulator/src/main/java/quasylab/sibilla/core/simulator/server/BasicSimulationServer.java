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

import quasylab.sibilla.core.simulator.SimulationTask;
import quasylab.sibilla.core.simulator.Trajectory;
import quasylab.sibilla.core.simulator.manager.NetworkTask;
import quasylab.sibilla.core.simulator.serialization.CustomClassLoader;
import quasylab.sibilla.core.simulator.serialization.SerializationType;
import quasylab.sibilla.core.simulator.serialization.Serializer;

public class BasicSimulationServer<S> implements SimulationServer<S> {
    private ServerSocket serverSocket;
    private ExecutorService taskExecutor = Executors.newCachedThreadPool();
    private ExecutorService connectionExecutor = Executors.newCachedThreadPool();
    private final SerializationType serialization;

    public BasicSimulationServer( SerializationType serialization){
        this.serialization = serialization;
    }

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
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
            cloader = new CustomClassLoader();
            client = Serializer.createSerializer(socket, cloader, serialization);
        }

        private void init() throws Exception {
            String modelName;
            modelName = (String) client.readObject();
            byte[] myClass = (byte[]) client.readObject();
            cloader.defClass(modelName, myClass);
        }

        private void manageClient() {
            try {
                while (true) {

                    String request = (String) client.readObject();
                    if (request.equals("PING")) {
                        client.writeObject("PONG");
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
                    int reachCount = 0;
                    for( SimulationTask<S> task : tasks){
                        results.add(task.getTrajectory());
                        if(task.reach()){
                            reachCount++;
                        }
                    }
                    client.writeObject(new ComputationResult<>(results, reachCount));

                }

            } catch (EOFException e) {
                System.out.println("Client closed input stream because we timed out or the session has been completed");
                return;
            } catch (SocketException e) {
                System.out.println("Client closed output stream because we timed out");
                return;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
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
    }
}