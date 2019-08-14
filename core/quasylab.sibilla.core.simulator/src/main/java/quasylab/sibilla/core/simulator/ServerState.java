package quasylab.sibilla.core.simulator;

import java.io.IOException;

import org.nustaq.net.TCPObjectSocket;

public class ServerState {
    private Serializer server;
    private int expectedTasks, actualTasks;
    private boolean isRemoved, isTimeout;
    private long runningTime;
    public double devRTT;
    private double sampleRTT;
    public double estimatedRTT;
    private final static double alpha = 0.125;
    private final static double beta = 0.250;
    private final static int threshold = 256;
    private final static long maxRunningTime = 3600000000000L; // 1 hour in nanoseconds

    //private ObjectOutputStream oos;
    //private ObjectInputStream ois;

    public ServerState(Serializer server) throws IOException {
        this.server = server;
        expectedTasks = 1;
        actualTasks = 0;
        isRemoved = false;
        isTimeout = false;
        runningTime = 0L;
        devRTT = 0.0;
        sampleRTT = 0.0;
        estimatedRTT = 0.0;
    }

    public void update(long elapsedTime, int tasksSent){

        actualTasks = tasksSent;
        runningTime = elapsedTime;

        if(devRTT != 0.0){
            if(runningTime >= getTimeLimit()){
                expectedTasks = expectedTasks == 1 ? 1 : expectedTasks / 2;
            }else if(expectedTasks < threshold){
                expectedTasks = expectedTasks * 2;
            }else if(expectedTasks >= threshold){
                expectedTasks = expectedTasks + 1;
            }
        }else{
            expectedTasks = 2;
        }

        sampleRTT = runningTime / actualTasks;
        estimatedRTT = alpha * sampleRTT + (1-alpha) * estimatedRTT;
        devRTT = devRTT == 0.0 ? sampleRTT * 2 : beta * Math.abs(sampleRTT - estimatedRTT) + (1-beta)*devRTT;
    }

    public void forceExpiredTimeLimit(){
        expectedTasks = expectedTasks == 1 ? 1 : expectedTasks / 2;
    }

    public void migrate(Serializer server) throws IOException {
        close();
        this.server = server;
        isRemoved = false; 
        isTimeout = false;     
    }

    public void close() throws IOException {
        server.getSocket().close();
    }

    public double getTimeout(){  // after this time, a timeout has occurred and the server is not to be contacted again
        return expectedTasks == 1 ? 1000000000 : expectedTasks*estimatedRTT + expectedTasks*4*devRTT;
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

    public boolean isTimeout(){
        return isTimeout;
    }

    @Override
    public String toString(){
        if(isRemoved()){
            return "Server has been removed.";
        }
        if(isTimeout()){
            return "Server has timed out, reconnecting...";
        }
        return  "Tasks received: "+actualTasks+" "+       
                "Window runtime: "+runningTime+"ns "+
                "sampleRTT: "+sampleRTT+"ns "+
                "estimatedRTT: "+estimatedRTT+"ns "+
                "devRTT: "+devRTT+"ns "+
                "Next task window: "+expectedTasks+" "+
                "Next time limit: "+getTimeLimit()+"ns "+
                "Next timeout: "+getTimeout()+"ns\n";
    }

    public Serializer getServer(){
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