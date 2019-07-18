package quasylab.sibilla.core.simulator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

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
            try {
                CustomClassLoader cloader = new CustomClassLoader();
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                Deserializer deserializer = new Deserializer(socket.getInputStream(), cloader);

                //ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                String modelName = (String) deserializer.readObject();
                byte[] myClass = (byte []) deserializer.readObject();
                cloader.defClass(modelName, myClass);

                @SuppressWarnings("unchecked")
                NetworkTask<S> ntask = ((NetworkTask<S>) deserializer.readObject());
                
                SimulationTask<S> task = ntask.getTask();
                int repetitions = ntask.getRepetitions();
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