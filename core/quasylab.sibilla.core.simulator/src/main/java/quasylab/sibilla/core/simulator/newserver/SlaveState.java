package quasylab.sibilla.core.simulator.newserver;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

public class SlaveState implements Serializable {
    private final static double alpha = 0.125;
    private final static double beta = 0.250;
    private final static int threshold = 256;
    private final static long maxRunningTime = 3600000000000L; // 1 hour in nanoseconds
    public double devRTT;
    public double estimatedRTT;
    private int expectedTasks, actualTasks;
    private boolean isRemoved, isTimeout;
    private long runningTime;
    private double sampleRTT;
    private PropertyChangeSupport updateSupport;

    /**
     * Creates a SlaveState object and sets the given MasterState as its listener
     *
     * @param masterState MasterState that listens to the changes of this object
     */
    public SlaveState(MasterState masterState) {
        expectedTasks = 1;
        actualTasks = 0;
        isRemoved = false;
        isTimeout = false;
        runningTime = 0L;
        devRTT = 0.0;
        sampleRTT = 0.0;
        estimatedRTT = 0.0;
        updateSupport = new PropertyChangeSupport(this);
        this.addPropertyChangeListener(masterState);
    }

    public synchronized void addPropertyChangeListener(PropertyChangeListener pcl) {
        updateSupport.addPropertyChangeListener(pcl);
    }

    private void updateListeners() {
        updateSupport.firePropertyChange("SlaveState", null, this);
    }

    /**
     * Updates the state of the slave server given the data about new executions
     *
     * @param elapsedTime time used to execute the tasks
     * @param tasksSent   number of tasks executed
     */
    public void update(long elapsedTime, int tasksSent) {

        actualTasks = tasksSent;
        runningTime = elapsedTime;

        if (devRTT != 0.0) {
            if (runningTime >= getTimeLimit()) {
                expectedTasks = expectedTasks == 1 ? 1 : expectedTasks / 2;
            } else if (expectedTasks < threshold) {
                expectedTasks = expectedTasks * 2;
            } else {
                expectedTasks = expectedTasks + 1;
            }
        } else {
            expectedTasks = 2;
        }

        sampleRTT = runningTime / actualTasks;
        estimatedRTT = alpha * sampleRTT + (1 - alpha) * estimatedRTT;
        devRTT = devRTT == 0.0 ? sampleRTT * 2 : beta * Math.abs(sampleRTT - estimatedRTT) + (1 - beta) * devRTT;
        this.updateListeners();
    }

    /**
     * Lowers the expected tasks following the TCP window size algorithm and signals it to the listeners
     */
    public void forceExpiredTimeLimit() {
        expectedTasks = expectedTasks == 1 ? 1 : expectedTasks / 2;
        this.updateListeners();
    }

    /**
     * TODO ???
     */
    public void migrate() {
        isRemoved = false;
        isTimeout = false;
        this.updateListeners();
    }

    /**
     * Gets timeout time of this server after which the server is removed
     *
     * @return timeout length of this server
     */
    public double getTimeout() {
        return expectedTasks == 1 ? 1000000000 : expectedTasks * estimatedRTT + expectedTasks * 4 * devRTT;
    }

    /**
     * Gets the time limit of this server after which the expected tasks are halved
     *
     * @return time limit length of this server
     */
    public double getTimeLimit() { // after this time, the tasks to be sent to this server is to be halved
        return getTimeLimit(expectedTasks);
    }

    /**
     * Gets the time limit of this server for a certain number of tasks
     *
     * @param tasks number of sent tasks
     * @return time limit length of this server for a certain number of tasks
     */
    private double getTimeLimit(int tasks) {
        return tasks * estimatedRTT + tasks * devRTT;
    }

    /**
     * Gets the possibility to complete a certain number of tasks for this server whithin the time limit
     *
     * @param tasks number of tasks to be executed
     * @return whether the server can execute these tasks in time or not
     */
    public boolean canCompleteTask(int tasks) {
        return getTimeLimit(tasks) < maxRunningTime;
    }

    public int getExpectedTasks() {
        return expectedTasks;
    }

    public boolean isTimeout() {
        return isTimeout;
    }


    @Override
    public String toString() {
        if (isRemoved()) {
            return "Server has been removed.";
        }
        if (isTimeout()) {
            return "Server has timed out, reconnecting...";
        }
        return "Tasks received: " + actualTasks + " " + "Window runtime: " + runningTime + "ns " + "sampleRTT: "
                + sampleRTT + "ns " + "estimatedRTT: " + estimatedRTT + "ns " + "devRTT: " + devRTT + "ns "
                + "Next task window: " + expectedTasks + " " + "Next time limit: " + getTimeLimit() + "ns "
                + "Next timeout: " + getTimeout() + "ns\n";
    }

    public boolean isRemoved() {
        return isRemoved;
    }

    /**
     * Sets this server as removed and updates his listeners
     */
    public void removed() {
        isRemoved = true;
        this.updateListeners();
    }

    /**
     * Sets this server as timed out and updates his listeners
     */
    public void timedOut() {
        isTimeout = true;
        this.updateListeners();
    }

}