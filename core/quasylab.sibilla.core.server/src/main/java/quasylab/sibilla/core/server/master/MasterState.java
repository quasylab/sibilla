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

import quasylab.sibilla.core.server.ServerInfo;
import quasylab.sibilla.core.server.slave.SlaveState;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Class that contains the state of the master server
 */
public class MasterState implements Serializable, PropertyChangeListener, Comparable<MasterState>, Cloneable {

    private Date startDate;
    private volatile int runningServers;
    private volatile int connectedServers;
    private volatile int executedSimulations;
    private Set<SlaveState> servers;
    private ServerInfo masterInfo;
    private PropertyChangeSupport updateSupport;
    private Date lastUpdate;

    public MasterState(ServerInfo masterInfo) {
        this.masterInfo = masterInfo;
        this.startDate = new Date();
        this.lastUpdate = startDate;
        this.runningServers = 0;
        this.connectedServers = 0;
        this.executedSimulations = 0;
        this.servers = new HashSet<>();
        this.updateSupport = new PropertyChangeSupport(this);
        this.updateListeners();
    }


    public synchronized void addPropertyChangeListener(PropertyChangeListener pcl) {
        this.updateSupport.addPropertyChangeListener(pcl);
    }

    public synchronized void increaseRunningServers() {
        this.runningServers++;
        this.updateListeners();
    }

    public synchronized void decreaseRunningServers() {
        this.runningServers--;
        this.updateListeners();
    }

    public void increaseExecutedSimulations() {
        this.executedSimulations++;
        this.updateListeners();
    }

    public synchronized boolean addServer(ServerInfo server) {
        return this.addServer(new SlaveState(this, server));
    }

    public synchronized boolean removeServer(ServerInfo server) {
        return this.removeServer(this.getSlaveStateByServerInfo(server));
    }

    public synchronized boolean removeServer(SlaveState state) {
        if (this.servers.remove(state)) {
            this.connectedServers--;
            this.updateListeners();
            return true;
        } else {
            return false;
        }
    }

    public synchronized boolean addServer(SlaveState state) {
        if (this.servers.add(state)) {
            this.connectedServers++;
            this.updateListeners();
            return true;
        } else {
            return false;
        }

    }

    public SlaveState getSlaveStateByServerInfo(ServerInfo serverInfo) {
        return this.servers.stream().filter(slaveState -> {
            return slaveState.getSlaveInfo().equals(serverInfo);
        }).findFirst().get();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        this.lastUpdate = new Date();
        this.updateListeners();
    }

    private void updateListeners() {
        updateSupport.firePropertyChange("Master", null, this.clone());
    }

    public synchronized Set<SlaveState> getServers() {
        return new HashSet<>(this.servers);
    }

    public ServerInfo getMasterInfo() {
        return this.masterInfo;
    }

    public int getConnectedServers() {
        return this.connectedServers;
    }

    public synchronized int getRunningServers() {
        return runningServers;
    }

    public int getExecutedSimulations() {
        return executedSimulations;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getLastUpdate(){
        return this.lastUpdate;
    }

    @Override
    public int compareTo(MasterState o) {
        return this.lastUpdate.compareTo(o.lastUpdate);
    }

    @Override
    public MasterState clone(){
        
    }
}
