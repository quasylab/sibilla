package quasylab.sibilla.core.simulator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class SimulationServer<S> {
    ServerSocket serverSocket;

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        while(true){
            new Thread(new TaskHandler(serverSocket.accept())).start();
        }
    }

    private class TaskHandler implements Runnable{
        Socket socket;
        public TaskHandler(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run() {
            ObjectInputStream ois;
            ObjectOutputStream oos;
            SimulationTask<S> task;
            int repetitions;
            NetworkTask<S> ntask;
            try {
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());
                ntask = ((NetworkTask<S>) ois.readObject());
                task = ntask.getTask();
                repetitions = ntask.getRepetitions();
                List<Trajectory<S>> results = new LinkedList<>();
                for(int i = 0; i < repetitions; i++){
                    results.add(task.get());
                    task.reset();
                }
                oos.writeObject(results);

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}