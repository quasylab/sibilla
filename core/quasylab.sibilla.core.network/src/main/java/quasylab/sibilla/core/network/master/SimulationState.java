/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 * Copyright (C) 2020.
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package quasylab.sibilla.core.network.master;

import quasylab.sibilla.core.network.NetworkInfo;
import quasylab.sibilla.core.network.SimulationDataSet;
import quasylab.sibilla.core.network.communication.TCPNetworkManager;
import quasylab.sibilla.core.network.slave.SlaveState;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Wraps the state of a client submitted simulation.
 * Its updates can be listened by {@link java.beans.PropertyChangeListener} instances.
 *
 * @author Stelluti Francesco Pio
 * @author Zamponi Marco
 */
public class SimulationState implements Serializable, PropertyChangeListener, Comparable<SimulationState>, Cloneable {

    /**
     * The date the simulation was initiated.
     */
    private Date simulationStartDate;

    /**
     * The simulation model name.
     */
    private String simulationModelName;

    /**
     * The number of slave servers that are currently executing the simulation.
     */
    private volatile int runningSlaveServers;

    /**
     * Collection that contains the state of every connected slave server.
     */
    private volatile Set<SlaveState> slaveServers;

    /**
     * Network related infos about the master server that initiated the simulation.
     */
    private NetworkInfo masterNetworkInfo;

    /**
     * Network related infos about the client that submitted the simulation.
     */
    private NetworkInfo clientNetworkInfo;

    /**
     * The client communication related manager.
     */
    private TCPNetworkManager clientConnection;

    /**
     * The last time the state was updated.
     */
    private Date lastUpdate;

    /**
     * The number of pending simulation tasks.
     */
    private int pendingTasks;

    /**
     * The number of total simulation tasks.
     */
    private int totalSimulationTasks;

    /**
     * Signals if the simulation is concluded.
     */
    private volatile boolean concluded;

    /**
     * The wrapper related to the simulation datas.
     */
    private SimulationDataSet<?> simulationDataSet;

    /**
     * To manage the {@link java.beans.PropertyChangeListener} instances.
     */
    private transient PropertyChangeSupport updateSupport;

    /**
     * Initializes the state
     *
     * @param masterState                       the state of the master that initiated the simulation. It will be updated at every simulation update.
     * @param masterNetworkInfo                 related to the master that initiated the simulation.
     * @param clientNetworkInfo                 related to the client that submitted the simulation.
     * @param slaveNetworkInfos                 related to the slave servers the simulation will be submitted to.
     * @param masterServerSimulationEnvironment the environment that manages the simulation. It will be updated at every simulation update.
     */
    public SimulationState(MasterState masterState, NetworkInfo masterNetworkInfo, NetworkInfo clientNetworkInfo, Set<NetworkInfo> slaveNetworkInfos, MasterServerSimulationEnvironment masterServerSimulationEnvironment) {
        this.masterNetworkInfo = masterNetworkInfo;
        this.clientNetworkInfo = clientNetworkInfo;
        this.simulationStartDate = new Date();
        this.lastUpdate = simulationStartDate;
        this.runningSlaveServers = 0;
        this.totalSimulationTasks = 0;

        this.slaveServers = new HashSet<>();

        this.updateSupport = new PropertyChangeSupport(this);
        this.addPropertyChangeListener("Master State Update", masterState);
        this.addPropertyChangeListener("Master Environment Update", masterServerSimulationEnvironment);
        slaveNetworkInfos.forEach(info -> slaveServers.add(new SlaveState(this, info)));
        masterState.addSimulation(this);

        this.updateListeners();
    }

    /**
     * Updates the registered {@link java.beans.PropertyChangeListener} instances.
     */
    private void updateListeners() {
        this.lastUpdate = new Date();
        updateSupport.firePropertyChange("Master State Update", null, this);
        updateSupport.firePropertyChange("Master Environment Update", null, this);
    }

    /**
     * @return the simulation model name.
     */
    public String getSimulationModelName() {
        return simulationModelName;
    }

    /**
     * Sets the simulation model name.
     *
     * @param simulationModelName the name to be set.
     */
    public void setSimulationModelName(String simulationModelName) {
        this.simulationModelName = simulationModelName;
        this.updateListeners();
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        this.updateListeners();
    }

    /**
     * Compares two simulation states for ordering.
     *
     * @param simulationState the {@link quasylab.sibilla.core.network.master.SimulationState} to be compared.
     * @return the result of the compareTo method called on the lastUpdate instance.
     */
    @Override
    public int compareTo(SimulationState simulationState) {
        return this.lastUpdate.compareTo(simulationState.lastUpdate);
    }


