/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 *  Copyright (C) 2020.
 *
 *  See the NOTICE file distributed with this work for additional information
 *  regarding copyright ownership.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package quasylab.sibilla.core.server.slave;

import quasylab.sibilla.core.server.ServerInfo;
import quasylab.sibilla.core.server.master.MasterState;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Objects;

/**
 * Represents the state of a slave server
 */
public class SlaveState implements Serializable, Cloneable {
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
    private ServerInfo slaveInfo;
    private transient PropertyChangeSupport updateSupport;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SlaveState that = (SlaveState) o;
        return Objects.equals(slaveInfo, that.slaveInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(slaveInfo);
    }

    /**
     * Creates a SlaveState object and sets the given MasterState as its listener
     *
     * @param masterState MasterState that listens to the changes of this object
     */
    public SlaveState(MasterState masterState, ServerInfo slaveInfo) {
        this.slaveInfo = slaveInfo;
        expectedTasks = 1;
        actualTasks = 0;
        isRemoved = false;
        isTimeout = false;
        runningTime = 0L;
        devRTT = 0.0;
        sampleRTT = 0.0;
        estimatedRTT = 0.0;
        updateSupport = new PropertyChangeSupport(this);
        this.addPropertyChangeListener("Master", masterState);
    }

    /**
     * Updates the state of the slave server given the data about new executions
     *
     * @param elapsedTime time used to execute the tasks
     * @param tasksSent   number of tasks executed
     */
    public void update(String property, long elapsedTime, int tasksSent) {

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
        this.updateListeners(property);
    }

    /**
     * Lowers the expected tasks following the TCP window size algorithm and signals it to the listeners
     */
    public void forceExpiredTimeLimit(String property) {
        expectedTasks = expectedTasks == 1 ? 1 : expectedTasks / 2;
        this.updateListeners(property);
    }

    /**
     * TODO ???
     */
    public void migrate(String property, ServerInfo newSlaveInfo) {
        this.slaveInfo = newSlaveInfo;
        isRemoved = false;
        isTimeout = false;
        this.updateListeners(property);
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

    public synchronized void addPropertyChangeListener(String property, PropertyChangeListener pcl) {
        updateSupport.addPropertyChangeListener(property, pcl);
    }

    private void updateListeners(String property) {
        updateSupport.firePropertyChange(property, null, this);
    }

    public ServerInfo getSlaveInfo(){
        return this.slaveInfo;
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
        return " - Tasks received: " + actualTasks +
                "\n - Window runtime: " + runningTime + "ns " +
                "\n - sampleRTT: " + sampleRTT + "ns " +
                "\n - estimatedRTT: " + estimatedRTT + "ns " +
                "\n - devRTT: " + devRTT + "ns "
                + "\n - Next task window: " + expectedTasks + " " + "\n - Next time limit: " + getTimeLimit() + "ns "
                + "\n - Next timeout: " + getTimeout() + "ns";
    }

    public boolean isRemoved() {
        return isRemoved;
    }

    /**
     * Sets this server as removed and updates his listeners
     */
    public void removed(String property) {
        isRemoved = true;
        this.updateListeners(property);
    }

    /**
     * Sets this server as timed out and updates his listeners
     */
    public void timedOut(String property) {
        isTimeout = true;
        this.updateListeners(property);
    }
    
    public SlaveState clone() {
        SlaveState clone = null;
        try {
            clone = (SlaveState) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assert clone != null;
        clone.slaveInfo = this.slaveInfo.clone();
        return clone;
    }

}