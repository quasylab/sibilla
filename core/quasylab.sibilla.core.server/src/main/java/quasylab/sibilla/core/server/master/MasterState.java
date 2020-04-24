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

package quasylab.sibilla.core.server.master;

import quasylab.sibilla.core.server.NetworkInfo;
import quasylab.sibilla.core.server.slave.SlaveState;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.*;

/**
 * Contains the state of a master server.
 * TODO: update with multiple clients
 */
public class MasterState implements Serializable, PropertyChangeListener, Comparable<MasterState>, Cloneable {

    /**
     * The default integer value associated with a slave server that is up and running.
     * Any value below the default reports that the associated server has been unreachable for an amount of time
     */
    final static Integer SLAVE_SERVER_UP_AND_RUNNING_DEFAULT = 3;

    /**
     * The date the master server started its execution.
     */
    private Date masterServerStartDate;
    /**
     * The number of slave servers that are executing simulations.
     */
    private volatile int runningSlaveServers;
    /**
     * The number of slave servers the master server is currently connected to.
     */
    private volatile int connectedSlaveServers;
    /**
     * The number of client submitted simulations that have been executed since the startup of the master server.
     */
    private volatile int executedSimulations;
    /**
     * The slave servers currently monitored by the master server.
     * Every slave server's state is associated with an integer value that is used to signal if the slave server is still up and running.
     * If the integer value associated to a slave server is not equal to the default value reports that the slave server has been unreachable for an amount of time.
     * If the integer value associated to a slave server is equal to the default value reports that server is still up and running.
     */
    private Map<SlaveState, Integer> slaveServers;
    /**
     * The network related informations about this master server.
     */
    private NetworkInfo masterNetworkInfo;
    /**
     * The date the master server state was last updated.
     */
    private Date lastUpdate;
    /**
     * The number of remaining simulation tasks from the client.
     * TODO: update with multiple clients
     */
    private int pendingTasks;
    /**
     * The number of simulation tasks submitted from the client.
     * TODO: update with multiple clients
     */
    private int totalSimulationTasks;

    private transient PropertyChangeSupport updateSupport;


    /**
     * Objects constructor.
     *
     * @param masterNetworkInfo The network related informations about this master server.
     */
    public MasterState(NetworkInfo masterNetworkInfo) {
        this.masterNetworkInfo = masterNetworkInfo;
        this.masterServerStartDate = new Date();
        this.lastUpdate = masterServerStartDate;
        this.runningSlaveServers = 0;
        this.connectedSlaveServers = 0;
        this.executedSimulations = 0;
        this.totalSimulationTasks = 0;
        this.slaveServers = new HashMap<>();
        this.updateSupport = new PropertyChangeSupport(this);
        this.updateListeners();
    }


    public synchronized void addPropertyChangeListener(PropertyChangeListener pcl) {
        this.updateSupport.addPropertyChangeListener(pcl);
    }

    /**
     * Increases the number of slave servers that are executing simulations.
     */
    public synchronized void increaseRunningServers() {
        this.runningSlaveServers++;
        this.updateListeners();
    }

    /**
     * Decreases the number of slave servers that are executing simulations.
     */
    public synchronized void decreaseRunningServers() {
        this.runningSlaveServers--;
        this.updateListeners();
    }

    /**
     * Increases the number of client submitted simulations that have been executed since the startup of the master server.
     */
    public synchronized void increaseExecutedSimulations() {
        this.executedSimulations++;
        this.updateListeners();
    }

    /**
     * Registers a new slave server.
     *
     * @param slaveNetworkInfo The network related informations about the to be registered slave server.
     * @return boolean to report the result of the operation.
     */
    public synchronized boolean addSlaveServer(NetworkInfo slaveNetworkInfo) {
        return this.addSlaveServer(new SlaveState(this, slaveNetworkInfo));
    }