    public synchronized void addPropertyChangeListener(String property, PropertyChangeListener pcl) {
        updateSupport.addPropertyChangeListener(property, pcl);
        this.updateListeners();
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
     * Returns the state associated with a specific slave server.
     *
     * @param slaveNetworkInfo related to the slave.
     * @return {@link quasylab.sibilla.core.network.slave.SlaveState} associated with the slave, null if the slave requested was not present.
     */
    public synchronized SlaveState getSlaveStateByServerInfo(NetworkInfo slaveNetworkInfo) {
        try {
            return this.slaveServers.stream().filter(slaveState -> {
                return slaveState.getSlaveInfo().equals(slaveNetworkInfo);
            }).findFirst().get();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    /**
     * @return {@link java.util.Set} related to registered slave servers' states.
     */
    public synchronized Set<SlaveState> getSlaveServersStates() {
        return new HashSet<>(this.slaveServers);
    }

    /**
     * @return Network related infos about the master server that initiated the simulation.
     */
    public synchronized NetworkInfo getMasterNetworkInfo() {
        return this.masterNetworkInfo;
    }

    /**
     * @return Network related infos about the client that submitted the simulation.
     */
    public synchronized NetworkInfo getClientNetworkInfo() {
        return this.clientNetworkInfo;
    }

    /**
     * @return the number of registered and running slave servers.
     */
    public synchronized int getRegisteredSlaveServers() {
        return (int) this.slaveServers.stream().filter(slaveState -> !slaveState.isRemoved()).count();
    }

    /**
     * @return The number of slave servers that are currently executing the simulation.
     */
    public synchronized int getRunningSlaveServers() {
        return runningSlaveServers;
    }

    /**
     * @return The date the simulation was initiated.
     */
    public synchronized Date getSimulationStartDate() {
        return simulationStartDate;
    }

    /**
     * @return The last time the state was updated.
     */
    public synchronized Date getLastUpdate() {
        return this.lastUpdate;
    }

    /**
     * @return The number of pending simulation tasks.
     */
    public synchronized int getPendingTasks() {
        return this.pendingTasks;
    }

    /**
     * Sets the value of pending simulation tasks.
     *
     * @param pendingTasks the value to be set.
     */
    public synchronized void setPendingTasks(int pendingTasks) {
        this.pendingTasks = pendingTasks;
        this.updateListeners();
    }

    /**
     * @return The number of total simulation tasks.
     */
    public synchronized int getTotalSimulationTasks() {
        return this.totalSimulationTasks;
    }

    /**
     * @return if the simulation is concluded.
     */
    public boolean isConcluded() {
        return this.concluded;
    }

    /**
     * Marks the simulation related to this state as concluded.
     */
    public synchronized void setConcluded() {
        this.concluded = true;
        this.updateListeners();
    }

    /**
     * @return The wrapper related to the simulation datas.
     */
    public SimulationDataSet<?> simulationDataSet() {
        return simulationDataSet;
    }

    /**
     * Sets a new simulation data set.
     *
     * @param simulationDataSet the set to be set.
     */
    public void setSimulationDataSet(SimulationDataSet<?> simulationDataSet) {
        this.simulationDataSet = simulationDataSet;
        this.totalSimulationTasks = simulationDataSet.getReplica();
        this.updateListeners();
    }

    /**
     * @return The client communication related manager.
     */
    public TCPNetworkManager clientConnection() {
        return clientConnection;
    }

    /**
     * Sets a new client communication related manager.
     *
     * @param clientConnection the manager to be set.
     */
    public void setClientConnection(TCPNetworkManager clientConnection) {
        this.clientConnection = clientConnection;
    }


    /**
     * @return a deep clone of the {@link quasylab.sibilla.core.network.master.SimulationState} in which is called.
     */
    @Override
    public SimulationState clone() {
        SimulationState clone = null;
        try {
            clone = (SimulationState) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assert clone != null;
        clone.simulationStartDate = (Date) this.simulationStartDate.clone();
        clone.slaveServers = this.slaveServers.stream().map(SlaveState::clone).collect(Collectors.toSet());
        clone.masterNetworkInfo = this.masterNetworkInfo.clone();
        clone.clientNetworkInfo = this.clientNetworkInfo.clone();
        clone.lastUpdate = (Date) this.lastUpdate.clone();
        return clone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimulationState that = (SimulationState) o;
        return runningSlaveServers == that.runningSlaveServers &&
                pendingTasks == that.pendingTasks &&
                totalSimulationTasks == that.totalSimulationTasks &&
                Objects.equals(simulationStartDate, that.simulationStartDate) &&
                Objects.equals(slaveServers, that.slaveServers) &&
                Objects.equals(masterNetworkInfo, that.masterNetworkInfo) &&
                Objects.equals(clientNetworkInfo, that.clientNetworkInfo) &&
                Objects.equals(lastUpdate, that.lastUpdate) &&
                Objects.equals(updateSupport, that.updateSupport);
    }

    @Override
    public int hashCode() {
        return Objects.hash(simulationStartDate, runningSlaveServers, slaveServers, masterNetworkInfo, clientNetworkInfo, lastUpdate, pendingTasks, totalSimulationTasks, updateSupport);
    }
}
