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
import quasylab.sibilla.core.network.slave.SlaveState;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Wraps the state of a master server.
 * Its updates can be listened by {@link java.beans.PropertyChangeListener} instances.
 *
 * @author Stelluti Francesco Pio
 * @author Zamponi Marco
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

    /**
     * Collection of the {@link quasylab.sibilla.core.network.NetworkInfo} associated with every slave server registered.
     * Every entry is associated with a boolean value that is used to remove the slave servers' infos if no longer reachable.
     */
    private Map<NetworkInfo, Boolean> slaveServers;
    /**
     * The network related informations about this master server.
     */
    private NetworkInfo masterNetworkInfo;

    /**
     * The states of the simulations submitted by this master.
     */
    private Set<SimulationState> simulationStates;

    /**
     * To manage the {@link java.beans.PropertyChangeListener} instances.
     */
    private PropertyChangeSupport updateSupport;


    /**
     * Initializes the state.
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

    /**
     * Registers a client submitted simulation.
     *
     * @param simulationState state associated with the simulation.
     */
    public synchronized void addSimulation(SimulationState simulationState) {
        this.simulationStates.add(simulationState);
    }

    /**
     * @return {@link java.util.Map} related to registered slave servers.
     */
    public synchronized Map<NetworkInfo, Boolean> slaveServersMap() {
        return new HashMap<NetworkInfo, Boolean>(slaveServers);
    }

    /**
     * @return {@link java.util.Set} related to submitted simulation states.
     */
    public synchronized Set<SimulationState> getSimulationStates() {
        return simulationStates;
    }

    /**
     * Removes a {@link quasylab.sibilla.core.network.master.SimulationState}.
     *
     * @param simulationState the state to be removed.
     * @return {@link java.lang.Boolean} that indicates the result of the operation.
     */
    public synchronized boolean removeSimulation(SimulationState simulationState) {
        return this.simulationStates.remove(simulationState);
    }

    /**
     * @return {@link java.util.Set} contaning {@link quasylab.sibilla.core.network.NetworkInfo} related to registered slave servers.
     */
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


    /**
     * Registers a new slave server.
     *
     * @param slaveNetworkInfo related to the to be registered slave server.
     * @return {@link java.lang.Boolean} that indicates the result of the operation.
     */
    public synchronized boolean addSlaveServer(NetworkInfo slaveNetworkInfo) {
        if (this.slaveServers.put(slaveNetworkInfo, true) == null) {
            this.updateListeners();
            return true;
        } else {
            return false;
        }
    }


    /**
     * Unregisters a slave server.
     *
     * @param slaveNetworkInfo related to the to be unregistered slave server.
     * @return {@link java.lang.Boolean} that indicates the result of the operation.
     */
    public synchronized boolean removeSlaveServer(NetworkInfo slaveNetworkInfo) {
        if (this.slaveServers.remove(slaveNetworkInfo) != null) {
            this.simulationStates.forEach(simulationState -> {
                simulationState.getSlaveStateByServerInfo(slaveNetworkInfo).setRemoved();
                SlaveState toRemove = simulationState.getSlaveStateByServerInfo(slaveNetworkInfo);
                if (toRemove != null) {
                    toRemove.setRemoved();
                }
            });
            this.updateListeners();
            return true;
        } else {
            return false;
        }
    }


    /**
     * Updates the {@link java.lang.Boolean} value associated with every slave server that signals if that server is still up and running.
     * Every slave servers is declared as unreachable after the calling of this method.
     */
    public synchronized void resetKeepAlive() {
        this.slaveServers.keySet().stream().forEach(slaveState -> {
            this.slaveServers.put(slaveState, false);
        });

    }

    /**
     * Removes any registered slave server that is marked as no longer reachable.
     */
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

    /**
     * Updates the registered {@link java.beans.PropertyChangeListener} instances.
     */
    private void updateListeners() {
        updateSupport.firePropertyChange("Master Listener Update", null, this.clone());
    }

    /**
     * @return the network related informations about this master server.
     */
    public synchronized NetworkInfo getMasterNetworkInfo() {
        return this.masterNetworkInfo;
    }

    /**
     * @return the number of slave servers currently registered.
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

    /**
     * Compares two master states for ordering.
     *
     * @param masterState the {@link quasylab.sibilla.core.network.master.MasterState} to be compared.
     * @return the result of the compareTo method called on the masterServerStartDate instance.
     */
    @Override
    public int compareTo(MasterState masterState) {
        return this.masterServerStartDate.compareTo(masterState.masterServerStartDate);
    }
}