    /**
     * Registers a new slave server with the default integer value associated with an up and running slave server.
     *
     * @param slaveState The state of the to be registered slave server.
     * @return boolean to report the result of the operation.
     */
    private synchronized boolean addSlaveServer(SlaveState slaveState) {
        if (this.slaveServers.put(slaveState, SLAVE_SERVER_UP_AND_RUNNING_DEFAULT) == null) {
            this.connectedSlaveServers++;
            this.updateListeners();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Unregisters a slave server.
     *
     * @param slaveNetworkInfo The network related informations about the to be unregistered slave server.
     * @return boolean to report the result of the operation.
     */
    public synchronized boolean removeSlaveServer(NetworkInfo slaveNetworkInfo) {
        return this.removeSlaveServer(this.getSlaveStateByServerInfo(slaveNetworkInfo));
    }

    /**
     * Unregisters a slave server.
     *
     * @param slaveState The state of the to be unregistered slave server.
     * @return boolean to report the result of the operation.
     */
    private synchronized boolean removeSlaveServer(SlaveState slaveState) {
        if (this.slaveServers.remove(slaveState) != null) {
            this.connectedSlaveServers--;
            this.updateListeners();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Update the integer value associated with every slave server that signals if that server is still up and running.
     * A slave server is removed if its associated integer value is equal to zero.
     */
    public synchronized void updateServersKeepAlive() {
        List<SlaveState> toRemove = new ArrayList<>();
        this.slaveServers.keySet().stream().forEach(slaveState -> {
            this.slaveServers.put(slaveState, this.slaveServers.get(slaveState) - 1);
            if (this.slaveServers.get(slaveState) == 0) {
                toRemove.add(slaveState);
            }
        });
        toRemove.stream().forEach(slave -> {
            this.slaveServers.remove(slave);
        });
    }

    /**
     * Get the state of a selected slave server.
     *
     * @param slaveNetworkInfo The network related informations about the slave server selected.
     * @return SlaveState of the selected Slave Server.
     */
    public synchronized SlaveState getSlaveStateByServerInfo(NetworkInfo slaveNetworkInfo) {
        return this.slaveServers.keySet().stream().filter(slaveState -> {
            return slaveState.getSlaveInfo().equals(slaveNetworkInfo);
        }).findFirst().get();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        this.updateListeners();
    }

    private void updateListeners() {
        this.lastUpdate = new Date();
        updateSupport.firePropertyChange("Master", null, this.clone());
    }

    /**
     * @return Set<SlaveState> containing the registered slave servers' states.
     */
    public synchronized Set<SlaveState> getSlaveServersStates() {
        return new HashSet<>(this.slaveServers.keySet());
    }

    /**
     * @return The network related informations about this master server.
     */
    public synchronized NetworkInfo getMasterNetworkInfo() {
        return this.masterNetworkInfo;
    }

    /**
     * @return The number of slave servers the master server is currently connected to.
     */
    public synchronized int getConnectedSlaveServers() {
        return this.connectedSlaveServers;
    }

    /**
     * @return The number of slave servers that are executing simulations.
     */
    public synchronized int getRunningSlaveServers() {
        return runningSlaveServers;
    }

    /**
     * @return The number of client submitted simulations that have been executed since the startup of the master server.
     */
    public synchronized int getExecutedSimulations() {
        return executedSimulations;
    }

    /**
     * @return The date the master server started its execution.
     */
    public synchronized Date getMasterServerStartDate() {
        return masterServerStartDate;
    }

    /**
     * @return The date the master server state was last updated.
     */
    public synchronized Date getLastUpdate() {
        return this.lastUpdate;
    }

    /**
     * @return The number of remaining simulation tasks from the client.
     */
    public synchronized int getPendingTasks() {
        return this.pendingTasks;
    }

    /**
     * @return The number of simulation tasks submitted from the client.
     */
    public synchronized int getTotalSimulationTasks() {
        return this.totalSimulationTasks;
    }

    /**
     * Set the number of remaining simulation tasks from the client.
     *
     * @param pendingTasks The number of remaining simulation tasks from the client.
     */
    public synchronized void setPendingTasks(int pendingTasks) {
        this.pendingTasks = pendingTasks;
        this.updateListeners();
    }

    /**
     * Set the number of simulation tasks submitted from the client.
     *
     * @param totalSimulationTasks The number of simulation tasks submitted from the client.
     */
    public synchronized void setTotalSimulationTasks(int totalSimulationTasks) {
        this.totalSimulationTasks = totalSimulationTasks;
        this.updateListeners();
    }

    @Override
    public int compareTo(MasterState o) {
        return this.lastUpdate.compareTo(o.lastUpdate);
    }

    @Override
    public MasterState clone() {
        MasterState clone = null;
        try {
            clone = (MasterState) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        clone.masterServerStartDate = (Date) this.masterServerStartDate.clone();
        final Map<SlaveState, Integer> tempMapCopy = new HashMap<>();
        this.slaveServers.entrySet().stream().forEach(entry -> tempMapCopy.put(entry.getKey().clone(), entry.getValue()));
        clone.slaveServers.putAll(tempMapCopy);
        clone.masterNetworkInfo = this.masterNetworkInfo.clone();
        clone.lastUpdate = (Date) this.lastUpdate.clone();
        return clone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MasterState that = (MasterState) o;
        return runningSlaveServers == that.runningSlaveServers &&
                connectedSlaveServers == that.connectedSlaveServers &&
                executedSimulations == that.executedSimulations &&
                Objects.equals(masterServerStartDate, that.masterServerStartDate) &&
                Objects.equals(slaveServers, that.slaveServers) &&
                Objects.equals(masterNetworkInfo, that.masterNetworkInfo) &&
                Objects.equals(lastUpdate, that.lastUpdate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(masterServerStartDate, runningSlaveServers, connectedSlaveServers, executedSimulations, slaveServers, masterNetworkInfo, lastUpdate);
    }

}
