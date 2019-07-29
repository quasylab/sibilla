package quasylab.sibilla.core.simulator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class ServerState {
    private Socket server;
    private int expectedTasks, actualTasks;
    private boolean running;
    private boolean isRemoved, isTimeout;
    private long startTime, elapsedTime;
    private long runningTime;
    public double devRTT;
    private double sampleRTT;
    public double estimatedRTT;
    private final static double alpha = 0.125;
    private final static double beta = 0.250;
    private final static int threshold = 256;
    private final static long maxRunningTime = 3600000000000L; // 1 hour in nanoseconds

    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    public ServerState(Socket server) throws IOException {
        this.server = server;
        expectedTasks = 1;
        actualTasks = 0;
        running = false;
        isRemoved = false;
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
        actualTasks = executionTimes.size();
        runningTime = executionTimes.stream().reduce(0L, Long::sum);
        sampleRTT = runningTime / actualTasks;
        estimatedRTT = alpha * sampleRTT + (1-alpha) * estimatedRTT;
        devRTT = expectedTasks == 1 ? sampleRTT * 2 : beta * Math.abs(sampleRTT - estimatedRTT) + (1-beta)*devRTT;
        if(runningTime >= getTimeLimit()){
            expectedTasks = expectedTasks == 1 ? 1 : expectedTasks / 2;
        }else if(expectedTasks < threshold){
            expectedTasks = expectedTasks * 2;
        }else if(expectedTasks >= threshold){
            expectedTasks = expectedTasks + 1;
        }
    }

    public void forceExpiredTimeLimit(){
        expectedTasks = expectedTasks == 1 ? 1 : expectedTasks / 2;
    }

    public void migrate(Socket server) throws IOException {
        this.server = server;
        oos = new ObjectOutputStream(server.getOutputStream());
        ois = new ObjectInputStream(server.getInputStream());
        running = false;
        isRemoved = false; 
        isTimeout = false;     
    }

    public double getTimeout(){  // after this time, a timeout has occurred and the server is not to be contacted again
        return expectedTasks*estimatedRTT + expectedTasks*4*devRTT;
        //return Double.MAX_VALUE;
    }

    public double getTimeLimit(){ // after this time, the tasks to be sent to this server is to be halved
        return getTimeLimit(expectedTasks);
    }

    private double getTimeLimit(int tasks){
        return tasks*estimatedRTT + tasks*devRTT;
    }

    public boolean canCompleteTask(int tasks){ 
        return getTimeLimit(tasks) < maxRunningTime;
    }

    public int getActualTasks(){
        return actualTasks;
    }


    public int getExpectedTasks(){
        return expectedTasks;
    }

    public boolean isRunning(){
        return running;
    }

    public boolean isTimeout(){
        return isTimeout;
    }

    public long getElapsedTime(){
        return (elapsedTime =  System.nanoTime() - startTime);
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
        System.out.println("Tasks: "+expectedTasks +" devRTT: "+devRTT+" server: "+server);
    }

    @Override
    public String toString(){
        if(isRemoved()){
            return "Server has been removed.";
        }
        if(isTimeout()){
            return "Server has timed out, reconnecting...";
        }
        return "Task window: "+expectedTasks+" "+
                "Effective tasks: "+actualTasks+" "+
                "Window runtime: "+runningTime+" "+
                "sampleRTT: "+sampleRTT+" "+
                "estimatedRTT: "+estimatedRTT+" "+
                "devRTT: "+devRTT+"\n";
    }

    public Socket getServer(){
        return server;
    }

    public boolean isRemoved(){
        return isRemoved;
    }

    public void removed(){
        isRemoved = true;
    }

    public void timedout(){
        isTimeout = true;
    }

}