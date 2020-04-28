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
import java.util.stream.Collectors;

public class SimulationState implements Serializable, PropertyChangeListener, Comparable<SimulationState>, Cloneable {


    private Date simulationStartDate;

    private String simulationModelName;

    private volatile int runningSlaveServers;

    private volatile int connectedSlaveServers;

    private Set<SlaveState> slaveServers;

    private NetworkInfo masterNetworkInfo;

    private NetworkInfo clientNetworkInfo;

    private Date lastUpdate;

    private int pendingTasks;

    private int totalSimulationTasks;

    private transient PropertyChangeSupport updateSupport;

    public SimulationState(MasterState masterState, NetworkInfo masterNetworkInfo, NetworkInfo clientNetworkInfo, Set<NetworkInfo> slaveNetworkInfos) {
        this.masterNetworkInfo = masterNetworkInfo;
        this.clientNetworkInfo = clientNetworkInfo;
        this.simulationStartDate = new Date();
        this.lastUpdate = simulationStartDate;
        this.runningSlaveServers = 0;
        this.connectedSlaveServers = 0;
        this.totalSimulationTasks = 0;

        this.slaveServers = new HashSet<>();

        this.updateSupport = new PropertyChangeSupport(this);
        this.addPropertyChangeListener("Master Update", masterState);

        slaveNetworkInfos.forEach(info -> slaveServers.add(new SlaveState(this, info)));
        masterState.addSimulation(this);

        this.updateListeners();
    }

    private void updateListeners() {
        this.lastUpdate = new Date();
        updateSupport.firePropertyChange("Master Update", null, this);
    }

    public String getSimulationModelName() {
        return simulationModelName;
    }

    public void setSimulationModelName(String simulationModelName) {
        this.simulationModelName = simulationModelName;
        this.updateListeners();
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        this.updateListeners();
    }

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

    public synchronized SlaveState getSlaveStateByServerInfo(NetworkInfo slaveNetworkInfo) {
        return this.slaveServers.stream().filter(slaveState -> {
            return slaveState.getSlaveInfo().equals(slaveNetworkInfo);
        }).findFirst().get();
    }

    public synchronized Set<SlaveState> getSlaveServersStates() {
        return new HashSet<>(this.slaveServers);
    }

    public synchronized NetworkInfo getMasterNetworkInfo() {
        return this.masterNetworkInfo;
    }

    public synchronized NetworkInfo getClientNetworkInfo() {
        return this.clientNetworkInfo;
    }

    public synchronized int getConnectedSlaveServers() {
        return (int) this.slaveServers.stream().filter(slaveState -> !slaveState.isRemoved()).count();
    }

    public synchronized int getRunningSlaveServers() {
        return runningSlaveServers;
    }

    public synchronized Date getSimulationStartDate() {
        return simulationStartDate;
    }

    public synchronized Date getLastUpdate() {
        return this.lastUpdate;
    }

    public synchronized int getPendingTasks() {
        return this.pendingTasks;
    }

    public synchronized int getTotalSimulationTasks() {
        return this.totalSimulationTasks;
    }

    public synchronized void setPendingTasks(int pendingTasks) {
        this.pendingTasks = pendingTasks;
        this.updateListeners();
    }

    public synchronized void setTotalSimulationTasks(int totalSimulationTasks) {
        this.totalSimulationTasks = totalSimulationTasks;
        this.updateListeners();
    }

    @Override
    public SimulationState clone() {
        SimulationState clone = null;
        try {
            clone = (SimulationState) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        clone.simulationStartDate = (Date) this.simulationStartDate.clone();
        final Map<SlaveState, Integer> tempMapCopy = new HashMap<>();
        clone.slaveServers = this.slaveServers.stream().map(slaveState -> slaveState.clone()).collect(Collectors.toSet());
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
                connectedSlaveServers == that.connectedSlaveServers &&
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
        return Objects.hash(simulationStartDate, runningSlaveServers, connectedSlaveServers, slaveServers, masterNetworkInfo, clientNetworkInfo, lastUpdate, pendingTasks, totalSimulationTasks, updateSupport);
    }
}
