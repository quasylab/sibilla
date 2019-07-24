package quasylab.sibilla.core.simulator;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SimulationServer<S> {
    private ServerSocket serverSocket;

    public void start(int port) throws IOException, ClassNotFoundException {
        serverSocket = new ServerSocket(port);
        while(true){
            new Thread(new TaskHandler(serverSocket.accept())).start();
        }
    }

    private class TaskHandler implements Runnable{
        private ObjectOutputStream oos;
        private Deserializer deserializer;
        private CustomClassLoader cloader;
        public TaskHandler(Socket socket) throws IOException, ClassNotFoundException {
            cloader = new CustomClassLoader();
            oos = new ObjectOutputStream(socket.getOutputStream());
            deserializer = new Deserializer(socket.getInputStream(), cloader);
            String modelName = (String) deserializer.readObject();
            byte[] myClass = (byte []) deserializer.readObject();
            cloader.defClass(modelName, myClass);
        }

        @Override
        public void run() {
            try {
                while(true){
                    @SuppressWarnings("unchecked")
                    NetworkTask<S> ntask = ((NetworkTask<S>) deserializer.readObject());
                    
                    List<SimulationTask<S>> tasks = ntask.getTasks();
                    List<ComputationResult<S>> results = new LinkedList<>();
<<<<<<< HEAD
                    Trajectory<S> tempTrajectory;
                    
                    for(int i = 0; i < tasks.size(); i++){
                        tempTrajectory = tasks.get(i).get();
                        results.add(new ComputationResult<>(tempTrajectory, elapsedTime));
=======
                    CompletableFuture<?>[] futures = new CompletableFuture<?>[tasks.size()];
                    for(int i = 0; i < tasks.size(); i++){
                        futures[i] = CompletableFuture.supplyAsync(tasks.get(i));
>>>>>>> dee81bbea591c9bbf0f1c92ea95046a3b6f792ad
                    }
                    CompletableFuture.allOf(futures).join();
                    tasks.stream().forEach(x -> results.add(new ComputationResult<>(x.getTrajectory(), x.getElapsedTime())));
                    oos.writeObject(results);
                }

            }catch(EOFException e){
                System.out.println("session complete");
                return;
            }catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}