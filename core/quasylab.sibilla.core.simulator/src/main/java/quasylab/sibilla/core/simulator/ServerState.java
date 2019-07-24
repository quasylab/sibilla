package quasylab.sibilla.core.simulator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class ServerState {
    private Socket server;
    private int tasks;
    private boolean running;
    private long startTime, elapsedTime;
    private long runningTime;
    private double devRTT;
    private double sampleRTT;
    private double estimatedRTT;
    private final static double alpha = 0.5;
    private final static double beta = 0.5;
    private final static int threshold = 256;
    private final static long maxRunningTime = 3600000000000L; // 1 hour in nanoseconds

    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    public ServerState(Socket server) throws IOException {
        this.server = server;
        tasks = 1;
        running = false;
        startTime = 0L;
        elapsedTime = 0L;
        runningTime = 0L;
        devRTT = 0;
        sampleRTT = 0;
        estimatedRTT = 0;
        oos = new ObjectOutputStream(server.getOutputStream());
        ois = new ObjectInputStream(server.getInputStream());
    }

    public void update(List<Long> executionTimes){
        runningTime = executionTimes.stream().reduce(0L, Long::sum);
        sampleRTT = runningTime / tasks;
        estimatedRTT = alpha * sampleRTT + (1-alpha) * estimatedRTT;
        devRTT = tasks == 1 ? sampleRTT * 2 : beta * Math.abs(sampleRTT - estimatedRTT) + (1-beta)*devRTT;
        if(runningTime > getTimeLimit()){
            tasks = tasks == 1 ? 1 : tasks / 2;
            System.out.println("halving");
        }else if(tasks < threshold){
            tasks = tasks * 2;
            System.out.println("doubling");
        }else if(tasks >= threshold){
            tasks = tasks + 1;
            System.out.println("increasing");
        }
    }

    public double getTimeout(){  // after this time, a timeout has occurred and the server is not to be contacted again
        //return tasks*estimatedRTT + tasks*4*devRTT;
        return Double.MAX_VALUE;
    }

    public double getTimeLimit(){ // after this time, the tasks to be sent to this server is to be halved
        return getTimeLimit(tasks);
    }

    private double getTimeLimit(int tasks){
        return tasks*estimatedRTT + tasks*devRTT;
    }

    public boolean canCompleteTask(int tasks){ 
        return getTimeLimit(tasks) < maxRunningTime;
    }

    public int getTasks(){
        return tasks;
    }

    public boolean isRunning(){
        return running;
    }

    public boolean isTimeout(){
        return (elapsedTime =  System.nanoTime() - startTime) > getTimeout() ;
    }

    public long getElapsedTime(){
        return elapsedTime;
    }

    public void startRunning(){
        running = true;
        startTime = System.nanoTime();
    }

    public void stopRunning(){
        elapsedTime = System.nanoTime() - startTime;
        running = false;
    }

    public ObjectInputStream getObjectInputStream(){
        return ois;
    }

    public ObjectOutputStream getObjectOutputStream(){
        return oos;
    }

    public void printState(){
        System.out.println("Tasks: "+tasks +" devRTT: "+devRTT+" server: "+server);
    }
}