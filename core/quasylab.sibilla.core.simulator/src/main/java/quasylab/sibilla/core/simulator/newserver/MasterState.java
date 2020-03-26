package quasylab.sibilla.core.simulator.newserver;

import quasylab.sibilla.core.simulator.server.ServerInfo;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MasterState implements Serializable, PropertyChangeListener {
    private volatile int runningServers;
    private Map<ServerInfo, SlaveState> servers;
    private ServerInfo masterInfo;
    private PropertyChangeSupport updateSupport;

    public MasterState(ServerInfo masterInfo) {
        this.masterInfo = masterInfo;
        runningServers = 0;
        servers = Collections.synchronizedMap(new HashMap<>());
        updateSupport = new PropertyChangeSupport(this);
        this.updateListeners();
    }

    public synchronized void addPropertyChangeListener(PropertyChangeListener pcl) {
        updateSupport.addPropertyChangeListener(pcl);
    }

    private void updateListeners() {
        updateSupport.firePropertyChange(String.format("MasterState - %s", this.masterInfo.getAddress().getHostName()), null, this);
    }

    public synchronized void increaseRunningServers() {
        runningServers++;
        this.updateListeners();
    }

    public synchronized void decreaseRunningServers() {
        runningServers--;
        this.updateListeners();
    }

    public synchronized int getRunningServers() {
        return runningServers;
    }

    public synchronized void addServer(ServerInfo server) {
        this.addServer(server, new SlaveState(this));
    }

    public synchronized void removeServer(ServerInfo server) {
        this.servers.remove(server);
        this.updateListeners();
    }

    public synchronized void addServer(ServerInfo server, SlaveState state) {
        this.servers.put(server, state);
        this.updateListeners();
    }

    public synchronized Map<ServerInfo, SlaveState> getServersMap() {
        return new HashMap<>(this.servers);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        this.updateListeners();
    }

    @Override
    public String toString() {
        return String.format("%s", servers);
    }
}
