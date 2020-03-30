package quasylab.sibilla.core.server.master;

import quasylab.sibilla.core.server.ServerInfo;
import quasylab.sibilla.core.server.slave.SlaveState;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.*;

public class MasterState implements Serializable, PropertyChangeListener {
    private Date startDate;
    private volatile int runningServers;
    private volatile int connectedServers;
    private volatile int executedSimulations;
    private Set<SlaveState> servers;
    private ServerInfo masterInfo;
    private PropertyChangeSupport updateSupport;

    public MasterState(ServerInfo masterInfo) {
        this.masterInfo = masterInfo;
        this.startDate = new Date();
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

    public synchronized void addServer(ServerInfo server) {
        this.addServer(new SlaveState(this, server));
    }

    public synchronized void removeServer(ServerInfo server) {
        this.removeServer(this.getSlaveStateByServerInfo(server));
    }

    public synchronized void removeServer(SlaveState state) {
        this.servers.remove(state);
        this.connectedServers--;
        this.updateListeners();
    }

    public synchronized void addServer(SlaveState state) {
        this.servers.add(state);
        this.connectedServers++;
        this.updateListeners();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        this.updateListeners();
    }

    private void updateListeners() {
        updateSupport.firePropertyChange("Master", null, this);
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

    public SlaveState getSlaveStateByServerInfo(ServerInfo serverInfo) {
        return this.servers.stream().filter(slaveState -> {
            return slaveState.getSlaveInfo().equals(serverInfo);
        }).findFirst().get();
    }
}
