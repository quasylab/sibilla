package quasylab.sibilla.core.simulator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Supplier;

public class SimulationServer<S> {
    ServerSocket serverSocket;

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        while(true){
            new TaskHandler(serverSocket.accept()).run();
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
            SimulationTask<S> task;
            Trajectory<S> trajectory = null;
            try {
                ois = new ObjectInputStream(socket.getInputStream());
                task = (SimulationTask<S>) ois.readObject();
                trajectory = task.get();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            try {
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeObject(trajectory);
                oos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}