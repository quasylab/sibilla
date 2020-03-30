package quasylab.sibilla.core.server.master;

import quasylab.sibilla.core.server.ServerInfo;
import quasylab.sibilla.core.server.slave.SlaveState;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MasterState implements Serializable, PropertyChangeListener {
    private Date startDate;
    private volatile int runningServers;
    private volatile int connectedServers;
    private volatile int executedSimulations;
    private Map<ServerInfo, SlaveState> servers;
    private ServerInfo masterInfo;
    private PropertyChangeSupport updateSupport;

    public MasterState(ServerInfo masterInfo) {
        this.masterInfo = masterInfo;
        this.startDate = new Date();
        this.runningServers = 0;
        this.connectedServers = 0;
        this.executedSimulations = 0;
        this.servers = Collections.synchronizedMap(new HashMap<>());
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
        this.addServer(server, new SlaveState(this));
    }

    public synchronized void removeServer(ServerInfo server) {
        this.servers.remove(server);
        this.connectedServers--;
        this.updateListeners();
    }

    public synchronized void addServer(ServerInfo server, SlaveState state) {
        this.servers.put(server, state);
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

    public synchronized Map<ServerInfo, SlaveState> getServersMap() {
        return new HashMap<>(this.servers);
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
}
