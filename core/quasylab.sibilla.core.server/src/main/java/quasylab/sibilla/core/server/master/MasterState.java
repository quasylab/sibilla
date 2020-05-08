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

/**
 * Contains the state of a master server.
 */

public class MasterState implements Serializable, Comparable<MasterState>, PropertyChangeListener, Cloneable {

    /**
     * The date the master server started its execution.
     */
    private Date masterServerStartDate;

    /**
     * The number of client submitted simulations that have been executed since the startup of the master server.
     */
    private volatile int executedSimulations;

    private Map<NetworkInfo, Boolean> slaveServers;
    /**
     * The network related informations about this master server.
     */
    private NetworkInfo masterNetworkInfo;

    private Set<SimulationState> simulationStates;

    private transient PropertyChangeSupport updateSupport;


    /**
     * Objects constructor.
     *
     * @param masterNetworkInfo The network related informations about this master server.
     */
    public MasterState(NetworkInfo masterNetworkInfo) {
        this.masterNetworkInfo = masterNetworkInfo;
        this.masterServerStartDate = new Date();
        this.executedSimulations = 0;
        this.slaveServers = new HashMap<>();
        this.updateSupport = new PropertyChangeSupport(this);
        this.simulationStates = new HashSet<>();
    }

    public synchronized void addSimulation(SimulationState simulationState) {
        this.simulationStates.add(simulationState);
    }

    public synchronized Map<NetworkInfo, Boolean> getSlaveServers() {
        return slaveServers;
    }

    public synchronized Set<SimulationState> getSimulationStates() {
        return simulationStates;
    }

    public synchronized void setSimulationStates(Set<SimulationState> simulationStates) {
        this.simulationStates = simulationStates;
    }

    public synchronized boolean removeSimulation(SimulationState simulationState) {
        return this.simulationStates.remove(simulationState);
    }

    public synchronized Set<NetworkInfo> getSlaveServersNetworkInfos() {
        return new HashSet<NetworkInfo>(this.slaveServers.keySet());
    }

    public synchronized void addPropertyChangeListener(String property, PropertyChangeListener pcl) {
        this.updateSupport.addPropertyChangeListener(property, pcl);
        this.updateListeners();
    }


    /**
     * Increases the number of client submitted simulations that have been executed since the startup of the master server.
     */
    public synchronized void increaseExecutedSimulations() {
        this.executedSimulations++;
        this.updateListeners();
    }


    //Aggiunta dal discovery
    public synchronized boolean addSlaveServer(NetworkInfo slaveNetworkInfo) {
        if (this.slaveServers.put(slaveNetworkInfo, true) == null) {
            this.updateListeners();
            return true;
        } else {
            return false;
        }
    }


    public synchronized boolean removeSlaveServer(NetworkInfo slaveNetworkInfo) {

        if (this.slaveServers.remove(slaveNetworkInfo) != null) {
            this.simulationStates.forEach(simulationState -> {
                simulationState.getSlaveStateByServerInfo(slaveNetworkInfo).removed();
            });
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
    public synchronized void resetKeepAlive() {
        this.slaveServers.keySet().stream().forEach(slaveState -> {
            this.slaveServers.put(slaveState, false);
        });

    }

    public synchronized void cleanKeepAlive() {
        List<NetworkInfo> toRemove = new ArrayList<>();
        this.slaveServers.keySet().stream().forEach(slaveState -> {

            if (!this.slaveServers.get(slaveState)) {
                toRemove.add(slaveState);
            }
        });
        toRemove.stream().forEach(slave -> {
            this.removeSlaveServer(slave);
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getNewValue() instanceof SimulationState) {
            SimulationState simState = (SimulationState) evt.getNewValue();
            simState.getSlaveServersStates().stream().filter(SlaveState::isRemoved).forEach(slaveState -> this.removeSlaveServer(slaveState.getSlaveInfo()));
            this.simulationStates.removeIf(SimulationState::isConcluded);
        }
        this.updateListeners();
    }

    private void updateListeners() {
        updateSupport.firePropertyChange("Master Listener Update", null, this.clone());
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
        return this.slaveServers.size();
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


    @Override
    public MasterState clone() {
        MasterState clone = null;
        try {
            clone = (MasterState) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        clone.simulationStates = this.simulationStates.stream().map(simulationState -> simulationState.clone()).collect(Collectors.toSet());
        clone.masterServerStartDate = (Date) this.masterServerStartDate.clone();
        final Map<NetworkInfo, Boolean> tempMapCopy = new HashMap<>();
        this.slaveServers.entrySet().stream().forEach(entry -> tempMapCopy.put(entry.getKey().clone(), entry.getValue()));
        clone.slaveServers.putAll(tempMapCopy);
        clone.masterNetworkInfo = this.masterNetworkInfo.clone();
        return clone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MasterState that = (MasterState) o;
        return
                executedSimulations == that.executedSimulations &&
                        Objects.equals(masterServerStartDate, that.masterServerStartDate) &&
                        Objects.equals(slaveServers, that.slaveServers) &&
                        Objects.equals(masterNetworkInfo, that.masterNetworkInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(masterServerStartDate, executedSimulations, slaveServers, masterNetworkInfo);
    }

    @Override
    public int compareTo(MasterState masterState) {
        return this.masterServerStartDate.compareTo(masterState.masterServerStartDate);
    }
}
